package es.meliseoperez.safehaven.api.aemet;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class AlertInfo {
    public int id;
    public String effective;
    public String onset;
    public String expires;
    public String senderName;
    public String headline;
    public String description;
    public String instruction;
    public String language;
    public String polygon;
    public String color;
    private List<LatLng> coordenadas;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<LatLng> getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(List<LatLng> coordenadas) {
        this.coordenadas = coordenadas;
    }

    public String getEffective() {
        return effective;
    }

    public void setEffective(String effective) {
        this.effective = effective;
    }

    public String getOnset() {
        return onset;
    }

    public void setOnset(String onset) {
        this.onset = onset;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPolygon() {
        return polygon;
    }

    public void setPolygon(String polygon) {
        this.polygon = polygon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        // Formato personalizado para la impresión de información de alerta.
        return "AlertInfo{" +
                "id=" + id + '\'' +
                ", effective='" + effective + '\'' +
                ", onset='" + onset + '\'' +
                ", expires='" + expires + '\'' +
                ", senderName='" + senderName + '\'' +
                ", headline='" + headline + '\'' +
                ", description='" + description + '\'' +
                ", instruction='" + instruction + '\'' +
                ", language='" + language + '\'' +
                ", polygon='" + polygon + '\'' +
                '}';
    }

}
