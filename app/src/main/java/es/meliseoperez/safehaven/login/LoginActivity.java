package es.meliseoperez.safehaven.login;

import android.content.Context;
import android.content.Intent;
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

import es.meliseoperez.MainActivity;
import es.meliseoperez.safehaven.R;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        loginButton = findViewById(R.id.btnLogin);
        registerButton= findViewById(R.id.btnRegister);

        // Obtén los extras del intent, si existen
        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");

        //Establezco el email y password recibidos por los EditText
        if(email != null && password != null){
            emailEditText.setText(email);
            passwordEditText.setText(password);
        }

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String email = String.valueOf(emailEditText.getText());
                String password = String.valueOf(passwordEditText.getText());

                if(email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Email o password vacios.", Toast.LENGTH_LONG).show();

                    Log.d(TAG, "Email or password is empty.");
                    return;
                }
                loginUser(email,password);
            }
        });

        registerButton.setOnClickListener(view ->
        {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginUser(String email, String password) {
        String url="http://172.20.10.2:8000/api/login";

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("email", email);
            jsonObject.put("password", password);
        }catch(JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonObject,
                response ->{
                Log.d(TAG, "Login realizado con éxito: " + response);
                //Extracción del token del objeto JSON
                    try {
                        String token = response.getString("token");
                        String idUsario = response.getString("idUsuario");
                        String tipoUsuario = response.getString("typeUser");
                        //Almacenamiento del token utilizo SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token",token);
                        editor.putString("idUsuario", idUsario);
                        editor.putString("tipoUsuario", tipoUsuario);
                        editor.apply();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            },
                error ->{
                        Log.d(TAG, "Login failed: " + error.getMessage());
                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                        startActivity(intent);
                    }

        );
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);

    }
}