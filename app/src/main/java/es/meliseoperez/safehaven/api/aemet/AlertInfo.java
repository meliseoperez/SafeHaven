package es.meliseoperez.safehaven.api.aemet;

public class AlertInfo {
    public String effective;
    public String onset;
    public String expires;
    public String senderName;
    public String headline;
    public String description;
    public String instruction;
    public String language;
    public String polygon;

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
                ", language='" + language + '\'' +
                ", polygon='" + polygon + '\'' +
                '}';
    }

}
