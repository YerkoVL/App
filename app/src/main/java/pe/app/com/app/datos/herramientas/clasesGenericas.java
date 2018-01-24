package pe.app.com.app.datos.herramientas;

public class clasesGenericas {
    public String validarEspacios(String texto){
        String nuevoTexto = "";
        nuevoTexto = texto.replace(" ", "%25");
        return nuevoTexto;
    }

    public String validarOtro(String texto){
        String nuevoTexto = "";
        nuevoTexto = texto.replace(" ", "_");
        return nuevoTexto;
    }
}
