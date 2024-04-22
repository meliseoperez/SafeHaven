package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.meliseoperez.MainActivity;
import es.meliseoperez.safehaven.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Activity que permite a los usuarios actualizar o eliminar un comentario específico.
 * Los usuarios pueden modificar el texto del comentario o eliminar el comentario por completo.
 */
public class UDCommentActivity extends AppCompatActivity {
    private Button actualizar;
    private Button eliminar;
    private EditText etNuevoComentario;
    private String comentarioUnico;
    private String nuevoComentario;
    private SharedPreferences sharedPreferences;
    private int commentId;
    private int idUsuario;

    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udcomment);

        sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        okHttpClient = new OkHttpClient();

        actualizar = findViewById(R.id.btnModificarComentario);
        eliminar = findViewById(R.id.btnElinarComentario);

        etNuevoComentario = findViewById(R.id.etnuevoComentario);

        idUsuario = getIntent().getIntExtra("usuarioId",0);
        commentId = getIntent().getIntExtra("comentarioId", 0);

        // Carga inicial del comentario para permitir su edición.
        cargarComentario(commentId, sharedPreferences.getString("token", ""));
        eliminar.setOnClickListener(v -> eliminarMensaje(commentId, sharedPreferences.getString("token", "")));
        actualizar.setOnClickListener(v -> modificarMensaje(commentId, sharedPreferences.getString("token", "")));

        // Configura la ActionBar para incluir un botón de retroceso.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Carga el comentario específico del servidor para mostrarlo en el EditText.
     * Permite al usuario ver y editar el comentario actual antes de enviar una actualización.
     *
     * @param commentId ID del comentario a cargar.
     * @param token Token de autenticación para la solicitud de API.
     */
    private void cargarComentario(int commentId, String token) {
        String url = "http://" + MainActivity.serverIP + ":8000/api/v1/comments/"+commentId +"?type=idComent";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(),"error en carga comentario antiguo.",Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    Comentario comentario = gson.fromJson(responseData, ComentariosResponse.class).getData().get(0);
                    comentarioUnico = comentario.getCommentText();
                    runOnUiThread(()->{
                        etNuevoComentario.setText(comentarioUnico);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error al cargar el comentario.", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    /**
     * Envía una solicitud al servidor para eliminar un comentario específico.
     *
     * @param commentId ID del comentario a eliminar.
     * @param token Token de autenticación para la solicitud de API.
     */
    private void eliminarMensaje(int commentId, String token) {
        String url = "http://" + MainActivity.serverIP + ":8000/api/v1/comments/"+commentId;
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Authorization", "Bearer " + token)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error al actualizar el comentario.", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() ->Toast.makeText(getApplicationContext(),"Comentario ELIMINADO.",Toast.LENGTH_LONG).show());
                    Intent intent = new Intent(UDCommentActivity.this, ComentariosActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    runOnUiThread(() ->Toast.makeText(getApplicationContext(),"error al ELIMINAR comentario .",Toast.LENGTH_LONG).show());
                }
            }
        });
    }
    /**
     * Envía una solicitud al servidor para actualizar el comentario con el nuevo texto proporcionado por el usuario.
     *
     * @param commentId ID del comentario a actualizar.
     * @param token Token de autenticación para la solicitud de API.
     */
    private void modificarMensaje(int commentId, String token){
        String  nuevoComentario = etNuevoComentario.getText().toString();
        if(nuevoComentario == null){
            Toast.makeText(getApplicationContext(),"El comentario está vacío.",Toast.LENGTH_LONG).show();
            return;
        }
        String url = "http://" + MainActivity.serverIP + ":8000/api/v1/comments/"+commentId;

        JSONObject comentarioJson = new JSONObject();
        try{
            comentarioJson.put("comment_text",nuevoComentario);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Error al crear el cuerpo de la solicitud.", Toast.LENGTH_LONG).show();
        }
        RequestBody body = RequestBody.create(comentarioJson.toString(), MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error al actualizar el comentario.", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (response.isSuccessful()) {
                    runOnUiThread(() ->Toast.makeText(getApplicationContext(),"Comentario MODIFICADO.",Toast.LENGTH_LONG).show());
                    Intent intent = new Intent(UDCommentActivity.this, ComentariosActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    runOnUiThread(() ->Toast.makeText(getApplicationContext(),"Error al actualizar el comentario.",Toast.LENGTH_LONG).show());
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
