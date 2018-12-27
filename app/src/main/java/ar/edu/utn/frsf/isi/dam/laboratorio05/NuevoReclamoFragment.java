package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Environment;
import android.widget.Toast;


import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

public class NuevoReclamoFragment extends Fragment {

    public interface OnNuevoLugarListener {
        public void obtenerCoordenadas();
    }

    public void setListener(OnNuevoLugarListener listener) {
        this.listener = listener;
    }

    static final int REQUEST_IMAGE_CAPTURE=1;
    static final int RESULT_OK=-1;
    static final int REQUEST_IMAGE_SAVE=2;

    private Reclamo reclamoActual;
    private ReclamoDao reclamoDao;

    private EditText reclamoDesc;
    private EditText mail;
    private Spinner tipoReclamo;
    private TextView tvCoord;
    private Button buscarCoord;
    private Button btnGuardar;
    private Button btnFoto;
    private OnNuevoLugarListener listener;
    private ImageView miniImagen;
    private String pathFoto=null;
    private Button btnGrabarAudio;
    private MediaRecorder mRecorder;
    private String mFileName=null;
    private static final String LOG_TAG = "AudioRecordTest";
    private Boolean imagenCapturada=false;

    private ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;
    public NuevoReclamoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();

        View v = inflater.inflate(R.layout.fragment_nuevo_reclamo, container, false);
        reclamoDesc = (EditText) v.findViewById(R.id.reclamo_desc);
        mail= (EditText) v.findViewById(R.id.reclamo_mail);
        tipoReclamo= (Spinner) v.findViewById(R.id.reclamo_tipo);
        tvCoord= (TextView) v.findViewById(R.id.reclamo_coord);
        buscarCoord= (Button) v.findViewById(R.id.btnBuscarCoordenadas);
        btnGuardar= (Button) v.findViewById(R.id.btnGuardar);
        btnFoto= (Button)  v.findViewById(R.id.btnTomarFoto);
        miniImagen= (ImageView) v.findViewById(R.id.foto);
        btnGrabarAudio=(Button) v.findViewById(R.id.btnGrabarAudio);

        miniImagen.setImageBitmap(null);

        tipoReclamoAdapter = new ArrayAdapter<Reclamo.TipoReclamo>(getActivity(),android.R.layout.simple_spinner_item,Reclamo.TipoReclamo.values());
        tipoReclamoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoReclamo.setAdapter(tipoReclamoAdapter);

        int idReclamo =0;
        if(getArguments()!=null){
            idReclamo = getArguments().getInt("idReclamo",0);}

        cargarReclamo(idReclamo);

        boolean edicionActivada = !tvCoord.getText().toString().equals("0;0");
        reclamoDesc.setEnabled(edicionActivada );
        mail.setEnabled(edicionActivada );
        tipoReclamo.setEnabled(edicionActivada);
        btnGuardar.setEnabled(edicionActivada);

        if(idReclamo==0)
            btnGuardar.setEnabled(false);

        tipoReclamo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Reclamo.TipoReclamo tr = tipoReclamoAdapter.getItem(position);
                if(tr==Reclamo.TipoReclamo.VEREDAS ||
                        tr==Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO){
                    if(pathFoto==null){
                        btnGuardar.setEnabled(false);
                        Toast.makeText(getActivity(), getString(R.string.sacarFoto), Toast.LENGTH_LONG).show();}
                    else
                        btnGuardar.setEnabled(true);}
                else{
                    if(reclamoDesc.getText().toString().length()<8){
                        btnGuardar.setEnabled(false);
                        Toast.makeText(getActivity(), getString(R.string.descripcionOSonido), Toast.LENGTH_LONG).show();}
                    else
                        btnGuardar.setEnabled(true);}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        reclamoDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Reclamo.TipoReclamo tr = tipoReclamoAdapter.getItem(tipoReclamo.getSelectedItemPosition());
                if(tr!=Reclamo.TipoReclamo.VEREDAS && tr!=Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO){

                    btnGuardar.setEnabled(s.length()>=8 && !tvCoord.getText().toString().equals("0;0"));
                    //TODO: modificar condicion de lo de audio cuando este
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        buscarCoord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.obtenerCoordenadas();}
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveOrUpdateReclamo();}
        });

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sacarGuardarFoto();
            }
        });
        btnGrabarAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grabarAudio();
            }
        });
        return v;
    }

    private void cargarReclamo(final int id){
        if( id >0){
            Runnable hiloCargaDatos = new Runnable() {
                @Override
                public void run() {
                    reclamoActual = reclamoDao.getById(id);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mail.setText(reclamoActual.getEmail());
                            tvCoord.setText(reclamoActual.getLatitud()+";"+reclamoActual.getLongitud());
                            reclamoDesc.setText(reclamoActual.getReclamo());
                            miniImagen.setImageURI(Uri.parse(reclamoActual.getPathImagen()));
                            Reclamo.TipoReclamo[] tipos= Reclamo.TipoReclamo.values();
                            for(int i=0;i<tipos.length;i++) {
                                if(tipos[i].equals(reclamoActual.getTipo())) {
                                    tipoReclamo.setSelection(i);
                                    break;
                                }
                            }
                        }
                    });
                }
            };
            Thread t1 = new Thread(hiloCargaDatos);
            t1.start();
        }else{
            String coordenadas = "0;0";
            if(getArguments()!=null) coordenadas = getArguments().getString("latLng","0;0");
            tvCoord.setText(coordenadas);
            reclamoActual = new Reclamo();
        }
    }

    private void saveOrUpdateReclamo(){
        reclamoActual.setEmail(mail.getText().toString());
        reclamoActual.setReclamo(reclamoDesc.getText().toString());
        reclamoActual.setTipo(tipoReclamoAdapter.getItem(tipoReclamo.getSelectedItemPosition()));
        if(tvCoord.getText().toString().length()>0 && tvCoord.getText().toString().contains(";")) {
            String[] coordenadas = tvCoord.getText().toString().split(";");
            reclamoActual.setLatitud(Double.valueOf(coordenadas[0]));
            reclamoActual.setLongitud(Double.valueOf(coordenadas[1]));
        }
        if(pathFoto!=null)
            reclamoActual.setPathImagen(pathFoto);
        if(mFileName!=null)
            reclamoActual.setPathAudio(mFileName);
      
        Runnable hiloActualizacion = new Runnable() {
            @Override
            public void run() {

                if(reclamoActual.getId()>0) reclamoDao.update(reclamoActual);
                else reclamoDao.insert(reclamoActual);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // limpiar vista
                        mail.setText(R.string.texto_vacio);
                        tvCoord.setText(R.string.texto_vacio);
                        reclamoDesc.setText(R.string.texto_vacio);
                        miniImagen.setImageBitmap(null);
                        getActivity().getFragmentManager().popBackStack();
                        btnGuardar.setEnabled(false);
                    }
                });
            }
        };
        Thread t1 = new Thread(hiloActualizacion);
        t1.start();
    }

    private void sacarGuardarFoto(){

        Intent i1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(i1.resolveActivity(getActivity().getPackageManager())!=null){
            File foto_file= null;
            try{
                foto_file= crearImagenFile();}
            catch (IOException e){
                e.printStackTrace(); }

            if(foto_file!=null){
//aca
                Uri foto_URI = FileProvider.getUriForFile(getActivity().getApplication(),
                        "com.example.android.fileprovider",
                        foto_file);
                i1.putExtra(MediaStore.EXTRA_OUTPUT, foto_URI);
            startActivityForResult(i1, REQUEST_IMAGE_SAVE);
            }
        }
    }

    private File crearImagenFile()throws IOException{

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        pathFoto = image.getAbsolutePath();
        return image;
    }


   public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            miniImagen.setImageBitmap(imageBitmap);
        }

        if (requestCode == REQUEST_IMAGE_SAVE && resultCode == RESULT_OK) {
            File file = new File(pathFoto);
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.fromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (imageBitmap != null) {
                miniImagen.setImageBitmap(imageBitmap);

                //se habilita el boton si se saco foto para VEREDAS o CALLE EN MAL ESTADO
                Reclamo.TipoReclamo tr = tipoReclamoAdapter.getItem(tipoReclamo.getSelectedItemPosition());

                if (!tvCoord.getText().toString().equals("0;0") && (tr==Reclamo.TipoReclamo.VEREDAS
                        || tr==Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO))
                    btnGuardar.setEnabled(true);
            }
        }
    }
   //TODO: en metodo de grabar sonido se deberia chequear longitud de descripcion para habilitar el boton guardar

    public void grabarAudio(){

        if(mRecorder==null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if((ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    !=PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.RECORD_AUDIO)
                    !=PackageManager.PERMISSION_GRANTED)){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity()
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(getActivity()
                            , Manifest.permission.RECORD_AUDIO))
                        (new AlertDialog.Builder(getContext()))
                            .setTitle(getString(R.string.solicPermiso)).setMessage(getString(R.string.solicPermGrabyEsc))
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                                            , Manifest.permission.RECORD_AUDIO}, 1000);
                                    return;}})
                            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(), getString(R.string.errorGrabacion)
                                            , Toast.LENGTH_LONG).show();}})
                            .create()
                            .show();
                    else
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.RECORD_AUDIO}, 1000);
                return;
                }
                if((ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        ==PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.RECORD_AUDIO)
                        ==PackageManager.PERMISSION_GRANTED)){
                    iniciarGrabacion();
                }
            }
        }
        else if(mRecorder!=null){
            //para detener la grabacion...
            mRecorder.stop();
            mRecorder.release();
            mRecorder=null;
            btnGrabarAudio.setText(getString(R.string.btnGrabarAudio));
            btnGrabarAudio.setTextColor(Color.BLACK);
            Toast.makeText(getActivity(),"Grabacion finalizada",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
                if(grantResults.length>0&& grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    iniciarGrabacion();
                }else
                    Toast.makeText(getActivity(), getString(R.string.errorGrabacion), Toast.LENGTH_LONG).show();
        }

    public void iniciarGrabacion (){
        //para diferenciar grabaciones se les agrega fecha y hora al nombre de archivo

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnGrabarAudio.setText(getString(R.string.btnGrabarAudio2));
                btnGrabarAudio.setTextColor(Color.RED);
            }
        });
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss");
        Date date = new Date();
        String strDate = dateFormat.format(date);

        mFileName = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/audioreclamo-"+strDate+".3gp";

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mFileName);
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }
}
