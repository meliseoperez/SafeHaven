package es.meliseoperez.safehaven.api.googlemaps;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.meliseoperez.safehaven.api.comments.ZonaDetallesActivity;

/**
 * La clase Zona representa una alerta geográfica en el mapa, la cual puede visualizarse como un polígono.
 */
public class Zona {

    private final List<LatLng> coordenadas; // Almacena las coordenadas del polígono de la zona.
    private final String nivelAlarma; // Representa el nivel de alarma mediante colores.
    public final String descripcion; // Descripción textual de la zona.
    public final String indicaciones; // Instrucciones o recomendaciones para la zona.
    public final int idAlerta; // Identificador único de la alerta.

    /**
     * Constructor de la clase Zona.
     * @param coordenadas Lista de coordenadas que forman el polígono de la zona.
     * @param color Color representativo del nivel de alarma.
     * @param descripcion Descripción de la zona.
     * @param indicaciones Instrucciones específicas para la zona.
     * @param idAlerta Identificador único para la alerta.
     */
    public Zona(List<LatLng> coordenadas, String color, String descripcion, String indicaciones, int idAlerta) {
        this.coordenadas = coordenadas;
        this.descripcion = descripcion;
        this.nivelAlarma = color;
        this.indicaciones = indicaciones;
        this.idAlerta = idAlerta;
    }

    /**
     * Parsea un string conteniendo coordenadas latitud/longitud y las convierte en una lista de LatLng.
     * @param data String que contiene las coordenadas.
     * @return Lista de objetos LatLng generados a partir del string.
     */
    public static List<LatLng> parsearCoordenadas(String data) {
        List<LatLng> coordenadasProcesadas = new ArrayList<>();
        String regex = "(\\d+\\.\\d+),(-?\\d+\\.\\d+)";
        Matcher matcher = Pattern.compile(regex).matcher(data);
        while (matcher.find()) {
            double lat = Double.parseDouble(matcher.group(1));
            double lng = Double.parseDouble(matcher.group(2));
            coordenadasProcesadas.add(new LatLng(lat, lng));
        }
        return coordenadasProcesadas;
    }

    /**
     * Extrae el color asociado al nivel de alarma de una cadena de texto.
     * @param headline Texto que contiene el nivel de alarma.
     * @return Color en formato string.
     */
    public static String extractColorFromHeadline(String headline) {
        Pattern pattern = Pattern.compile("nivel (\\w+)");
        Matcher matcher = pattern.matcher(headline);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }
        return "transparente";
    }

    /**
     * Convierte el nombre de un color a su correspondiente valor ARGB como entero.
     * @param colorName Nombre del color.
     * @return Valor ARGB del color.
     */
    private int getColorFromString(String colorName) {
        switch (colorName.toLowerCase()) {
            case "rojo": return 0xFFFF0000;
            case "verde": return 0xFF00FF00;
            case "amarillo": return 0xFFFFFF00;
            case "naranja": return 0xFFFFA500;
            default: return 0x00000000; // Transparente
        }
    }

    /**
     * Dibuja la zona en el mapa proporcionado usando un polígono.
     * @param mapa GoogleMap donde se dibujará la zona.
     * @param context Contexto utilizado para iniciar una nueva actividad.
     */
    public void dibujarZona(GoogleMap mapa, Context context) {
        InformacionPoligo infoPoligono = new InformacionPoligo(this.idAlerta, this.descripcion, this.indicaciones);

        if (coordenadas.isEmpty()) {
            throw new IllegalStateException("Coordenadas no inicializadas o vacías");
        }

        int fillColor = getColorFromString(this.nivelAlarma);
        PolygonOptions polygonOptions = new PolygonOptions()
                .addAll(coordenadas)
                .fillColor(fillColor)
                .strokeWidth(5)
                .clickable(true);

        Polygon addPolygon = mapa.addPolygon(polygonOptions);
        addPolygon.setTag(infoPoligono);

        mapa.setOnPolygonClickListener(polygon -> {
            InformacionPoligo info = (InformacionPoligo) polygon.getTag();
            Intent intent = new Intent(context, ZonaDetallesActivity.class)
                    .putExtra("ZONA_ID", info.getIdAlerta())
                    .putExtra("ZONA_DESCRIPCION", info.getDescription())
                    .putExtra("ZONA_INSTRUCCIONES", info.getIndicaciones());
            context.startActivity(intent);
        });
    }

    /**
     * Clase auxiliar para almacenar información relevante de la zona para su posterior uso.
     */
    public class InformacionPoligo {
        private final int idAlerta;
        private final String description;
        private final String indicaciones;

        public InformacionPoligo(int idAlerta, String description, String indicaciones) {
            this.idAlerta = idAlerta;
            this.description = description;
            this.indicaciones = indicaciones;
        }

        // Métodos getters para acceder a la información almacenada.
        public int getIdAlerta() { return idAlerta; }
        public String getDescription() { return description; }
        public String getIndicaciones() { return indicaciones; }
    }
}
