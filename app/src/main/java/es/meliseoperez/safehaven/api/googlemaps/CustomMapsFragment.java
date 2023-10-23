package es.meliseoperez.safehaven.api.googlemaps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import es.meliseoperez.safehaven.R;

public class CustomMapsFragment extends Fragment implements OnMapReadyCallback {

    //Declaración de variables.
    //mMap:Representa el objeto que permite iteractuar con el mapa en pantalla.
    private GoogleMap mMap;
    //locationRequesest: Configura cómo se solicitan las actualizaciones de ubicación (frecuencia, precisión).
    private LocationRequest locationRequest;
    //locationCallback: Es llamado cuando se recibe una actualización de ubicación.
    private LocationCallback locationCallback;
    //myLocationMarker:Marcador que muestra la posición actual en el mapa.
    private Marker myLocationMarker;
    //fuseLocationProviderClient:; Cliente que proporciona servicios de ubicación.
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle saveInstanceState){
        //Método llamdo al inicio de la creación de la vista fragmento
        //Infla y retorna el diseño asociado con este fragmeto
        return inflater.inflate(R.layout.fragment_maps, container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view,@Nullable Bundle saveInstanceState){
        super.onViewCreated(view,saveInstanceState);
        //Inicialización del cliente de ubiciación
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(requireContext());
        //Configuración de callback de ubicación
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if(locationResult==null){
                    return;
                }
                for(Location location:locationResult.getLocations()){
                    LatLng currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
                    //Si no se ha creado el marcador, se crea. Si ya existe, se actualiza su posición
                    if(myLocationMarker==null){
                        myLocationMarker= mMap.addMarker(new MarkerOptions().position(currentLocation).title("Mi Ubicaicón"));

                    }else{
                        myLocationMarker.setPosition(currentLocation);
                    }
                    //Centra el mapa en la ubicaicón actual
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
                }
            }
        };

        //Vinculación del fragmento del mapa con este fragmento personalizado
        SupportMapFragment mapFragment=(SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if(mapFragment!=null){
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //Método que se llama cuando el mapa está listo para ser utilizado
        mMap=googleMap;
        //Verifica si la aplicación tiene el permiso para acceder a la ubicación del dispositivo
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);//Muestra el botón de ubicación en el mapa.
            mMap.getUiSettings().setZoomControlsEnabled(true);//Hablilita controles de zoom en el mapa

            //Solicita la útlima ubicación conocida
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        LatLng currentLocation=new LatLng(location.getLatitude(),location.getLongitude());// Si no se ha creado el marcador, se crea. Si ya existe, se actualiza su posición.
                        if (myLocationMarker == null) {
                            myLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Mi ubicación"));
                        } else {
                            myLocationMarker.setPosition(currentLocation);
                        }

                        // Centra el mapa en la ubicación actual.
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    }

                }
            });
            // Configuración de cómo se solicitarán las actualizaciones de ubicación.
            if (locationRequest == null) {
                locationRequest = com.google.android.gms.location.LocationRequest.create();
                locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(10000); // Intervalo de actualización: 10 segundos.
                locationRequest.setFastestInterval(5000); // Intervalo de actualización más rápido: 5 segundos.

                // Solicita actualizaciones de ubicación con la configuración definida.
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        }

    }
    @Override
    public void onResume(){
        super.onResume();
        //Al reanudar el fragmento, vuelve a solicitar actualizaciones de ubicación
        if(ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null);
        }
    }
    @Override
    public void onPause(){
        super.onPause();
        //Detiene las actualizacioens de ubicació cuando el fragmento se pausa.
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}

