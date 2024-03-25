package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ConsulataComentariosAPI {

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final ComentariosAdapter comentariosAdapter;

    public ConsulataComentariosAPI(Context context, ComentariosAdapter comentariosAdapter) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        this.comentariosAdapter = comentariosAdapter;
    }

    public void cargaComentarios(Integer idAlert, String tipo) {
        String token = sharedPreferences.getString("token", "");
        OkHttpClient client = new OkHttpClient();
        String url = "http://172.20.10.2:8000/api/v1/comments";
        if (idAlert != null) {
            url += "/" + idAlert + "?type=" + tipo;
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Aquí asumimos que si la llamada no tiene éxito, aún tenemos un cuerpo de respuesta con un mensaje
                String responseData = response.body().string();
                if (response.isSuccessful()) {
                    List<Comentario> comentarioList = parseComentarios(responseData);
                    runOnUiThread(() -> {
                        comentariosAdapter.setComentariosList(comentarioList);
                        comentariosAdapter.notifyDataSetChanged();
                    });
                } else {
                    // Aquí manejas el caso de error, incluyendo el error 404
                    Gson gson = new Gson();
                    try {
                        ErrorResponseComentario errorResponse = gson.fromJson(responseData, ErrorResponseComentario.class);
                        if (errorResponse != null && errorResponse.getError() != null) {
                            // Aquí decides mostrar el mensaje de error como un comentario
                            List<Comentario> comentarioList = new ArrayList<>();
                            Comentario comentarioError = new Comentario();
                            comentarioError.setCommentText(errorResponse.getError()); // Asume que tienes un método setCommentText en tu clase Comentario
                            comentarioList.add(comentarioError);
                            runOnUiThread(() -> {
                                comentariosAdapter.setComentariosList(comentarioList);
                                comentariosAdapter.notifyDataSetChanged();
                            });
                        }
                    } catch (JsonParseException e) {
                        showErrorToast("Error al procesar la respuesta del servidor.");
                        Log.e("Error al procesar response reserver",e.toString());
                    } catch (Exception e) {
                        showErrorToast("Error desconocido.");
                        Log.e("AppError", "Error desconocido", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showErrorToast("Error de conexión con el servidor.");
            }

            private void runOnUiThread(Runnable action) {
                ((AppCompatActivity) context).runOnUiThread(action);
            }

            private void showErrorToast(String message) {
                runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
            }
        });
    }

    private List<Comentario> parseComentarios(String responseData) throws JsonParseException {
        Gson gson = new Gson();
        // Intenta primero deserializar a la estructura esperada de comentarios
        try {
            ComentariosResponse comentariosResponse = gson.fromJson(responseData, ComentariosResponse.class);
            if (comentariosResponse != null && comentariosResponse.getData() != null) {
                return comentariosResponse.getData();
            }
        } catch (JsonParseException ignored) {
            // Si falla, intenta deserializar a la estructura de error
            ErrorResponseComentario errorResponse = gson.fromJson(responseData, ErrorResponseComentario.class);
            if (errorResponse != null && errorResponse.getError() != null) {
                throw new JsonParseException(errorResponse.getError());
            }
        }
        return new ArrayList<>();
    }

}
