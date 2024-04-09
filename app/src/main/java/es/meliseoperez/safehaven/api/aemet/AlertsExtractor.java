package es.meliseoperez.safehaven.api.aemet;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase AlertsExtractor: Se encarga de extraer la información de alertas meteorológicas almacenadas en un archivo JSON.
 * Utiliza la biblioteca Gson para deserializar los datos JSON en objetos AlertInfo.
 */
public class AlertsExtractor {

    private static final String TAG = "AlertsExtractor"; // Etiqueta para el registro de depuración.
    private Context context; // Contexto de la aplicación Android.
    private final String fileToRead; // Nombre del archivo a leer.
    private String jsonContenido; // Contenido JSON como cadena de texto.
    private FileReader fileReader; // Lector de archivos para leer el contenido del archivo.

    /**
     * Constructor para inicializar la instancia de AlertsExtractor con un FileReader específico.
     * @param context Contexto de la aplicación.
     * @param fileToRead Nombre del archivo a leer.
     * @param fileReader Lector de archivos preparado para leer el archivo.
     */
    public AlertsExtractor(Context context, String fileToRead, FileReader fileReader) {
        this.context = context;
        this.fileToRead = fileToRead;
        this.fileReader = fileReader;
    }

    /**
     * Constructor para inicializar la instancia de AlertsExtractor sin un FileReader.
     * @param context Contexto de la aplicación.
     * @param fileXML Nombre del archivo a leer.
     */
    public AlertsExtractor(Context context, String fileXML) {
        this.context = context;
        this.fileToRead = fileXML;
    }

    /**
     * Extrae y devuelve una lista de AlertInfo a partir del contenido JSON del archivo especificado.
     * @return Lista de objetos AlertInfo con la información de las alertas.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public List<AlertInfo> extractAlertsInfo() throws IOException {
        List<AlertInfo> alerts = new ArrayList<>();

        File contentFile = new File(context.getFilesDir(), "alertas2.json");

        try {
            jsonContenido = new String(Files.readAllBytes(Paths.get(contentFile.getPath())), StandardCharsets.UTF_8);
            if (jsonContenido.trim().isEmpty()) {
                return new ArrayList<>();
            }
            Gson gson = new Gson();
            Type listType = new TypeToken<AlertResponse>() {}.getType();
            AlertResponse alertResponse = gson.fromJson(jsonContenido, listType);

            if (alertResponse != null && alertResponse.getData() != null) {
                alerts = alertResponse.getData();
            }

        } catch (IOException e) {
            Log.e(TAG, "Error al leer el archivo: " + e.getMessage());
            throw e;
        }

        return alerts;
    }

    /**
     * Clase interna AlertResponse para deserializar la respuesta JSON.
     * Contiene una lista de AlertInfo que representa las alertas específicas.
     */
    public class AlertResponse {
        @SerializedName("data") // Anotación para indicar el nombre del campo en el JSON.
        private List<AlertInfo> data= new ArrayList<>();

        public List<AlertInfo> getData() {
            return data;
        }
    }

    // Los métodos getTagValue y displayAlerts no se modifican en este contexto y deben ser documentados de manera similar.
}
