package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {
    private GoogleMap miMapa;
    private onMapaListener listener;
    private Double maxlat =null, maxlon = null;
    private Double minlat =null, minlon = null;
    private List<Reclamo> reclamos;

    public interface onMapaListener{
        void coordenadasSeleccionadas(LatLng c);
    }

    public void setListener(onMapaListener listener) {
        this.listener = listener;
    }

    public MapaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        int tipoMapa =0;
        Bundle argumentos = getArguments();
        if(argumentos !=null)
            tipoMapa = argumentos .getInt("tipo_mapa",0);
        getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        miMapa = map;
        actualizar();

        Bundle argumentos = getArguments();
        if(argumentos !=null) {
            if (argumentos.getInt("tipo_mapa", 0) == 1)
                map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        listener.coordenadasSeleccionadas(latLng);
                    }
                });

            if (argumentos.getInt("tipo_mapa", 0) == 2){

                Runnable hiloReclamos = new Runnable() {
                    @Override
                    public void run() {
                        ReclamoDao reclamoDao = MyDatabase.getInstance(getActivity()).getReclamoDao();
                        reclamos = reclamoDao.getAll();
                    }
                };
                Thread t1 = new Thread(hiloReclamos);
                t1.start();
                try {
                    Thread.sleep(1500); //TODO: si no duermo el hilo...como obtengo reclamos ANTES de dibujar el mapa?
                    for (Reclamo r: reclamos) {

                        // TODO: retenerMaxMin se podria hacer de otra forma?
                        retenerMaxMin(r);
                        miMapa.addMarker(new MarkerOptions().
                                position(new LatLng(r.getLatitud(), r.getLongitud())).
                                title(String.valueOf(r.getId())+" - "+r.getTipo()).
                                snippet(r.getReclamo()).
                                draggable(false));
                        LatLngBounds limites = new LatLngBounds(new LatLng(minlat-0.005,minlon-0.005), new LatLng(maxlat+0.005,maxlon+0.005));
                        miMapa.moveCamera(CameraUpdateFactory.newLatLngBounds(limites, 10));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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

    private void actualizar(){
        final String[] perm = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && (ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION)
                !=PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_COARSE_LOCATION)
                !=PackageManager.PERMISSION_GRANTED)){
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity()
                    , Manifest.permission.ACCESS_FINE_LOCATION))
                (new AlertDialog.Builder(getContext()))
                        .setTitle(getString(R.string.solicPermisoUbic)).setMessage(getString(R.string.textoSolicPermUbic))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(), perm, 5);
                                return;}
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(), getString(R.string.errorUbicacion), Toast.LENGTH_LONG).show();}
                        }).create().show();
            else
                ActivityCompat.requestPermissions(getActivity(),perm,5);
            return;}

        //Lo que empieza aca:
        LocationManager locationManagerCt = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location locationCt = locationManagerCt.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng latLng = new LatLng(locationCt.getLatitude(),
                locationCt.getLongitude());

        miMapa.setMyLocationEnabled(true);

        miMapa.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        miMapa.animateCamera(CameraUpdateFactory.zoomTo(15));
        //y llega hasta aca, salvo el "setMyLocationEnabled" es para que mueva la camara
        // automaticamente al lugar donde uno esta ubicado
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch(requestCode) {
            case 5:
                if(grantResults.length>0&& grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    actualizar();
                else
                    Toast.makeText(getActivity(), getString(R.string.errorUbicacion), Toast.LENGTH_LONG).show();
        }
    }
}

