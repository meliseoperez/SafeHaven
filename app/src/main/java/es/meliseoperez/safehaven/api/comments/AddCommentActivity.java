package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import es.meliseoperez.safehaven.R;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class AddCommentActivity extends AppCompatActivity {

    private EditText editTextComment;
    private Button buttonSubmit;
    String comment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        editTextComment = findViewById(R.id.editTextComment);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComment();
            }
        });
    }

    private void submitComment() {
        // Obtén el comentario del EditText
        comment = editTextComment.getText().toString().trim();
        storeApi();
    }
    private void storeApi(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        String usuario = sharedPreferences.getString("idUsuario","");

        OkHttpClient okHttpClient = new OkHttpClient();

        MediaType JSON= MediaType.parse("application/json; utf-8");
        JSONObject requestBody = new JSONObject();
        try{
            requestBody.put("alert_id","1000");
            requestBody.put("user_id", usuario);
            requestBody.put("comment_text", comment);
            requestBody.put("image_url","https://example.com/image.jpg");
        } catch (JSONException e) {
            Log.e("JSONError", "Error creating JSON request body", e);
            Toast.makeText(this, "Error al enviar el comentario", Toast.LENGTH_SHORT).show();
            return;
        }
        enviarSolicitudConToken(requestBody,token);
    }

    public void enviarSolicitudConToken(JSONObject requestBody,String token) {
        String url = "http://10.0.2.2:8000/api/v1/store";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    //Manejo respuesta exitosa
                    Log.d("Response: ", response.toString());
                    // Aquí implementarías la lógica para enviar el comentario al backend o hacer lo que sea necesario con el comentario.
                    Toast.makeText(this, "Comentario envíado: " + comment, Toast.LENGTH_SHORT).show();
                    // Opcional: Si quieres cerrar la actividad después de enviar el comentario, llama a finish();
                    finish();
                },
                error -> {
                    //Manejo los errores
                    Log.e("Response Error.", error.toString());
                }) {
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-type", "application/json");
                headers.put("Authorization","Bearer " + "47|CvSlEQmdDOxJVoJFRkzLZPG89OilRtO968pvjkuS8c54375d");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }
}

