package com.personal.firebaseapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class ListaUsuariosActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Familiar> lista = new ArrayList<>();
    private ArrayList<String> ids = new ArrayList<>();
    private UsuarioAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_usuarios);

        Toolbar tb = findViewById(R.id.toolbar1);
        setSupportActionBar(tb);

        listView = findViewById(R.id.listViewUsuarios);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        adapter = new UsuarioAdapter(this, lista);
        listView.setAdapter(adapter);

        cargarFamiliaresDesdeFirestore();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            mostrarDialogoEliminar(position);
        });
    }

    private void cargarFamiliaresDesdeFirestore() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("familiares")
                .whereEqualTo("uid", uid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    lista.clear();
                    ids.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Familiar f = doc.toObject(Familiar.class);
                        lista.add(f);
                        ids.add(doc.getId());  // guardar ID para eliminación
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void mostrarDialogoEliminar(int position) {
        Familiar f = lista.get(position);
        String docId = ids.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Eliminar familiar")
                .setMessage("¿Deseas eliminar a:\n" + f.nombres + "?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarFamiliar(docId))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarFamiliar(String docId) {
        firestore.collection("familiares").document(docId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Familiar eliminado", Toast.LENGTH_SHORT).show();
                    cargarFamiliaresDesdeFirestore(); // refrescar lista
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.ver_sms) {
            startActivity(new Intent(this, SMSActivity.class));
        } else if (id == R.id.registrar) {
            startActivity(new Intent(this, PerfilActivity.class));
        } else if (id == R.id.cerrar_sesion) {
            auth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public static class Familiar {
        public String cedula, nombres, celular, direccion;

        public Familiar() {
        }
    }
}
