package com.personal.firebaseapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class UsuarioAdapter extends ArrayAdapter<ListaUsuariosActivity.Familiar> {

    private final Context context;
    private final List<ListaUsuariosActivity.Familiar> familiares;

    public UsuarioAdapter(Context context, List<ListaUsuariosActivity.Familiar> familiares) {
        super(context, 0, familiares);
        this.context = context;
        this.familiares = familiares;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = view.findViewById(android.R.id.text1);
        ListaUsuariosActivity.Familiar f = familiares.get(position);

        String texto = "Cédula: " + f.cedula + "\n" +
                "Nombre: " + f.nombres + "\n" +
                "Celular: " + f.celular + "\n" +
                "Dirección: " + f.direccion;

        textView.setText(texto);
        textView.setTextSize(16f);
        textView.setTextColor(0xFF000000); // negro

        return view;
    }
}
