package ar.edu.utn.frsf.isi.dam.laboratorio05;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListaReclamosFragment extends Fragment {

    private static final int EVENTO_UPDATE_LISTA = 100;
    private ReclamoArrayAdapter adapter;
    private List<Reclamo> listaReclamos;
    private ListView lvReclamos;
    private ReclamoDao reclamoDao;
    private static final String LOG_TAG = "AudioRecordTest";
    private MediaPlayer mPlayer = new MediaPlayer();
    private int idReproduciendo=-1;
    private boolean reproduciendo=false;

    public ListaReclamosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lista_reclamos, container, false);
        lvReclamos = (ListView) v.findViewById(R.id.listaReclamos);
        listaReclamos = new ArrayList<>();
        adapter = new ReclamoArrayAdapter(getActivity(),listaReclamos);
        adapter.setOnReclamoListener(eventosAdapterManager);
        lvReclamos.setAdapter(adapter);

        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();
        cargarReclamosAsyn();
        return v;
    }

    ReclamoArrayAdapter.OnReclamoListener eventosAdapterManager = new ReclamoArrayAdapter.OnReclamoListener() {
        @Override
        public void editarReclamo(int id) {
            NuevoReclamoFragment f = new NuevoReclamoFragment ();
            // Supply index input as an argument.
            Bundle args = new Bundle();
            args.putInt("idReclamo",id);
            f.setArguments(args);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenido, f)
                    .commit();
        }

        @Override
        public void borrarReclamo(final int id) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Reclamo r= reclamoDao.getById(id);
                    reclamoDao.delete(r);
                    listaReclamos.clear();
                    listaReclamos.addAll(reclamoDao.getAll());
                    Message completeMessage = handler.obtainMessage(EVENTO_UPDATE_LISTA);
                    completeMessage.sendToTarget();
                }
            };
            Thread t1 = new Thread(r);
            t1.start();
        }

        @Override
        public void mostrarMapa(int id) {
            Fragment f = new MapaFragment();
            Bundle args = new Bundle();

            args.putInt("tipo_mapa",3);
            args.putInt("idReclamo",id);

            f.setArguments(args);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenido, f, "mostrarReclamo")
                    .commit();
        }

        public void reproducirAudio(int id){
            final int reclamoAReproducir=id;

            //se usa booleano reproduciendo porque si uso metodo isPlaying de mPlayer en if
            // y no esta reproduciendo da excepcion si no esta inicializado aun.

            if (reproduciendo && reclamoAReproducir == idReproduciendo){
                mPlayer.stop();
                mPlayer.release();
                idReproduciendo=-1;
                reproduciendo=false;
                ArrayList<View> listaViews = new ArrayList<>();
                lvReclamos.findViewsWithText(listaViews, getContext().getString(R.string.btnReproducirAudio),
                        View.FIND_VIEWS_WITH_TEXT);
                if(listaViews.size()>0) {
                    ((Button) listaViews.get(0)).setText("Reproducir Audio");
                    ((Button) listaViews.get(0)).setTextColor(Color.BLACK);
                }
            }
            else{
                if (reproduciendo)
                    mPlayer.stop();
                mPlayer=new MediaPlayer();

                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        reproduciendo=false;
                        idReproduciendo=-1;
                        ArrayList<View> listaViews = new ArrayList<>();
                        lvReclamos.findViewsWithText(listaViews, getString(R.string.btnReproducirAudio),
                                View.FIND_VIEWS_WITH_TEXT);
                        if(listaViews.size()>0) {
                            ((Button) listaViews.get(0)).setText("Reproducir Audio");
                            ((Button) listaViews.get(0)).setTextColor(Color.BLACK);
                        }
                    }});

                Runnable codigoRepr= new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mPlayer.setDataSource(reclamoDao.getById(reclamoAReproducir).getPathAudio());
                            mPlayer.prepare();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "prepare() failed");
                        }
                        mPlayer.start();
                        reproduciendo=true;
                        idReproduciendo=reclamoAReproducir;
                    }};
                Thread hiloReproduccion = new Thread(codigoRepr);
                hiloReproduccion.start();
            }
        }
    };

    private void cargarReclamosAsyn(){
        Runnable hiloCargarReclamos = new Runnable() {
            @Override
            public void run() {
                listaReclamos.clear();
                listaReclamos.addAll(reclamoDao.getAll());
                Message completeMessage = handler.obtainMessage(EVENTO_UPDATE_LISTA);
                completeMessage.sendToTarget();
            }
        };
        Thread t1 = new Thread(hiloCargarReclamos);
        t1.start();
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message inputMessage) {
            switch (inputMessage.what){
                case EVENTO_UPDATE_LISTA:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
}
