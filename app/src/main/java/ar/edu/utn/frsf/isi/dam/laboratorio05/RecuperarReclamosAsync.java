package ar.edu.utn.frsf.isi.dam.laboratorio05;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

public class RecuperarReclamosAsync extends AsyncTask<ParamsAsyncTask,Integer,List<Reclamo>> {

    private List<Reclamo> reclamos= new ArrayList<Reclamo>();
    private Reclamo reclamo;
    private int tipo;
    private GoogleMap mapa;
    private FragmentActivity actividad;
    private Double maxlat =null, maxlon = null;
    private Double minlat =null, minlon = null;

    @Override
    protected List<Reclamo> doInBackground(ParamsAsyncTask... parametros) {

        tipo= parametros[0].argumentos.getInt("tipo_mapa",0);
        mapa= parametros[0].mapa;
        actividad= parametros[0].actividad;

        ReclamoDao reclamoDao = MyDatabase.getInstance(actividad).getReclamoDao();
        if (tipo==3) {
            reclamo = reclamoDao.getById(parametros[0].argumentos.getInt("idReclamo", 0));
            reclamos.add(reclamo);
        }else
            reclamos = reclamoDao.getAll();
        return reclamos;
    }

    @Override
    protected void onPostExecute(List<Reclamo> recuperados){
        if (tipo != 3) {
            if(reclamos.size()>0) {
                PolylineOptions rectOptions = new PolylineOptions();
                List<LatLng> lista = new ArrayList<LatLng>();
                for (Reclamo r : reclamos) {
                    // TODO: retenerMaxMin se podria hacer de otra forma?
                    retenerMaxMin(r);
                    if(tipo==5)
                        rectOptions.add(new LatLng(r.getLatitud(), r.getLongitud())).color(Color.RED);
                    if(tipo!=4)
                        mapa.addMarker(new MarkerOptions().
                                position(new LatLng(r.getLatitud(), r.getLongitud())).
                                title(String.valueOf(r.getId()) + " - " + r.getTipo()).
                                snippet(r.getReclamo()).
                                draggable(false));
                    else
                        lista.add(new LatLng(r.getLatitud(), r.getLongitud()));
                }
                if(tipo==5)
                    mapa.addPolyline(rectOptions);
                if(tipo==4){
                    HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().data(lista).build();
                    mapa.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));}

                LatLngBounds limites = new LatLngBounds(new LatLng(minlat - 0.005, minlon - 0.005), new LatLng(maxlat + 0.005, maxlon + 0.005));
                mapa.moveCamera(CameraUpdateFactory.newLatLngBounds(limites, 10));}
            else
                Toast.makeText(actividad, actividad.getString(R.string.ceroReclamos), Toast.LENGTH_LONG).show();
        }
        if(tipo==3) {
            if (reclamo != null) {
                mapa.addMarker(new MarkerOptions().
                        position(new LatLng(reclamo.getLatitud(), reclamo.getLongitud())).
                        title(String.valueOf(reclamo.getId()) + " - " + reclamo.getTipo()).
                        snippet(reclamo.getReclamo()).
                        draggable(false));

                LatLng centro = new LatLng(reclamo.getLatitud(), reclamo.getLongitud());
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(centro, 15));

                CircleOptions circleOptions = new CircleOptions()
                        .center(centro)
                        .radius(500)
                        .strokeColor(Color.RED)
                        .fillColor(0x22FF0000)
                        .strokeWidth(5);
                mapa.addCircle(circleOptions);
            } else
                Toast.makeText(actividad, actividad.getString(R.string.ceroReclamos), Toast.LENGTH_LONG).show();
        }
    }

    private void retenerMaxMin(Reclamo r){
        if (maxlat==null && minlat == null && maxlon==null && minlon==null) {
            maxlat = r.getLatitud();
            minlat = r.getLatitud();
            maxlon = r.getLongitud();
            minlon = r.getLongitud();
        }
        if (maxlat<r.getLatitud())
            maxlat=r.getLatitud();
        else if (minlat>r.getLatitud())
            minlat=r.getLatitud();
        if (maxlon<r.getLongitud())
            maxlon=r.getLongitud();
        else if (minlon>r.getLongitud())
            minlon=r.getLongitud();
    }
}
