package com.personal.firebaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrarUsuarioActivity extends AppCompatActivity {

    private EditText email, password;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_usuario);

        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios");

        // Vincular vistas
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);

        // Botón para registrar
        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        // Verificar que los campos no estén vacíos
        if (validarCampos()) {
            String emailUser = email.getText().toString().trim();
            String passUser = password.getText().toString().trim();

            // Crear un nuevo usuario con email y contraseña
            firebaseAuth.createUserWithEmailAndPassword(emailUser, passUser)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Usuario creado exitosamente
                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                            if (currentUser != null) {
                                // Crear un objeto Usuario
                                Usuario nuevoUsuario = new Usuario(currentUser.getUid(), emailUser);

                                // Guardar el usuario en Firebase Realtime Database
                                databaseReference.child(currentUser.getUid()).setValue(nuevoUsuario)
                                        .addOnSuccessListener(aVoid -> {
                                            // Redirigir a MainActivity
                                            Intent intent = new Intent(RegistrarUsuarioActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish(); // Finalizar para evitar que regrese al registrarse
                                            Toast.makeText(RegistrarUsuarioActivity.this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RegistrarUsuarioActivity.this, "Error al guardar en la base de datos", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            manejarErrorRegistro(task.getException());
                        }
                    });
        }
    }

    private void manejarErrorRegistro(Exception exception) {
        if (exception != null) {
            String message;
            if (exception instanceof FirebaseAuthWeakPasswordException) {
                message = "La contraseña es demasiado débil. Intenta con una más segura.";
            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                message = "El formato del correo electrónico es inválido.";
            } else if (exception instanceof FirebaseAuthUserCollisionException) {
                message = "El correo electrónico ya está registrado. Usa otro.";
            } else {
                message = "Error al registrar el usuario: " + exception.getMessage();
            }
            Toast.makeText(RegistrarUsuarioActivity.this, message, Toast.LENGTH_LONG).show();
        }
    }

    private boolean validarCampos() {
        if (email.getText().toString().trim().isEmpty() || password.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validación de formato de correo electrónico
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) {
            Toast.makeText(this, "El correo electrónico no es válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validación de longitud mínima de contraseña
        if (password.getText().toString().trim().length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
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
