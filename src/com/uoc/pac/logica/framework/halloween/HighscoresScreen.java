package com.uoc.pac.logica.framework.halloween;

import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import com.uoc.pac.logica.framework.Game;
import com.uoc.pac.logica.framework.Input.TouchEvent;
import com.uoc.pac.logica.framework.gl.Camera2D;
import com.uoc.pac.logica.framework.gl.SpriteBatcher;
import com.uoc.pac.logica.framework.impl.GLState;
import com.uoc.pac.logica.framework.math.OverlapTester;
import com.uoc.pac.logica.framework.math.Rectangle;
import com.uoc.pac.logica.framework.math.Vector2;

/*
 * En esta pantalla (estado) simplemente se representan las 5 puntuaciones
 * m�s altas que se han obtenido. 
 * Las puntuaciones son recojidas del fichero de configuraci�n. 
 * 
 * Tarea: 
 *  -> Incluir aqu� el WS para obtener las puntuaciones
 *  
 *  Son represnetadas por pantalla mediante la clase Font.
 */
public class HighscoresScreen extends GLState {
    Camera2D guiCam;
    SpriteBatcher batcher;
    Rectangle backBounds;
    Vector2 touchPoint;
    String[] highScores; 
    
    // xOffset nos permitira calcular la presentacion de cada linea, 
    // para que esten centradas horizontalmente.
    float xOffset = 0;
    
    public HighscoresScreen(Game game) {
        super(game);
        
        guiCam = new Camera2D(glGraphics, 320, 480);
        backBounds = new Rectangle(0, 0, 64, 64);
        touchPoint = new Vector2();
        batcher = new SpriteBatcher(glGraphics, 100);
        highScores = new String[5]; 
        //Lo hacemos a partir de la linea mas grande.
        for(int i = 0; i < 5; i++) {
            highScores[i] = (i + 1) + ". " + Settings.highscores[i];
            xOffset = Math.max(highScores[i].length() * Assets.font.glyphWidth, xOffset);
        }       
        // "Assets.font.glyphWidth / 2" esto es necesario porque la clase Font utiliza el centro, en lugar de las esquinas.
        xOffset = 160 - xOffset / 2 + Assets.font.glyphWidth / 2;
    }    

    @Override
    public void update(float deltaTime) {
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        game.getInput().getKeyEvents();
        int len = touchEvents.size();
        for(int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            touchPoint.set(event.x, event.y);
            guiCam.touchToWorld(touchPoint);
            
            /*
             * Verificamos si se ha pulsado la flecha de retorno
             * y volvemos a la pantalla de menu principal.
             */
            if(event.type == TouchEvent.TOUCH_UP) {
                if(OverlapTester.pointInRectangle(backBounds, touchPoint)) {
                    Assets.playSound(Assets.clickSound);
                    game.setScreen(new MainMenuScreen(game));
                    return;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.uoc.pac.logica.framework.State#present(float)
     * 
     * Presentamos las puntuaciones por pantalla.
     */
    @Override
    public void present(float deltaTime) {
        GL10 gl = glGraphics.getGL();        
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        guiCam.setViewportAndMatrices();
        
        gl.glEnable(GL10.GL_TEXTURE_2D);
        
        batcher.beginBatch(Assets.background);
        batcher.drawSprite(160, 240, 320, 480, Assets.backgroundRegion);
        batcher.endBatch();
        
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        
        batcher.beginBatch(Assets.items);
        batcher.drawSprite(160, 360, 300, 33, Assets.highScoresRegion);
        
        float y = 240;
        for(int i = 4; i >= 0; i--) {
            Assets.font.drawText(batcher, highScores[i], xOffset, y);
            y += Assets.font.glyphHeight;
        }
        
        batcher.drawSprite(32, 32, 64, 64, Assets.arrow);
        batcher.endBatch();
        
        gl.glDisable(GL10.GL_BLEND);
    }
    
    @Override
    public void resume() {        
    }
    
    @Override
    public void pause() {        
    }

    @Override
    public void dispose() {
    }
}
