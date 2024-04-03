package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import es.meliseoperez.safehaven.R;

public class ZonaDetallesActivity extends AppCompatActivity {

    private Button buttonCrearComentario;
    private Button buttonVerComentarios;
    private TextView textDescripcion;
    private TextView textIndicaciones;
    private int zonaID;
    private String descripcion;
    private String indicaciones;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zona_detalles);
        //Obtener zonaID de la zona desde el Inten
        zonaID = getIntent().getIntExtra("ZONA_ID",0);
        descripcion = getIntent().getStringExtra("ZONA_DESCRIPCION");
        indicaciones = getIntent().getStringExtra("ZONA_INSTRUCCIONES");

        textDescripcion = findViewById(R.id.texVistaDescripcion);
        textIndicaciones = findViewById(R.id.texVistaIntrucciones);
        try{
            textDescripcion.setText(descripcion);
            textIndicaciones.setText(indicaciones);
        } catch (Exception e) {
           Log.e("Error en setText", e.getMessage());
        }


        //Inicializar los botones y establecer los onClickListener
        buttonCrearComentario = findViewById(R.id.crearComentario);
        buttonVerComentarios = findViewById(R.id.verComentario);

        buttonVerComentarios.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(acesoPermitido())
                {
                    Intent intent = new Intent(getApplicationContext(), ComentariosActivity.class);
                    intent.putExtra("ZONA_ID", zonaID);
                    intent.putExtra("TIPO","alert");
                    startActivity(intent);
                }else
                    Toast.makeText(getApplicationContext(), "Solo para usuarios Premium.",Toast.LENGTH_LONG).show();
            }
        });
        buttonCrearComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(acesoPermitido())
                {
                    Intent intent = new Intent(getApplicationContext(), AddCommentActivity.class);
                    intent.putExtra("ZONA_ID", zonaID);
                    startActivity(intent);
                }else
                    Toast.makeText(getApplicationContext(), "Solo para usuarios Premium.",Toast.LENGTH_LONG).show();
            }
        });
        //Habilitar la flecha de retorno en el ActionBar
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public boolean acesoPermitido(){
        boolean permitido=true;
        SharedPreferences sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String tipoUsuario=sharedPreferences.getString("tipoUsuario","basico");
        if(tipoUsuario.equals("null") || tipoUsuario.equals("basico")){
            permitido=false;
        }
        return permitido;
    }
}