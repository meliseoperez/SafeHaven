package es.meliseoperez.safehaven.api.comments;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

        textDescripcion.setText(descripcion);
        textIndicaciones.setText(indicaciones);


        //Inicializar los botones y establecer los onClickListener
        buttonCrearComentario = findViewById(R.id.crearComentario);
        buttonVerComentarios = findViewById(R.id.verComentario);

        buttonVerComentarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ComentariosActivity.class);
                intent.putExtra("ZONA_ID", zonaID);
                startActivity(intent);
            }
        });
        buttonCrearComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddCommentActivity.class);
                intent.putExtra("ZONA_ID", zonaID);
                startActivity(intent);
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
}