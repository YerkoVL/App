package pe.app.com.app.interactor.login;

import com.facebook.AccessToken;

public interface interactorLogin {
    interface OnFinalLoginListener {
        void onErrorUsuario();

        void onErrorPassword();

        void onSuccess();
    }

    void loginObtenerDatosFB(AccessToken acceso);
    void loginLogeoUsuario(final String usuario, final String password, final OnFinalLoginListener listener);
}
