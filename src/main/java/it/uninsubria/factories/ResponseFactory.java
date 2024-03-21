package it.uninsubria.factories;

import it.uninsubria.response.Response;
import it.uninsubria.servercm.ServerInterface;

public class ResponseFactory {
    public static Response buildResponse(String clientId, String requestId, String responseId, ServerInterface.ResponseType responseType, ServerInterface.Tables table, Object result){
        //TODO only if necessary
        return null;
    }

}
