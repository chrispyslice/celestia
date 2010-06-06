package processing.app;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * ServerObject represents any object that you see on the screen (be it a Ship or a Shot).
 * Using polymorphism allows us to have better quality and reduce the complexity of the code.
 * Allows Ships and Shots to inherit some common functionality, such as collision detection
 * and the same basic position + displacement = new position structure.
 * 
 * <p>It should be noted that although they look similar, <code>ServerObject</code> and
 * <code>ClientObject</code> are actually rather different, as only ServerObject includes code
 * to calculate displacements, positions, etc - <code>ClientObject</code> only allows drawing.</p> 
 * 
 * @author Chris Granville <v2t04 at students.keele.ac.uk>
 * @version 3.0f (12/05/2010)
 * @copyright 2009 - 2010 Chris Granville. All Rights Reserved.
 * @see <a href="http://goo.gl/VOIb">Fireball: Multiplayer Asteroids</a>
 */

@SuppressWarnings("static-access")

public abstract class ServerObject
{
	/**
	 * Angle of this object in degrees
	 */
	protected int angle;
	
	/**
	 * Do we need to force destruction in this frame
	 */
	protected boolean force_destruction;
	
	/**
	 * The current position of this object
	 */
	protected PVector position;
	
	/**
	 * The displacement to be added onto the ServerObject and the next draw
	 */
	protected PVector displacement;
	
	/**
	 * Parent PApplet. Need this to use Processing functions like sin(), cos() and round()
	 */
	protected PApplet parent;
	
	/**
	 * Constructor for ServerObject
	 * @param parent the parent PApplet to draw to, use functions from, etc
	 * @param x the starting x position
	 * @param y the starting y position
	 */
	public ServerObject(PApplet parent, float x, float y)
	{
		position = new PVector(x, y);
		displacement = new PVector();
		this.parent = parent;
	}
	
	/**
	 * Draw the ServerObject at [position.x, position.y]
	 */
	abstract void draw();
	
	/**
	 * Move the ServerObject by adding <code>displacement</code> to <code>position</code>
	 * Would like to code this here, but Ship needs to do some additional stuff that I
	 * don't want to put in another method if I can help it, so I'm making it abstract
	 */
	abstract void move();
	
	/**
	 * Is the object to be destroyed in this frame? For example, has the shot now expired it's lifetime
	 * or is a Ship dead.
	 * @return boolean indicating whether the ServerObject should be removed at the next draw
	 */
	abstract boolean toDestroy();

	/**
	 * Get the radius of the ServerObject. Used for collision detection
	 * @return integer signifying the radius of the ServerObject
	 */
	abstract int getEffectiveRadius();

	/**
	 * Get the current X position from <code>position.x</code> and round it so it's not a float
	 * without truncating it
	 * @return an integer signifying the nearest x pixel coordinate
	 */
	public int getXPosition()
	{
		return parent.round(position.x);
	}
	
	/**
	 * Get the current Y position from <code>position.y</code> and round it so it's not a float
	 * without truncating it
	 * @return integer signifying the nearest y pixel coordinate
	 */
	public int getYPosition()
	{
		return parent.round(position.y);
	}
	
	/**
	 * Get the current rotation angle in degrees of the ServerObject
	 * @return integer signifying the angle (in degrees) of the ServerObject
	 */
	public int getAngle()
	{
		return angle;
	}

	/**
	 * A broad-phase collision detection test. We only need to do a BP test as all the
	 * objects are circular, which is handy (and unintentional!)
	 * @param so the ServerObject to test against
	 * @return a boolean indicating whether the ServerObject is colliding with <code>so</code>
	 */
	public boolean collidingWith(ServerObject so)
	{
		int radius = so.getEffectiveRadius();
		int so_x, so_y, this_x, this_y;
		float dx, dy, distance, radii;
		this_x = parent.round(position.x);
		this_y = parent.round(position.y);
		so_x = so.getXPosition();
		so_y = so.getYPosition();
		dx = so_x - this_x;
		dy = so_y - this_y;
		distance = parent.sqrt( ( (dx * dx) + (dy * dy) ) );
		radii = (getEffectiveRadius() + radius);
		if(radii >= distance) return true;
		else return false;
	}
	
	/**
	 * Add a x-value to the displacement value of the displacement PVector. This means that
	 * this will move the ServerObject at the next <code>draw()</code>
	 * 
	 * @param xvec the scalar to add to the x-displacement
	 * @param scale magnitude of the scalar, used for things like mass
	 */
	public void addXVec(float xvec, float scale)
	{
		displacement.add(new PVector(xvec * scale, 0));
	}
	
	/**
	 * Add a y-value to the displacement value of the displacement PVector. This means that
	 * this will move the ServerObject at the next <code>draw()</code>
	 * 
	 * @param yvec the scalar to add to the y-displacement
	 * @param scale magnitude of the scalar, used for things like mass
	 */
	public void addYVec(float yvec, float scale)
	{
		displacement.add(new PVector(0, yvec * scale));
	}
	
	/**
	 * Subtract a x-value to the displacement value of the displacement PVector. This means that
	 * this will move the ServerObject at the next <code>draw()</code>
	 * 
	 * @param xvec the scalar to remove from the x-displacement
	 * @param scale magnitude of the scalar, used for things like mass
	 */
	public void subXVec(float xvec, float scale)
	{
		displacement.sub(new PVector(xvec * scale, 0));
	}
	
	/**
	 * Subtract a y-value to the displacement value of the displacement PVector. This means that
	 * this will move the ServerObject at the next <code>draw()</code>
	 * 
	 * @param yvec the scalar to remove from the y-displacement
	 * @param scale magnitude of the scalar, used for things like mass
	 */
	public void subYVec(float yvec, float scale)
	{
		displacement.sub(new PVector(0, yvec * scale));
	}
}