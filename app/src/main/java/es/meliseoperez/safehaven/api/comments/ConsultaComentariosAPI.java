package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
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

import es.meliseoperez.MainActivity;
import es.meliseoperez.safehaven.R;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Realiza solicitudes a la API para cargar comentarios y actualiza la UI basada en la respuesta.
 */
public class ConsultaComentariosAPI {

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final ComentariosAdapter comentariosAdapter;

    public ConsultaComentariosAPI(Context context, ComentariosAdapter comentariosAdapter) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        this.comentariosAdapter = comentariosAdapter;
    }

    /**
     * Carga los comentarios de la API y los muestra en el RecyclerView a través del adapter.
     *
     * @param idAlert El ID de la alerta para la que cargar los comentarios, null para cargar todos.
     * @param tipo    El tipo de comentarios a cargar.
     */
    public void cargaComentarios(Integer idAlert, String tipo) {
        String token = sharedPreferences.getString("token", "");
        OkHttpClient client = new OkHttpClient();
        String url = "http://" + MainActivity.serverIP + ":8000/api/v1/comments";
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
                String responseData = response.body().string();
                if (response.isSuccessful()) {
                    List<Comentario> comentarioList = parseComentarios(responseData);
                    runOnUiThread(() -> comentariosAdapter.setComentariosList(comentarioList));
                } else {
                    handleErrorResponse(responseData);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showErrorToast(context.getString(R.string.server_connection_error));
            }
        });
    }

    private void handleErrorResponse(String responseData) {
        Gson gson = new Gson();
        try {
            ErrorResponseComentario errorResponse = gson.fromJson(responseData, ErrorResponseComentario.class);
            if (errorResponse != null && errorResponse.getError() != null) {
                showErrorMessageAsToast(errorResponse.getError());
            } else {
                showErrorToast(context.getString(R.string.comment_send_error));
            }
        } catch (JsonParseException e) {
            showErrorToast(context.getString(R.string.server_response_error));
            Log.e("ConsultaComentariosAPI", "Error al procesar la respuesta del servidor", e);
        }
    }

    private List<Comentario> parseComentarios(String responseData) {
        Gson gson = new Gson();
        ComentariosResponse comentariosResponse = gson.fromJson(responseData, ComentariosResponse.class);
        return comentariosResponse != null ? comentariosResponse.getData() : new ArrayList<>();
    }

    private void runOnUiThread(Runnable action) {
        ((AppCompatActivity) context).runOnUiThread(action);
    }

    private void showErrorToast(String message) {
        runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }

    private void showErrorMessageAsToast(String errorMessage) {
        List<Comentario> comentarioList = new ArrayList<>();
        Comentario comentarioError = new Comentario();
        comentarioError.setCommentText(errorMessage); // Asume que tienes un método setCommentText en tu clase Comentario
        comentarioList.add(comentarioError);
        runOnUiThread(() -> comentariosAdapter.setComentariosList(comentarioList));
    }
}
