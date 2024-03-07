import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import es.meliseoperez.safehaven.R;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Es importante inicializar las variables fuera del método onResponse
        // para poderlas usar dentro de la clase anónima
        final EditText nameInput = findViewById(R.id.etName);
        final EditText emailInput = findViewById(R.id.etEmail);
        final EditText passwordInput = findViewById(R.id.etPassword);
        final EditText typeUserInput = findViewById(R.id.ettype_user_input);

        // Usar .trim() para eliminar espacios en blanco innecesarios
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String typeUser = typeUserInput.getText().toString().trim();

        // Crear un nuevo usuario con la información proporcionada
        User newUser = new User(name, email, password, typeUser);

        // Enviar una solicitud de registro al servidor utilizando Volley
        String url = "https://example.com/api/register";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Registro exitoso: " + response);
                        // Redirigir al usuario a la pantalla de inicio de sesión
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Registro fallido: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Obtener los valores dentro del método getParams para asegurar
                // que sean los últimos valores ingresados
                params.put("name", nameInput.getText().toString().trim());
                params.put("email", emailInput.getText().toString().trim());
                params.put("password", passwordInput.getText().toString().trim());
                params.put("type_user", typeUserInput.getText().toString().trim());
                return params;
            }
        };

        // Agregar la solicitud a la cola de solicitudes de Volley
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
