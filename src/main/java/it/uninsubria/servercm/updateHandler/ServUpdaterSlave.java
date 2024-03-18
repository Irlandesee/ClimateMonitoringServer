package it.uninsubria.servercm.updateHandler;

import it.uninsubria.factories.RequestFactory;
import it.uninsubria.request.Request;
import it.uninsubria.response.Response;
import it.uninsubria.servercm.CallableQuery;
import it.uninsubria.servercm.ServerInterface;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class ServUpdaterSlave extends Thread{
    private final Socket socket;
    private final int slaveId;
    private final Properties props;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private final ExecutorService executorService;
    private final Logger updaterLogger;
    private boolean runCondition;
    public ServUpdaterSlave(Socket socket, int slaveId, Properties props){
        this.socket = socket;
        this.slaveId = slaveId;
        this.props = props;
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
            while(getRunCondition()){
                String s = inputStream.readObject().toString();
                switch(s){
                    case ServerInterface.ID -> {
                        clientId = inputStream.readObject().toString();
                        System.out.printf("UpdaterSlave %d connected to clientUpdater %s\n", slaveId, clientId);
                    }
                    case ServerInterface.UPDATE -> {
                        Request updateRequest = (Request)inputStream.readObject();
                        //Verify that the object received really is and updateRequest
                        if(updateRequest.getRequestType() != ServerInterface.RequestType.requestUpdate){
                            System.err.println("Received some undefined behaviour");
                            outputStream.writeObject(ServerInterface.UNDEFINED_BEHAVIOUR);
                        }else{
                            CallableQuery callableQuery = new CallableQuery(updateRequest, props);
                            Future<Response> futureResponse = executorService.submit(callableQuery);
                            try{
                                Response response = futureResponse.get();
                                outputStream.writeObject(response);
                            }catch(InterruptedException | ExecutionException e){
                                e.printStackTrace();
                            }

                        }
                    }
                    case ServerInterface.QUIT -> {
                        System.out.printf("Client %s has disconnected, Slave %d terminating\n", clientId, slaveId);
                        runCondition = false;
                    }
                    default -> {
                        System.err.println("Received some undefined behaviour");
                        outputStream.writeObject(ServerInterface.UNDEFINED_BEHAVIOUR);
                    }
                }

            }
        }catch(IOException | ClassNotFoundException e){
            updaterLogger.info(e.getMessage());
            runCondition = false;
        }finally{
            try{
                outputStream.close();
                inputStream.close();
                socket.close();
            }catch(IOException ioe){updaterLogger.info(ioe.getMessage());}
        }
        executorService.shutdown();
    }
}
