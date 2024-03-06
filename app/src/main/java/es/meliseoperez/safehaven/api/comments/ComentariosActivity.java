package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.meliseoperez.safehaven.R;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ComentariosActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;

    private ComentariosAdapter comentariosAdapter;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_comentarios);

        recyclerView = findViewById(R.id.recyclerViewComentarios);
        sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);

        //Configurar RecyclerView con un adaptador inicial vacío o con algún tipo de indicador de carga
        comentariosAdapter = new ComentariosAdapter(new ArrayList<>());
        recyclerView.setAdapter(comentariosAdapter);

        cargarComentarios();
    }

    private void cargarComentarios() {
        String token = sharedPreferences.getString("token","");
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://1237.0.0.1:8000/api/v1/coments")
                .addHeader("Authorization", "Bearer" + token)
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

                        }
                    });
                } else if (response.code() == 200) {
                    String responseData= response.body().string();

                    //Aquí proceso el JSON y actualizo el RecyclerView
                    //Esto se debe hacer en el hilo de la interfaz de usuario
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                Toast.makeText(getApplicationContext(),"ERROR EN SERVIDOR.",Toast.LENGTH_LONG).show();

            }
        });
    }

    //Método para parsear la respuesta JSON y convertirla en una lista de Comentarios

    private List<Comentario> parseComentarios(String responseData) {
        List<Comentario> comentarioList = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(responseData);
            JSONArray dataArray = jsonResponse.getJSONArray("data");

            for(int i=0; i < dataArray.length(); i++){
                JSONObject comentarioJason = dataArray.getJSONObject(i);
                Comentario comentario = new Comentario();
                //Llenar el objeto comentario con los datos del JSON
                comentarioList.add(comentario);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return comentarioList;
    }
}
