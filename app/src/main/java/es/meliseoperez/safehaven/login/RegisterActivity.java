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

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private RadioGroup typeUserInput;
    private Button btnRegister;
    private Button btnCancelar;
    private String typeUser = ""; // Variable de instancia para almacenar el tipo de usuario seleccionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameInput = findViewById(R.id.etName);
        emailInput = findViewById(R.id.etEmail);
        passwordInput = findViewById(R.id.etPassword);
        //Comprueba identificación layout en remoto.
        typeUserInput = findViewById(R.id.rgTypeUser);

        // Escucha el cambio en la selección de RadioGroup
        typeUserInput.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = findViewById(checkedId);
            typeUser = selectedRadioButton.getText().toString().trim();
        });

        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(view -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            sendRegistrationRequest(name, email, password, typeUser); // Método separado para enviar la solicitud
        });
        btnCancelar = findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(view -> {
            finish();
        });
    }



    private void sendRegistrationRequest(String name, String email, String password, String typeUser) {
        String url = "http://10.0.2.2:8000/api/register";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response ->
                { Log.d(TAG, "Registro exitoso: " + response);
                    //Creo un Intent para iniciar LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);

                    //Pasar el email y password como extras
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    //Inicio LoginActivity
                    startActivity(intent);
                    //Finalizo la actividad actual
                    finish();

                },
                error -> Log.d(TAG, "Registro fallido: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("type_user", typeUser);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}