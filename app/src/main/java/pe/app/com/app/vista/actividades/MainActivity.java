package pe.app.com.app.vista.actividades;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import pe.app.com.app.R;
import pe.app.com.app.presentador.menuPrincipal.implemPresentadorMenuPrincipal;
import pe.app.com.app.presentador.menuPrincipal.presentadorMenuPrincipal;

public class MainActivity extends AppCompatActivity {

    private presentadorMenuPrincipal menuPrincipalVista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializar();
    }

    public void inicializar(){
        menuPrincipalVista = new implemPresentadorMenuPrincipal(this,this);

        menuPrincipalVista.iniciarMenu();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return menuPrincipalVista.envioEvento(ev);
    }
}
