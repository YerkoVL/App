package pe.app.com.app.vista.actividades;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import pe.app.com.app.R;
import pe.app.com.app.datos.modelos.Usuario;

import static pe.app.com.app.datos.herramientas.GenericStrings.GENERAL_ESTADO_HABILITADO;
import static pe.app.com.app.datos.herramientas.GenericStrings.GENERAL_PERFIL_EMPLEADOS;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth autorizacion;
    FirebaseDatabase db;
    DatabaseReference usuarios;
    Context ctx;
    LocationManager adminLocalizacion;

    @BindView(R.id.regEdtCorreo)
    EditText correo;
    @BindView(R.id.regEdtNombres)
    EditText nombres;
    @BindView(R.id.regEdtApellidos)
    EditText apellidos;
    @BindView(R.id.regEdtCelular)
    EditText celular;
    @BindView(R.id.regEdtPass)
    EditText password;
    @BindView(R.id.regEdtRepPass)
    EditText repPassword;
    @BindView(R.id.regEdtBtnRegistrar)
    TextView registrar;

    String valCorreo, valNombres, valApellidos, valPassword, valRepPassword;
    int valCelular;
    double latitud, longitud;
    boolean bandera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        adminLocalizacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obtenerTextos();
                validarTextos();
                if (bandera) {
                    if (verificarUbicacion()) {
                        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        adminLocalizacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2 * 20 * 1000, 10, escuchadorLocalizacion);
                        //Toast.makeText(ctx,"Debe habilitar su GPS",Toast.LENGTH_SHORT).show();
                    }
                    autorizacion.createUserWithEmailAndPassword(valCorreo, valPassword)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult resultadoAutorizacion) {
                                    Usuario usuario = new Usuario();
                                    usuario.setNombres(valNombres);
                                    usuario.setApellidos(valApellidos);
                                    usuario.setTelefono(valCelular);
                                    usuario.setPassword(valPassword);
                                    usuario.setLatitud(String.valueOf(latitud));
                                    usuario.setLongitud(String.valueOf(longitud));
                                    usuario.setIdEstado(GENERAL_ESTADO_HABILITADO);
                                    usuario.setIdPerfil(GENERAL_PERFIL_EMPLEADOS);

                                    usuarios.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(usuario).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(ctx, getResources().getString(R.string.regCompleto), Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ctx, getResources().getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ctx, getResources().getString(R.string.errorInicio) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        inicializar();
    }

    private void inicializar() {
        ctx = this;
        autorizacion = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        usuarios = db.getReference("Usuarios");
    }

    private void obtenerTextos() {
        valCorreo = correo.getText().toString();
        valNombres = nombres.getText().toString();
        valApellidos = apellidos.getText().toString();
        valPassword = password.getText().toString();
        valRepPassword = repPassword.getText().toString();
        valCelular = Integer.valueOf(celular.getText().toString());
    }

    private void validarTextos() {
        if (TextUtils.isEmpty(valCorreo)) {
            Toast.makeText(ctx, "Ingrese correo", Toast.LENGTH_SHORT).show();
            bandera = false;
            return;
        }
        if (TextUtils.isEmpty(valNombres)) {
            Toast.makeText(ctx, "Ingrese nombres", Toast.LENGTH_SHORT).show();
            bandera = false;
            return;
        }
        if (TextUtils.isEmpty(valApellidos)) {
            Toast.makeText(ctx, "Ingrese apellidos", Toast.LENGTH_SHORT).show();
            bandera = false;
            return;
        }
        if (TextUtils.isEmpty(String.valueOf(valCelular))) {
            Toast.makeText(ctx, "Ingrese celular", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(valPassword)) {
            Toast.makeText(ctx, "Ingrese contraseña", Toast.LENGTH_SHORT).show();
            bandera = false;
            return;
        }
        if (!valPassword.equals(valRepPassword)) {
            Toast.makeText(ctx, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            bandera = false;
            return;
        }
    }

    private final LocationListener escuchadorLocalizacion = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            longitud = location.getLongitude();
            latitud = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private boolean verificarUbicacion() {
        if (!estaHabilitadaLocalizacion())
            mostrarAlerta();
            return estaHabilitadaLocalizacion();
    }

    private boolean estaHabilitadaLocalizacion() {
        return adminLocalizacion.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                adminLocalizacion.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void mostrarAlerta() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Habilitar GPS")
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación " +
                        "usa esta app")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }
}
