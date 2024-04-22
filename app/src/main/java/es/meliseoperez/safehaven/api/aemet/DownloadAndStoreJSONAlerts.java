package es.meliseoperez.safehaven.api.aemet;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import es.meliseoperez.MainActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Maneja la descarga de datos de alertas desde un servidor y su almacenamiento local en el dispositivo.
 */
public class DownloadAndStoreJSONAlerts {

    public static final String TAG = "DownloadStoreJSONAlerts"; // Etiqueta para registros de log

    /**
     * Descarga datos de alertas desde una URL específica y los guarda localmente.
     *
     * @param myCallback Callback para notificar cuando la descarga y el almacenamiento se completan.
     * @param context Contexto de la aplicación para acceder a los archivos del sistema.
     */
    public void downloadData(MyCallBack myCallback, Context context) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String requestUrl = "http://" + MainActivity.serverIP + ":8000/api/alertas";
            try {
                Request request = new Request.Builder()
                        .url(requestUrl)
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "Error en la respuesta: " + response);
                        return; // Salir si hay un error en la respuesta
                    }

                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        Log.e(TAG, "Respuesta recibida, pero sin contenido.");
                        return;
                    }

                    // Directamente se usa la respuesta como String, sin procesamiento adicional
                    String responseData = responseBody.string();
                    saveToFile(responseData, "alertas2.json", context);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error durante la descarga de datos", e);
            }
            myCallback.onCompleted();
        }).start();
    }

    /**
     * Guarda el contenido en un archivo en el almacenamiento interno del dispositivo.
     *
     * @param content Contenido a guardar.
     * @param fileName Nombre del archivo donde se guardará el contenido.
     * @param context Contexto de la aplicación.
     */
    private void saveToFile(String content, String fileName, Context context) {
        File file = new File(context.getFilesDir(), fileName);

        if (file.exists()) {
            file.delete(); // Elimina el archivo si ya existe
        }
        try (FileOutputStream outputStream = new FileOutputStream(file);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(content); // Escribe el contenido en el archivo
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar el contenido en el archivo", e);
        }

        Log.d(TAG, "Archivo guardado en: " + file.getAbsolutePath());
    }
}
