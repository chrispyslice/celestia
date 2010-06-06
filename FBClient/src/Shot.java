import processing.core.PApplet;

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
 */

public class Shot extends ClientObject
{
	/**
	 * Radius of the shot
	 */
	private final int SHOT_SIZE = 5;
	
	/**
	 * Contructor for the Shot class
	 * @param parent the parent PApplet to draw to
	 * @param x starting x position
	 * @param y starting y position
	 */
	Shot(PApplet parent, int x, int y)
	{
		super(parent, x, y);
	}

	/**
	 * Draw the object at [position.x, position.y]
	 * @see ClientObject#draw()
	 */
	void draw() {
		parent.fill(255);
		parent.stroke(255);
		parent.strokeWeight(5);
		parent.ellipse(position.x, position.y, SHOT_SIZE, SHOT_SIZE);
		parent.strokeWeight(1);
	}
}
