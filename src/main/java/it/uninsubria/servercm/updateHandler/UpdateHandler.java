package it.uninsubria.servercm.updateHandler;

import it.uninsubria.servercm.ServerInterface;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class UpdateHandler extends Thread {
    private ServerSocket updatesSocket;
    private boolean runCondition;
    private Properties props;
    private ExecutorService clientHandler;
    private ExecutorService connChecker;
    private final String name = "ServerUpdateHandler";
    private final int MAX_NUMBER_OF_THREADS;
    private LinkedBlockingQueue<ServUpdaterSlave> slaves;
    private Logger updatesLogger;
    private final String dbUrl;

    public UpdateHandler(String dbUrl, Properties props, int MAX_NUMBER_OF_THREADS){
        this.dbUrl = dbUrl;
        this.props = props;
        this.MAX_NUMBER_OF_THREADS = MAX_NUMBER_OF_THREADS;
        Thread.currentThread().setName(name);
        updatesLogger = Logger.getLogger(this.name);
        try{
            updatesSocket = new ServerSocket(ServerInterface.UPDATE_PORT);
            updatesLogger.fine(this.name+" Started on port: " + ServerInterface.UPDATE_PORT);
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
        try{
            while(getRunCondition()){
                Socket updateSocket = updatesSocket.accept();
                i++;
                updatesLogger.info("New connection accepted");
                ServUpdaterSlave slave = new ServUpdaterSlave(updateSocket, i, props);
                addSlave(slave);
                Future<?> future = clientHandler.submit(slave);

            }

        }catch(IOException ioe){
            updatesLogger.info(ioe.getMessage());
            //setRunCondition(false);
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
