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
        if (!validarCampos()) return;

        String emailUser = email.getText().toString().trim();
        String passUser = password.getText().toString().trim();

        firebaseAuth.createUserWithEmailAndPassword(emailUser, passUser)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        if (currentUser != null) {
                            // Enviar email de verificación
                            currentUser.sendEmailVerification()
                                    .addOnSuccessListener(aVoid -> {
                                        guardarUsuarioEnBaseDeDatos(currentUser, emailUser);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error al enviar email de verificación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        manejarErrorRegistro(task.getException());
                    }
                });
    }

    private void guardarUsuarioEnBaseDeDatos(FirebaseUser user, String emailUser) {
        if (databaseReference == null) {
            Toast.makeText(this, "Error al conectar con Firebase", Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario nuevoUsuario = new Usuario(user.getUid(), emailUser);

        databaseReference.child(user.getUid()).setValue(nuevoUsuario)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Usuario registrado. Verifica tu correo antes de iniciar sesión.", Toast.LENGTH_LONG).show();
                    firebaseAuth.signOut(); // Cerrar sesión hasta que el usuario verifique su correo
                    startActivity(new Intent(RegistrarUsuarioActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar en la base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void manejarErrorRegistro(Exception exception) {
        if (exception != null) {
            String message;
            if (exception instanceof FirebaseAuthWeakPasswordException) {
                message = "La contraseña es demasiado débil.";
            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                message = "El formato del correo es inválido.";
            } else if (exception instanceof FirebaseAuthUserCollisionException) {
                message = "El correo ya está registrado.";
            } else {
                message = "Error al registrar: " + exception.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private boolean validarCampos() {
        if (email.getText().toString().trim().isEmpty() || password.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.getText().toString().trim().length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public static class Usuario {
        public String userId;
        public String email;

        public Usuario() {}

        public Usuario(String userId, String email) {
            this.userId = userId;
            this.email = email;
        }
    }
}
