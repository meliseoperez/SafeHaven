package es.meliseoperez.safehaven.pruebabbdd;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import es.meliseoperez.safehaven.R;
import es.meliseoperez.safehaven.api.aemet.AlertInfo;
import es.meliseoperez.safehaven.database.AlertRepository;

public class DisplayDatabaseActivity extends AppCompatActivity {
    ListView listView;
    ArrayAdapter<String> adapter;
    public static List<String> polygonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_database);

        listView =findViewById(R.id.listView);
        AlertRepository repository=new AlertRepository(this);
        repository.open();
        List<AlertInfo> alerts= repository.getAllAlerts();
        repository.close();

        polygonList=new ArrayList<>();
        for(AlertInfo alert: alerts){
            polygonList.add(alert.polygon);
        }

        adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, polygonList);
        listView.setAdapter(adapter);
    }
}