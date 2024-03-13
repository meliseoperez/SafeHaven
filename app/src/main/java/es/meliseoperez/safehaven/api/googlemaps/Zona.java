package es.meliseoperez.safehaven.api.googlemaps;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.meliseoperez.safehaven.api.aemet.AlertInfo;
import es.meliseoperez.safehaven.api.comments.ZonaDetallesActivity;
import es.meliseoperez.safehaven.database.AlertRepository;

public class Zona {

    private final List<LatLng> coordenadas;// Lista que almacena las coordenadas de la zona.
    private final String nivelAlarma; // Nombre de la zona
    public final String descripcion;
    public final String indicaciones;
    public final int idAlerta;



    /**
     * Constructor que recibe un String con las coordenadas.
     */
    public Zona(List<LatLng> coordenadas, String color, String descripcion, String indicaciones, int idAlerta) {
        this.coordenadas = coordenadas;
        this.descripcion = descripcion;
        this.nivelAlarma = color;

        this.indicaciones = indicaciones;
        this.idAlerta = idAlerta;
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
        InformacionPoligo infoPoligono = new InformacionPoligo(this.idAlerta, this.descripcion,this.indicaciones);

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
        mapa.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon polygon) {
                // Obtiene el objeto InformacionPoligo asociado al polígono clickeado.
                InformacionPoligo infoPoligono = (InformacionPoligo) polygon.getTag();

                // Ahora puedes usar infoPoligono para obtener el idAlerta específico de este polígono.
                int idAlertaClickeada = infoPoligono.getIdAlerta();

                AlertRepository accesoBD = new AlertRepository(context.getApplicationContext());
                accesoBD.open();
                AlertInfo alerta = accesoBD.getAlertById(idAlertaClickeada);
                //Iniciar ZonaDetallesActivity pasando el ID de la zona como extra
                Intent intent = new Intent(context, ZonaDetallesActivity.class);
                intent.putExtra("ZONA_ID", idAlertaClickeada);

                intent.putExtra("ZONA_DESCRIPCION", alerta.getDescription() != null ?  alerta.getDescription() : "No hay descripción para la alerta." );
                intent.putExtra("ZONA_INSTRUCCIONES", alerta.getInstruction() != null ? alerta.getInstruction() : "No hay instrucciones para la alerta.");


                context.startActivity(intent);
            }
        });
    }
    //Clase auxiliar para almacenar información sobre alertas,
    public class InformacionPoligo{
        private final String description;
        private final String indicaciones;

        private final int idAlerta;

        public InformacionPoligo(int idAlerta, String description, String indicaciones) {
            this.description = description;
            this.indicaciones = indicaciones;
            this.idAlerta = idAlerta;
        }

        public String getDescription() {
            return description;
        }

        public String getIndicaciones() {
            return indicaciones;
        }
        public int getIdAlerta(){return idAlerta;}
    }
}
