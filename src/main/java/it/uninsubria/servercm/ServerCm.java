package it.uninsubria.servercm;

import it.uninsubria.request.Request;
import it.uninsubria.response.Response;
import it.uninsubria.util.IDGenerator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ServerCm {

    private ServerSocket ss;
    private final int PORT = ServerInterface.PORT;
    private final String name = "ServerCm";
    private LinkedBlockingQueue<ServerSlave> slaves;
    protected static final String dbUrl = "jdbc:postgresql://localhost/postgres";
    private final Properties props;

    private final Logger logger;
    private final String user = "postgres";
    private final String password = "qwerty";

    private final ExecutorService clientHandler;
    private final ExecutorService connectionChecker;
    private final int MAX_NUMBER_OF_THREADS = 10;

    public ServerCm(){
        try{
            ss = new ServerSocket(PORT);
            System.err.printf("%s started on port: %d\n", this.name, this.PORT);
        }catch(IOException ioe){ioe.printStackTrace();}

        props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);

        clientHandler = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
        connectionChecker = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);

        slaves = new LinkedBlockingQueue<ServerSlave>();
        this.logger = Logger.getLogger(this.name);
    }

    public void addSlave(ServerSlave ss){
        try{
            slaves.put(ss);
        }catch(InterruptedException ie){ie.printStackTrace();}
    }

    public boolean removeSlave(ServerSlave s){
        return slaves.remove(s);
    }

    public int getSlavesSize(){
        return this.slaves.size();
    }

    public String getDbUrl(){
        return this.dbUrl;
    }

    public static void main(String[] args){
        int i = 0;
        ServerCm serv = new ServerCm();

        try{
            while(true){
                Socket sock = serv.ss.accept();
                i++;
                serv.logger.info("New connection accepted");
                ServerSlave serverSlave = new ServerSlave(sock, i, serv.props);
                serv.slaves.add(serverSlave);
                Future<?> future = serv.clientHandler.submit(serverSlave);

                serv.connectionChecker.execute(() -> {
                    try{
                        future.get();
                        System.out.println("Client has disconnected");
                    }catch(InterruptedException | ExecutionException exception){
                        exception.printStackTrace();
                    }
                });

            }
        }catch(IOException ioe){ioe.printStackTrace();}
        finally{
            try{
                serv.logger.info("Master server closing server socket" + i);
                serv.ss.close();
                serv.clientHandler.shutdown();
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }

    }


}
