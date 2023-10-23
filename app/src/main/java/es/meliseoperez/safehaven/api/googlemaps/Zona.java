package es.meliseoperez.safehaven.api.googlemaps;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Zona {
    // Lista que almacena las coordenadas de la zona.
    private List<LatLng> coordenadas;

    /**
     * Constructor que recibe un String con las coordenadas.
     * @param data String con las coordenadas en formato "latitud,longitud".
     */
    public Zona(String data) {
        coordenadas = new ArrayList<>();
        procesarDatos(data); // Llamada al método que procesa el String de datos.
    }

    /**
     * Método privado que procesa el String de datos para obtener las coordenadas.
     * @param data String con las coordenadas.
     */
    private void procesarDatos(String data) {
        // Usamos una expresión regular para identificar y extraer las coordenadas.
        String regex = "(\\d+\\.\\d+),(-?\\d+\\.\\d+)";
        Matcher matcher = Pattern.compile(regex).matcher(data);

        while (matcher.find()) {
            double lat = Double.parseDouble(matcher.group(1));
            double lng = Double.parseDouble(matcher.group(2));
            coordenadas.add(new LatLng(lat, lng)); // Añadir la coordenada a la lista.
        }
    }

    /**
     * Método que dibuja la zona en el mapa proporcionado.
     * @param mapa Instancia de GoogleMap donde se dibujará la zona.
     */
    public void dibujarZona(GoogleMap mapa) {
        // Crear las opciones de polígono con las coordenadas.
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(coordenadas);
        polygonOptions.fillColor(0x5500FF00); // Color de relleno del polígono.
        polygonOptions.strokeWidth(5); // Ancho del borde del polígono.

        // Dibujar el polígono en el mapa.
        mapa.addPolygon(polygonOptions);
    }
    public LatLng getCentro() {
        if (!coordenadas.isEmpty()) {
            return coordenadas.get(0); // Retorna el primer punto como centro. Idealmente, deberías calcular el centro real del polígono.
        }
        return null; // En caso de que no haya coordenadas
    }

}
