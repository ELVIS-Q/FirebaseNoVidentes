package com.personal.firebaseapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class SMSActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ListView listViewHistorial;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> historialList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsactivity);

        // Inicializar Firebase Realtime Database
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("received_sms");

        // Inicializar ListView y ArrayAdapter
        listViewHistorial = findViewById(R.id.listViewHistorial);
        historialList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historialList);
        listViewHistorial.setAdapter(adapter);

        // Cargar historial desde Firebase
        cargarHistorialDesdeFirebase();

        // Verificar permisos
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.RECEIVE_SMS,
                    android.Manifest.permission.READ_SMS}, 1);
        }

        // Obtener datos del Intent
        String smsSender = getIntent().getStringExtra("sms_sender");
        String smsCoordinates = getIntent().getStringExtra("sms_coordinates");

        // Mostrar los datos en TextViews
        TextView smsSenderView = findViewById(R.id.sms_sender);
        TextView coordinatesView = findViewById(R.id.sms_coordinates);

        if (smsSender != null) {
            smsSenderView.setText("Remitente: " + smsSender);
        }
        if (smsCoordinates != null) {
            coordinatesView.setText("Coordenadas: " + smsCoordinates);

            // Configurar el listener para abrir Google Maps
            coordinatesView.setOnClickListener(v -> openGoogleMaps(smsCoordinates));

            // Guardar las coordenadas en Firebase
            saveCoordinatesToRealtimeDatabase(smsSender, smsCoordinates);
        }

        // Configurar el botón de regreso a ListaUsuariosActivity
        Button btnRegresar = findViewById(R.id.btnregresa);
        btnRegresar.setOnClickListener(v -> {
            Intent intent = new Intent(SMSActivity.this, ListaUsuariosActivity.class);
            startActivity(intent);
            finish();
        });

        // Configurar clic en ListView para abrir ActivityMap con coordenadas
        listViewHistorial.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = historialList.get(position);
            if (selectedItem.contains("Coordenadas: ")) {
                String coordinates = selectedItem.split("Coordenadas: ")[1];

                // Iniciar ActivityMap con las coordenadas
                Intent intent = new Intent(SMSActivity.this, ActivityMap.class);
                intent.putExtra("sms_coordinates", coordinates);
                startActivity(intent);
            }
        });
    }

    // Método para abrir Google Maps con las coordenadas
    private void openGoogleMaps(String coordinates) {
        String uri = "geo:" + coordinates;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    // Método para guardar las coordenadas en Firebase Realtime Database
    private void saveCoordinatesToRealtimeDatabase(String sender, String coordinates) {
        if (sender != null && coordinates != null) {
            SmsMessageData smsData = new SmsMessageData(sender, coordinates);

            databaseReference.push().setValue(smsData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SMSActivity.this, "Coordenadas guardadas correctamente", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SMSActivity.this, "Error al guardar las coordenadas", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Método para cargar el historial de mensajes desde Firebase
    private void cargarHistorialDesdeFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historialList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String sender = dataSnapshot.child("sender").getValue(String.class);
                    String coordinates = dataSnapshot.child("coordinates").getValue(String.class);

                    if (sender != null && coordinates != null) {
                        historialList.add("Remitente: " + sender + "\nCoordenadas: " + coordinates);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al obtener historial", error.toException());
            }
        });
    }

    // Clase interna para representar los datos del SMS
    public static class SmsMessageData {
        public String sender;
        public String coordinates;

        public SmsMessageData() {
        }

        public SmsMessageData(String sender, String coordinates) {
            this.sender = sender;
            this.coordinates = coordinates;
        }
    }

    // Manejar la respuesta de los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                Toast.makeText(this, "Permisos necesarios para continuar", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
