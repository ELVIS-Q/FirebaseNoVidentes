package com.personal.firebaseapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PerfilActivity extends AppCompatActivity {

    private EditText ced, nom, cel, dir;
    private Button btnReg, btnBack;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Referencias a vistas
        ced = findViewById(R.id.cedula);
        nom = findViewById(R.id.nombres);
        cel = findViewById(R.id.celular);
        dir = findViewById(R.id.direccion);
        btnReg = findViewById(R.id.btnregistrar);
        btnBack = findViewById(R.id.btnRegresar); // Asegúrate que el botón exista en el XML

        // Botón Registrar Familiar
        btnReg.setOnClickListener(v -> registrarFamiliar());

        // Botón Regresar
        btnBack.setOnClickListener(v -> finish());
    }

    private void registrarFamiliar() {
        // Obtener valores
        String c = ced.getText().toString().trim();
        String n = nom.getText().toString().trim();
        String ce = cel.getText().toString().trim();
        String d = dir.getText().toString().trim();

        // Validación
        if (c.isEmpty() || n.isEmpty() || ce.isEmpty() || d.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener UID del usuario autenticado
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el mapa de datos a guardar
        Map<String, Object> data = new HashMap<>();
        data.put("cedula", c);
        data.put("nombres", n);
        data.put("celular", ce);
        data.put("direccion", d);
        data.put("uid", uid);

        // Guardar en la colección "familiares"
        firestore.collection("familiares")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Familiar guardado exitosamente", Toast.LENGTH_SHORT).show();
                    finish(); // Vuelve a la pantalla anterior (ListaUsuarios)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
