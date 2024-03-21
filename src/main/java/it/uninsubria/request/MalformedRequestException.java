package it.uninsubria.request;

import it.uninsubria.servercm.ServerInterface;

import java.util.Map;

public class MalformedRequestException extends Exception{

    private final String message;
    public MalformedRequestException(String message) {
        this.message = message;
    }
    public String getMessage(){ return this.message;}

}
