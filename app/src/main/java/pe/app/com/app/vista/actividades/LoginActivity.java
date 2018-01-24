package pe.app.com.app.vista.actividades;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.gson.Gson;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pe.app.com.app.R;
import pe.app.com.app.datos.conexion.Singleton;
import pe.app.com.app.datos.herramientas.GenericAlerts;
import pe.app.com.app.datos.herramientas.YourPreference;
import pe.app.com.app.datos.modelos.Usuario;
import pe.app.com.app.interactor.login.implemInteractLogin;
import pe.app.com.app.presentador.login.implemPresentLogin;
import pe.app.com.app.presentador.login.presentadorLogin;
import pe.app.com.app.vista.vistaLogin;

public class LoginActivity extends AppCompatActivity implements vistaLogin{

    CallbackManager callbackManager;
    private Usuario usuario;
    private YourPreference preferencias;
    Gson gson = new Gson();
    Context mCtx;
    ProgressDialog barraProgreso = null;
    GenericAlerts alertas = new GenericAlerts();
    String URL_LOG = "";

    private presentadorLogin presentador;

    @BindView(R.id.logEdtUsuario) EditText EdtUsuario;
    @BindView(R.id.logEdtPassword) EditText EdtPass;
    @BindView(R.id.logBtnIngresar) TextView TxtIngresar;
    @BindView(R.id.logBtnRegistrar) TextView TxtRegistrar;
    @BindView(R.id.logTxtOlvido) TextView TxtOlvido;
    @BindView(R.id.imgFondo) KenBurnsView mImg;
    @BindView(R.id.login_button) LoginButton loginButton;

    String usuarioValidado,contrasenaValidada;
    public static final int MY_PERMISSIONS_REQUEST_UBICACION = 1;
    public static final int MY_PERMISSIONS_REQUEST_ALMACENAMIENTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mCtx = this;

        inicializar();

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult resultadoLogin) {
                presentador.validarCredencialesFB(resultadoLogin.getAccessToken());
            }

            @Override
            public void onCancel() {
                alertas.mensajeInfo("Omitido", "Se canceló el login", mCtx);
            }

            @Override
            public void onError(FacebookException error) {
                alertas.mensajeInfo("Error", error.getMessage().toString(), mCtx);
            }
        });

        TxtIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presentador.validarCredenciales(EdtUsuario.getText().toString(), EdtPass.getText().toString());
            }
        });

        TxtRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarAPantallaRegistro();
            }
        });

        TxtOlvido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarAPantallaRecuperar();
            }
        });
    }

    public void inicializar() {
        barraProgreso = new ProgressDialog(mCtx);
        preferencias = YourPreference.getInstance(mCtx);

        requerrimientosFacebook();

        presentador = new implemPresentLogin(this,new implemInteractLogin(this));

        if(preferencias.sesionIniciada()){
            navegarAPantallaPrincipal();
        }else{
            validarPermisosUbicacion();
        }
    }

    public void requerrimientosFacebook() {
        List<String> permissions = new ArrayList<>();
        permissions.add("public_profile");
        permissions.add("email");
        permissions.add("user_birthday");
        loginButton.setReadPermissions(permissions);

        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void mostrarProgreso() {
        barraProgreso.show();
    }

    @Override
    public void ocultarProgreso() {
        barraProgreso.dismiss();
    }

    @Override
    public void setErrorConexion(String titulo,String respuesta) {
        alertas.mensajeInfo(titulo, respuesta, mCtx);
    }

    @Override
    public void setErrorUsuario() {
        EdtUsuario.setError(getString(R.string.falta_usuario));
    }

    @Override
    public void setErrorPassword() {
        EdtPass.setError(getString(R.string.falta_password));
    }

    @Override
    public void agregarColaConexion(Request<String> respuesta) {
        Singleton.getInstance(mCtx).addToRequestQueue(respuesta);
    }

    @Override
    public void setAlerta(String titulo, String respuesta) {
        alertas.mensajeInfo(titulo,respuesta,mCtx);
    }

    public void setDialogo(String titulo, String respuesta) {
        new LovelyStandardDialog(mCtx)
                .setTopColorRes(R.color.colorPrimary)
                .setButtonsColorRes(R.color.colorAccent)
                .setIcon(R.drawable.ic_enfermera)
                .setTitle("Completado")
                .setMessage(respuesta)
                .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dirigirMenuPrincipal();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public void navegarAPantallaPrincipal() {
        startActivity(new Intent(mCtx,MainActivity.class));
    }

    @Override
    public void navegarAPantallaRegistro() {
        //startActivity(new Intent(mCtx,RegistroActivity.class));
    }

    @Override
    public void navegarAPantallaRecuperar() {
        //startActivity(new Intent(LoginActivity.this,RecuperarActivity.class));
    }

    public void validarPermisosUbicacion(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_UBICACION);
            }
        }
    }

    public void validarPermisosAlmacenamiento(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_ALMACENAMIENTO);

            }
        }
    }

    public void dirigirMenuPrincipal(){
        Intent intent = new Intent(mCtx,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ALMACENAMIENTO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_UBICACION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                validarPermisosAlmacenamiento();
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}