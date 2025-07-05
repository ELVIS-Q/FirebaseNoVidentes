package com.personal.firebaseapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ActivityMap extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private double latitude = 0.0, longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        String link = getIntent().getStringExtra("sms_link");

        if (link != null && (link.contains("maps.google.com") || link.contains("www.google.com/maps"))) {
            try {
                Uri uri = Uri.parse(link);
                String latLngString = null;

                // Verifica si el enlace tiene ?q=lat,lng
                if (uri.getQueryParameter("q") != null) {
                    latLngString = uri.getQueryParameter("q");
                } else if (uri.getPath() != null && uri.getPath().contains("/place/")) {
                    // Intenta extraer de /place/lat,lng
                    String[] segments = uri.getPath().split("/");
                    for (String segment : segments) {
                        if (segment.matches("-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?")) {
                            latLngString = segment;
                            break;
                        }
                    }
                }

                if (latLngString != null && latLngString.contains(",")) {
                    String[] parts = latLngString.split(",");
                    latitude = Double.parseDouble(parts[0].trim());
                    longitude = Double.parseDouble(parts[1].trim());
                } else {
                    Toast.makeText(this, "No se encontraron coordenadas válidas", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error al procesar el enlace", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Enlace inválido o faltante", Toast.LENGTH_SHORT).show();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (latitude != 0.0 && longitude != 0.0) {
            LatLng location = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(location).title("Ubicación recibida"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        } else {
            Toast.makeText(this, "No hay coordenadas disponibles", Toast.LENGTH_LONG).show();
        }

        mMap.setOnMapClickListener(latLng -> {
            double lat = latLng.latitude;
            double lng = latLng.longitude;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Tocado: " + lat + ", " + lng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            Toast.makeText(this, "Tocado: " + lat + ", " + lng, Toast.LENGTH_SHORT).show();
        });
    }
}
