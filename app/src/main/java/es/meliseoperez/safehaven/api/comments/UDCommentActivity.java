package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.meliseoperez.safehaven.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UDCommentActivity extends AppCompatActivity {
    private Button actualizar;
    private Button eliminar;
    private TextView textView;
    private EditText etnuevoComentario;
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

        textView = findViewById(R.id.idComent);
        actualizar = findViewById(R.id.btnModificarComentario);
        eliminar = findViewById(R.id.btnElinarComentario);

        etnuevoComentario = findViewById(R.id.etnuevoComentario);

        idUsuario = getIntent().getIntExtra("usuarioId",0);
        commentId = getIntent().getIntExtra("comentarioId", 0);

        cargarComentario(commentId,token);
        eliminar.setOnClickListener(v-> eliminarMensaje(commentId,token));
        actualizar.setOnClickListener(v-> modificarMensaje(commentId, token));

        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    private void cargarComentario(int commentId, String token){
       String url = "http://172.20.10.2:8000/api/v1/comments/"+commentId +"?type=idComent";
       Request request = new Request.Builder()
               .url(url)
               .addHeader("Authorization", "Bearer " + token)
               .build();
       okHttpClient.newCall(request).enqueue(new Callback() {
           @Override
           public void onFailure(@NonNull Call call, @NonNull IOException e) {
               Toast.makeText(getApplicationContext(),"error en carga comentario antiguo.",Toast.LENGTH_LONG).show();;
           }

           @Override
           public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
               String responseData = response.body().string();
               if (response.isSuccessful()) {
                   Gson gson = new Gson();
                   Comentario comentario = gson.fromJson(responseData, ComentariosResponse.class).getData().get(0);
                   comentarioUnico = comentario.getCommentText();
                   runOnUiThread(()->{
                       textView.setText(comentarioUnico);
                   });
               } else {
                   runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error al cargar el comentario.", Toast.LENGTH_LONG).show());
               }
           }
       });
    }
    private void eliminarMensaje(int commentId, String token){
        String url = "http://172.20.10.2:8000/api/v1/comments/"+commentId;
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
                } else {
                    runOnUiThread(() ->Toast.makeText(getApplicationContext(),"error al ELIMINAR comentario .",Toast.LENGTH_LONG).show());
                }
            }
        });
    }
    private void modificarMensaje(int commentId, String token){
        String  nuevoComentario = etnuevoComentario.getText().toString();
        if(nuevoComentario == null){
            Toast.makeText(getApplicationContext(),"El comentario está vacío.",Toast.LENGTH_LONG).show();
            return;
        }
        String url = "http://172.20.10.2:8000/api/v1/comments/"+commentId;

        JSONObject comentarioJson = new JSONObject();
        try{
            comentarioJson.put("comment_text",nuevoComentario);
        } catch (JSONException e) {
            throw new RuntimeException(e);
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
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "El comentario ha sido modificado.", Toast.LENGTH_LONG).show());
                    finish();
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error al actualizar el comentario.", Toast.LENGTH_LONG).show());
                }
            }

        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
            finish();
            Intent intent = new Intent(getApplicationContext(), ComentariosActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}