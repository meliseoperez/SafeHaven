package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import es.meliseoperez.MainActivity;
import es.meliseoperez.safehaven.R;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ComentariosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;
    private ComentariosAdapter comentariosAdapter;

    private Button buttonAddComment;
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_comentarios);

        recyclerView = findViewById(R.id.recyclerViewComentarios);
        sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);

        //Configurar RecyclerView con un adaptador inicial vacío o con algún tipo de indicador de carga
        comentariosAdapter = new ComentariosAdapter(new ArrayList<>());
        recyclerView.setAdapter(comentariosAdapter);

        // Establecer un LayoutManager para el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cargarComentarios();
        // Activar el botón de retroceso en la barra de acciones
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }



    private void cargarComentarios() {
        String token = sharedPreferences.getString("token","");
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://10.0.2.2:8000/api/v1/comments")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        // Enviar la solicitud de forma asincrónica
        client.newCall(request).enqueue(new okhttp3.Callback(){
            // Este es el método onResponse donde procesarás la respuesta.
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 500){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Mostrar mensaje de que debe ser usuario premium
                            Toast.makeText(getApplicationContext(),"El usario no tiene acceso. Registrese como premiun si todavía no lo ha hecho.",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(ComentariosActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else if (response.code() == 200) {
                    String responseData= response.body().string();

                    //Aquí proceso el JSON y actualizo el RecyclerView
                    //Esto se debe hacer en el hilo de la interfaz de usuario
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).show();
                            List<Comentario> comentarioList = parseComentarios(responseData);
                            comentariosAdapter.setComentariosList(comentarioList);
                            comentariosAdapter.notifyDataSetChanged();
                        }
                    });

                }
            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Manejar el fallo, por ejemplo, mostrar un mensaje al usuario
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "ERROR EN SERVIDOR.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ComentariosActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    }
                });

            }
        });
    }

    //Método para parsear la respuesta JSON y convertirla en una lista de Comentarios

    private List<Comentario> parseComentarios(String responseData) {
        Gson gson = new Gson();
        Type comentarioListType = new TypeToken<ComentariosResponse>() {}.getType();
        ComentariosResponse comentariosResponse = gson.fromJson(responseData, comentarioListType);

        List<Comentario> comentarioList = new ArrayList<>();
        if (comentariosResponse != null && comentariosResponse.getData() != null) {
            comentarioList = comentariosResponse.getData();
        }

        return comentarioList;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.comentarios_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Finaliza la actividad actual y vuelve a la anterior en la pila de actividades
            finish();
            return true;
        }
        if (id == R.id.action_add_comment) {
            Intent intent = new Intent(ComentariosActivity.this, AddCommentActivity.class);
            startActivity(intent);
            Log.d("COMENTARIOS: ","LLAMADA A AGREGAR COMENTRAIOS");
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        cargarComentarios();
    }

}
