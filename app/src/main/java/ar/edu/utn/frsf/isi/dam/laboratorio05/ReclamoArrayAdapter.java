package ar.edu.utn.frsf.isi.dam.laboratorio05;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;


import java.util.List;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;

public class ReclamoArrayAdapter extends ArrayAdapter<Reclamo> {

    private OnReclamoListener listenerOnReclamo;

    public interface OnReclamoListener {
        public void editarReclamo(int id);
        public void borrarReclamo(int id);
        public void mostrarMapa(int id);
    }

    public void setOnReclamoListener(OnReclamoListener listener){
        listenerOnReclamo = listener;
    }

    public ReclamoArrayAdapter(Context ctx, List<Reclamo> datos){
        super(ctx,0,datos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v =  convertView;
        if(v==null){
            v = LayoutInflater.from(getContext()).inflate(R.layout.fila_reclamo,null);
        }
        TextView tvTitulo = (TextView) v.findViewById(R.id.fila_reclamo_titulo);
        TextView tvTipo = (TextView) v.findViewById(R.id.fila_reclamo_tipo);
        TextView pathrec=(TextView)v.findViewById(R.id.fila_reclamo_path);
        Button btnEditar= (Button) v.findViewById(R.id.btnEditar);
        Button btnBorrar= (Button) v.findViewById(R.id.btnBorrar);
        Button btnVerMapa= (Button) v.findViewById(R.id.btnVerEnMapa);
        Reclamo aux = getItem(position);
        tvTitulo.setText(aux.getReclamo());
        tvTipo.setText(aux.getTipo().toString());
        pathrec.setText(aux.getPathImagen());
        btnEditar.setTag(aux.getId());
        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = Integer.valueOf(view.getTag().toString());
                listenerOnReclamo.editarReclamo(id);
            }
        });
        btnBorrar.setTag(aux.getId());
        btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = Integer.valueOf(view.getTag().toString());
                listenerOnReclamo.borrarReclamo(id);
            }
        });

        btnVerMapa.setTag(aux.getId());
        btnVerMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = Integer.valueOf(view.getTag().toString());
                listenerOnReclamo.mostrarMapa(id);
            }
        });
        return v;
    }
}
