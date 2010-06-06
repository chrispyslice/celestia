import processing.core.PApplet;
import processing.core.PVector;

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

public class Ship extends ClientObject
{
	/**
	 * Size of the ship
	 */
	private int size = 10;
	
	/**
	 * Angle of the ship
	 */
	private float angle;
	
	/**
	 * Current shield strength
	 */
	private float shield_strength;
	
	/**
	 * Shield color
	 */
	private float shield_color;
	
	/**
	 * Array of PVectors for the points
	 */
	private PVector[] points = new PVector[3];
	
	/**
	 * Constructor for the Ship class
	 * @param parent the parent PApplet to draw to
	 * @param x initial x position
	 * @param y initial y position
	 * @param angle the angle of the ship
	 * @param shield_strength current shield strength
	 * @param shield_color current shield color
	 */
	Ship(PApplet parent, int x, int y, float angle, float shield_strength, float shield_color)
	{
		super(parent, x, y);
		this.angle = angle;
		this.shield_strength = shield_strength;
		this.shield_color = shield_color;
	}
	
	/**
	 * Update the points, and draw the ship
	 * @see ClientObject#draw()
	 */
	void draw()
	{
		// nose
		points[0] = new PVector(position.x + size * parent.cos(parent.radians(angle)), position.y + size * parent.sin(parent.radians(angle)));
		// bottom left
		points[1] = new PVector(position.x + 1.7f * size * parent.cos(parent.radians(angle) + (parent.PI + 0.7f)), position.y + 1.7f * size * parent.sin(parent.radians(angle) + (parent.PI + 0.7f)));
		// bottom right
		points[2] = new PVector(position.x + 1.7f * size * parent.cos(parent.radians(angle) + (parent.PI - 0.7f)), position.y + 1.7f * size * parent.sin(parent.radians(angle) + (parent.PI - 0.7f)));
		
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
	}
}
