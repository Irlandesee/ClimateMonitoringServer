package it.uninsubria.servercm.updateHandler;

import it.uninsubria.servercm.ServerInterface;
import it.uninsubria.update.Update;
import javafx.util.Pair;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class UpdateHandler extends Thread {
    private ServerSocket updatesSocket;
    private UpdateCacheHandler updateCacheHandler;
    private boolean runCondition;
    private final Properties props;
    private final ExecutorService clientHandler;
    private final ExecutorService connChecker;
    private final String name = "ServerUpdateHandler";
    private final int MAX_NUMBER_OF_THREADS;
    private LinkedBlockingQueue<ServUpdaterSlave> slaves;
    private Logger updatesLogger;
    private final String dbUrl;

    public UpdateHandler(String dbUrl, Properties props, int MAX_NUMBER_OF_THREADS){
        this.dbUrl = dbUrl;
        this.props = props;
        this.MAX_NUMBER_OF_THREADS = MAX_NUMBER_OF_THREADS;
        updateCacheHandler = new UpdateCacheHandler("UpdaterCacheHandler", props);
        Thread.currentThread().setName(name);
        updatesLogger = Logger.getLogger(this.name);
        try{
            updatesSocket = new ServerSocket(ServerInterface.UPDATE_PORT);
            updatesLogger.info(this.name+" Started on port: " + ServerInterface.UPDATE_PORT);
        }catch(IOException ioe){ioe.printStackTrace();}

        clientHandler = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
        connChecker = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);

        slaves = new LinkedBlockingQueue<ServUpdaterSlave>();

    }

    public boolean getRunCondition(){return this.runCondition;}
    public void setRunCondition(boolean runCondition){this.runCondition = runCondition;}
    public void addSlave(ServUpdaterSlave ss){slaves.add(ss);}
    public boolean rmServerSlave(ServUpdaterSlave ss){return slaves.remove(ss);}

    @Override
    public void run(){
        int i = 0;
        while(getRunCondition()){
            try{
                Socket updateSocket = updatesSocket.accept();
                i++;
                updatesLogger.fine("New connection accepted");
                ServUpdaterSlave slave = new ServUpdaterSlave(updateSocket, updateCacheHandler, i);
                addSlave(slave);
                Future<?> future = clientHandler.submit(slave);

            }catch(IOException ioe){
                updatesLogger.fine(ioe.getMessage());

            }finally{
                updatesLogger.fine("Master Server closing server socket");
                try{
                    updatesSocket.close();
                }catch(IOException ioe){
                    updatesLogger.fine(ioe.getMessage());
                }
                clientHandler.shutdown();
                connChecker.shutdown();
            }

        }

    }
}
