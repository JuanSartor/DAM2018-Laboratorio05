package ar.edu.utn.frsf.isi.dam.laboratorio05;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;

public class ReclamoArrayAdapter extends ArrayAdapter<Reclamo> {

    private OnReclamoListener listenerOnReclamo;
    private int idReproduciendo=-1;

    public interface OnReclamoListener {
        public void editarReclamo(int id);
        public void borrarReclamo(int id);
        public void mostrarMapa(int id);
        public void reproducirAudio(int id);
    }

    public void setOnReclamoListener(OnReclamoListener listener){
        listenerOnReclamo = listener;
    }

    public ReclamoArrayAdapter(Context ctx, List<Reclamo> datos){
        super(ctx,0,datos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewGroup listView = parent;
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
        final Button btnReproducirAudio= (Button) v.findViewById(R.id.btnReproducir);

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

        btnReproducirAudio.setTag(aux.getId());
        btnReproducirAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = Integer.valueOf(v.getTag().toString());
                ArrayList<View> listaViews = new ArrayList<>();
                listView.findViewsWithText(listaViews, getContext().getString(R.string.btnReproducirAudio), View.FIND_VIEWS_WITH_TEXT);
                if(listaViews.size()>0) {
                  ((Button) listaViews.get(0)).setText("Reproducir Audio");
                  ((Button) listaViews.get(0)).setTextColor(Color.BLACK);
                }
                btnReproducirAudio.setText(getContext().getString(R.string.btnReproducirAudio));
                btnReproducirAudio.setTextColor(Color.RED);

                idReproduciendo=id;
                listenerOnReclamo.reproducirAudio(id);
            }
        });

        return v;
    }
}
