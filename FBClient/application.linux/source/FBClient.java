import processing.core.*;
import processing.net.*;
import ddf.minim.*;
import javax.swing.JOptionPane;

/**
 * The FBClient class connects to a running instance of FBClient and sends/receives
 * information regarding the keys pressed and the locations of each ship/shot on the
 * screen. FBClient then processes that information and draws each ship and shot using
 * instances of the <code>Ship</code> and <code>Shot</code> classes.
 * 
 * @author Chris Granville <v2t04 at students.keele.ac.uk>
 * @version 3.0f (12/05/2010)
 * @copyright 2009 - 2010 Chris Granville. All Rights Reserved.
 */

public class FBClient extends PApplet
{
	/**
	 * Serial number that Eclipse insists on
	 */
	private static final long serialVersionUID = -4376644817720276514L;

	/**
	 * Port that the client should connect to the server using
	 */
	private int port;
	
	/**
	 * Address of the server that the client should connect to
	 */
	private String address;

	/**
	 * FBClient version string
	 */
	private final String VERSION = "3.0.34f";
	
	/**
	 * Array of booleans used to store key presses
	 */
	private boolean[] keys = new boolean[4];
	
	/**
	 * Currently connected to a server?
	 */
	private boolean connected;
	
	/**
	 * Font used for the text in the top left corner
	 */
	private PFont font = loadFont("SansSerif-10.vlw");
	
	/**
	 * Instance of the Client class used to connected to a server
	 */
	private Client client;
	
	/**
	 * Sound class
	 */
	private Minim minim;
	
	/**
	 * The AudioSample for the shooting audio
	 */
	private AudioSample shoot;
	
	/**
	 * Set the client up - set fonts, connect to the server, load sounds, etc
	 * @see processing.core.PApplet#setup()
	 */
	public void setup()
	{
		size(800, 800);
		frameRate(30);
		textFont(font);
		smooth();
		
		println("---------------------------------------------------------");
		println("Celestia Client v" + VERSION);
		println("Copyright (C) 2010 Chris Granville. All rights reseved");
		println("---------------------------------------------------------");
		
		minim = new Minim(this);
		shoot = minim.loadSample("shoot.wav");
		if(shoot == null) println("Could not load one or more sfx");
		
		address = JOptionPane.showInputDialog(null, "IP of server:");
		port = Integer.parseInt(JOptionPane.showInputDialog(null, "Port:"));
		//server_address = "127.0.0.1";
		
		JOptionPane.showMessageDialog(frame, "Welcome to Celestia! Don't forget to open port " + port + " and forward it if required." +
				"\n\nControls:\nUp: thrust\nLeft/right: rotate\nSpace: fire");
		
		connect();
	}
	
	/**
	 * Draw the <code>Ship</code>s and <code>Shot</code>s every <code>frameRate</code> times/second
	 * Also sends/receives data from the server
	 * @see processing.core.PApplet#draw()
	 */
	public void draw()
	{
		background(0);
		fill(255);
		text("Celestia Client v" + VERSION, 10, 25);
		text(round(frameRate) + "fps", 10, 35);
		
		// Send the data
		String transmission = keys[0] + ":" + keys[1] + ":" + keys[2] + ":" + keys[3];
		client.write(transmission);
		
		// Receive the data
		String temp[] = null, temp_ships[], temp_shots[] = null;
		boolean weHaveShots = false;
		
		if(client.available() > 0)
		{
			String returned = client.readString();
			//println("frame " + frameCount + ", message: " + returned);
			temp = returned.split("//");		// At this point, all ships => 0, all shots => 1
			
			if(temp.length == 2)
			{
				if(!temp[1].equals("false"))
				{
					temp_shots = temp[1].split(";");
					weHaveShots = true;
				}
				
				// Create arrays of Ships and Shots
				temp_ships = temp[0].split(";");
				Ship ships[] = new Ship[temp_ships.length];
				Shot shots[] = null;
				if(weHaveShots) shots = new Shot[temp_shots.length];
				
				// Extract information for each ship from <code>temp_ships</code>
				for(int i = 0; i < temp_ships.length; i++)
				{
					int x = 0, y = 0;
					float angle = 0.0f, shield_strength = 0.0f, shield_color = 0.0f;
					
					String details[] = temp_ships[i].split(",");
					
					try
					{
						x = Integer.parseInt(details[0]);
						y = Integer.parseInt(details[1]);
						angle = Float.parseFloat(details[2]);
						shield_color = Float.parseFloat(details[3]);
						shield_strength = Float.parseFloat(details[4]);
						
						ships[i] = new Ship(this, x, y, angle, shield_strength, shield_color);
						ships[i].draw();
					}
					catch (Exception excep)	// Just catch every exception as we always want to do the same thing
					{
						println("[" + frameCount + "] ship transmission malfored, skipping");
						//excep.printStackTrace();
					}
				}
				
				// And now the Shots...
				if(weHaveShots)
				{
					for(int i = 0; i < temp_shots.length; i++)
					{
						int x = 0, y = 0;
						
						String details[] = temp_shots[i].split(",");
						
						try
						{
							x = Integer.parseInt(details[0]);
							y = Integer.parseInt(details[1]);
							
							shots[i] = new Shot(this, x, y);
							shots[i].draw();
						}
						catch (Exception excep)		// Catch all exceptions, just in case
						{
							println("[" + frameCount + "] shot transmission malfored, skipping");
							//excep.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	/**
	 * Capture key presses so we can send this data to the server for processing
	 * @see processing.core.PApplet#keyPressed()
	 */
	public void keyPressed()
	{
		if(key == CODED)
		{
			switch(keyCode)
			{
				case UP: keys[0] = true; break;
				case LEFT: keys[1] = true; break;
				case RIGHT: keys[2] = true; break;
			}
		}
		
		// Space is not a CODED key, so we need to add additional logic here
		if(key == ' ')
		{
			shoot.trigger();
			keys[3] = true;
		}
		
		// Disconnect from the server
		if(key == 'd' || key == 'D') disconnect();
		
		// Connect to the server again if we want to
		if((key == 'c' || key == 'C') && !connected) connect();
	}
	
	/**
	 * Capture key releases so we can send this data to the server for processing
	 * @see processing.core.PApplet#keyReleased()
	 */
	public void keyReleased()
	{
		if(key == CODED)
		{
			switch(keyCode)
			{
				case UP: keys[0] = false; break;
				case LEFT: keys[1] = false; break;
				case RIGHT: keys[2] = false; break;
			}
		}
		
		// Space is not a CODED key, so we need to add additional logic here
		if(key == ' ') keys[3] = false;
	}
	
	/**
	 * Connect to the server
	 */
	private void connect()
	{
		println("Connecting to " + address + " on port " + port);
		client = new Client(this, address, port);
		connected = true; 
	}
	
	/**
	 * Disconnect from the server
	 */
	private void disconnect()
	{
		println("Disconnecting from server");
		client.stop();
		client = null;
		connected = false;
		noLoop();
	}

	/**
	 * Close down Minim's audio thingies so they are released for other applications
	 * @see processing.core.PApplet#stop()
	 */
	public void stop()
	{
		shoot.close();
		minim.stop();
		super.stop();
	}

	/**
	 * The method that Processing uses to convert this from a PApplet to an application
	 * @param args	the arguments to pass to the application
	 */
	public static void main(String args[])
	{
		PApplet.main(new String[] {"--bgcolor=#000000", "FBClient"});
	}
}