package it.uninsubria.servercm;
import it.uninsubria.request.Request;
import it.uninsubria.response.Response;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ServerSlave implements Runnable{

    private int slaveId;
    private Socket sock;
    private ObjectInputStream inStream;
    private ObjectOutputStream outStream;
    private Properties props;
    private final ExecutorService executorService;
    private final int MAX_NUMBER_OF_THREADS = 5;
    public ServerSlave(Socket sock, int slaveId, Properties props){
        //executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
        executorService = Executors.newSingleThreadExecutor();
        this.sock = sock;
        this.slaveId = slaveId;
        this.props = props;
    }

    public void run(){
        String clientId = "";
        boolean runCondition = true;
        try{
            outStream = new ObjectOutputStream(sock.getOutputStream());
            inStream = new ObjectInputStream(sock.getInputStream());
            while(runCondition){
                String s = inStream.readObject().toString();
                switch(s){
                    case ServerInterface.ID -> {
                        clientId = inStream.readObject().toString();
                        System.out.printf("Slave %d connected to client %s\n", slaveId, clientId);
                    }
                    case ServerInterface.NEXT -> {
                        Request request = (Request) inStream.readObject();
                        CallableQuery callableQuery = new CallableQuery(request, props);
                        Future<Response> futureResponse = executorService.submit(callableQuery);
                        try{
                            Response response = futureResponse.get();
                            outStream.writeObject(response);
                        }catch(InterruptedException | ExecutionException e){
                            e.printStackTrace();
                        }
                    }
                    case ServerInterface.TEST -> {
                        int number = (int) inStream.readObject();
                        System.out.printf("Slave %d received %d\n", slaveId, number);
                        number += 1;
                        System.out.printf("Slave %d sending: %d\n", slaveId, number);
                        outStream.writeObject(number);
                    }
                    case ServerInterface.QUIT -> {
                        System.out.printf("Client %s has disconnected, Slave %d terminating\n", clientId, slaveId);
                        runCondition = false;
                    }
                    default -> {
                        System.err.println("Received some undefined behaviour");
                        outStream.writeObject(ServerInterface.UNDEFINED_BEHAVIOUR);
                    }
                }
            }
        }catch(IOException ioe){
            System.out.printf("Client %s has disconnected, Slave %d terminating\n", clientId, slaveId);
            //runCondition = false;
            ioe.printStackTrace();
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }finally{
            try{
                outStream.close();
                inStream.close();
                sock.close();
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
        executorService.shutdown();

    }

}
