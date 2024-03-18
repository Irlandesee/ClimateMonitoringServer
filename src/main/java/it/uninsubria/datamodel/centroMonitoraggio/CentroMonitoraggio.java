package it.uninsubria.datamodel.centroMonitoraggio;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;

public class CentroMonitoraggio implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    //key: String => areaID

    //contiene solo gli id delle aree interesse associate
    private LinkedList<String> areeInteresseIdAssociate;

    public static final String emptyAreeInteresse = "empty";
    public static final String generalSeparator = ";";
    public static final String areeSeparator = ",";

    private String centroID;
    private String denominazione;
    private String comune;
    private String country;

    public CentroMonitoraggio(String centroID, String denominazioneCentro,
                              String comune, String country){
        this.centroID = centroID;
        this.denominazione = denominazioneCentro;
        this.comune = comune;
        this.country = country;

        areeInteresseIdAssociate = new LinkedList<String>();
    }

    public String getCentroID() {
        return centroID;
    }

    public void setCentroID(String centroID) {
        this.centroID = centroID;
    }

    public String getDenominazione() {
        return this.denominazione;
    }

    public void setDenominazione(String denominazioneCentro) {
        this.denominazione = denominazioneCentro;
    }

    public String getComune() {
        return this.comune;
    }

    public void setComune(String comune) {
        this.comune = comune;
    }

    public String getCountry(){
        return this.country;
    }

    public void setCountry(String country){
        this.country = country;
    }

    public void putAreaId(String areaID){
        if(!areeInteresseIdAssociate.contains(areaID))
            areeInteresseIdAssociate.add(areaID);
        else{
            System.out.println("CM contenente già id");
        }
    }

    public LinkedList<String> getAreeInteresseIdAssociate(){
        return this.areeInteresseIdAssociate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CentroMonitoraggio that = (CentroMonitoraggio) o;
        return Objects.equals(areeInteresseIdAssociate, that.areeInteresseIdAssociate) && Objects.equals(centroID, that.centroID) && Objects.equals(denominazione, that.denominazione) && Objects.equals(comune, that.comune) && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(areeInteresseIdAssociate, centroID, denominazione, comune, country);
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(centroID).append(generalSeparator)
                .append(denominazione).append(generalSeparator)
                .append(comune).append(generalSeparator)
                .append(country).append(generalSeparator);

        if(areeInteresseIdAssociate.isEmpty()) builder.append(emptyAreeInteresse);
        else{
            for (String tmp : areeInteresseIdAssociate) //append the keys
                builder.append(tmp).append(areeSeparator);
        }
        return builder.toString();
    }

}
