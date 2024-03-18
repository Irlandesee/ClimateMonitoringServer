package it.uninsubria.servercm.requestHandler;

import it.uninsubria.servercm.ServerInterface;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class RequestHandler extends Thread{
    private ServerSocket requestsSocket;
    private boolean runCondition;
    private Properties props;
    private ExecutorService clientHandler;
    private ExecutorService connChecker;
    private final String name = "ServerRequestsHandler";
    private final int MAX_NUMBER_OF_THREADS;
    private LinkedBlockingQueue<ServRequestSlave> slaves;
    private Logger requestsLogger;
    private final String dbUrl;
    public RequestHandler(String dbUrl, Properties props, int MAX_NUMBER_OF_THREADS){
        this.dbUrl = dbUrl;
        this.MAX_NUMBER_OF_THREADS = MAX_NUMBER_OF_THREADS;
        this.props = props;
        Thread.currentThread().setName(name);
        this.requestsLogger = Logger.getLogger(this.name);
        try{
            requestsSocket = new ServerSocket(ServerInterface.PORT);
            requestsLogger.info(this.name + " Started on port: " + ServerInterface.PORT);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
        clientHandler = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
        connChecker = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
        slaves = new LinkedBlockingQueue<ServRequestSlave>();
        this.setRunCondition(true);

    }

    public boolean getRunCondition(){
        return this.runCondition;
    }

    public void setRunCondition(boolean runCondition){
        this.runCondition = runCondition;
    }
    public void addSlave(ServRequestSlave ss){
        slaves.add(ss);
    }

    public boolean rmServerSlave(ServRequestSlave ss){
        return slaves.remove(ss);
    }
    @Override
    public void run(){
        int i = 0;
        while(getRunCondition()){
            try{
                Socket sock = requestsSocket.accept();
                i++;
                requestsLogger.info("New connection accepted");
                ServRequestSlave slave = new ServRequestSlave(sock, i, props);
                addSlave(slave);
                Future<?> future = clientHandler.submit(slave);

            }catch(IOException ioe){
                requestsLogger.fine(ioe.getMessage());

            }finally{
                requestsLogger.fine("Master server closing server socket");
                try{
                    requestsSocket.close();
                }catch(IOException ioe){
                    ioe.printStackTrace();}
                clientHandler.shutdown();
                connChecker.shutdown();

            }

        }
    }

}
