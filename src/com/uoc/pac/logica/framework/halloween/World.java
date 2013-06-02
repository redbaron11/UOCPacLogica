package com.uoc.pac.logica.framework.halloween;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.uoc.pac.logica.framework.math.OverlapTester;
import com.uoc.pac.logica.framework.math.Vector2;

/*
 * En esta clase se define todo el mundo y se inicializan los diferentes objetos, se controlan
 * las colisiones y los diferentes elementos.
 */
public class World {
	
	/*
	 * Definimos una interfaz que nos permitira tener m�s limpio el codigo referente a la 
	 * reproduccion de sonidos.
	 */
    public interface WorldListener {
        public void jump();

        public void highJump();

        public void hit();

        public void coin();
    }

    //Definimos el tama�o del mundo, se ha decidido representar el mundo en 10 metros de amplio
    //300 metros de alto.
    public static final float WORLD_WIDTH = 10;
    public static final float WORLD_HEIGHT = 15 * 20;
    //El mundo puede encotrarse en uno de estos tres estados.
    public static final int WORLD_STATE_RUNNING = 0;
    public static final int WORLD_STATE_NEXT_LEVEL = 1;
    public static final int WORLD_STATE_GAME_OVER = 2;
    //Definimos la gravedad que interviene en el mundo.
    public static final Vector2 gravity = new Vector2(0, -12);

    //Definimos todos los objetos que intervendran en nuestro mundo.
    public final Bob bob;
    public final List<Platform> platforms;
    public final List<Spring> springs;
    public final List<Squirrel> squirrels;
    public final List<Coin> coins;
    public Castle castle;
    public final WorldListener listener;
    public final Random rand;

    //Variable que nos permite acumular la altura del mundo que se ha alcanzado
    public float heightSoFar;
    //Nos permite ir acumulando la puntuancion
    public int score;
    //Nos indica el estado en el que nos encontramos.
    public int state;

    public World(WorldListener listener) {
    	//Inicializamos todos los objetos que intervendran en nuestro mundo.
        this.bob = new Bob(5, 1);
        this.platforms = new ArrayList<Platform>();
        this.springs = new ArrayList<Spring>();
        this.squirrels = new ArrayList<Squirrel>();
        this.coins = new ArrayList<Coin>();
        this.listener = listener;
        rand = new Random();
        generateLevel();

        this.heightSoFar = 0;
        this.score = 0;
        this.state = WORLD_STATE_RUNNING;
    }

    /*
     * Generamos un nivel al azar.
     */
	private void generateLevel() {
		
		//Definimos el numero de plataformas, su ubicacion y su tipo al azar. Mientras no supere los limites del mundo.
	    float y = Platform.PLATFORM_HEIGHT / 2;
	    float maxJumpHeight = Bob.BOB_JUMP_VELOCITY * Bob.BOB_JUMP_VELOCITY
	            / (2 * -gravity.y);
	    while (y < WORLD_HEIGHT - WORLD_WIDTH / 2) {
	        int type = rand.nextFloat() > 0.8f ? Platform.PLATFORM_TYPE_MOVING
	                : Platform.PLATFORM_TYPE_STATIC;
	        float x = rand.nextFloat()
	                * (WORLD_WIDTH - Platform.PLATFORM_WIDTH)
	                + Platform.PLATFORM_WIDTH / 2;
	
	        Platform platform = new Platform(type, x, y);
	        platforms.add(platform);
	
	        //Ponemos al azar sobre las plataformas estaticas un propulsor.
	        if (rand.nextFloat() > 0.9f
	                && type != Platform.PLATFORM_TYPE_MOVING) {
	            Spring spring = new Spring(platform.position.x,
	                    platform.position.y + Platform.PLATFORM_HEIGHT / 2
	                            + Spring.SPRING_HEIGHT / 2);
	            springs.add(spring);
	        }
	
	        //Colocamos ardillas voladoras, que pueden matar al personaje principal de forma aleatoria, pero a partir de un 
	        //avance del mundo.
	        if (y > WORLD_HEIGHT / 3 && rand.nextFloat() > 0.8f) {
	            Squirrel squirrel = new Squirrel(platform.position.x
	                    + rand.nextFloat(), platform.position.y
	                    + Squirrel.SQUIRREL_HEIGHT + rand.nextFloat() * 2);
	            squirrels.add(squirrel);
	        }
	
	        //Colocamos de forma aleatoria diferentes calabazas sobre el mundo.
	        if (rand.nextFloat() > 0.6f) {
	            Coin coin = new Coin(platform.position.x + rand.nextFloat(),
	                    platform.position.y + Coin.COIN_HEIGHT
	                            + rand.nextFloat() * 3);
	            coins.add(coin);
	        }
	
	        y += (maxJumpHeight - 0.5f);
	        y -= rand.nextFloat() * (maxJumpHeight / 3);
	    }
	    
	    //Definimos el castillo en la ultima posicion de y.
	    castle = new Castle(WORLD_WIDTH / 2, y);
	}

	/*
	 * Este metodo nos permite ir actualizando el juego. Actualizamos los diferentes objetos, verificamos las colisiones
	 * y si la partida a llegado a su fin.
	 */
public void update(float deltaTime, float accelX) {
    updateBob(deltaTime, accelX);
    updatePlatforms(deltaTime);
    updateSquirrels(deltaTime);
    updateCoins(deltaTime);
    //Si el personaje no esta en el estado golpeado, se validan las colisiones.
    if (bob.state != Bob.BOB_STATE_HIT)
        checkCollisions();
    checkGameOver();
}

/*
 * Actualizamos el estado de nuestro personaje principal. 
 */

private void updateBob(float deltaTime, float accelX) {
	//Comprobamos si se encuentra tocando el suelo. En caso afirmativo lo propulsamos.
    if (bob.state != Bob.BOB_STATE_HIT && bob.position.y <= 0.5f)
        bob.hitPlatform();
    //Si no esta muerto, actualizamos la velocidad horizontal a partir del eje X del acelometro
    if (bob.state != Bob.BOB_STATE_HIT)
        bob.velocity.x = -accelX / 10 * Bob.BOB_MOVE_VELOCITY;
    //Invocamos a la propia funcion de actualizar
    bob.update(deltaTime);
    //Actualizamos la posicion m�s alta alcanzada.
    heightSoFar = Math.max(bob.position.y, heightSoFar);
}

/*
 * Nos recorremos la lista de plataformas y las actualizamos mediante su propia
 * funcion de update().
 * Validamos si una plataforma esta en el estado pulverizar, en ese caso si el tiempo
 * transcurrido es mayor al tiempo estimado de pulverizar eliminamos la plataforma de 
 * la lista.
 */
private void updatePlatforms(float deltaTime) {
    int len = platforms.size();
    for (int i = 0; i < len; i++) {
        Platform platform = platforms.get(i);
        platform.update(deltaTime);
        if (platform.state == Platform.PLATFORM_STATE_PULVERIZING
                && platform.stateTime > Platform.PLATFORM_PULVERIZE_TIME) {
            platforms.remove(platform);
            len = platforms.size();
        }
    }
}

/*
 * Nos recorremos la lista de ardillas y las actualizamos mediante su propio metodo.
 */
private void updateSquirrels(float deltaTime) {
    int len = squirrels.size();
    for (int i = 0; i < len; i++) {
        Squirrel squirrel = squirrels.get(i);
        squirrel.update(deltaTime);
    }
}

/*
 * Nos recorremos la lista de calabazas y las actualizamos mediante su propio metodo.
 */
private void updateCoins(float deltaTime) {
    int len = coins.size();
    for (int i = 0; i < len; i++) {
        Coin coin = coins.get(i);
        coin.update(deltaTime);
    }
}

//Validamos todas las colisiones posibles.
	private void checkCollisions() {
	    checkPlatformCollisions();
	    checkSquirrelCollisions();
	    checkItemCollisions();
	    checkCastleCollisions();
	}
	
	/*
	 * Nos permite controlar la colision de nuestro personaje con todas las
	 * plataformas del juego.
	 * 
	 * Partimos de la premisa que el personaje esta por encima de la plataforma. 
	 * Esto nos permite que el personaje pueda cruzar la plataforma por la parte inferior.
	 */
	private void checkPlatformCollisions() {
	    
		//Si estamos ascendiendo no controlamos las colisiones con las plataformas
		if (bob.velocity.y > 0)
	        return;
	
	    int len = platforms.size();
	    for (int i = 0; i < len; i++) {
	        Platform platform = platforms.get(i);
	        //Comprobamos si el personaje esta por encima de la plataforma.
	        if (bob.position.y > platform.position.y) {
	        	//Comprobamos si hay colision
	            if (OverlapTester
	                    .overlapRectangles(bob.bounds, platform.bounds)) {
	                //Actualizamos los estados.
	            	bob.hitPlatform();
	                listener.jump();
	                //De forma aleatoria decidimos si la plataforma se destruira o no.
	                if (rand.nextFloat() > 0.5f) {
	                    platform.pulverize();
	                }
	                break;
	            }
	        }
	    }
	}
	
	/*
	 * Nos permite controlar la colision de nuestro personaje con todas las
	 * ardillas del juego.
	 */
	private void checkSquirrelCollisions() {
	    int len = squirrels.size();
	    for (int i = 0; i < len; i++) {
	        Squirrel squirrel = squirrels.get(i);
	        //Validamos si existe colision
	        if(!Settings.inmortal){
	        	if (OverlapTester.overlapRectangles(squirrel.bounds, bob.bounds)) {
	        		//Actualizamos estados.
	            	bob.hitSquirrel();
	            	listener.hit();
	        	}
	        }
	    }
	}
	
	/*
	 * Nos permite controlar las colisiones de nuestro personaje con todas los demas items
	 * en este caso las calabazas y los prepulsores del juego.
	 */
	private void checkItemCollisions() {
	    int len = coins.size();
	    for (int i = 0; i < len; i++) {
	        Coin coin = coins.get(i);
	        //Comprobamos si hay colision
	        if (OverlapTester.overlapRectangles(bob.bounds, coin.bounds)) {
	        	//Eliminamos la calabaza recogida
	            coins.remove(coin);
	            //Actualizamos lista de calabazas
	            len = coins.size();
	            //reproducimos ruido.
	            listener.coin();
	            //actualizamos puntuacion actual.
	            score += Coin.COIN_SCORE;
	        }
	
	    }
	    
	    //Si estamos ascendiendo no validamos la colision con los propulsores.
	    if (bob.velocity.y > 0)
	        return;
	
	    
	    len = springs.size();
	    for (int i = 0; i < len; i++) {
	        Spring spring = springs.get(i);
	        //Si el personaje esta por encima del propulsor
	        if (bob.position.y > spring.position.y) {
	        	//Si se produce una colision
	            if (OverlapTester.overlapRectangles(bob.bounds, spring.bounds)) {
	            	//actualizamos estados.
	                bob.hitSpring();
	                listener.highJump();
	            }
	        }
	    }
	}
	
	/*
	 * Comprobamos si el personaje principal colisiona con la calabaza definitiva,
	 * en caso afirmativo actualizamos el estado del mundo "Next Level". 
	 */
	private void checkCastleCollisions() {
	    if (OverlapTester.overlapRectangles(castle.bounds, bob.bounds)) {
	        state = WORLD_STATE_NEXT_LEVEL;
	    }
	}

	/*
	 * Comprobamos si el personaje principal esta por debajo de la altura maxima
	 * alcanzada menos la mitad de la pantalla, en caso afirmativo se termina el 
	 * juego "Game Over". 
	 */
    private void checkGameOver() {
        if (heightSoFar - 7.5f > bob.position.y) {
            state = WORLD_STATE_GAME_OVER;
        }
    }
}