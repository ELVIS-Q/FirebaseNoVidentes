package com.personal.firebaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrarUsuarioActivity extends AppCompatActivity {

    private EditText etEmail, etPass;
    private Button btnReg;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_registrar_usuario);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("usuarios");

        etEmail = findViewById(R.id.email);
        etPass = findViewById(R.id.password);
        btnReg = findViewById(R.id.btnRegistrar);

        btnReg.setOnClickListener(v -> {
            String e = etEmail.getText().toString().trim();
            String p = etPass.getText().toString().trim();

            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(e, p)
                    .addOnSuccessListener(res -> {
                        String uid = res.getUser().getUid();
                        dbRef.child(uid).setValue(new Usuario(uid, e))
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(error -> {
                                    Toast.makeText(this, "Error al guardar usuario: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(err -> {
                        if (err instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Registro exitoso...", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, err.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    public static class Usuario {
        public String userId;
        public String email;

        public Usuario() {} // Constructor vacío requerido por Firebase

        public Usuario(String userId, String email) {
            this.userId = userId;
            this.email = email;
        }
    }
}
