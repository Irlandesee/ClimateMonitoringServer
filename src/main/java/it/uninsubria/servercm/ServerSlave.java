package it.uninsubria.servercm;
import it.uninsubria.factories.RequestFactory;
import it.uninsubria.request.Request;
import it.uninsubria.response.Response;
import it.uninsubria.util.IDGenerator;

import java.util.Map;
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

    private final int slaveId;
    private final Socket sock;
    private ObjectInputStream inStream;
    private ObjectOutputStream outStream;
    private final Properties props;
    private final ExecutorService executorService;
    private final String defaultSlaveUser;
    private final String defaultSlavePassword;
    public ServerSlave(Socket sock, int slaveId, Properties props){
        executorService = Executors.newSingleThreadExecutor();
        this.sock = sock;
        this.slaveId = slaveId;

        this.props = props;
        defaultSlaveUser = this.props.getProperty("db.username");
        defaultSlavePassword = this.props.getProperty("db.password");
    }

    /**
     * Implementazione del metodo run dell'interfaccia Runnable
     * Legge, riceve e manda le varie richieste e risposte al client.
     */
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
                            System.out.println(e.getMessage());
                        }
                    }
                    case ServerInterface.TEST -> {
                        int number = (int) inStream.readObject();
                        System.out.printf("Slave %d received %d\n", slaveId, number);
                        number += 1;
                        System.out.printf("Slave %d sending: %d\n", slaveId, number);
                        outStream.writeObject(number);
                    }
                    case ServerInterface.LOGIN -> {
                        Request loginRequest = (Request) inStream.readObject();
                        CallableQuery callableQuery = new CallableQuery(loginRequest, props);
                        Future<Response> futureResponse = executorService.submit(callableQuery);
                        try{
                            Response loginResponse = futureResponse.get();
                            if(loginResponse.getResponseType() == ServerInterface.ResponseType.loginOk){
                                System.out.println("Login ok... setting slave properties to match the user's properties");
                                Map<String, String> params = loginRequest.getParams();
                                props.setProperty("db.username", params.get(RequestFactory.userKey));
                                props.setProperty("db.password", params.get(RequestFactory.passwordKey));
                                System.out.println(props);
                            }
                            outStream.writeObject(loginResponse);
                        }catch(InterruptedException | ExecutionException e){
                            System.out.println(e.getMessage());
                        }
                    }
                    case ServerInterface.LOGOUT -> {
                        Request logoutRequest = (Request) inStream.readObject();
                        if(logoutRequest.getRequestType() == ServerInterface.RequestType.executeLogout){
                            System.out.printf("Client %s has logged out, setting slave properties to default\n", clientId);
                            this.props.setProperty("db.username", defaultSlaveUser);
                            this.props.setProperty("db.password", defaultSlavePassword);
                            System.out.println(props);
                            Response logoutResponse = new Response(
                                    clientId,
                                    logoutRequest.getRequestId(),
                                    IDGenerator.generateID(),
                                    ServerInterface.ResponseType.logoutOk,
                                    null,
                                    null);
                            outStream.writeObject(logoutResponse);
                        }
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
            System.out.println(ioe.getMessage());
        }catch(ClassNotFoundException cnfe){
            System.out.println(cnfe.getMessage());
        }finally{
            try{
                outStream.close();
                inStream.close();
                sock.close();
            }catch(IOException ioe){
                System.out.println(ioe.getMessage());
            }
        }
        executorService.shutdown();

    }

}
