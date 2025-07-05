package com.personal.firebaseapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText et1, et2;
    private Button btningresa, btnRegister;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ImageView imageView;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar FirebaseAuth y DatabaseReference
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios");

        // Vincular vistas
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        btningresa = findViewById(R.id.btningresa);
        btnRegister = findViewById(R.id.btnregistraruser);
        imageView = findViewById(R.id.imageView2);

        // Configurar listeners
        btningresa.setOnClickListener(v -> ingresa());
        btnRegister.setOnClickListener(v -> irARegistro());

        // Cambiar imagen programáticamente
        imageView.setImageResource(R.drawable.fer);

        // Solicitar permisos en tiempo de ejecución
        solicitarPermisos();

        // Ya no abrimos automáticamente otra activity aquí
        // verificarUsuarioAutenticado();  // <- Esta línea fue comentada
    }

    private void solicitarPermisos() {
        List<String> permisosARequerir = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            permisosARequerir.add(Manifest.permission.RECEIVE_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permisosARequerir.add(Manifest.permission.READ_SMS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permisosARequerir.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!permisosARequerir.isEmpty()) {
            ActivityCompat.requestPermissions(this, permisosARequerir.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.e("PERMISOS", "Permiso denegado: " + permissions[i]);
                }
            }
        }
    }

    private void verificarUsuarioAutenticado() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("AUTH", "Usuario ya autenticado: " + currentUser.getEmail());
            abrirListaUsuarios();
        }
    }

    public void ingresa() {
        String email = et1.getText().toString().trim();
        String password = et2.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Por favor, ingresa ambos campos", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            Log.d("AUTH", "Usuario autenticado: " + user.getEmail());
                            verificarUsuarioEnBaseDeDatos(user.getUid());
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Log.e("AUTH", "Error en login: " + error);
                        Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void verificarUsuarioEnBaseDeDatos(String userId) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("DATABASE", "Usuario encontrado en Firebase Realtime Database");
                    abrirListaUsuarios();
                } else {
                    Log.e("DATABASE", "El usuario no está registrado en la base de datos");
                    Toast.makeText(MainActivity.this, "El usuario no está registrado en la base de datos", Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DATABASE", "Error en Firebase: " + error.getMessage());
                Toast.makeText(MainActivity.this, "Error al verificar usuario: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirListaUsuarios() {
        Intent intent = new Intent(MainActivity.this, ListaUsuariosActivity.class);
        startActivity(intent);
        finish();
    }

    private void irARegistro() {
        Intent intent = new Intent(MainActivity.this, RegistrarUsuarioActivity.class);
        startActivity(intent);
    }

    public void registrarUsuario(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            Usuario nuevoUsuario = new Usuario(userId, email);

                            databaseReference.child(userId).setValue(nuevoUsuario)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MainActivity.this, "Usuario registrado correctamente.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(MainActivity.this, "Error al registrar usuario en la base de datos", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class Usuario {
        public String userId;
        public String email;

        public Usuario() {
            // Constructor vacío necesario para Firebase
        }

        public Usuario(String userId, String email) {
            this.userId = userId;
            this.email = email;
        }
    }
}
