package it.uninsubria.request;

import it.uninsubria.servercm.ServerInterface.RequestType;
import it.uninsubria.servercm.ServerInterface.Tables;
import it.uninsubria.util.IDGenerator;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

public class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String requestId;
    private final String clientId;
    private final Tables table;
    private final RequestType requestType;
    private final Map<String, String> params;
    public Request(String clientId, RequestType requestType, Tables table, Map<String, String> params){
        this.requestId = IDGenerator.generateID();
        this.clientId = clientId;
        this.table = table;
        this.requestType = requestType;
        this.params = params;
    }

    public String getClientId(){
        return this.clientId;
    }

    public String getRequestId(){
        return this.requestId;
    }

    public Tables getTable(){
        return this.table;
    }

    public RequestType getRequestType(){
        return this.requestType;
    }

    public Map<String, String> getParams(){
        return this.params;
    }

    public String toString(){
        return "{%s, %s := %s}".formatted(this.clientId, this.requestId, this.requestType);
    }
}
