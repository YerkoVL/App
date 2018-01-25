package pe.app.com.app.presentador.login;

import com.facebook.AccessToken;

import pe.app.com.app.interactor.login.implemInteractLogin;
import pe.app.com.app.interactor.login.interactorLogin;
import pe.app.com.app.vista.vistaLogin;

public class implemPresentLogin implements presentadorLogin, interactorLogin.OnFinalLoginListener {

    private vistaLogin loginVista;
    private interactorLogin loginInteractor;

    public implemPresentLogin(vistaLogin loginVista, implemInteractLogin loginInteractor){
        this.loginVista = loginVista;
        this.loginInteractor = loginInteractor;
    }

    @Override
    public void validarCredencialesFB(AccessToken acceso) {
        if(loginVista != null){
            loginVista.mostrarProgreso();
        }
        loginInteractor.loginObtenerDatosFB(acceso);
    }

    @Override
    public void validarCredenciales(String usuario, String password) {
        if(loginVista != null){
            loginInteractor.loginLogeoUsuario(usuario,password,this);
        }
    }

    @Override
    public void onDestroy() {
        loginVista = null;
    }

    @Override
    public void onErrorUsuario() {
        if(loginVista != null){
            loginVista.ocultarProgreso();
            loginVista.setErrorUsuario();
        }
    }

    @Override
    public void onErrorPassword() {
        if(loginVista != null){
            loginVista.ocultarProgreso();
            loginVista.setErrorPassword();
        }
    }

    @Override
    public void onSuccess() {
        if(loginVista != null){
            loginVista.navegarAPantallaPrincipal();
        }
    }
}
