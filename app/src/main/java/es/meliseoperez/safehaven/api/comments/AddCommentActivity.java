package es.meliseoperez.safehaven.api.comments;

import static es.meliseoperez.safehaven.api.aemet.DownloadAndStoreJSONAlerts.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.io.IOException;

import es.meliseoperez.MainActivity;
import es.meliseoperez.safehaven.R;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Clase AddCommentActivity: Permite a los usuarios añadir comentarios a alertas específicas.
 * Maneja la lógica para capturar la entrada del usuario, validarla y enviarla a un servidor
 * mediante una solicitud HTTP POST.
 */
public class AddCommentActivity extends AppCompatActivity {

    private EditText editTextComment;
    private Button buttonSubmit;
    private String comment;
    private int alertID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        alertID = getIntent().getIntExtra("ZONA_ID", 0);

        editTextComment = findViewById(R.id.editTextComment);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(v -> submitComment());

        // Habilita la flecha de retorno en el ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Maneja el evento de clic en la flecha de retorno del ActionBar.
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Valida y envía el comentario ingresado por el usuario.
     * Muestra un mensaje si el campo de comentario está vacío.
     */
    private void submitComment() {
        comment = editTextComment.getText().toString().trim();
        if (!comment.isEmpty()) {
            storeApi();
        } else {
            Toast.makeText(this, R.string.comment_empty_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prepara y envía una solicitud HTTP POST para almacenar el comentario en el servidor.
     * Utiliza SharedPreferences para obtener el token de autenticación y el ID del usuario.
     */
    private void storeApi() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        int idUsuario = Integer.parseInt(sharedPreferences.getString("idUsuario", "0"));

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        org.json.JSONObject requestBody = new org.json.JSONObject();
        try {
            requestBody.put("alert_id", alertID > 0 ? alertID : 1000);
            requestBody.put("user_id", idUsuario);
            requestBody.put("comment_text", comment);
            requestBody.put("image_url", "https://via.placeholder.com/640x480.png/005566?text=cupiditate");

            RequestBody body = RequestBody.create(JSON, requestBody.toString());
            Request request = new Request.Builder()
                    .url("http://" + MainActivity.serverIP + ":8000/api/v1/store")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(AddCommentActivity.this, "Comentario enviado.", Toast.LENGTH_SHORT).show());
                    } else {
                        handleErrorResponse(response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(AddCommentActivity.this, R.string.server_connection_error, Toast.LENGTH_LONG).show());
                }
            });
        } catch (JSONException e) {
            Toast.makeText(this, R.string.request_body_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Maneja las respuestas de error del servidor, mostrando un mensaje adecuado al usuario.
     * @param response La respuesta del servidor.
     * @throws IOException Si ocurre un error al leer el cuerpo de la respuesta.
     */
    private void handleErrorResponse(Response response) throws IOException {
        String responseData = response.body().string();
        runOnUiThread(() -> {
            try {
                Gson gson = new Gson();
                ErrorResponseComentario errorResponse = gson.fromJson(responseData, ErrorResponseComentario.class);
                if (errorResponse != null && errorResponse.getError() != null) {
                    Toast.makeText(AddCommentActivity.this, errorResponse.getError(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddCommentActivity.this, R.string.comment_send_error, Toast.LENGTH_LONG).show();
                }
            } catch (JsonParseException e) {
                Toast.makeText(AddCommentActivity.this, getString(R.string.server_response_error) + e, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error procesando respuesta del servidor: ", e);
            }
        });
    }

    /**
     * Clase interna para modelar las respuestas de error del servidor.
     */
    private class ErrorResponseComentario {
        private String error;

        public String getError() {
            return error;
        }

        // Constructor, getters y setters
    }
}
