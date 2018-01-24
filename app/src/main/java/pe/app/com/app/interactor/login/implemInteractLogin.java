package pe.app.com.app.interactor.login;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import pe.app.com.app.datos.herramientas.YourPreference;
import pe.app.com.app.datos.herramientas.clasesGenericas;
import pe.app.com.app.datos.modelos.Respuesta;
import pe.app.com.app.datos.modelos.Usuario;
import pe.app.com.app.presentador.login.presentadorLogin;
import pe.app.com.app.vista.vistaLogin;

import static pe.app.com.app.datos.herramientas.GenericStrings.CONTIN;
import static pe.app.com.app.datos.herramientas.GenericStrings.INICIAL;
import static pe.app.com.app.datos.herramientas.GenericStrings.LOGIN_PARAM_PASS;
import static pe.app.com.app.datos.herramientas.GenericStrings.LOGIN_PARAM_USUARIO;
import static pe.app.com.app.datos.herramientas.GenericStrings.PARAM_APELLIDOS;
import static pe.app.com.app.datos.herramientas.GenericStrings.PARAM_CORREO;
import static pe.app.com.app.datos.herramientas.GenericStrings.PARAM_LATITUD;
import static pe.app.com.app.datos.herramientas.GenericStrings.PARAM_LONGITUD;
import static pe.app.com.app.datos.herramientas.GenericStrings.PARAM_NOMBRES;
import static pe.app.com.app.datos.herramientas.GenericStrings.PARAM_PASS;
import static pe.app.com.app.datos.herramientas.GenericStrings.PARAM_TELEFONO;
import static pe.app.com.app.datos.herramientas.GenericStrings.URL_LOGIN;
import static pe.app.com.app.datos.herramientas.GenericStrings.URL_REGISTRO;

public class implemInteractLogin implements interactorLogin {
    Usuario usuario;
    private presentadorLogin loginPresentador;
    private vistaLogin loginVista;
    private YourPreference preferencias;

    FirebaseAuth autorizacion;
    FirebaseDatabase db;
    DatabaseReference usuarios;

    Gson gson = new Gson();
    clasesGenericas genericos = new clasesGenericas();

    public implemInteractLogin(vistaLogin loginVista){
        this.loginVista = loginVista;
    }

    @Override
    public void loginObtenerDatosFB(AccessToken accessToken) {
        obtenerDatos(accessToken);
    }

    public void loginLogeoUsuario(final String usuario, final String password, final OnFinalLoginListener listener){
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                if (TextUtils.isEmpty(usuario)) {
                    listener.onErrorUsuario();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    listener.onErrorPassword();
                    return;
                }
                listener.onSuccess();
            }
        }, 2000);

        validarDatos(usuario,password);
    }

    public void obtenerDatos(AccessToken token){
        GraphRequest fb = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback(){

            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Profile profile = Profile.getCurrentProfile();
                usuario = new Usuario();
                usuario.setId(profile.getId().toString());
                usuario.setNombres(genericos.validarEspacios(profile.getName()));
                //String pruebaNombre = profile.getMiddleName();
                usuario.setNombreUsuario(genericos.validarEspacios(genericos.validarEspacios(profile.getFirstName())));
                usuario.setPassword(genericos.validarOtro((profile.getFirstName() + "_" + profile.getLastName())));
                usuario.setApellidos(genericos.validarEspacios((profile.getLastName())));
                usuario.setImagen(profile.getProfilePictureUri(100,100).toString());
                usuario.setPerfil("CLIENTE");
                try {
                    usuario.setCorreo(object.getString("email"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    usuario.setDireccion(object.getString("address"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    String pruebaHometown = object.getString("hometown");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    usuario.setImagen(object.getString("picture"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    usuario.setTelefono(Integer.parseInt(object.getString("user_mobile_phone")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                validarExistenciaFb(usuario);
            }
        });
        Bundle adicional = new Bundle();
        adicional.putString("fields","email, gender, address, hometown, location, short_name, picture");
        fb.setParameters(adicional);
        fb.executeAsync();
    }

    public void validarExistenciaFb(final Usuario user){

        String URL_LOG = URL_LOGIN + INICIAL + LOGIN_PARAM_USUARIO + user.getCorreo()
                + CONTIN + LOGIN_PARAM_PASS + user.getPassword();

        loginVista.mostrarProgreso();

        StringRequest respuestaLogin = new StringRequest(Request.Method.GET,URL_LOG,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String Response) {

                        if(Response!=null) {
                            try {
                                Usuario usuario = gson.fromJson(Response, Usuario.class);
                                if(usuario.getNombres()!=null) {
                                    preferencias.saveUsuario(usuario);
                                    loginVista.ocultarProgreso();
                                    loginVista.navegarAPantallaPrincipal();
                                }else{
                                    Respuesta respuesta = gson.fromJson(Response,Respuesta.class);
                                    if(respuesta.getMensaje().equals("Usuario no ha activado su cuenta")) {
                                        LoginManager.getInstance().logOut();
                                        loginVista.ocultarProgreso();
                                        loginVista.setAlerta("Error",respuesta.getMensaje());
                                    }else {
                                        loginVista.ocultarProgreso();
                                        registrarUsuarioFb(user);
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                loginVista.ocultarProgreso();
                                registrarUsuarioFb(user);
                            }
                        }else{
                            Respuesta respuesta = gson.fromJson(Response,Respuesta.class);
                            loginVista.ocultarProgreso();
                            registrarUsuarioFb(user);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                e.printStackTrace();
                loginVista.ocultarProgreso();
                registrarUsuarioFb(user);
            }
        });

        respuestaLogin.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        loginVista.agregarColaConexion(respuestaLogin);
    }

    public void validarDatos(final String correo,final String password){

        String URL_LOG = URL_LOGIN + INICIAL + LOGIN_PARAM_USUARIO + correo
                + CONTIN + LOGIN_PARAM_PASS + password;

        loginVista.mostrarProgreso();

        StringRequest respuestaLogin = new StringRequest(Request.Method.GET,URL_LOG,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String Response) {

                        if(Response!=null) {
                            try {
                                Usuario usuario = gson.fromJson(Response, Usuario.class);
                                if(usuario.getNombres()!=null) {
                                    preferencias.saveUsuario(usuario);
                                    loginVista.ocultarProgreso();
                                    loginVista.navegarAPantallaPrincipal();
                                }else{
                                    Respuesta respuesta = gson.fromJson(Response,Respuesta.class);
                                    loginVista.setAlerta("Error",respuesta.getMensaje());
                                    loginVista.ocultarProgreso();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                loginVista.setAlerta("Error","None");
                                loginVista.ocultarProgreso();
                            }
                        }else{
                            Respuesta respuesta = gson.fromJson(Response,Respuesta.class);
                            loginVista.setAlerta("Error",respuesta.getMensaje());
                            loginVista.ocultarProgreso();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                e.printStackTrace();
                loginVista.ocultarProgreso();
                loginVista.setAlerta("Error","Error desconocido");
            }
        });

        respuestaLogin.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        loginVista.agregarColaConexion(respuestaLogin);
    }

    public void validarFirebase(final String correo, final String password){
        autorizacion = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        usuarios = db.getReference("Usuarios");

        autorizacion.signInWithEmailAndPassword(correo,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                loginVista.navegarAPantallaPrincipal();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loginVista.setAlerta("Error", e.getMessage());
            }
        });
    }

    public void registrarUsuarioFb(Usuario usuario){
        String URL_REG = URL_REGISTRO + INICIAL
                + PARAM_NOMBRES + usuario.getNombreUsuario()
                + CONTIN + PARAM_APELLIDOS + usuario.getApellidos()
                + CONTIN + PARAM_CORREO + usuario.getCorreo()
                + CONTIN + PARAM_TELEFONO + usuario.getTelefono()
                + CONTIN + PARAM_PASS + usuario.getPassword()
                + CONTIN + PARAM_LONGITUD + 0
                + CONTIN + PARAM_LATITUD + 0;

        //Nombres=Pepe&Apellidos=Vasquez&Correo=micorreo@gmail.com&Telefono=12345678&Contrasena=654321&Longitud=0&Latitud=0

        loginVista.mostrarProgreso();

        StringRequest respuestaRegistroXfb = new StringRequest(Request.Method.GET,URL_REG,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String Response) {

                        if(Response!=null) {
                            try {
                                Respuesta respuesta = gson.fromJson(Response, Respuesta.class);
                                if(respuesta.getValor()==true) {
                                    loginVista.ocultarProgreso();

                                }else{
                                    loginVista.setAlerta("Error",respuesta.getMensaje());
                                    LoginManager.getInstance().logOut();
                                    loginVista.ocultarProgreso();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                loginVista.setAlerta("Error","None");
                                loginVista.ocultarProgreso();
                                LoginManager.getInstance().logOut();
                            }
                        }else{
                            LoginManager.getInstance().logOut();
                            Respuesta respuesta = gson.fromJson(Response,Respuesta.class);
                            loginVista.setAlerta("Error",respuesta.getMensaje());
                            loginVista.ocultarProgreso();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                e.printStackTrace();
                LoginManager.getInstance().logOut();
                loginVista.ocultarProgreso();
                loginVista.setAlerta("Error","Error Desconocido");
            }
        });

        respuestaRegistroXfb.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        loginVista.agregarColaConexion(respuestaRegistroXfb);
    }
}
