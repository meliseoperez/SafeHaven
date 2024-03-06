package es.meliseoperez.safehaven.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import es.meliseoperez.MainActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle saveInstanceState) {

        super.onCreate(saveInstanceState);
        //Verifico si existe un token y es váslido
        SharedPreferences sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token",null);

        Intent intent;
        if(token != null && !token.isEmpty()){
            //Si el token existe y es válido, navega a la pantalla principal
            intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        }else{
            //Si no hay token, dirige al usuario a la pantalla de inicio de sesión
            intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();//Finalizo la actividad actual para que el usuario no pueda volver a ella con boton retorno.

    }

}
