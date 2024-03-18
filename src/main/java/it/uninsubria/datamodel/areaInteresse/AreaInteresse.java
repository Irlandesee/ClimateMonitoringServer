package it.uninsubria.datamodel.areaInteresse;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class AreaInteresse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String areaid;
    private String denominazione;
    private String stato;
    private float latitude;
    private float longitude;

    public static final String separatorArea = ";";
    public static final String separatorCoords = ",";

    public AreaInteresse(String areaid){
        this.areaid = areaid;
    }

    public AreaInteresse(String areaid, String denominazione,
                         String stato, float latitude, float longitude) {
        this.areaid = areaid;
        this.denominazione = denominazione;
        this.stato = stato;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAreaid(){return this.areaid;}

    public String getDenominazione(){return this.denominazione;}

    public String getStato(){return this.stato;}

    public float getLatitudine(){return this.latitude;}

    public float getLongitudine(){return this.longitude;}

    public void setAreaid(String areaid) {
        this.areaid = areaid;
    }

    public void setDenominazione(String denominazione) {
        this.denominazione = denominazione;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaInteresse that = (AreaInteresse) o;
        return Float.compare(that.latitude, latitude) == 0
                && Float.compare(that.longitude, longitude) == 0
                && Objects.equals(areaid, that.areaid)
                && Objects.equals(denominazione, that.denominazione)
                && Objects.equals(stato, that.stato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(areaid, denominazione, stato, latitude, longitude);
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(areaid).append(separatorArea)
                .append(denominazione).append(separatorArea)
                .append(stato).append(separatorArea)
                .append(latitude).append(separatorCoords)
                .append(longitude);

        return builder.toString();
    }
}
