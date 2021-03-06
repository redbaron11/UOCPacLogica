package com.uoc.pac.logica.framework.halloween;

import java.util.List;

import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

import com.uoc.pac.logica.framework.Game;
import com.uoc.pac.logica.framework.Input.TouchEvent;
import com.uoc.pac.logica.framework.gl.Camera2D;
import com.uoc.pac.logica.framework.gl.SpriteBatcher;
import com.uoc.pac.logica.framework.impl.GLState;
import com.uoc.pac.logica.framework.math.Rectangle;
import com.uoc.pac.logica.framework.math.Vector2;

/*
 * Esta es la clase que represneta la pantalla devuelta por getStartScreen().
 * En esta pantalla se visualiza simplemente el logotipo de la UOC
 * 
 * Actualmente hay que puslar para proceder al siguiente estado.
 * 
 * Cambios a realizar:
 * 	-> Visualizaci�n de un video
 *  -> Cambio de estado al finalizar el video o en X segundos.
 */

public class VideoPresentationScreen extends GLState {
    Camera2D guiCam;
    SpriteBatcher batcher;
    Rectangle soundBounds;
    Rectangle playBounds;
    Rectangle highscoresBounds;
    Rectangle helpBounds;
    Vector2 touchPoint;
    float stateTime;

    public VideoPresentationScreen(Game game) {
        super(game);
        guiCam = new Camera2D(glGraphics, 320, 480);
        batcher = new SpriteBatcher(glGraphics, 100);
        touchPoint = new Vector2();  
        stateTime = 0;
        
    }       

    @Override
    public void update(float deltaTime) {
    	stateTime += deltaTime;
  
    	Log.d("timeInWindows: ", "" + stateTime);
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        game.getInput().getKeyEvents();
        int len = touchEvents.size();
        for(int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);                        
            if(event.type == TouchEvent.TOUCH_UP) {
                touchPoint.set(event.x, event.y);
                //procedemos a la siguiente pantalla (estado) Menu principal
                game.setScreen(new MainMenuScreen(game));
                return;
            }
        }
        if(stateTime > 10){
        	//renderSecondLogo();
        }
        if(stateTime > 20){
        	//Si superamos un determinado tiempo pasamos a la siguiente pantalla
            game.setScreen(new MainMenuScreen(game));
            return;       	
        }
    }

    @Override
    public void present(float deltaTime) {
        GL10 gl = glGraphics.getGL();  
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        guiCam.setViewportAndMatrices();
 
        gl.glEnable(GL10.GL_TEXTURE_2D);
        batcher.beginBatch(Assets.logo1);                  
        batcher.drawSprite(150, 200, 241, 179, Assets.logo1Region);
        batcher.endBatch();

    }
    public void renderSecondLogo(){
        GL10 gl = glGraphics.getGL(); 
        gl.glClearColor(0,0,0,0); /* El color de limpiado ser� cero */ 
        gl.glClearDepthf(0); 

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glClear(GL10.GL_DEPTH_BUFFER_BIT);
        guiCam.setViewportAndMatrices();
        gl.glEnable(GL10.GL_TEXTURE_2D);
        batcher.beginBatch(Assets.logo2);                  
        batcher.drawSprite(150, 200, 241,75, Assets.logo2Region);
        batcher.endBatch();
    }
    
    @Override
    public void pause() {        
        Settings.save(game.getFileIO());
    }

    @Override
    public void resume() {        
    }       

    @Override
    public void dispose() {        
    }
}
