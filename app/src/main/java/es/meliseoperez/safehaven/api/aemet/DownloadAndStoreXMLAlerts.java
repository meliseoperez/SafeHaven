package es.meliseoperez.safehaven.api.aemet;

import android.content.Context;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;

public class DownloadAndStoreXMLAlerts {

    public static final String TAG = "DownloadStoreXMLAlerts"; // Etiqueta para logs

    public void downloadData(MyCallBack myCallback,Context context) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String apiKey = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZXBlcmV6bGF2YW5kZWlyYUBnbWFpbC5jb20iLCJqdGkiOiJkYjk3Y2UzYy1hNDc5LTQzOTItYjgzNi1mMGZmNzM3M2U3YzYiLCJpc3MiOiJBRU1FVCIsImlhdCI6MTY5NjMyOTAxMywidXNlcklkIjoiZGI5N2NlM2MtYTQ3OS00MzkyLWI4MzYtZjBmZjczNzNlN2M2Iiwicm9sZSI6IiJ9.BQDnZISF0QfkCvBYEmCJbyvPZgw_5ayn2YZzMXntcjM";
            String requestUrl = "https://opendata.aemet.es/opendata/api/avisos_cap/ultimoelaborado/area/esp?api_key=" + apiKey;

            try {
                Request initialRequest = new Request.Builder()
                        .url(requestUrl)
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .build();

                try (Response initialResponse = client.newCall(initialRequest).execute()) {

                    if (!initialResponse.isSuccessful()) {
                        Log.e(TAG, "Error en la respuesta: " + initialResponse);
                        return; // Salir del método si hay un error de red
                    }

                    ResponseBody responseBody = initialResponse.body();
                    if (responseBody == null) {
                        Log.e(TAG, "Respuesta recibida, pero sin contenido.");
                        return;
                    }

                    String responseData = responseBody.string();
                    JSONObject json = new JSONObject(responseData);
                    String dataUrl = json.getString("datos");

                    // Siguiente petición para obtener los datos XML
                    Request dataRequest = new Request.Builder()
                            .url(dataUrl)
                            .get()
                            .addHeader("cache-control", "no-cache")
                            .build();

                    try (Response dataResponse = client.newCall(dataRequest).execute()) {
                        if (!dataResponse.isSuccessful()) {
                            Log.e(TAG, "Error en la respuesta de datos: " + dataResponse);
                            return;
                        }

                        ResponseBody dataResponseBody = dataResponse.body();
                        if (dataResponseBody == null) {
                            Log.e(TAG, "Respuesta de datos recibida, pero sin contenido.");
                            return;
                        }

                        String xmlData = dataResponseBody.string();
                        // Aquí deberías procesar el contenido XML si es necesario
                        // y luego guardar los datos en el almacenamiento interno.

                        String processedContent = processXMLContent(xmlData); // Si existe esta función
                        saveToFile(processedContent, "alertas.xml", context);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error durante la descarga de datos", e);

            }
            myCallback.onCompleted();
        }).start();
    }

    private String processXMLContent(String xmlContent) {
        // Implementa tu lógica de procesamiento de XML aquí si es necesario
        return xmlContent; // Retorna el contenido procesado
    }

    private void saveToFile(String content, String fileName, Context context) {
        File file = new File(context.getFilesDir(), fileName);

        //Verifico si el archivo ya existe, en caso afirmativo, lo elimina.
        if(file.exists()){
            file.delete();
        }
        try (FileOutputStream outputStream = new FileOutputStream(file);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(content);
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar el contenido en el archivo", e);
        }

        // Registro de la ruta del archivo guardado.
        String filePath = file.getAbsolutePath();
        Log.d(TAG, "Archivo guardado en: " + filePath);
    }
}
