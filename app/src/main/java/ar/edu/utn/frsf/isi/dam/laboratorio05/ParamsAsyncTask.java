package ar.edu.utn.frsf.isi.dam.laboratorio05;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;

public class ParamsAsyncTask {
    FragmentActivity actividad;
    Bundle argumentos;
    GoogleMap mapa;

    ParamsAsyncTask(FragmentActivity act, Bundle arg, GoogleMap map) {
        this.actividad = act;
        this.argumentos = arg;
        this.mapa = map;
    }
}
