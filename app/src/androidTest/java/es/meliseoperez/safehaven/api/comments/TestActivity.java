package es.meliseoperez.safehaven.api.comments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.meliseoperez.safehaven.R;

public class TestActivity extends AppCompatActivity {

    private List<Comentario> comentarios;
    private ComentariosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        // Inicializa la lista de comentarios
        comentarios = new ArrayList<>();
        comentarios.add(new Comentario("Comentario 1", "URL 1"));
        comentarios.add(new Comentario("Comentario 2", "URL 2"));

        // Inicializa el RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        assert recyclerView != null;

        // Crea el adaptador y asigna la lista de comentarios
        adapter = new ComentariosAdapter(comentarios);

        // Asigna el adaptador al RecyclerView
        recyclerView.setAdapter(adapter);

        // Configura el LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}