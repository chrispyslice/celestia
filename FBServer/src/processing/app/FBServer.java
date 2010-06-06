package processing.app;

import processing.core.*;
import processing.net.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * The FBServer class co-ordinates the clients which are connected to the server
 * (when running). It creates the ArrayLists - one for the Ships, one for the Clients
 * and another for the Shots. Every frame it iterates through these and performs
 * actions on them - be it updating their positions, doing collision detection or
 * sending/receiving data to/from any clients that are connected.
 * 
 * @author Chris Granville <v2t04 at students.keele.ac.uk>
 * @version 3.0f (12/05/2010)
 * @copyright 2009 - 2010 Chris Granville. All Rights Reserved.
 * @see <a href="http://goo.gl/VOIb">Fireball: Multiplayer Asteroids</a>
 */

public class FBServer extends PApplet
{
	private static final long serialVersionUID = -7946560263801588659L;
	private final byte TOTAL_CLIENTS = 10;			// The maximum number of clients that can connect
	private int port;					// The port the server is running on
	private final boolean GRIDLINES = false;		// Display gridlines?
	private final boolean DEBUG = true;				// Display debugging messages?	
	private final float SHIP_WEIGHT = 0.035f;		// Weight of the ship
	private final String VERSION = "2.6g";			// Server version number and release status
	private ArrayList<Ship> ship_list;				// List of all the ships/players
	private ArrayList<Client> client_list;			// List of all the clients connected
	private ArrayList<Shot> shot_list;				// List of all the shots currently in existance
	private PFont smallfont, bigfont;				// The font used to display debug info (ex. coordinates)
	private Server server;							// Instance of the server
	
	/**
	 * Set the server up, doing things like setting a target framerate, loading
	 * fonts, creating lists etc. This is all fairly standard stuff, it just needs to be
	 * done somewhere and here seems like the best place.
	 * @see processing.core.PApplet#setup()
	 */
	public void setup()
	{
		size(800, 800);
		frameRate(30);
		//smooth();
		ellipseMode(CENTER);
		
		println("---------------------------------------------------------");
		println("Celestia Server v" + VERSION);
		println("Copyright (C) 2010 Chris Granville. All rights reseved");
		println("---------------------------------------------------------");
		
		smallfont = loadFont("SansSerif-10.vlw");
		bigfont = loadFont("SansSerif-16.vlw");
		textFont(smallfont);
		ship_list = new ArrayList<Ship>();
		client_list = new ArrayList<Client>();
		shot_list = new ArrayList<Shot>();
		
		// Take the port as a user input, keep trying until we get an integer
		while(true)
		{
			boolean isInt = true;
			try
			{
				port = Integer.parseInt(JOptionPane.showInputDialog(null, "Please enter a port (make sure you have permission to access it):"));
			}
			catch (NumberFormatException e)
			{
				JOptionPane.showMessageDialog(frame, "Port must be an integer");
				isInt = false;
			}
			if(isInt) break;
		}
		
		JOptionPane.showMessageDialog(frame, "Welcome to Celestia Server!\n" +
												"Don't forget to open port " + port + " on your firewall (and forward ports if required)!");
		
		// Start a server on the port specified earlier
		try
		{
			log_message("Starting server on port " + port, 1);
			server = new Server(this, port);
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(frame, "An error occured while attempting to start the server. Do you have permission to use port " + port + "?");
			log_message("There was an exception while creating the server. Cannot continue.", 1);
			e.printStackTrace();
		}
	}
	
	/**
	 * @see processing.core.PApplet#draw()
	 */
	public void draw()
	{
		background(0);
		if(GRIDLINES) drawGrid();
		
		// Draw any clients that are connected
		if(server.clientCount >= 1)
		{
			doCollisions();
			for(int i = 0; i < client_list.size(); i++)
			{
				// Get the current client and associated ship...
				String temp[] = null, message = null;
				boolean left, right, up, space, addPropulsion = false;
				Client this_client = (Client) client_list.get(i);
				Ship current = (Ship) ship_list.get(i);
				
				// Check if it's been destroyed and make sure we have the right ship
				if(current.toDestroy() && current.getShipName().equals(client_list.get(i).ip()))
				{
					// Ship is to be destroyed, so remove it from the lists
					client_list.remove(i);
					ship_list.remove(i);
				}
				else	// Not to be destroyed, so let's get some data and process it using the Ship class
				{
					if(this_client != null && this_client.available() > 1)
					{
						// Protocol documtation is available in the...documentation
						boolean hasDrawn = false;
						message = this_client.readString();
						temp = message.split(":");
						if(temp.length >= 4)	// Is the data malfored (ArrayIndexOutOfBoundsException, most commonly)
						{
							try
							{
								// Get the boolean data if we can
								up = Boolean.valueOf(temp[0]);
								left = Boolean.valueOf(temp[1]);
								right = Boolean.valueOf(temp[2]);
								space = Boolean.valueOf(temp[3]);

								if(up == true) addPropulsion = true;
								if(left == true) current.rotate(true);
								if(right == true) current.rotate(false);
								if(space == true && !current.isShooting()) current.shoot();

								current.addPropulsion = addPropulsion;
								current.move();
								current.draw();
								hasDrawn = true;
							}
							catch (Exception e)	// Lots of things could possibly go wrong here, but we'll want to draw the ship
							{					// if we have valid data or not
								current.draw();
								hasDrawn = true;
							}
						}
						else
						{
							// Malformed data
							log_message("Data from client " + this_client.ip() + " is malformed, skipping", 1);
							log_message("Malformed data: " + message, 1);
						}
						
						// We want to draw the ship if it hasn't already drawn. Prevents the flicker problem due to malformed data
						if(!hasDrawn) current.draw();
					}
				}
			}
			
			// Clear Shots that have expired
			clearShots();
			
			// Send new positions of Shots and Ships to all connected clients
			transmitLocations();
		}
		
		// Basic debugging information
		drawServerInfo();
	}
	
	/**
	 * Draws the grid, if we want one
	 * Influenced by my own application for CSC-10030, the drawing application
	 * It should be noted that drawing the grid takes up a stupid amount of CPU (~80% on a 2.66GHz C2D (P8800))
	 */
	private void drawGrid()
	{
		stroke(64);
		// First, vertical
		for(int i = 0; i < width; i += 20)
		{
			line(i, 0, i, height);
		}
		
		// Horizontal
		for(int i = 0; i < height; i += 20)
		{
			line(0, i, width, i);
		}
	}
	
	/**
	 * Draws the version number, number of connected clients, frame rate and number of Ships, Shots and Clients
	 */
	private void drawServerInfo()
	{
		textFont(bigfont);
		fill(255);
		text("Celestia Server v" + VERSION, 10, 25);
		text(server.clientCount + " client(s) connected", 10, 45);
		text(round(frameRate) + "fps", 10, 65);
		text("Ships: " + ship_list.size() + ", shots: " + shot_list.size() + ", clients: " + client_list.size(), 10, 85);
		textFont(smallfont);
	}
	
	/**
	 * Iterate through every ServerObject (but need to do Ships and Shots separately
	 * as we need to know the difference between the two so different actions can be
	 * taken). Most of the heavy lifting here is actually done by
	 * ServerObject.collidingWith(), here we're really just taking the appropriate
	 * action (either make the ships bounce off each other or take 10 hit points off
	 * the shield strength).
	 * @see ServerObject.collidingWith() 
	 */
	private void doCollisions()
	{
		Ship a, b;	
		// Between ships
		for(int i = 0; i < client_list.size(); i++)
		{
			// First, we want to check for collisions between ships
			a = (Ship) ship_list.get(i);
			for(int j = 0; j < client_list.size(); j++)
			{
				b = (Ship) ship_list.get(j);
				if(a.collidingWith(b) && a != b)
				{
					// The ships are colliding, make them rebound from each other
					// This section is heavily influenced by Dave Collin's pool2
					int a_x = a.getXPosition();
					int b_x = b.getXPosition();
					int a_y = a.getYPosition();
					int b_y = b.getYPosition();
					float dx = a_x - b_x;
					float dy = a_y - b_y;
					float angle = atan2(dx * dx, dy * dy);
					float targetX = b_x + cos(angle) * 25;
					float targetY = b_y + sin(angle) * 25;
					float ax = (targetX - a_x);
					float ay = (targetY - a_y);
					a.subXVec(ax, SHIP_WEIGHT);
					a.subYVec(ay, SHIP_WEIGHT);
					b.addXVec(ax, SHIP_WEIGHT);
					b.addYVec(ay, SHIP_WEIGHT);
					
					log_message("Collision: " + a.getShipName() + ", " + b.getShipName() + "  [" + dx +", " + dy + "]", 2);
				}
			}
			// Add the ships shot if it isn't already added, which it should be
			if(a.isShooting() && !shot_list.contains(a.getShot())) shot_list.add(a.getShot());
		}
		
		// And now the shots
		for(int i = 0; i < client_list.size(); i++)
		{
			for(int j = 0; j < shot_list.size(); j++)
			{
				Ship ship = (Ship) ship_list.get(i);
				Shot shot = (Shot) shot_list.get(j);
				if(ship.collidingWith(shot) && !ship.hitBeforeWithShot(shot.getId()))
				{
					// Let's really, ridiculously kill the shot like the utter vermin that it is
					// (Had soooo many problems with them it's actually ridiculous and so I now hate my own class.)
					ship.hit(10, shot.getId());
					ship.destroyShot();
					shot.destroy();
					shot_list.remove(j);
				}
			}
		}
	}
	
	/**
	 * Remove all the expired shots on the screen
	 */
	private void clearShots()
	{
		for(int i = 0; i < shot_list.size(); i++)
		{
			if(shot_list.get(i).toDestroy())
			{
				log_message("Decayed shot id: " + shot_list.get(i).getId(), 2);
				shot_list.remove(i);
			}
		}
	}

	/**
	 * When a client connects, add them to the list of clients and assign them a Ship, assuming that
	 * the number of ships < maximum number of ships. We limit the number of ships not only for
	 * performance (it's bad enough with only a few) but also gameplay - with too many the game
	 * would become far too hard and confusing.
	 * @see processing.net.Server#serverEvent()
	 */
	public void serverEvent(Server s, Client c)
	{
		String ip = c.ip();
		if(server.clientCount < TOTAL_CLIENTS)
		{
			ship_list.add(new Ship(this, new PVector(width/2, height/2), 10, ip, 150f));
			client_list.add(c);
			log_message("Added a new client from " + ip, 1);
		}
		else
		{
			log_message("Could not add new client from " + ip + " during frame " + frameCount + ": too many clients connected", 1);
		}	
	}
	
	/**
	 * Generate the transmission and send it to all connected clients. The transmission contains
	 * the details of the co-ordinates of every object in the game. This is a very basic and
	 * rudimentary approach, but it's simple and relatively effective. For full details of the
	 * protocol, see the documentation.		
	 */
	public void transmitLocations()
	{
		// First, actually build the transmission 
		// The ships go first
		String transmission = "";
		for(int i = 0; i < client_list.size(); i++)
		{
			Ship s = (Ship) ship_list.get(i);
			transmission += s.getXPosition() + "," + s.getYPosition() + "," + s.getAngle() + "," + s.getShieldColor() + "," + s.getShieldStrength();
			if(i != client_list.size() - 1) transmission += ";";
		}
		
		// Add the delimiter...
		transmission += "//";
		
		// And now the shots, if we have any
		if(shot_list.size() == 0) transmission += "false";
		else
		{
			for(int i = 0; i < shot_list.size(); i++)
			{
				Shot s = (Shot) shot_list.get(i);
				transmission += s.getXPosition() + "," + s.getYPosition();
				if(i != shot_list.size() - 1) transmission +=  ";";
			}
		}
		
		// Send the transmission on its way. Bye little string, may you reach your destination intact (please use TCP instead of UDP!)
		server.write(transmission);
	}
	
	/**
	 * Log a message to the terminal window.
	 * There are two priorities:
	 * 		1: high importance and is always displayed
	 * 		2: low importance, only displayed in debug mode
	 * @param message
	 * @param priority
	 */
	private void log_message(String message, int priority)
	{
		if(priority == 1 || (DEBUG && priority == 2)) println("[f" + frameCount + "] " + message);
	}

	/**
	 * The method that Processing uses to convert this from a PApplet to an application
	 * @param args	the arguments to pass to the application
	 */
	public static void main(String args[])
	{
		PApplet.main(new String[] { "--bgcolor=#000000", "processing.app.FBServer" });
	}
	
}