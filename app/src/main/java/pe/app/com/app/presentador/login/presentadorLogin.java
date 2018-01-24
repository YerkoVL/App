package pe.app.com.app.presentador.login;

import com.facebook.AccessToken;

public interface presentadorLogin {
    void validarCredencialesFB(AccessToken acceso);
    void validarCredenciales(String usuario, String password);
    void onDestroy();
}
