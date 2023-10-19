package es.meliseoperez.safehaven.api;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class SimpleAemetDownloader {
    private static final String TAG = "AlertasAemetDownloader";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZXBlcmV6bGF2YW5kZWlyYUBnbWFpbC5jb20iLCJqdGkiOiJkYjk3Y2UzYy1hNDc5LTQzOTItYjgzNi1mMGZmNzM3M2U3YzYiLCJpc3MiOiJBRU1FVCIsImlhdCI6MTY5NjMyOTAxMywidXNlcklkIjoiZGI5N2NlM2MtYTQ3OS00MzkyLWI4MzYtZjBmZjczNzNlN2M2Iiwicm9sZSI6IiJ9.BQDnZISF0QfkCvBYEmCJbyvPZgw_5ayn2YZzMXntcjM"; // Asegúrate de usar tu clave API real.
    private static final String AEMET_ALERTS_URL = "https://opendata.aemet.es/opendata/api/avisos_cap/ultimoelaborado/area/esp";

    // Agregar el Context como parámetro del método.
    public void downloadLatestAlerts(Context context) { // Context se necesita para FileUtils.
        new Thread(() -> {
            // Crea un interceptor para registrar las solicitudes y respuestas.
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d(TAG, "OkHttp: " + message));
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Construir el cliente HTTP con el interceptor.
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            // Construir la solicitud con la URL y la clave API.
            String urlWithApiKey = AEMET_ALERTS_URL + "?api_key=" + API_KEY;
            Request request = new Request.Builder()
                    .url(urlWithApiKey)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error al realizar la petición: " + response);
                } else {
                    // Procesar la respuesta JSON para obtener la URL de los datos.
                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    String dataUrl = jsonObject.getString("datos");

                    // Realizar la segunda solicitud para obtener los datos XML.
                    Request dataRequest = new Request.Builder()
                            .url(dataUrl)
                            .build();

                    try (Response dataResponse = client.newCall(dataRequest).execute()) {
                        if (!dataResponse.isSuccessful()) {
                            Log.e(TAG, "Error al realizar la petición de datos: " + dataResponse);
                        } else {
                            // Extraer y registrar los datos XML.
                            String xmlData = dataResponse.body().string();
                            Log.i(TAG, "Respuesta XML: " + xmlData);

                            // Aquí es donde guardamos los datos XML utilizando FileUtils.
                            // El nombre del archivo se ha establecido como "alertas_aemet.xml".
                            FileUtils.saveToFile(context, xmlData, "alertas_aemet.xml");
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error en la petición HTTP", e);
            } catch (Exception e) {
                Log.e(TAG, "Error al procesar la respuesta JSON", e);
            }
        }).start(); // Iniciar el proceso en un nuevo hilo.
    }
}
