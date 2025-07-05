package com.personal.firebaseapp;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.util.ArrayList;
import java.util.List;

public class UsuarioAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> usuariosList;
    private List<String> usuariosIds;
    private OnItemClickListener onItemClickListener;

    // Constructor vacío necesario para evitar errores de instanciación
    public UsuarioAdapter(@NonNull Context context) {
        super(context, android.R.layout.simple_list_item_1, new ArrayList<>());
        this.context = context;
        this.usuariosList = new ArrayList<>();
        this.usuariosIds = new ArrayList<>();
    }

    public UsuarioAdapter(Context context, List<String> usuariosList, List<String> usuariosIds, OnItemClickListener onItemClickListener) {
        super(context, android.R.layout.simple_list_item_1, usuariosList);
        this.context = context;
        this.usuariosList = usuariosList;
        this.usuariosIds = usuariosIds;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(usuariosList.get(position));

        // Manejo de clics
        view.setOnClickListener(v -> {
            if (onItemClickListener != null && position < usuariosIds.size()) {
                onItemClickListener.onItemClick(usuariosIds.get(position));
            }
        });

        return view;
    }

    public interface OnItemClickListener {
        void onItemClick(String usuarioId);
    }
}
