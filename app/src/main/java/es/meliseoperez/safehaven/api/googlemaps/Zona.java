package es.meliseoperez.safehaven.api.googlemaps;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Zona {

    private final List<LatLng> coordenadas;// Lista que almacena las coordenadas de la zona.
    private final String nivelAlarma; // Nombre de la zona
    private final String descripcion;
    private final String indicaciones;



    /**
     * Constructor que recibe un String con las coordenadas.
     */
    public Zona(List<LatLng> coordenadas, String color, String descripcion, String indicaciones) {
        this.coordenadas = coordenadas;
        this.descripcion = descripcion;
        this.nivelAlarma = color;

        this.indicaciones = indicaciones;
    }



    /**
     * Método privado que procesa el String de datos para obtener las coordenadas.
     *
     * @param data String con las coordenadas.
     */
    public static List<LatLng> parsearCoordenadas(String data) {
        // Usamos una expresión regular para identificar y extraer las coordenadas.
        List<LatLng> coordenadasProcesadas = new ArrayList<>();
        String regex = "(\\d+\\.\\d+),(-?\\d+\\.\\d+)";
        Matcher matcher = Pattern.compile(regex).matcher(data);

        while (matcher.find()) {
            double lat = Double.parseDouble(matcher.group(1));
            double lng = Double.parseDouble(matcher.group(2));
            coordenadasProcesadas.add(new LatLng(lat, lng)); // Añadir la coordenada a la lista.
        }
        return coordenadasProcesadas;
    }

    public static String extractColorFromHeadline(String headline) {
        Pattern pattern = Pattern.compile("nivel (\\w+)");
        Matcher matcher = pattern.matcher(headline);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }
        return "transparente";
    }

    private int getColorFromString(String colorName) {
        switch (colorName.toLowerCase()) {
            case "rojo":
                return 0xFFFF0000;//Color rojo
            case "verde":
                return 0xFF00FF00;//Color verde
            case "amarillo":
                return 0xFFFFFF00;//Color amarillo
            case "naranja":
                return 0xFFFFA500; // Color naranja
            default:
                return 0x00000000; // Por defecto transparente
        }
    }

    /**
     * Método que dibuja la zona en el mapa proporcionado.
     *
     * @param mapa Instancia de GoogleMap donde se dibujará la zona.
     */
    public void dibujarZona(GoogleMap mapa, Context context ){
        InformacionPoligo infoPoligono=new InformacionPoligo(this.descripcion,this.indicaciones);

        // Asegúrate de que las coordenadas no estén vacías o no inicializadas.
        if (coordenadas == null || coordenadas.isEmpty()) {
            throw new IllegalStateException("Coordenadas no inicializadas o vacías");
        }

        // Convierte el nombre del color en un valor de color.
        int fillColor = getColorFromString(this.nivelAlarma);


        // Crea las opciones de polígono con las coordenadas y configura estilos.
        PolygonOptions polygonOptions = new PolygonOptions()
                .addAll(coordenadas)
                .fillColor(fillColor) // Color de relleno del polígono.
                .strokeWidth(5) // Ancho del borde del polígono.
                .clickable(true); // Hace que el polígono sea clickable.

        // Agrega el polígono al mapa y guarda la instancia del polígono.
        Polygon polygon = mapa.addPolygon(polygonOptions);
        polygon.setTag(infoPoligono); // Establece la descripcion como tag para el polígono.

        // Establece el evento de clic para el polígono.
        mapa.setOnPolygonClickListener(polygonClicked -> {
            // Aquí se manejará el clic en el polígono.
            InformacionPoligo tag = (InformacionPoligo) polygonClicked.getTag();
            // Muestra un mensaje con el ID del polígono.
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            builder.setTitle("Información Alerta");
            builder.setMessage("Descripción: " + "\n" + tag.getDescription()+
                    "\nIndicaciones: " + "\n" + tag.getIndicaciones());
            builder.setPositiveButton("OK", (dialog, wicht) -> dialog.dismiss());

            AlertDialog alert = builder.create();
            alert.show();
           // Toast.makeText(context, "ID de la zona: " + tag, Toast.LENGTH_LONG).show();
        });
    }
    //Clase auxiliar para almacenar información sobre alertas,
    public class InformacionPoligo{
        private final String description;
        private final String indicaciones;

        public InformacionPoligo(String description, String indicaciones) {
            this.description = description;
            this.indicaciones = indicaciones;
        }

        public String getDescription() {
            return description;
        }

        public String getIndicaciones() {
            return indicaciones;
        }
    }
}
