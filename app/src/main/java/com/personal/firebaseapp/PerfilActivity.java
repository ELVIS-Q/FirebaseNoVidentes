package com.personal.firebaseapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class PerfilActivity extends AppCompatActivity {
    private EditText cedula, nombres, celular, direccion;
    private Button btnregresar, btnregistrar;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("usuarios");

        // Vincular vistas
        cedula = findViewById(R.id.cedula);
        nombres = findViewById(R.id.nombres);
        celular = findViewById(R.id.celular);
        direccion = findViewById(R.id.direccion);
        btnregistrar = findViewById(R.id.btnregistrar);
        btnregresar = findViewById(R.id.btnRegresar);

        btnregresar.setOnClickListener(v -> finish());

        btnregistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });
    }

    private void registrarUsuario() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Crear objeto con los datos del usuario
            Usuario usuario = new Usuario(
                    cedula.getText().toString(),
                    nombres.getText().toString(),
                    celular.getText().toString(),
                    direccion.getText().toString()
            );

            // Guardar datos en Realtime Database
            databaseReference.child(userId).setValue(usuario)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(PerfilActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                limpiarFormulario();
                            } else {
                                Toast.makeText(PerfilActivity.this, "Error al registrar datos", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarFormulario() {
        cedula.setText("");
        nombres.setText("");
        celular.setText("");
        direccion.setText("");
    }

    // Clase interna para representar al usuario
    public static class Usuario {
        public String cedula;
        public String nombres;
        public String celular;
        public String direccion;

        public Usuario() {
            // Constructor vacío necesario para Firebase
        }

        public Usuario(String cedula, String nombres, String celular, String direccion) {
            this.cedula = cedula;
            this.nombres = nombres;
            this.celular = celular;
            this.direccion = direccion;
        }
    }
}
