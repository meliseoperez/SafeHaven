package es.meliseoperez.safehaven.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;
import es.meliseoperez.safehaven.R;

/**
 * La actividad RegisterActivity permite a los nuevos usuarios registrarse en la aplicación.
 * Los usuarios pueden ingresar su nombre, email, contraseña y seleccionar un tipo de usuario.
 */
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity"; // Etiqueta para el logging
    private EditText nameInput; // Campo de texto para el nombre
    private EditText emailInput; // Campo de texto para el email
    private EditText passwordInput; // Campo de texto para la contraseña
    private RadioGroup typeUserInput; // Grupo de botones radio para seleccionar el tipo de usuario
    private Button btnRegister; // Botón para enviar el formulario de registro
    private Button btnCancelar; // Botón para cancelar el registro y cerrar la actividad
    private String typeUser = ""; // Variable para almacenar el tipo de usuario seleccionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicialización de los componentes de la interfaz
        nameInput = findViewById(R.id.etName);
        emailInput = findViewById(R.id.etEmail);
        passwordInput = findViewById(R.id.etPassword);
        typeUserInput = findViewById(R.id.rgTypeUser);

        // Listener para capturar el tipo de usuario seleccionado
        typeUserInput.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = findViewById(checkedId);
            typeUser = selectedRadioButton.getText().toString().trim();
        });

        // Listener para el botón de registro
        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(view -> {
            // Recopilación de los datos ingresados por el usuario
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Envío de la solicitud de registro
            sendRegistrationRequest(name, email, password, typeUser);
        });

        // Listener para el botón de cancelar
        btnCancelar = findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(view -> {
            // Cierra la actividad actual
            finish();
        });
    }

    /**
     * Envia la solicitud de registro al servidor.
     * @param name Nombre del usuario.
     * @param email Email del usuario.
     * @param password Contraseña del usuario.
     * @param typeUser Tipo de usuario seleccionado.
     */
    private void sendRegistrationRequest(String name, String email, String password, String typeUser) {
        String url = "http://" + LoginActivity.serverIP + ":8000/api/register";

        // Creación de la solicitud con método POST
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    // Log del éxito del registro y navegación a LoginActivity
                    Log.d(TAG, "Registro exitoso: " + response);
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    // Log del fallo del registro
                    Log.d(TAG, "Registro fallido: " + error.getMessage());
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Mapeo de los datos del formulario para ser enviados
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("type_user", typeUser);
                return params;
            }
        };

        // Añadir la solicitud a la cola de Volley
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
