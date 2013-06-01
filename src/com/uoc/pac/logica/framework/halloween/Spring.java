package com.uoc.pac.logica.framework.halloween;

import com.uoc.pac.logica.framework.GameObject;

/*
 * Este objeto nos permite impulsar al personaje, como es un objeto estatico
 * extiende de GameObject y solo tenemos que determinar la posicion y el tama�o.
 */

public class Spring extends GameObject {
    public static float SPRING_WIDTH = 0.3f;
    public static float SPRING_HEIGHT = 0.3f;
    
    public Spring(float x, float y) {
        super(x, y, SPRING_WIDTH, SPRING_HEIGHT);
    }        
}
