package pe.app.com.app.presentador.menuPrincipal;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;

import pe.app.com.app.R;
import pe.app.com.app.datos.menuDeslizable.ResideMenu;
import pe.app.com.app.datos.menuDeslizable.ResideMenuItem;

public class implemPresentadorMenuPrincipal implements presentadorMenuPrincipal {

    Context contexto;
    Activity actividad;
    ResideMenu resideMenu = null;
    ResideMenuItem item1, item2, item3;

    public implemPresentadorMenuPrincipal(Context contexto, Activity actividad){
        this.contexto = contexto;
        this.actividad = actividad;
    }

    @Override
    public void iniciarMenu() {
        resideMenu = new ResideMenu(contexto, R.layout.header_menu,-1);
        resideMenu.setUse3D(true);
        resideMenu.setBackground(R.drawable.img_fondo);
        resideMenu.attachToActivity(actividad);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
        resideMenu.setScaleValue(0.6f);

        iniciarElementos();
    }

    public void iniciarElementos() {
        item1 = new ResideMenuItem(contexto, R.drawable.ic_launcher_background, "Item N1");
        item2 = new ResideMenuItem(contexto, R.drawable.ic_launcher_background, "Item N2");
        item3 = new ResideMenuItem(contexto, R.drawable.ic_launcher_background, "Item N3");

        asignarPosicion();
    }

    public void asignarPosicion(){
        resideMenu.addMenuItem(item1, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(item2, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(item3, ResideMenu.DIRECTION_LEFT);
    }

    public boolean envioEvento(MotionEvent evento){
        return resideMenu.dispatchTouchEvent(evento);
    }
}
