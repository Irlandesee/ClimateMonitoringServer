package it.uninsubria.servercm;

import it.uninsubria.servercm.requestHandler.RequestHandler;
import it.uninsubria.servercm.updateHandler.UpdateHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ServerCm {

    private final String name = "ServerCm";
    protected static final String dbUrl = "jdbc:postgresql://localhost/postgres";
    private static Properties props;
    private static Properties updaterProps;

    protected static final String user = "server_slave";
    protected static final String password = "serverslave";
    protected static final String updaterUser = "server_updater";
    protected static final String updaterPassword = "servUpdater";

    private static RequestHandler requestHandler;
    private static UpdateHandler updateHandler;

    public static void main(String[] args){

        props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);

        updaterProps = new Properties();
        updaterProps.setProperty("user", updaterUser);
        updaterProps.setProperty("password", updaterPassword);

        requestHandler = new RequestHandler(dbUrl, props, 10);
        //updateHandler = new UpdateHandler(dbUrl, props, 10);
        requestHandler.start();
        //updateHandler.start();

        try{
            requestHandler.join();
            //updateHandler.join();
        }catch(InterruptedException ie){
            ie.printStackTrace();
        }

    }


}
