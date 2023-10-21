package es.meliseoperez.safehaven.api.aemet;

public class AlertInfo {
    String effective;
    String onset;
    String expires;
    String senderName;
    String headline;
    String description;
    String instruction;

    @Override
    public String toString() {
        // Formato personalizado para la impresión de información de alerta.
        return "AlertInfo{" +
                "effective='" + effective + '\'' +
                ", onset='" + onset + '\'' +
                ", expires='" + expires + '\'' +
                ", senderName='" + senderName + '\'' +
                ", headline='" + headline + '\'' +
                ", description='" + description + '\'' +
                ", instruction='" + instruction + '\'' +
                '}';
    }

}
