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


public class CustomMapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker myLocationMarker;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<Marker> zoneMarkers = new ArrayList<>();


    Zona zona1=new Zona("28.49,-16.43 28.43,-16.39 28.39,-16.44 28.35,-16.48 28.31,-16.5 28.28,-16.54 28.25,-16.55 28.2,-16.64 28.21,-16.68 28.27,-16.7 28.31,-16.79 28.27,-16.85 28.31,-16.88 28.34,-16.92 28.36,-16.92 28.37,-16.86 28.39,-16.83 28.38,-16.81 28.37,-16.75 28.38,-16.7 28.4,-16.67 28.39,-16.6 28.42,-16.55 28.42,-16.52 28.44,-16.47 28.49,-16.43");
    Zona zona2=new Zona("38.17,-4.17 38.21,-4.2 38.25,-4.22 38.3,-4.21 38.35,-4.27 38.39,-4.28 38.4,-4.26 38.38,-4.16 38.38,-4.07 38.37,-3.96 38.38,-3.85 38.42,-3.81 38.42,-3.73 38.39,-3.62 38.41,-3.58 38.45,-3.58 38.45,-3.54 38.41,-3.53 38.4,-3.47 38.41,-3.42 38.44,-3.38 38.48,-3.37 38.48,-3.3 38.46,-3.28 38.45,-3.18 38.44,-3.13 38.48,-3.06 38.42,-3.01 38.45,-2.99 38.47,-2.96 38.47,-2.9 38.45,-2.88 38.37,-2.91 38.35,-2.94 38.31,-2.94 38.25,-3.0 38.26,-3.03 38.23,-3.07 38.21,-3.05 38.18,-3.1 38.15,-3.18 38.13,-3.2 38.15,-3.3 38.13,-3.34 38.17,-3.37 38.12,-3.4 38.13,-3.44 38.09,-3.48 38.09,-3.56 38.04,-3.59 38.02,-3.63 38.01,-3.68 38.04,-3.68 38.06,-3.73 38.04,-3.76 38.04,-3.9 37.98,-3.9 37.95,-3.91 37.97,-3.95 38.0,-3.96 37.98,-4.04 38.0,-4.06 38.02,-4.13 38.07,-4.1 38.12,-4.08 38.14,-4.11 38.17,-4.17");

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
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 5));
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
            List<Zona> zonas = new ArrayList<>();
            zonas.add(zona1);
            zonas.add(zona2);

            addZonesToMap(zonas);
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

    private void addZonesToMap(List<Zona> zonas) {
        if (mMap == null) return;

        for (Marker marker : zoneMarkers) {
            marker.remove();
        }
        zoneMarkers.clear();

        for (Zona zona : zonas) {
            zona.dibujarZona(mMap);
            LatLng zoneCenter = zona.getCentro();
            Marker marker = mMap.addMarker(new MarkerOptions().position(zoneCenter).title("Zona"));
            zoneMarkers.add(marker);
        }
    }
}
