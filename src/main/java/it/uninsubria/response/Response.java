package it.uninsubria.response;

import it.uninsubria.servercm.ServerInterface.ResponseType;
import it.uninsubria.servercm.ServerInterface.Tables;

import java.io.Serial;
import java.io.Serializable;


public class Response implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String clientId;
    private final String requestId;
    private final String responseId;
    private final ResponseType respType;
    private final Tables table;
    private final Object result;

    public Response(String clientId, String requestId, String responseId, ResponseType respType, Tables table, Object result){
        this.clientId = clientId;
        this.requestId = requestId;
        this.responseId = responseId;
        this.respType = respType;
        this.table = table;
        this.result = result;
    }

    public String getClientId(){
        return this.clientId;
    }

    public String getResponseId(){
        return this.responseId;
    }

    public String getRequestId(){return this.requestId;}

    public ResponseType getResponseType(){
        return this.respType;
    }

    public Tables getTable(){
        return this.table;
    }

    public Object getResult(){
        return this.result;
    }

    public String toString(){
        return "{%s, %s, %s := %s, %s}".formatted(this.clientId, this.requestId, this.responseId, this.respType, this.table);
    }

}
