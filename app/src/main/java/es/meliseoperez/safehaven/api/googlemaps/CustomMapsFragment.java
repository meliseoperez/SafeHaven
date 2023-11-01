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

import java.util.ArrayList;
import java.util.List;

import es.meliseoperez.safehaven.R;
import es.meliseoperez.safehaven.api.aemet.AlertInfo;
import es.meliseoperez.safehaven.database.AlertRepository;


public class CustomMapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean firsLocationUpdate=true;
    private Marker myLocationMarker;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final List<Marker> zoneMarkers = new ArrayList<>();




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        super.onViewCreated(view, saveInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if (myLocationMarker == null) {
                        myLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Mi Ubicación"));
                    } else {
                        myLocationMarker.setPosition(currentLocation);
                    }

                    if (firsLocationUpdate) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 5));
                        firsLocationUpdate=false;
                    }
                }
            }
        };

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (myLocationMarker == null) {
                            myLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Mi ubicación"));
                        } else {
                            myLocationMarker.setPosition(currentLocation);
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    }
                }
            });

            if (locationRequest == null) {
                locationRequest = com.google.android.gms.location.LocationRequest.create();
                locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(10000);
                locationRequest.setFastestInterval(5000);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }


        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public void addZonesToMap(List<Zona> zonas) {
        if (mMap == null) return;

        for (Marker marker : zoneMarkers) {
            marker.remove();
        }
        zoneMarkers.clear();

        for (Zona zona : zonas) {
            zona.dibujarZona(mMap);

        }
    }
    public List<AlertInfo> cargarZonas(AlertRepository repository){

        repository.open();
        List<AlertInfo> alerts= repository.getAllAlerts();
        repository.close();


        for(AlertInfo alert: alerts){
            //Procesar y almacenar color
            String color= Zona.extractColorFromHeadline(alert.getHeadline());
            alert.setColor(color);
            //Procesar y almacenar coordenadas
            List<LatLng> coordenadasProcesadas=Zona.parsearCoordenadas(alert.getPolygon());
            alert.setCoordenadas(coordenadasProcesadas);

        }
        return  alerts;
    }
}
