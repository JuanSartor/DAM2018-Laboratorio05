package ar.edu.utn.frsf.isi.dam.laboratorio05;

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.TipoReclamoConverter;


// AGREGAR en MapaFragment una interface MapaFragment.OnMapaListener con el método coordenadasSeleccionadas 
// IMPLEMENTAR dicho método en esta actividad.

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener,
        NuevoReclamoFragment.OnNuevoLugarListener, MapaFragment.onMapaListener, BusquedaFragment.buscarListener {

    private DrawerLayout drawerLayout;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        //Handle when activity is recreated like on orientation Change
        shouldDisplayHomeUp();

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navView = (NavigationView)findViewById(R.id.navview);
        BienvenidoFragment fragmentInicio = new BienvenidoFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenido, fragmentInicio)
                .commit();

        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        boolean fragmentTransaction = false;
                        Fragment fragment = null;
                        String tag = "";
                        switch (menuItem.getItemId()) {
                            case R.id.optNuevoReclamo:
                                tag = "nuevoReclamoFragment";
                                fragment =  getSupportFragmentManager().findFragmentByTag(tag);
                                if(fragment==null) {
                                    fragment = new NuevoReclamoFragment();
                                    ((NuevoReclamoFragment) fragment).setListener(MainActivity.this);
                                }
                                fragmentTransaction = true;
                                break;
                            case R.id.optListaReclamo:
                                tag="listaReclamos";
                                fragment =  getSupportFragmentManager().findFragmentByTag(tag);
                                if(fragment==null) fragment = new ListaReclamosFragment();
                                fragmentTransaction = true;
                                break;
                            case R.id.optVerMapa:
                                tag="mapaReclamos";
                                fragment =  getSupportFragmentManager().findFragmentByTag(tag);
                                if(fragment==null) {
                                    Bundle args = new Bundle();
                                    args.putInt("tipo_mapa",2);
                                    fragment = new MapaFragment();
                                    fragment.setArguments(args);
                                    ((MapaFragment) fragment).setListener(MainActivity.this);
                                }
                                fragmentTransaction = true;
                                break;
                            case R.id.optHeatMap:
                                tag="mapaReclamos";
                                fragment =  getSupportFragmentManager().findFragmentByTag(tag);
                                if(fragment==null) {
                                    Bundle args2 = new Bundle();
                                    args2.putInt("tipo_mapa",4);
                                    fragment = new MapaFragment();
                                    fragment.setArguments(args2);
                                    ((MapaFragment) fragment).setListener(MainActivity.this);
                                }
                                fragmentTransaction = true;
                                break;

                            case R.id.optBusqueda:
                                tag="buscarReclamos";
                                fragment =  getSupportFragmentManager().findFragmentByTag(tag);
                                if(fragment==null) {
                                    fragment = new BusquedaFragment();
                                    ((BusquedaFragment) fragment).setListener(MainActivity.this);
                                }
                                fragmentTransaction = true;
                                break;
                        }

                        if(fragmentTransaction) {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.contenido, fragment,tag)
                                    .addToBackStack(null)
                                    .commit();

                            menuItem.setChecked(true);

                            getSupportActionBar().setTitle(menuItem.getTitle());
                        }
                        drawerLayout.closeDrawers();

                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp(){
        //Enable Up button only  if there are entries in the back stack
        boolean canback = getSupportFragmentManager().getBackStackEntryCount()>0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
    }

    // Invoca el fragmento "nuevoReclamoFragment"
    // pasando el objeto "LatLng" elegido por el usuario con click largo como lugar del reclamo

        @Override
        public void coordenadasSeleccionadas(LatLng c){
            String tag = "nuevoReclamoFragment";
            Fragment fragment =  getSupportFragmentManager().findFragmentByTag(tag);
            if(fragment==null) {
                fragment = new NuevoReclamoFragment();
                ((NuevoReclamoFragment) fragment).setListener(MainActivity.this);
            }
            Bundle bundle = new Bundle();
            bundle.putString("latLng",c.latitude+";"+c.longitude);
            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentByTag("nuevoReclamoFragment"))
                    .remove(getSupportFragmentManager().findFragmentByTag("seleccionCoordenadas"))
                    .commit();      // esto es para que cuando uno seleccione coordenadas
                                    // y apreta la fecha hacia atras, no vuelva a
                                    // seleccion de coordenadas, sino a la imagen principal

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenido, fragment,tag)
                    .commit();
        }

        @Override
        public void obtenerCoordenadas() {

            Fragment fragmento;
            Bundle args = new Bundle();

            args.putInt("tipo_mapa",1);
            fragmento= new MapaFragment();
            fragmento.setArguments(args);

            ((MapaFragment) fragmento).setListener(MainActivity.this);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenido, fragmento,"seleccionCoordenadas")
                    //.addToBackStack(null)  // si combino esto con los remove de fragmentos de la funcion
                                             // coordenadasSeleccionadas se produce un error. Si se pone esto
                                             // hay que sacar lo otro.
                    .commit();

            getSupportActionBar().setTitle("Seleccione coordenadas:");

            Toast.makeText(this, getString(R.string.seleccionCoord), Toast.LENGTH_LONG).show();
        }

    @Override
    public void ubicarResultados(Reclamo.TipoReclamo tipo){
        Fragment fragmento;
        Bundle args = new Bundle();

        args.putInt("tipo_mapa",5);
        args.putString("tipo_reclamo",TipoReclamoConverter.toString(tipo));

        fragmento= new MapaFragment();
        fragmento.setArguments(args);

        ((MapaFragment) fragmento).setListener(MainActivity.this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenido, fragmento,"ubicarResultados")
                .addToBackStack(null)
                .commit();

        getSupportActionBar().setTitle("Resultados de la Busqueda:");
    }
}

