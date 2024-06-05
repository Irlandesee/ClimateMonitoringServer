package it.uninsubria.servercm;

import java.sql.*;
import java.util.LinkedList;
import java.io.*;
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
    protected static String dbUrl;

    private Properties props;
    private final Logger logger;
    private static final String propertyDbUrl = "db.url";
    private static final String propertyDbUser = "db.username";
    private static final String propertyDbPassword = "db.password";

    private final ExecutorService clientHandler;
    private final ExecutorService connectionChecker;
    private final int MAX_NUMBER_OF_THREADS = 10;

    public ServerCm(){
        initDb();
        try{
            ss = new ServerSocket(PORT);
            System.err.printf("%s started on port: %d\n", this.name, this.PORT);
            System.err.printf("%s dbUrl: %s\n", this.name, ServerCm.dbUrl);
        }catch(IOException ioe){ioe.printStackTrace();}

        clientHandler = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
        connectionChecker = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);

        slaves = new LinkedBlockingQueue<ServerSlave>();
        this.logger = Logger.getLogger(this.name);
    }

    private void initDb(){
        //Read db config file
        try(InputStream in = ServerCm.class.getClassLoader().getResourceAsStream("db.properties")){
            if(in == null){
                System.out.println("Il file db.properties non e' stato trovato, controlla la sua posizione");
                System.exit(1);
            }
            this.props = new Properties();
            props.load(in);
            System.out.println(props);
            ServerCm.dbUrl = props.getProperty(ServerCm.propertyDbUrl);
            System.out.println("Cerco db...");
            try(Connection conn = DriverManager.getConnection(dbUrl,
                            props.getProperty(ServerCm.propertyDbUser),
                            props.getProperty(ServerCm.propertyDbPassword))){
                System.out.println("Connessione con il db eseguita con successo");
            }catch(SQLException sqle){System.err.println(sqle.getMessage());}
        }catch(IOException ioe){System.out.println(ioe.getMessage());}
    }

    public static void main(String[] args){
        int i = 0;
        ServerCm serv = new ServerCm();
        try{
            while(true){
                Socket sock = serv.ss.accept();
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
