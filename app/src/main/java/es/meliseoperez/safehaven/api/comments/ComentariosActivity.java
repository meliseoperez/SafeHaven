package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import es.meliseoperez.safehaven.R;

public class ComentariosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;
    private ComentariosAdapter comentariosAdapter;
    private Integer id;
    private String tipo;
    private Button buttonAddComment;
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_comentarios);

        // Código existente para recibir el zonaID

        tipo = getIntent().getStringExtra("TIPO");
        id =getIntent().getIntExtra("ZONA_ID",0);
        recyclerView = findViewById(R.id.recyclerViewComentarios);

        sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        if (id==0) {
            id = Integer.valueOf(sharedPreferences.getString("idUsuario",""));
        }
        //Configurar RecyclerView con un adaptador inicial vacío o con algún tipo de indicador de carga
        comentariosAdapter = new ComentariosAdapter(new ArrayList<>());
        recyclerView.setAdapter(comentariosAdapter);

        // Establecer un LayoutManager para el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Activar el botón de retroceso en la barra de acciones
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Crea una instancia de ConsultaComentariosAPI y llama a cargarComentarios
        ConsulataComentariosAPI consultaComentariosAPI = new ConsulataComentariosAPI(this, comentariosAdapter);
        if(id !=0 && tipo != null){
            consultaComentariosAPI.cargaComentarios(id,tipo); // Pasa null para cargar todos los comentarios o un idAlert para comentarios específicos.
        }else{
            consultaComentariosAPI.cargaComentarios(null,tipo);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.comentarios_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idItem = item.getItemId();
        if (idItem == android.R.id.home) {
            // Finaliza la actividad actual y vuelve a la anterior en la pila de actividades
            finish();
            return true;
        }
        if (idItem == R.id.action_add_comment) {
            // Crea una instancia de ConsultaComentariosAPI y llama a cargarComentarios
            this.tipo = "user";
            ConsulataComentariosAPI consultaComentariosAPI = new ConsulataComentariosAPI(this, comentariosAdapter);
            if(id !=0 ){
                consultaComentariosAPI.cargaComentarios(id,tipo); // Pasa null para cargar todos los comentarios o un idAlert para comentarios específicos.
            }else{
                consultaComentariosAPI.cargaComentarios(null,tipo);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        ConsulataComentariosAPI consultaComentariosAPI = new ConsulataComentariosAPI(this, comentariosAdapter);
        if(id!=0 || tipo != null){
            consultaComentariosAPI.cargaComentarios(id,tipo); // Pasa null para cargar todos los comentarios o un idAlert para comentarios específicos.
        }else{
            consultaComentariosAPI.cargaComentarios(null,tipo);
        }
    }

}
