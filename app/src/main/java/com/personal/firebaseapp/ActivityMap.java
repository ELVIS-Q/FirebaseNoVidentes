package com.personal.firebaseapp;

import android.content.pm.PackageManager;

import android.os.Bundle;

import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MapStyleOptions;

public class ActivityMap extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Button btnZoomIn, btnZoomOut, btnChangeStyle, btnCompass;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Vincular los botones
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        btnChangeStyle = findViewById(R.id.btnChangeStyle);
        btnCompass = findViewById(R.id.btnCompass);

        // Obtener el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Obtener las coordenadas desde el Intent
        String coordinates = getIntent().getStringExtra("sms_coordinates");
        if (coordinates != null && coordinates.contains(",")) {
            String[] parts = coordinates.split(",");
            try {
                latitude = Double.parseDouble(parts[0].trim());
                longitude = Double.parseDouble(parts[1].trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No se recibieron coordenadas", Toast.LENGTH_SHORT).show();
        }

        // Configurar los botones
        btnZoomIn.setOnClickListener(v -> zoomIn());
        btnZoomOut.setOnClickListener(v -> zoomOut());
        btnChangeStyle.setOnClickListener(v -> changeMapStyle());
        btnCompass.setOnClickListener(v -> toggleCompass());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Verificar si las coordenadas son válidas
        if (latitude != 0.0 && longitude != 0.0) {
            LatLng location = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(location).title("Ubicación de la emergencia"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15)); // Zoom más cercano
        } else {
            Toast.makeText(this, "No hay coordenadas disponibles", Toast.LENGTH_LONG).show();
        }

        // Configurar opciones del mapa
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true); // Activar localización
    }

    // Métodos para zoom
    private void zoomIn() {
        float zoomLevel = mMap.getCameraPosition().zoom + 1;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
    }

    private void zoomOut() {
        float zoomLevel = mMap.getCameraPosition().zoom - 1;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
    }

    // Cambiar estilo del mapa
    private void changeMapStyle() {
        try {
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success) {
                Toast.makeText(this, "No se pudo cargar el estilo", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al aplicar el estilo", Toast.LENGTH_SHORT).show();
        }
    }

    // Activar/desactivar brújula
    private void toggleCompass() {
        mMap.getUiSettings().setCompassEnabled(!mMap.getUiSettings().isCompassEnabled());
    }
}
