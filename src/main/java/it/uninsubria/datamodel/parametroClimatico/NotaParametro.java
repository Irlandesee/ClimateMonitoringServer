package it.uninsubria.datamodel.parametroClimatico;

import java.io.Serial;
import java.io.Serializable;

public class NotaParametro implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String notaId;
    private String notaVento;
    private String notaUmidita;
    private String notaPressione;
    private String notaTemperatura;
    private String notaPrecipitazioni;
    private String notaAltGhiacciai;
    private String notaMassaGhiacciai;

    public NotaParametro(String notaId){
        this.notaId = notaId;
    }

    public NotaParametro(String notaId, String notaVento, String notaUmidita, String notaPressione, String notaTemperatura, String notaPrecipitazioni, String notaAltGhiacciai,  String notaMassaGhiacciai){
        this.notaId = notaId;
        this.notaVento = notaVento;
        this.notaUmidita = notaUmidita;
        this.notaPressione = notaPressione;
        this.notaTemperatura = notaTemperatura;
        this.notaPrecipitazioni = notaPrecipitazioni;
        this.notaAltGhiacciai = notaAltGhiacciai;
        this.notaMassaGhiacciai = notaMassaGhiacciai;
    }

    public String getNotaId(){
        return this.notaId;
    }
    public String getNotaVento() {
        return notaVento;
    }

    public void setNotaVento(String notaVento) {
        this.notaVento = notaVento;
    }

    public String getNotaUmidita() {
        return notaUmidita;
    }

    public void setNotaUmidita(String notaUmidita) {
        this.notaUmidita = notaUmidita;
    }

    public String getNotaPressione() {
        return notaPressione;
    }

    public void setNotaPressione(String notaPressione) {
        this.notaPressione = notaPressione;
    }

    public String getNotaTemperatura() {
        return notaTemperatura;
    }

    public void setNotaTemperatura(String notaTemperatura) {
        this.notaTemperatura = notaTemperatura;
    }

    public String getNotaPrecipitazioni() {
        return notaPrecipitazioni;
    }

    public void setNotaPrecipitazioni(String notaPrecipitazioni) {
        this.notaPrecipitazioni = notaPrecipitazioni;
    }

    public String getNotaAltGhiacciai() {
        return notaAltGhiacciai;
    }

    public void setNotaAltGhiacciai(String notaAltGhiacciai) {
        this.notaAltGhiacciai = notaAltGhiacciai;
    }

    public String getNotaMassaGhiacciai() {
        return notaMassaGhiacciai;
    }

    public void setNotaMassaGhiacciai(String notaMassaGhiacciai) {
        this.notaMassaGhiacciai = notaMassaGhiacciai;
    }

    @Override
    public String toString() {
        return "NotaParametro{" +
                "notaId='" + notaId + '\'' +
                ", notaVento='" + notaVento + '\'' +
                ", notaUmidita='" + notaUmidita + '\'' +
                ", notaPressione='" + notaPressione + '\'' +
                ", notaTemperatura='" + notaTemperatura + '\'' +
                ", notaPrecipitazioni='" + notaPrecipitazioni + '\'' +
                ", notaAltGhiacciai='" + notaAltGhiacciai + '\'' +
                ", notaMassaGhiacciai='" + notaMassaGhiacciai + '\'' +
                '}';
    }
}
