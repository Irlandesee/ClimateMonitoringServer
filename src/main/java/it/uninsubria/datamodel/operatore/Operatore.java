package it.uninsubria.datamodel.operatore;

import java.io.Serial;
import java.io.Serializable;

public class Operatore implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String nome;
    private String cognome;
    private String codFiscale;
    private String email;

    private String userID;
    private String password;
    private String centroID;


    public static final String generalSep = ";";
    public static final String terminatingChar = ",";

    public Operatore(String nome, String cognome, String codFiscale, String email, String userID,
                     String password, String centroID){
        this.nome = nome;
        this.cognome = cognome;
        this.codFiscale = codFiscale;
        this.email = email;
        this.userID = userID;
        this.password = password;
        this.centroID = centroID;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getCodFiscale() {
        return codFiscale;
    }

    public void setCodFiscale(String codFiscale) {
        this.codFiscale = codFiscale;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserID(){return this.userID;}
    public void setUserID(String userID){this.userID = userID;}
    public String getPassword(){return this.password;}
    public void setPassword(String password){this.password = password;}
    public String getCentroID(){return this.centroID;}
    public void setCentroID(String centroID){this.centroID = centroID;}

    @Override
    public String toString(){
        return this.nome + "," + this.cognome + "," + this.userID + "," + this.password + "," + this.email+ "," + this.codFiscale + "," + this.centroID;
    }

}
