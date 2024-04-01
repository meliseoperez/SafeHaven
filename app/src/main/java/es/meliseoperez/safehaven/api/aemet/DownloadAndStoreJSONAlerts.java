package es.meliseoperez.safehaven.api.aemet;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import es.meliseoperez.MainActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadAndStoreJSONAlerts {

    public static final String TAG = "DownloadStoreJSONAlerts"; // Etiqueta para logs

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
                        return; // Salir del método si hay un error de red
                    }

                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        Log.e(TAG, "Respuesta recibida, pero sin contenido.");
                        return;
                    }

                    String responseData = responseBody.string();
                    JSONObject json = new JSONObject(responseData);
                    //JSONArray jsonArray = new JSONArray(responseData);
                    // Aquí deberías procesar el contenido JSON si es necesario
                    // y luego guardar los datos en el almacenamiento interno.

                    //String processedContent = processJSONContent(json); // Si existe esta función
                    String processedContent = responseData; // Directamente el JSON como String
                    saveToFile(processedContent, "alertas2.json", context);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error durante la descarga de datos", e);
            }
            myCallback.onCompleted();
        }).start();
    }

    private void saveToFile(String content, String fileName, Context context) {
        File file = new File(context.getFilesDir(), fileName);

        // Verifico si el archivo ya existe, en caso afirmativo, lo elimina.
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
