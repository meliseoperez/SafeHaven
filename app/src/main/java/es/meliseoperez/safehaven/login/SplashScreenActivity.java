package es.meliseoperez.safehaven.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import es.meliseoperez.MainActivity;

/**
 * La SplashScreenActivity actúa como punto de entrada a la aplicación.
 * Esta actividad verifica si el usuario ya posee un token de sesión almacenado
 * y, dependiendo de su existencia y validez, redirige al usuario a la actividad principal
 * de la aplicación o a la pantalla de inicio de sesión.
 */
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        // Acceso a SharedPreferences para verificar la existencia de un token de sesión
        SharedPreferences sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token",null);

        Intent intent;
        if(token != null && !token.isEmpty()){
            // Si el token existe y es válido, navega directamente a la pantalla principal.
            intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        }else{
            // Si no hay token, redirige al usuario a la pantalla de inicio de sesión para que inicie sesión o se registre.
            intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
        }

        // Inicia la actividad correspondiente
        startActivity(intent);
        // Finaliza la SplashScreenActivity para que el usuario no pueda volver a ella usando el botón de retorno.
        finish();
    }
}
