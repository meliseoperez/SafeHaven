package es.meliseoperez.safehaven.api.aemet;

import com.google.android.gms.maps.model.LatLng;
import java.util.List;

/**
 * Clase AlertInfo: Representa la información de alerta obtenida de AEMET (Agencia Estatal de Meteorología).
 * Almacena detalles como identificador, efectividad, inicio, expiración, emisor, título, descripción,
 * instrucciones, idioma, polígono y color de la alerta, junto con las coordenadas geográficas.
 */
public class AlertInfo {
    // Identificador único de la alerta.
    public int id;

    // Fecha de efectividad de la alerta.
    public String effective;

    // Fecha de inicio de la alerta.
    public String onset;

    // Fecha de expiración de la alerta.
    public String expires;

    // Nombre del emisor de la alerta.
    public String senderName;

    // Título de la alerta.
    public String headline;

    // Descripción detallada de la alerta.
    public String description;

    // Instrucciones para la respuesta o acción recomendada.
    public String instruction;

    // Idioma de la información de la alerta.
    public String language;

    // Representación de polígono de la zona de alerta en formato de cadena.
    public String polygon;

    // Color asociado a la alerta para visualización.
    public String color;

    // Coordenadas geográficas del polígono de alerta.
    private List<LatLng> coordenadas;

    // Métodos getters y setters para cada campo con documentación básica.

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
        // Proporciona una representación en cadena de la información de la alerta para propósitos de depuración o registro.
        return "AlertInfo{" +
                "id=" + id +
                ", effective='" + effective + '\'' +
                ", onset='" + onset + '\'' +
                ", expires='" + expires + '\'' +
                ", senderName='" + senderName + '\'' +
                ", headline='" + headline + '\'' +
                ", description='" + description + '\'' +
                ", instruction='" + instruction + '\'' +
                ", language='" + language + '\'' +
                ", polygon='" + polygon + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
