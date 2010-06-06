package processing.app;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Shot represents a single shot that occurs when a player presses the space button. Calculates
 * new points and can optionally display them, if we want (which we do, in the server that shows
 * the positions of all clients including their shots.
 * 
 * <p>It should be noted that although they look similar, <code>Shot</code> in 
 * <code>ServerObject</code> and <code>Shot</code> in <code>ClientObject</code> are actually rather 
 * different, as only ServerObject includes code to calculate displacements, positions, etc - 
 * <code>ClientObject</code> only allows drawing</p> 
 * 
 * @author Chris Granville <v2t04 at students.keele.ac.uk>
 * @version 3.0f (12/05/2010)
 * @copyright 2009 - 2010 Chris Granville. All Rights Reserved.
 * @see <a href="http://goo.gl/VOIb">Fireball: Multiplayer Asteroids</a>
 */

@SuppressWarnings("static-access")

public class Shot extends ServerObject {
	
	/**
	 * Radius of the shot
	 */
	private int SHOT_SIZE = 5;
	
	/**
	 * The number of x pixels to add to each shot per frame
	 */
	private float dx;
	
	/**
	 * The number of y pixels to add to each shot per frame
	 */
	private float dy;
	
	/**
	 * The velocity/speed of the Shot
	 */
	private float velocity;
	
	/**
	 * How long the Shot lasts for in frames
	 */
	private float lifetime;
	
	/**
	 * The first frame that the Shot appears on
	 */
	private int start_frame;
	
	/**
	 * Unique ID of the Shot
	 */
	private int id;
	
	/**
	 * Constructor for the Shot class
	 * @param parent the parent PApplet to draw to
	 * @param x starting x position (x position of the parent Ship)
	 * @param y starting y position (y position of the parent Ship)
	 * @param angle the angle of the parent Ship
	 */
	Shot(PApplet parent, float x, float y, float angle)
	{
		super(parent, x, y);
		this.angle = parent.round(angle);
		this.start_frame = parent.frameCount;
		force_destruction = false;
		lifetime = 0.8f * parent.frameRate;
		velocity = 24;
		id = (int) parent.random(1, 127);
		
		calculateVectors();
	}
	
	/**
	 * Update the <code>displacement</code> PVector as well as the associated <code>Shot</code> if there is one
	 */
	public void move() {
		if(!toDestroy()) position.add(displacement);
	}

	/**
	 * Draw the <code>Shot</code> at [position.x, position.y]
	 * @see ServerObject#draw()
	 */
	void draw() {
		if(!toDestroy())
		{
			parent.fill(255);
			parent.stroke(255);
			parent.strokeWeight(5);
			parent.ellipse(position.x, position.y, SHOT_SIZE, SHOT_SIZE);
			parent.strokeWeight(1);
		}
	}
	
	/**
	 * Calculate the dx and dy values to add to displacement each frame
	 */
	private void calculateVectors()
	{
		// Calculate the dx and dy to add to the <code>position</code> every frame
		dx = velocity * parent.cos(parent.radians(angle));
		dy = velocity * parent.sin(parent.radians(angle));
		displacement = new PVector(dx, dy);
	}

	
	/**
	 * Get the radius of the Shot
	 * @see ServerObject#getEffectiveRadius()
	 */
	public int getEffectiveRadius() {
		return SHOT_SIZE;
	}
	
	/**
	 * Destroy the Shot
	 */
	public void destroy()
	{
		force_destruction = true;
	}
	
	
	/**
	 * Should the Shot be destroyed - ie, expired past the lifetime
	 * @return a boolean indicating if the Shot should be destroyed
	 * @see ServerObject#toDestroy()
	 */
	public boolean toDestroy()
	{	
		// HACK: For some reason it insists in using a local parent instead of the superclasses. Stupid Processing/Java!
		if(parent.frameCount >= start_frame + lifetime || force_destruction == true) return true;
		else return false;
	}
	
	/**
	 * Get the unique ID for this Shot
	 * @return unique ID of the shot
	 */
	public int getId() {
		return id;
	}
}
