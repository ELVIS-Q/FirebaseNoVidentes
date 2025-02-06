package com.personal.firebaseapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ListaUsuariosActivity extends AppCompatActivity {

    private ListView listViewUsuarios;
    private ArrayList<String> usuariosList;
    private ArrayList<String> usuariosIds;
    private DatabaseReference databaseReference;
    private ArrayAdapter<String> usuariosAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_usuarios);

        // Inicialización de elementos
        listViewUsuarios = findViewById(R.id.listViewUsuarios);
        usuariosList = new ArrayList<>();
        usuariosIds = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios");

        // Configurar la toolbar
        Toolbar toolbar1 = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar1);

        // Configurar Listview
        usuariosAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usuariosList);
        listViewUsuarios.setAdapter(usuariosAdapter);

        // Actualizar la lista de usuarios al iniciar la actividad
        actualizarListaUsuarios();

        listViewUsuarios.setOnItemClickListener((parent, view, position, id) -> {
            String usuarioId = usuariosIds.get(position);
            eliminarUsuario(usuarioId);
        });
    }

    private void actualizarListaUsuarios() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuariosList.clear();
                usuariosIds.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String usuarioId = dataSnapshot.getKey();
                    String cedula = dataSnapshot.child("cedula").getValue(String.class);
                    String nombres = dataSnapshot.child("nombres").getValue(String.class);
                    String celular = dataSnapshot.child("celular").getValue(String.class);
                    String direccion = dataSnapshot.child("direccion").getValue(String.class);

                    if (cedula != null && nombres != null && celular != null && direccion != null) {
                        usuariosList.add("Cédula: " + cedula + "\nNombre: " + nombres + "\nCelular: " + celular + "\nDirección: " + direccion);
                        usuariosIds.add(usuarioId);
                    }
                }
                usuariosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListaUsuariosActivity.this, "Error al obtener los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarUsuario(String usuarioId) {
        databaseReference.child(usuarioId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ListaUsuariosActivity.this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ListaUsuariosActivity.this, "Error al eliminar el usuario", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.registrar) {
            Toast.makeText(this, "Ida al registro", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, PerfilActivity.class);
            startActivity(intent);
        } else if (id == R.id.cerrar_sesion) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.ver_sms) {
            Toast.makeText(this, "Ver SMS", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, SMSActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
