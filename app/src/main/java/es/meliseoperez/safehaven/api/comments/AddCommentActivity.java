package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComment();
            }
        });

        // Habilitar la flecha de retorno en el ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void submitComment() {
        comment = editTextComment.getText().toString().trim();
        if (!comment.isEmpty()) {
            storeApi();
        } else {
            Toast.makeText(this, "El comentario no puede estar vacío.", Toast.LENGTH_SHORT).show();
        }
    }

    private void storeApi() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        int idUsuario = Integer.parseInt(sharedPreferences.getString("idUsuario", "0"));

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        org.json.JSONObject requestBody = new org.json.JSONObject();
        try {
            requestBody.put("alert_id", alertID > 0 ? alertID : 1000); // Operador ternario para asignar alert_id
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
                        runOnUiThread(() -> {
                            Toast.makeText(AddCommentActivity.this, "Comentario enviado: " + comment, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        handleErrorResponse(response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(AddCommentActivity.this, "Error de conexión con el servidor.", Toast.LENGTH_LONG).show());
                }
            });
        } catch (JSONException e) {
            Toast.makeText(this, "Error al crear el cuerpo de la solicitud.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleErrorResponse(Response response) throws IOException {
        String responseData = response.body().string();
        runOnUiThread(() -> {
            try {
                Gson gson = new Gson();
                ErrorResponseComentario errorResponse = gson.fromJson(responseData, ErrorResponseComentario.class);
                if (errorResponse != null && errorResponse.getError() != null) {
                    Toast.makeText(AddCommentActivity.this, errorResponse.getError(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddCommentActivity.this, "Error al enviar el comentario.", Toast.LENGTH_LONG).show();
                }
            } catch (JsonParseException e) {
                Toast.makeText(AddCommentActivity.this, "Error al procesar la respuesta del servidor." + e, Toast.LENGTH_LONG).show();
                Log.e("Error envio comentario."  , String.valueOf(e));
            }
        });
    }

    // Define una clase interna para manejar la respuesta de error
    private class ErrorResponseComentario {
        private String error;

        public String getError() {
            return error;
        }

        // ... Constructor, getters y setters
    }
}
