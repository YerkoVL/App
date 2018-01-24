package pe.app.com.app.vista;

import com.android.volley.Request;

public interface vistaLogin {
    void mostrarProgreso();
    void ocultarProgreso();
    void setErrorConexion(String titulo, String respuesta);
    void setErrorUsuario();
    void setErrorPassword();
    void agregarColaConexion(Request<String> respuesta);
    void setAlerta(String titulo, String respuesta);
    void navegarAPantallaPrincipal();
    void navegarAPantallaRegistro();
    void navegarAPantallaRecuperar();
}
