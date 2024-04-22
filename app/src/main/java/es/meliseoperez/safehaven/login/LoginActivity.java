package es.meliseoperez.safehaven.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import es.meliseoperez.MainActivity;
import es.meliseoperez.safehaven.ConfigUtils;
import es.meliseoperez.safehaven.R;

/**
 * LoginActivity gestiona la pantalla de inicio de sesión de la aplicación.
 * Los usuarios pueden ingresar sus credenciales de email y contraseña para acceder.
 * Además, proporciona una opción para registrarse si aún no tienen una cuenta.
 */
public class LoginActivity extends AppCompatActivity {

    // Definición de componentes de la interfaz de usuario
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;

    private static final String TAG = "LoginActivity"; // Etiqueta para logs
    public static String serverIP; // IP del servidor para las solicitudes de red

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Obtener la dirección IP del servidor desde la configuración
        serverIP = ConfigUtils.getServerIp(getApplicationContext());

        // Inicializar componentes de la interfaz de usuario
        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        loginButton = findViewById(R.id.btnLogin);
        registerButton = findViewById(R.id.btnRegister);

        // Intentar prellenar los campos si se pasaron como extras
        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");
        if(email != null && password != null) {
            emailEditText.setText(email);
            passwordEditText.setText(password);
        }

        // Manejar el evento click en el botón de inicio de sesión
        loginButton.setOnClickListener(view -> {
            String emailValue = emailEditText.getText().toString();
            String passwordValue = passwordEditText.getText().toString();

            // Verificar si los campos están vacíos
            if(emailValue.isEmpty() || passwordValue.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Email o password vacíos.", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Email or password is empty.");
                return;
            }
            // Llamada al método de inicio de sesión
            loginUser(emailValue, passwordValue);
        });

        // Navegar a la actividad de registro al hacer clic en el botón de registro
        registerButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Realiza la solicitud de inicio de sesión al servidor y maneja la respuesta.
     * @param email El email del usuario.
     * @param password La contraseña del usuario.
     */
    private void loginUser(String email, String password) {
        String url = "http://" + serverIP + ":8000/api/login";

        // Crear el cuerpo de la solicitud
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Crear y enviar la solicitud de inicio de sesión
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    Log.d(TAG, "Login realizado con éxito: " + response);
                    try {
                        // Extraer el token y otros datos del usuario de la respuesta
                        String token = response.getString("token");
                        String idUsuario = response.getString("idUsuario");
                        String tipoUsuario = response.getString("typeUser");
                        // Almacenar los datos del usuario en SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token", token);
                        editor.putString("idUsuario", idUsuario);
                        editor.putString("tipoUsuario", tipoUsuario);
                        editor.apply();
                        // Navegar a MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    Log.d(TAG, "Falló el inicio de sesión: " + error.getMessage());
                    Toast.makeText(LoginActivity.this, "Falló el inicio de sesión", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
        );
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
