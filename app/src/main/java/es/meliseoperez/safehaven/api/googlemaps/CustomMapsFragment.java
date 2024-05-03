package es.meliseoperez.safehaven.api.googlemaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.meliseoperez.safehaven.R;
import es.meliseoperez.safehaven.api.aemet.AlertInfo;
import es.meliseoperez.safehaven.database.AlertRepository;

/**
 * Fragment que integra funcionalidades de Google Maps, mostrando la ubicación actual del usuario
 * y permitiendo la visualización de zonas específicas con marcadores o polígonos en el mapa.
 */
public class CustomMapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean firstLocationUpdate = true;
    private Marker myLocationMarker;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final List<Marker> zoneMarkers = new ArrayList<>();
    private HashMap<Polygon, Zona> zonaPoligonoMap = new HashMap<>();

    // Interfaz para notificar cuando el mapa esté listo.
    public interface MapReadyCallback {
        void onMapReady();
    }
    private MapReadyCallback mapReadyCallback;

    public void setMapReadyCallback(MapReadyCallback callback) {
        this.mapReadyCallback = callback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla el layout del fragmento de mapa.
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa el cliente de ubicación y define cómo manejar las actualizaciones de ubicación.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                // Actualiza la ubicación del usuario en el mapa cada vez que se recibe una nueva ubicación.
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if (myLocationMarker == null) {
                        // Crea un marcador para la ubicación actual si aún no existe.
                        myLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Mi Ubicación"));
                    } else {
                        // Actualiza la posición del marcador existente para la ubicación actual.
                        myLocationMarker.setPosition(currentLocation);
                    }

                    // Mueve la cámara a la primera actualización de ubicación.
                    if (firstLocationUpdate) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 5));
                        firstLocationUpdate = false;
                    }
                }
            }
        };

        // Obtiene el fragmento de mapa y lo prepara de manera asíncrona.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Verifica y solicita los permisos necesarios para acceder a la ubicación del usuario.
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            // Obtiene la última ubicación conocida del usuario y actualiza el mapa en consecuencia.
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if (myLocationMarker == null) {
                        myLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Mi ubicación"));
                    } else {
                        myLocationMarker.setPosition(currentLocation);
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20));
                }
            });

            // Configura y solicita actualizaciones de ubicación periódicas.
            if (locationRequest == null) {
                locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(10000); // Intervalo de actualización.
                locationRequest.setFastestInterval(5000); // Intervalo más rápido entre actualizaciones.
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        }

        // Notifica a través del callback que el mapa está listo.
        if (mapReadyCallback != null) {
            mapReadyCallback.onMapReady();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reanuda la solicitud de actualizaciones de ubicación cuando el fragmento está en primer plano.
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pausa las actualizaciones de ubicación para conservar recursos cuando el fragmento no está visible.
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Añade zonas en el mapa utilizando marcadores.
     * Este método es llamado con una lista de zonas que deben ser representadas en el mapa.
     *
     * @param zonas Lista de zonas a añadir en el mapa.
     */
    public void addZonesToMap(List<Zona> zonas) {
        if (mMap == null) return;
        // Asegura que la adición de zonas se ejecute en el hilo principal.
        getActivity().runOnUiThread(() -> {
            for (Marker marker : zoneMarkers) {
                marker.remove(); // Elimina marcadores antiguos antes de añadir los nuevos.
            }
            zoneMarkers.clear(); // Limpia la lista de marcadores para reutilización.

            for (Zona zona : zonas) {
                // Suponiendo que Zona tiene un método para dibujarse a sí misma en el mapa,
                // esta línea llamaría a ese método pasando el mapa y el contexto.
                zona.dibujarZona(mMap, getContext());
            }
        });
    }

    /**
     * Carga las zonas desde un repositorio y las prepara para ser añadidas en el mapa.
     * Este método podría ser utilizado para recuperar información de zonas de un almacenamiento local o remoto.
     *
     * @param repository Repositorio de donde se obtienen las alertas (zonas).
     * @return Lista de AlertInfo con las zonas cargadas y procesadas.
     */
    public List<AlertInfo> cargarZonas(AlertRepository repository){
        repository.open();
        List<AlertInfo> alerts = repository.getAllAlerts();

        for(AlertInfo alert : alerts){
            // Procesa y almacena el color extraído del título de la alerta.
            String color = Zona.extractColorFromHeadline(alert.getHeadline());
            alert.setColor(color);

            // Procesa y almacena las coordenadas.
            List<LatLng> coordenadasProcesadas = Zona.parsearCoordenadas(alert.getPolygon());
            alert.setCoordenadas(coordenadasProcesadas);
        }
        return alerts;
    }
}
