package com.personal.firebaseapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SMSActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ListView listViewHistorial;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> historialList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsactivity);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("received_sms");

        listViewHistorial = findViewById(R.id.listViewHistorial);
        historialList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historialList);
        listViewHistorial.setAdapter(adapter);

        cargarHistorialDesdeFirebase();
        solicitarPermisos();

        // Obtener datos del Intent
        String smsSender = getIntent().getStringExtra("sms_sender");
        String smsLink = getIntent().getStringExtra("sms_link");
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (smsSender != null && !smsSender.isEmpty() && smsLink != null && !smsLink.isEmpty()) {
            mostrarSmsEnPantalla(smsSender, smsLink);
            saveMessageToHistory(smsSender, smsLink, timestamp);
        }

        Button btnRegresar = findViewById(R.id.btnregresa);
        btnRegresar.setOnClickListener(v -> {
            Intent intent = new Intent(SMSActivity.this, ListaUsuariosActivity.class);
            startActivity(intent);
            finish();
        });

        listViewHistorial.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = historialList.get(position);
            if (selectedItem.contains("Ubicación: ")) {
                int index = selectedItem.indexOf("Ubicación: ") + 10;
                if (index < selectedItem.length()) {
                    String link = selectedItem.substring(index);
                    openGoogleMaps(link);
                }
            }
        });
    }

    private void solicitarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void mostrarSmsEnPantalla(String sender, String link) {
        TextView smsSenderView = findViewById(R.id.sms_sender);
        TextView linkView = findViewById(R.id.sms_link);

        smsSenderView.setText("Remitente: " + sender);
        linkView.setText("Ubicación: " + link);
        linkView.setTextColor(Color.BLUE);
        linkView.setPaintFlags(linkView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        linkView.setOnClickListener(v -> openGoogleMaps(link));
    }

    private void openGoogleMaps(String link) {
        Intent intent = new Intent(SMSActivity.this, ActivityMap.class);
        intent.putExtra("sms_link", link);
        startActivity(intent);
    }

    private void saveMessageToHistory(String sender, String link, String timestamp) {
        // Se utiliza el constructor de tres parámetros; si se requiere almacenar el mensaje completo, se puede ajustar.
        SmsMessageData smsData = new SmsMessageData(sender, link, timestamp);
        databaseReference.push().setValue(smsData)
                .addOnSuccessListener(aVoid -> {
                    historialList.add("Fecha y Hora: " + timestamp + "\nRemitente: " + sender + "\nUbicación: " + link);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(SMSActivity.this, "Mensaje guardado correctamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SMSActivity.this, "Error al guardar mensaje", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarHistorialDesdeFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historialList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SmsMessageData sms = dataSnapshot.getValue(SmsMessageData.class);
                    if (sms != null) {
                        historialList.add("Fecha y Hora: " + sms.timestamp + "\nRemitente: " + sms.sender + "\nUbicación: " + sms.link);
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
}

