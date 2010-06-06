import processing.core.PApplet;
import processing.core.PVector;

/**
 * ClientObjcet represents any object that you see on the screen (be it a Ship or a Shot).
 * Using polymorphism allows us to have better quality and reduce the complexity of the code.
 * 
 * <p>It should be noted that although they look similar, <code>ServerObject</code> and
 * <code>ClientObject</code> are actually rather different, as only ServerObject includes code
 * to calculate displacements, positions, etc - <code>ClientObject</code> only allows drawing.</p> 
 * 
 * @author Chris Granville <v2t04 at students.keele.ac.uk>
 * @version 3.0f (12/05/2010)
 * @copyright 2009 - 2010 Chris Granville. All Rights Reserved.
 */

public abstract class ClientObject
{
	/**
	 * The current position of this object
	 */
	public PVector position;
	
	/**
	 * Parent PApplet to draw to
	 */
	public PApplet parent;
	
	/**
	 * Constructor for ClientObject
	 * @param parent parent PApplet to draw to
	 * @param x initial x position
	 * @param y initial y position
	 */
	public ClientObject(PApplet parent, int x, int y)
	{
		this.position = new PVector(x, y);
		this.parent = parent;
	}
	
	/**
	 * Draw the object at [position.x, position.y]
	 */
	abstract void draw();
}