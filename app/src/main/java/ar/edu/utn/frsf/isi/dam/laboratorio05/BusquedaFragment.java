package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;

/**
 * A simple {@link Fragment} subclass.
 */
public class BusquedaFragment extends Fragment {

    private Spinner tipoReclamo;
    private Button btnBuscar;
    private ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;
    private BusquedaFragment.buscarListener listener;

    public BusquedaFragment() {
        // Required empty public constructor
    }

    public interface buscarListener {
        void ubicarResultados(Reclamo.TipoReclamo tipo);
    }

    public void setListener(BusquedaFragment.buscarListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_buscar, container, false);

        tipoReclamo= (Spinner) v.findViewById(R.id.tipoReclamo);
        btnBuscar = (Button) v.findViewById(R.id.btnBuscar);

        tipoReclamoAdapter = new ArrayAdapter<Reclamo.TipoReclamo>(getActivity(),android.R.layout.simple_spinner_item,Reclamo.TipoReclamo.values());
        tipoReclamoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoReclamo.setAdapter(tipoReclamoAdapter);

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.ubicarResultados(tipoReclamoAdapter.getItem(tipoReclamo.getSelectedItemPosition()));
            }
        });
        return v;
    }

}
