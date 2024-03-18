package it.uninsubria.response;

import it.uninsubria.servercm.ServerInterface;

public class MalformedResponseException extends Exception{
    private final String message;
    public MalformedResponseException(String clientId, String requestId, ServerInterface.ResponseType respType, ServerInterface.Tables table, Object result, String message) {
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }
}
