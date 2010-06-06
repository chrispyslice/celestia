import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;

/**
 * Ship represents a player (technically a client could have many Ships, but this would
 * require more logic on both sides and would lead to a terrible game play dynamic) on the
 * server. It calculates new positions of the points using the rotation angle (inherited from
 * <code>ServerObject</code>.
 * 
 * <p>It should be noted that although they look similar, <code>Ship</code> in 
 * <code>ServerObject</code> and <code>Ship</code> in <code>ClientObject</code> are actually rather 
 * different, as only ServerObject includes code to calculate displacements, positions, etc - 
 * <code>ClientObject</code> only allows drawing</p> 
 * 
 * @author Chris Granville <v2t04 at students.keele.ac.uk>
 * @version 3.0f (12/05/2010)
 * @copyright 2009 - 2010 Chris Granville. All Rights Reserved.
 * @see <a href="http://goo.gl/VOIb">Fireball: Multiplayer Asteroids</a>
 */

@SuppressWarnings("static-access")

public class Ship extends ServerObject
{
	/**
	 * What kind of effect should the "air" have on the ship. Not accurate, but it makes the game better to play.
	 */
	private final float AIR = 0.99f;
	
	/**
	 * How many degrees the ship turns by at each iteration
	 */
	private final int TURN = 10;
	
	/**
	 * How much propulsion to add if the up key is pressed
	 */
	private final float PROPULSION = 0.20f;
	
	/**
	 * Starting shield strength
	 */
	private final byte MAX_SHIELD = 100;
	
	/**
	 * Should the "air" actually effect the ship?
	 */
	private final boolean AIR_EFFECT = true;
	
	/**
	 * Array of PVectors holding the [x, y] co-ordinates of each point in the ship
	 * @see updatePoints()
	 */
	private PVector[] points = new PVector[3];
	
	/**
	 * List holding which Shots this ship has been hit with before. Added to prevent a single shot hitting a Ship multiple times/frame
	 */
	private ArrayList<Integer> been_shot_with_list;
	
	/**
	 * What color is the shield using HSB
	 */
	private float shield_color;
	
	/**
	 * The current shield strength, set to the maximum shield to begin with
	 */
	private byte shield_strength = MAX_SHIELD;
	
	/**
	 * Size scale of the ship. Could be changed to allow for people with poor eyesight in a later version
	 */
	private int size;
	
	/**
	 * Name of the ship, currently simply the IP address of the client controlling it
	 */
	private String ship_name;
	
	/**
	 * Any <code>Shot</code> that the Ship has. Only one allowed to exist at a time
	 */
	private Shot shot;
	
	/**
	 * Is the ship currently firing a shot
	 */
	private boolean has_shot;
	
	/**
	 * Should we add propulsion to the ship in this frame
	 */
	public boolean addPropulsion;
	
	/**
	 * Contructor for Ship
	 * @param parent the parent PApplet to draw to
	 * @param position starting position of the ship
	 * @param size scale for the size of the ship
	 * @param ship_name the name that we wish to assign to this ship
	 * @param shield_color color of the shield
	 */
	Ship(PApplet parent, PVector position, int size, String ship_name, float shield_color)
	{
		super(parent, position.x, position.y);
		this.size = size;
		this.ship_name = ship_name;
		this.shield_color = shield_color;
		angle = 270;
		been_shot_with_list = new ArrayList<Integer>();
	}
	
	/**
	 * Update the ship's co-ordinates by recalculating the vectors.
	 */
	private void updatePoints()
	{
		// nose
		points[0] = new PVector(position.x + size * parent.cos(parent.radians(angle)), position.y + size * parent.sin(parent.radians(angle)));
		// bottom left
		points[1] = new PVector(position.x + 1.7f * size * parent.cos(parent.radians(angle) + (parent.PI + 0.7f)), position.y + 1.7f * size * parent.sin(parent.radians(angle) + (parent.PI + 0.7f)));
		// bottom right
		points[2] = new PVector(position.x + 1.7f * size * parent.cos(parent.radians(angle) + (parent.PI - 0.7f)), position.y + 1.7f * size * parent.sin(parent.radians(angle) + (parent.PI - 0.7f)));
	}

	/**
	 * Update the <code>displacement</code> PVector as well as the associated <code>Shot</code> if there is one
	 * @see ServerObject#move()
	 */
	public void move()
	{
		if(AIR_EFFECT) displacement.mult(AIR);
		if(addPropulsion) displacement.add(new PVector(PROPULSION * parent.cos(parent.radians(angle)), PROPULSION * parent.sin(parent.radians(angle))));
		displacement.limit(3.3f);
		position.add(displacement);
		updatePoints();
		
		// Decide whether we need to destroy the shot, if it exists
		// so we can create a new one later
		if(has_shot && shot.toDestroy())
		{
			shot.destroy();
			shot = null;
			has_shot = false;
		}
	}

	/**
	 * Draw the <code>Ship</code> at [position.x, position.y]
	 * @see ServerObject#draw()
	 */
	@Override
	public void draw() {
		// Has the ship been destroyed?
		if(!toDestroy())
		{
			// At the edges?
			if(position.x > parent.width) position.x -= parent.width;
			if(position.x < 0) position.x += parent.width;
			if(position.y > parent.height) position.y -= parent.height;
			if(position.y < 0) position.y += parent.height;
			
			// The actual ship
			parent.strokeWeight(1);
			parent.stroke(255);
			parent.fill(0);
			parent.triangle(points[0].x, points[0].y, points[1].x, points[1].y, points[2].x, points[2].y);
			
			// The shield
			parent.colorMode(parent.HSB);
			parent.strokeWeight(5.0f);
			parent.stroke(shield_color, 1000f, shield_strength);
			parent.noFill();
			parent.ellipseMode(parent.RADIUS);
			parent.ellipse(position.x, position.y + 5, 25, 25);
			
			parent.colorMode(parent.RGB);
			parent.strokeWeight(1.0f);
			
			// Coordinate label
			parent.fill(255);
			parent.text(ship_name, position.x + 30, position.y - 5);
			parent.text("[" + parent.round(position.x) + ", " + parent.round(position.y) + "] shield: " + shield_strength, position.x + 30, position.y + 10);
			
			if(has_shot && !shot.toDestroy())
			{
				shot.move();
				shot.draw();
			}
		}
	}
	
	/**
	 * Rotate the ship by <code>TURN</code>
	 * @param anticlockwise determines whether the ship is rotating anticlockwise or not
	 */
	public void rotate(boolean anticlockwise)
	{
		if(anticlockwise) angle -= TURN;
		else angle += TURN;
	}
	
	/**
	 * Make the Ship shoot a Shot
	 */
	public void shoot()
	{
		has_shot = true;
		//parent.println(ship_name + " has fired a shot");
		shot = new Shot(parent, points[0].x, points[0].y, angle);
	}
	
	/**
	 * 
	 * @param weapon_strength strength of the Shot we've been hit with; default is 10
	 * @param shot_id which unique shot have we been hit with
	 */
	public void hit(int weapon_strength, int shot_id)
	{
		// This really isn't the best way of doing this, but we're already at such a high CPU usage that we
		// can't afford to do any more complex maths to solve this properly.
		shield_strength -= weapon_strength;
		been_shot_with_list.add(shot_id);
	}
	
	/**
	 * Check if the Ship has been hit before with a specific shot
	 * @param id identification of the query Shot
	 * @return boolean showing if he Ship has been hit before or not
	 */
	public boolean hitBeforeWithShot(int id)
	{
		boolean result = false;
		for(int i = 0; i < been_shot_with_list.size(); i++)
		{
			int current = been_shot_with_list.get(i);
			if(current == id) result = true;
		}
		return result;
	}

	/**
	 * Get the radius of the Ship, is hard-coded to 25 which works for a size of 10
	 * @see ServerObject#getEffectiveRadius()
	 */
	public int getEffectiveRadius()
	{
		return 25;
	}

	/**
	 * Get the name of the ship, probably the IP address but could be player names, for example
	 * @return String with the name of the Ship
	 */
	public String getShipName()
	{
		return ship_name;
	}
	
	/**
	 * Get a boolean determining if the Ship is current shooting
	 * @return a boolean indicating if the Ship is currently shooting
	 */
	public boolean isShooting()
	{
		return has_shot;
	}
	
	/**
	 * Destroy the Shot that the Ship currently has
	 */
	public void destroyShot()
	{
		parent.println("Destroying shot");
		shot = null;
		has_shot = false;
	}
	
	/**
	 * Actually return the entire Shot if the Ship current has one
	 * @return the Shot that belongs to the Ship
	 */
	public Shot getShot()
	{
		return shot;
	}
	
	/**
	 * Should the Ship be destroyed - ie, is the shield strength sufficiently low
	 * @return a boolean indicating if the Ship is to be destroyed
	 * @see ServerObject#toDestroy()
	 */
	public boolean toDestroy()
	{
		if(shield_strength < 10) return true;
		else return false;
	}
	
	/**
	 * Get the color of this ships shield
	 * @return a float indicating the shild color
	 */
	public float getShieldColor()
	{
	    return shield_color;
	}
	
	/**
	 * Get the strength of the shield
	 * @return a float indicating the strength of the shield
	 */
	public float getShieldStrength()
	{
	    return shield_strength;
	}
}