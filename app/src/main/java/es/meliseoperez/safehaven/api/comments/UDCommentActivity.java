package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.List;

import es.meliseoperez.safehaven.R;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UDCommentActivity extends AppCompatActivity {

    private TextView textView;
    private String comentarioUnico;
    private  SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udcomment);
        sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        textView = findViewById(R.id.idComent);
        String iD= String.valueOf(getIntent().getIntExtra("comentarioId",0));
        //textView.setText(iD);
        cargaUnComentario(Integer.valueOf(iD));

    }
    public void cargaUnComentario(Integer idComentario) {
        String token = sharedPreferences.getString("token", "");
        OkHttpClient client = new OkHttpClient();
        // Asume que este es el URL para obtener un solo comentario. Ajusta según sea necesario.
        String url =  "http://10.0.2.2:8000/api/v1/comments/" + idComentario + "?type=idComent";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                if (response.isSuccessful()) {
                    // Aquí asumimos que la respuesta es un solo comentario y lo almacenamos
                    Gson gson = new Gson();
                    Comentario comentario = gson.fromJson(responseData, ResponseWrapper.class).getData().get(0);
                    comentarioUnico = comentario.getCommentText();
                    // Aquí podrías actualizar la interfaz de usuario para mostrar el comentario o hacer algo con él
                    runOnUiThread(() -> {
                        // Por ejemplo, actualizar un TextView con el comentario
                         textView.setText(comentarioUnico);
                    });
                } else {
                    showErrorToast("Error al cargar el comentario.");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showErrorToast("Error de conexión con el servidor.");
            }

            // runOnUiThread y showErrorToast métodos como antes
        });
    }
    private void showErrorToast(String message) {
        // Utiliza el contexto para mostrar el Toast. Asegúrate de ejecutarlo en el hilo principal.
        ((AppCompatActivity) this).runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }
    public class ResponseWrapper {
        @SerializedName("data")
        private List<Comentario> data;

        // Getter para la lista de comentarios
        public List<Comentario> getData() {
            return data;
        }

        // Setter para la lista de comentarios (opcional)
        public void setData(List<Comentario> data) {
            this.data = data;
        }
    }

}
