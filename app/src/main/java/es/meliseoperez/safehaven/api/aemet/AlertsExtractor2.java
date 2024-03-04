package es.meliseoperez.safehaven.api.aemet;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AlertsExtractor2 {

    private static final String TAG = "AlertsExtractor";
    private Context context;
    private final String fileXML;
    private String jsonContenido;

    public AlertsExtractor2(Context context, String fileXML) {
        this.context = context;
        this.fileXML = fileXML;
    }

    public List<AlertInfo> extractAlertsInfo() {
        List<AlertInfo> alerts = new ArrayList<>();

        File contentFile = new File(context.getFilesDir(), "alertas2.json");

        try {
            jsonContenido = new String(Files.readAllBytes(Paths.get(contentFile.getPath())), StandardCharsets.UTF_8);
            Log.i(TAG, "Contenido JSON: " + jsonContenido);

            Gson gson = new Gson();
            Type listType = new TypeToken<AlertResponse>() {}.getType();
            AlertResponse alertResponse = gson.fromJson(jsonContenido, listType);

            // Verifica si la respuesta tiene la propiedad "data" antes de extraer las alertas.
            if (alertResponse != null && alertResponse.getData() != null) {
                alerts = alertResponse.getData();
            }

        } catch (IOException e) {
            Log.e(TAG, "Error al leer el archivo: " + e.getMessage());
        }

        return alerts;
    }

    // Otros métodos (getTagValue, displayAlerts) permanecen sin cambios
    public class AlertResponse {
        @SerializedName("data")
        private List<AlertInfo> data;

        public List<AlertInfo> getData() {
            return data;
        }
    }
}
