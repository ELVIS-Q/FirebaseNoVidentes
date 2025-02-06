package com.personal.firebaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private EditText et1, et2;
    private Button btningresa, btnRegister;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ImageView imageView;  // Declaramos el ImageView

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
        imageView = findViewById(R.id.imageView2);  // Vinculamos el ImageView

        // Configurar listeners
        btningresa.setOnClickListener(v -> ingresa());
        btnRegister.setOnClickListener(v -> irARegistro());

        // Cambiar la imagen programáticamente (opcional)
        imageView.setImageResource(R.drawable.fer);  // Cambia la imagen en el ImageView
    }

    private void verificarUsuarioAutenticado() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(MainActivity.this, ListaUsuariosActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void ingresa() {
        String username = et1.getText().toString().trim();
        String password = et2.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Por favor, ingresa ambos campos", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            verificarUsuarioEnBaseDeDatos(user.getUid());
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verificarUsuarioEnBaseDeDatos(String userId) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Intent intent = new Intent(MainActivity.this, ListaUsuariosActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "El usuario no está registrado en la base de datos", Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irARegistro() {
        Intent intent = new Intent(MainActivity.this, RegistrarUsuarioActivity.class);
        startActivity(intent);
    }

    // Método para registrar un nuevo usuario y agregarlo a Firebase Realtime Database
    public void registrarUsuario(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Crear datos del usuario en Realtime Database
                            String userId = user.getUid();
                            Usuario nuevoUsuario = new Usuario(userId, email);

                            // Guardar datos en la base de datos
                            databaseReference.child(userId).setValue(nuevoUsuario)
                                    .addOnSuccessListener(aVoid -> {
                                        // Registro exitoso
                                        Toast.makeText(MainActivity.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                                        irAListaUsuarios();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error al guardar en la base de datos
                                        Toast.makeText(MainActivity.this, "Error al registrar usuario en la base de datos", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para redirigir a la actividad de lista de usuarios
    private void irAListaUsuarios() {
        Intent intent = new Intent(MainActivity.this, ListaUsuariosActivity.class);
        startActivity(intent);
        finish();
    }

    // Clase interna para representar un usuario
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
