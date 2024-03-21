package it.uninsubria.datamodel.operatore;

import java.io.Serial;
import java.io.Serializable;

public class OperatoreAutorizzato implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String codFiscale;
    private String email;
    public OperatoreAutorizzato(String codFiscale, String email){
        this.codFiscale = codFiscale;
        this.email = email;
    }

    public String getCodFiscale(){
        return this.codFiscale;
    }

    public String getEmail(){
        return this.email;
    }

    public String toString(){
        return this.codFiscale + ":" + this.email;
    }

}
