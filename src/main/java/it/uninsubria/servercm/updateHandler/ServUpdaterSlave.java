package it.uninsubria.servercm.updateHandler;

import it.uninsubria.factories.RequestFactory;
import it.uninsubria.request.Request;
import it.uninsubria.response.Response;
import it.uninsubria.update.Update;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ServUpdaterSlave extends Thread{
    private final Socket socket;
    private final int slaveId;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private final ExecutorService executorService;
    private final Logger updaterLogger;
    private boolean runCondition;
    private UpdateCacheHandler updateCacheHandler;
    public ServUpdaterSlave(Socket socket, UpdateCacheHandler updateCacheHandler, int slaveId){
        this.socket = socket;
        this.updateCacheHandler = updateCacheHandler;
        this.slaveId = slaveId;
        this.executorService = Executors.newSingleThreadExecutor();
        updaterLogger = Logger.getLogger("[UpdaterLogger:"+slaveId+"]");
        this.setName("[UpdaterSlave:"+slaveId+"]");
        runCondition = true;
    }

    public boolean getRunCondition(){return this.runCondition;}
    public void setRunCondition(boolean runCondition){this.runCondition = runCondition;}

    @Override
    public void run(){
        String clientId = "";
        try{
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            clientId = inputStream.readObject().toString();
            updaterLogger.info("%s connected to client: %s".formatted(this.getName(), clientId));

            while(getRunCondition()){
                updaterLogger.info("%s sending updates to client: %s".formatted(this.getName(), clientId));
                updateCacheHandler.getLatestUpdates();
                try{
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
                }catch(InterruptedException ie){ie.printStackTrace();}
            }

        }catch(IOException | ClassNotFoundException e){
            updaterLogger.info(e.getMessage());
            this.setRunCondition(false);
        }finally{
            updaterLogger.info("Client has disconnected, slave %s terminating".formatted(this.getName()));
            try{
                outputStream.close();
                inputStream.close();
                socket.close();
            }catch(IOException ioe){updaterLogger.info(ioe.getMessage());}
        }
        executorService.shutdown();
    }
}
