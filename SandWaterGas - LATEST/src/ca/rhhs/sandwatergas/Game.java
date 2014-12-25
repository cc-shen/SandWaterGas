package ca.rhhs.sandwatergas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.rhhs.sandwatergas.block.Block;
import ca.rhhs.sandwatergas.block.Empty;
import ca.rhhs.sandwatergas.block.Gas;
import ca.rhhs.sandwatergas.block.Liquid;
import ca.rhhs.sandwatergas.block.Sand;
import ca.rhhs.sandwatergas.block.Wall;
import ca.rhhs.sandwatergas.block.Water;
import ca.rhhs.sandwatergas.generatemap.Map;

/**
 * Note to Mr. Ridout : You said it was okay to use X and Y in our 2D Array
 * because we use X and Y values for rendering the blocks P.S. You can access
 * the debug mode from the main menu by pressing the 'D' in Sand, although there
 * is a slight delay before turning on.
 * 
 */
public class Game extends JFrame
{
	final static private int WIDTH = 800;
	final static private int HEIGHT = 480;
	final static private int EMPTY = 0;
	final static private int SAND = 1;
	final static private int WATER = 2;
	final static private int GAS = 3;
	final static private int WALL = -1;

	private int blockType;

	private int[] cycleOfLife;

	private GameCanvas canvas;
	final static private String VERSION = "January 17, 2014 12:00pm";
	final static private String GAME_NAME = "Sand, Water, Gas";

	final static private Image MAP_EDITOR_IMAGE = new ImageIcon(
			"res/graphics/M.png").getImage();
	final static private Image MAP_EDITOR_ON_IMAGE = new ImageIcon(
			"res/graphics/M On.png").getImage();
	final static private Image PAUSE_BUTTON = new ImageIcon(
			"res/graphics/pause.png").getImage();
	final static private Image RESUME_BUTTON = new ImageIcon(
			"res/graphics/resume.png").getImage();
	final static private Image EXPORT_BUTTON = new ImageIcon(
			"res/graphics/E.png").getImage();
	final static private Image NEXT_BUTTON = new ImageIcon(
			"res/graphics/next.png").getImage();
	final static private Image PREVIOUS_BUTTON = new ImageIcon(
			"res/graphics/prev.png").getImage();
	final static private Image RESET_BUTTON = new ImageIcon(
			"res/graphics/R.png").getImage();
	final static private Image SAVE_BUTTON = new ImageIcon(
			"res/graphics/save.png").getImage();
	final static private Image MENU_BUTTON = new ImageIcon(
			"res/graphics/menu.png").getImage();
	final static private Image HINT_OFF = new ImageIcon("res/graphics/H.png")
			.getImage();
	final static private Image HINT_ON = new ImageIcon("res/graphics/H On.png")
			.getImage();

	final static private Image CLEAR_INDICATOR = new ImageIcon(
			"res/graphics/cleared.png").getImage();
	final static private Image UNCLEARED_INDICATOR = new ImageIcon(
			"res/graphics/notcleared.png").getImage();
	final static private Image BACKGROUND = new ImageIcon(
			"res/graphics/background.png").getImage();

	final static private Image SOLUTION = new ImageIcon(
			"res/graphics/solution.png").getImage();

	private int randomIcon;

	private boolean gameOver;
	private boolean menu;
	private boolean help;
	private boolean levelCleared;
	private boolean pause;
	private boolean debug;
	private boolean mapEditor;
	private int frames;
	private int updates;

	private BufferedWriter out;
	private Scanner in;

	private int gasToRemove;
	private int sandToRemove;
	private int waterToRemove;
	private int wallsCanRemove;

	private int sandStarting;
	private int waterStarting;
	private int gasStarting;

	private int dSand;
	private int dWater;
	private int dGas;

	private boolean showSolution;

	private int wallsRemoved;

	final static private int BLOCK_SIZE = 16;
	final static private int MAX_Y = 30;
	final static private int MAX_X = 39;
	private Block[][] grid;

	private ArrayList<String> mapCache;
	private ArrayList<String> tutorialCache;
	private int currentMap;
	private int mapCount;
	private boolean randomMap;

	private int mouseX;
	private int mouseY;

	private InputStream is;
	private Font font;

	public Game() throws FontFormatException, IOException
	{
		cycleOfLife = new int[] { 0, 2, 3, 1, 2, 3 };

		grid = new Block[MAX_X][MAX_Y];

		gameOver = false;
		menu = true;
		pause = false;
		debug = false;
		mapEditor = false;
		showSolution = false;

		mapCache = new ArrayList<String>();
		recache();
		tutorialCache = new ArrayList<String>();

		is = new FileInputStream("res/DK_Snemand.ttf");
		font = Font.createFont(Font.TRUETYPE_FONT, is);

		// Adds all the tutorials to the cache
		tutorialCache.add("/tutorial/tutorial1.map");
		tutorialCache.add("/tutorial/tutorial2.map");
		tutorialCache.add("/tutorial/tutorial3.map");
		tutorialCache.add("/tutorial/tutorial4.map");
		tutorialCache.add("/tutorial/tutorial5.map");

		canvas = new GameCanvas();
		canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.setContentPane(canvas);

		// JFrame operations
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setTitle(GAME_NAME);
		this.setVisible(true);
		this.setLocationRelativeTo(null);

		// Random icon for JFrame
		randomIcon = (int) (Math.random() * 3) + 1;
		if (randomIcon == SAND)
			this.setIconImage(ImageIO.read(new File("res/graphics/sand.png")));
		else if (randomIcon == WATER)
			this.setIconImage(ImageIO.read(new File("res/graphics/water.png")));
		else
			this.setIconImage(ImageIO.read(new File("res/graphics/gas.png")));

	}

	/**
	 * Every time this method is called it will look through the maps folder and
	 * add all the .MAP files to the map cache
	 * 
	 * @author Philip Liu
	 */
	public void recache()
	{
		// Clears the current cache and add all the maps to the cache, to
		// prevent duplicate maps being added
		mapCache.clear();
		mapCount = 1;

		File directory = new File("maps");
		if (directory.list().length > 0)
			for (File nextMap : directory.listFiles())
			{
				if (nextMap.getName().toLowerCase().endsWith(".map"))
				{
					mapCache.add(nextMap.getName());
					mapCount++;
				}
			}
	}

	/**
	 * Creates a new map object, and translates it the grid of blocks then
	 * proceeds to export it as a .MAP file
	 * 
	 * @author Philip Liu
	 * @param sand The amount of sand needed to be removed to pass
	 * @param water The amount of water needed to be removed to pass
	 * @param gas The amount of gas needed to be removed to pass
	 * @param wall The amount of walls that can be removed for this level
	 */
	public void generateMap(int sand, int water, int gas, int wall)
	{
		// Because the map object uses integer array instead of an array of
		// blocks, converting it to the grid format used in the game was
		// necessary to make the maps compatible
		Map newMap = new Map();
		int[][] temp = newMap.getMap().clone();

		for (int x = 0; x < MAX_X; x++)
		{
			for (int y = 0; y < MAX_Y; y++)
			{
				addBlock(EMPTY, x, y);
			}
		}

		for (int x = 0; x < MAX_X; x++)
		{
			for (int y = 0; y < MAX_Y; y++)
			{
				if (temp[x][y] == 4)
					temp[x][y] = 9;
				if (temp[x][y] == 5 || temp[x][y] == 7)
					temp[x][y] = 0;
			}
		}

		// This is the same code as the method code below except it uses the
		// temporary grid instead of the actual block grid
		recache();
		File filepath;
		
		mapCount++;
		
		if (mapCount >= 10)
			filepath = new File("maps/" + "level" + "" + mapCount + ".map");
		else
			filepath = new File("maps/" + "level" + "0" + mapCount + ".map");
		
		java.util.Date date = new java.util.Date();

		try
		{
			out = new BufferedWriter(new FileWriter(filepath));
			for (int y = 0; y < MAX_Y; y++)
			{
				for (int x = 0; x < MAX_X; x++)
				{
					out.write(Integer.toString(temp[x][y]));
				}
				out.newLine();
			}
			out.write(date.toString());
			out.newLine();
			out.write(sand + " " + water + " " + gas + " " + wall);
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		recache();
	}

	/**
	 * Exports the map as a grid of integers with the sand, water, gas
	 * requirements stored at the very bottom of the file, file naming is
	 * handled by the recache method
	 * 
	 * @author Philip Liu
	 * @param sand The amount of sand needed to be removed to pass
	 * @param water The amount of water needed to be removed to pass
	 * @param gas The amount of gas needed to be removed to pass
	 * @param wall The amount of walls that can be removed for this level
	 * @return Returns the file name and the path of the map
	 */
	public String exportMap(int sand, int water, int gas, int wall)
	{
		recache();

		// Due to the fact that arraylist's natural order is alphanumeric,
		// level11 will come before level2, it was changed so that single digit
		// levels will start with 0 to counteract this
		File filepath = null;
		
		mapCount++;
		
		if (mapCount >= 10)
			filepath = new File("maps/" + "level" + "" + mapCount + ".map");
		else
			filepath = new File("maps/" + "level" + "0" + mapCount + ".map");

		System.out.println (mapCount);
		java.util.Date date = new java.util.Date();

		// Writes all the block types in a grid format with the exception of
		// wallblocks due to the actual getType() being -1
		try
		{
			out = new BufferedWriter(new FileWriter(filepath));
			for (int y = 0; y < MAX_Y; y++)
			{
				for (int x = 0; x < MAX_X; x++)
				{
					int blockNo = grid[x][y].getType();
					if (blockNo == WALL)
						out.write("9");
					else
						out.write(Integer.toString(blockNo));
				}
				out.newLine();
			}
			out.write(date.toString());
			out.newLine();
			out.write(sand + " " + water + " " + gas + " " + wall);
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		recache();
		return "The file was successfully written to " + filepath.toString();
	}

	/**
	 * Reads a .MAP file and copies it to the grid of blocks
	 * 
	 * @author Philip Liu
	 * @param name The full name of the map file including the extensions type
	 * @return The sand, water, gas, wall numbers will be displayed
	 */

	public String importMap(String name)
	{
		wallsRemoved = 0;
		showSolution = false;
		String message = null;

		try
		{
			String line;
			in = new Scanner(new FileReader("maps/" + name));

			// Reads in the numbers provided by the .MAP and
			// converts the block types usable by the grid[][]
			for (int y = 0; y < MAX_Y; y++)
			{
				line = in.nextLine();
				for (int x = 0; x < MAX_X; x++)
				{
					int block = Character.getNumericValue((line.charAt(x)));
					if (block == 9)
						block = WALL;
					addBlock(block, x, y);
				}
			}
			message = (name + " was created on " + in.nextLine());

			if (in.hasNextLine())
			{
				sandToRemove = in.nextInt();
				waterToRemove = in.nextInt();
				gasToRemove = in.nextInt();
				wallsCanRemove = in.nextInt();
			}
			else
			{
				sandToRemove = 0;
				waterToRemove = 0;
				gasToRemove = 0;
				wallsCanRemove = 0;
			}
			in.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// The starting elements are counted in order to calculate
		// the delta of each element, this will be used to check if a level
		// was cleared
		sandStarting = 0;
		waterStarting = 0;
		gasStarting = 0;

		for (int y = 0; y < MAX_Y; y++)
		{
			for (int x = 0; x < MAX_X; x++)
			{
				int blockType = grid[x][y].getType();
				if (blockType == SAND)
					sandStarting++;
				else if (blockType == WATER)
					waterStarting++;
				else if (blockType == GAS)
					gasStarting++;
			}
		}

		// Because the debug ui does not contain information regarding
		// how many of each block needs to be removed to clear a level
		// this information is instead provided by the console
		return message + "\nSand needed to be removed : " + sandToRemove
				+ "\nGas needed to be removed : " + gasToRemove
				+ "\nWater needed to be removed : " + waterToRemove
				+ "\nWalls that can be removed : " + wallsCanRemove;
	}

	/**
	 * Used exclusively by the map editor, this will allow maps to saved in
	 * there current states
	 * 
	 * @author Philip Liu
	 * @return Returns the file name and path to which the map is saved to
	 */
	public String saveMap()
	{

		// The associated .MAP file will be found for the current map
		// and will be overwritten with the same method as the exportMap()
		String path = mapCache.get(currentMap);
		File filepath = new File("maps/" + path);
		java.util.Date date = new java.util.Date();

		try
		{
			out = new BufferedWriter(new FileWriter(filepath));
			for (int y = 0; y < MAX_Y; y++)
			{
				for (int x = 0; x < MAX_X; x++)
				{
					int blockNo = grid[x][y].getType();
					if (blockNo == WALL)
						out.write("9");
					else
						out.write(Integer.toString(blockNo));
				}
				out.newLine();
			}
			out.write(date.toString());
			out.newLine();
			out.write(sandToRemove + " " + waterToRemove + " " + gasToRemove
					+ " " + wallsCanRemove);
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return path + " has been saved.";
	}

	/**
	 * Loads the previous map found in the map cache, if there are no previous
	 * maps nothing will happen
	 * 
	 * @author Philip Liu
	 */

	public void loadPreviousMap()
	{
		// This is a debug feature and therefore will not be accessed in the
		// main game
		if (currentMap - 1 >= 0)
		{
			recache();
			if (debug)
			{
				System.out.println("");
				System.out
						.println(importMap(mapCache.get(--currentMap)) + "\n");
			}
			else
				importMap(mapCache.get(--currentMap));
		}
	}

	/**
	 * Loads the next map found in the map cache, this method can behave in 2
	 * different ways. In the first case, if there are already maps in the map
	 * cache but there is no succeeding level , it will do nothing if called,
	 * the second behavior is called when there are no .MAP files found in the
	 * maps folder, it will then create a random map
	 * 
	 * @author Philip Liu
	 */
	public void loadNextMap()
	{
		if (currentMap + 2 <= mapCache.size() && !help)
			if (debug)
			{
				System.out.println("");
				System.out
						.println(importMap(mapCache.get(++currentMap)) + "\n");
			}
			else
				importMap(mapCache.get(++currentMap));

		// Creates a random map and adds it to the cache
		// This is to prevent crashing if there are no maps found
		if (randomMap && !help)
		{
			File file = new File("/maps/" + mapCache.get(currentMap));
			file.setWritable(true);
			generateMap(99, 99, 99, 1);
			recache();
			importMap(mapCache.get(currentMap));
		}

		// If the user is in tutorial mode, it will load the maps from the
		// tutorial
		// cache instead
		if (currentMap + 2 <= tutorialCache.size() && help)
		{
			importMap(tutorialCache.get(++currentMap));
		}
	}

	/**
	 * Resets the map to its starting state
	 * 
	 * @author Philip Liu
	 */
	public void resetMap()
	{
		if (!help)
			importMap(mapCache.get(currentMap));
		else if (help)
			importMap(tutorialCache.get(currentMap));
		if (debug)
			System.out.println("Map has been reset.");
	}

	/**
	 * Looks through all the wall blocks found on the grid, checking if each
	 * block is a valid hint
	 * 
	 * @author Philip Liu
	 */
	private void findSolution()
	{
		// Generate all potential solution blocks
		for (int y = 0; y < MAX_Y; y++)
		{
			for (int x = 0; x < MAX_X; x++)
			{
				Block block = grid[x][y];
				if (block.getType() == WALL)
					if (isPossibleSolution(x, y))
						block.setSolution(true);
					else if (!isPossibleSolution(x, y))
						block.setSolution(false);
			}
		}
	}

	/**
	 * Finds for the number of Water that can flow out given the dimensions,
	 * this is assuming that the enclosed area of Water is a rectangle
	 * 
	 * @author Charles Shen
	 * @param startX the starting x position (inclusive)
	 * @param startY the starting y position (inclusive)
	 * @param endX the ending x position (exclusive)
	 * @return the number of Water that can fall out
	 */
	private int noOfWaterCanFlowOut(int startX, int startY, int endX)
	{
		int endY = 0;
		// Looks above the starting position (startX, startY) and finds
		// the lowest y value (y decrease as it goes up)
		for (int checkY = startY; checkY >= endY; checkY--)
			if (grid[startX][checkY].getType() != WATER)
				endY = checkY;
		return (endX - startX) * (startY - endY);
	}

	/**
	 * Calculates the number of Gas blocks that can escape a rectangle area of
	 * Gas
	 * 
	 * @author Charles Shen
	 * @param startX the starting x position (inclusive)
	 * @param startY the starting y position (inclusive)
	 * @param endX the ending x position (exclusive)
	 * @return the number of Gas blocks able to escape
	 */
	private int noOfGasCanEscape(int startX, int startY, int endX)
	{
		int endY = MAX_Y;
		// Looks below the starting position (startX, startY) and finds
		// the highest y position (because y increase as it goes down) of
		// the enclosed area of gas
		for (int checkY = startY; checkY < endY; checkY++)
			if (grid[startX][checkY].getType() != GAS)
				endY = checkY;
		return (endX - startX) * (endY - startY);
	}

	/**
	 * Calculates the amount of Sand that will fall out of an rectangular area
	 * while considering its hourglass physics
	 * 
	 * @author Charles Shen
	 * @param leftX the left most x value where there is a Sand block
	 *            (inclusive)
	 * @param startY the starting y value (inclusive)
	 * @param middleX the x value containing the hold where Sand will fall
	 *            through
	 * @param rightWallX the x value of the Wall to the right (exclusive)
	 * @return the number of Sand that can fall though a Wall Block
	 */
	private int noOfSandCanFall(int leftX, int startY, int middleX, int endX)
	{
		int noOfSandCanFall = 0;
		int checkY = startY;
		while (checkY >= 0 && grid[middleX][checkY].getType() == SAND)
		{
			// Find the row to calculate the number of sand that can fall
			// through. Due to its hourglass-like physics, the amount of
			// sand that will fall should start at 3 and increase by 2
			// every row if no obscuring wall
			int nthRow = (startY - checkY + 1);
			int numberOfSandThisRow = (2 * nthRow) + 1;
			noOfSandCanFall += numberOfSandThisRow;

			// If the theoretical number of sand should fall on this row exceeds
			// the surrounding wall, it will decrease the falling sand counter
			// by half of the theoretical number for one side and add back the
			// amount sand that can fall (applies to left and right side of the
			// middleX)
			if (middleX + nthRow >= endX)
			{
				noOfSandCanFall -= (numberOfSandThisRow / 2);
				noOfSandCanFall += (endX - middleX - 1);
			}
			if (middleX - nthRow < leftX)
			{
				noOfSandCanFall -= (numberOfSandThisRow / 2);
				noOfSandCanFall += (middleX - leftX);
			}
			// Goes to the row above
			checkY--;
		}
		return noOfSandCanFall;
	}

	/**
	 * Finds the first Wall to the left of the given position
	 * 
	 * @author Charles Shen
	 * @param startX the starting x position (inclusive)
	 * @param startY the starting y position (inclusive)
	 * @return the x position of the first Wall it finds or -1 if no Walls are
	 *         found
	 */
	private int checkLeftForWall(int startX, int startY)
	{
		for (int checkX = startX; checkX >= 0; checkX--)
		{
			if (grid[checkX][startY].getType() == WALL)
				return checkX;
		}
		return -1;
	}

	/**
	 * Finds the first Wall to the right of the given position
	 * 
	 * @author Charles Shen
	 * @param startX the starting x position (inclusive)
	 * @param startY the starting y position (inclusive)
	 * @return the x position of the first Wall it finds or -1 if no Walls are
	 *         found
	 */
	private int checkRightForWall(int startX, int startY)
	{
		for (int checkX = startX; checkX < MAX_X; checkX++)
		{
			if (grid[checkX][startY].getType() == WALL)
				return checkX;
		}
		return -1;
	}

	/**
	 * Finds if the given Wall Block can be used to solve the level
	 * 
	 * @author Charles Shen
	 * @param x the given x position of the Wall Block
	 * @param y the given y position of the Wall Block
	 * @return true if the Wall Block should be considered by the player in
	 *         solving the level; false if the Wall Block should be ignored by
	 *         the player
	 */
	private boolean isPossibleSolution(int x, int y)
	{
		// A special checks for Walls that are around the edges of the level
		// This only checks for solution through physics; such as water flowing
		// out of the level (downwards or sideways), sand falling out of the
		// level
		// (downwards), and gas escaping the level (upwards or sideways)
		// All the checks determine the number of element Blocks that will fall
		// out
		// and only returns true if it can beat the level in one go
		if (x == 0)
		{
			// Checks the right side of this Wall because it is on the left-most
			// side
			if (grid[1][y].getType() == WATER && dWater < waterToRemove)
			{
				int rightWallX = checkRightForWall(1, y);
				int noOfWaterCanFlowOut = noOfWaterCanFlowOut(1, y, rightWallX);
				if (noOfWaterCanFlowOut >= waterToRemove)
					return true;
			}
			return false;
		}
		else if (x == MAX_X - 1)
		{
			// Checks the left of this Wall because it is on the right-most side
			if (grid[x - 1][y].getType() == WATER && dWater < waterToRemove)
			{
				int leftWallX = checkLeftForWall(x - 1, y);
				int noOfWaterCanFlowOut = noOfWaterCanFlowOut(leftWallX + 1, y,
						x);
				if (noOfWaterCanFlowOut >= waterToRemove)
					return true;
			}
			return false;
		}
		else if (y == 0)
		{
			// Checks below this Wall because it is at the top
			if (grid[x][y + 1].getType() == GAS && dGas < gasToRemove)
			{
				int leftWallX = checkLeftForWall(x, 1);
				int rightWallX = checkRightForWall(x, 1);
				int noOfGasCanEscape = noOfGasCanEscape(leftWallX + 1, 1,
						rightWallX);
				if (noOfGasCanEscape >= gasToRemove)
					return true;
			}
			return false;
		}
		else if (y == MAX_Y - 1)
		{
			// Checks above this Wall because it is at the very bottom
			int blockTypeN = grid[x][y - 1].getType();
			if (blockTypeN == WATER && dWater < waterToRemove)
			{
				int leftWallX = checkLeftForWall(x, y - 1);
				int rightWallX = checkRightForWall(x, y - 1);
				int noOfWaterCanFlowOut = noOfWaterCanFlowOut(leftWallX + 1,
						y - 1, rightWallX);

				if (noOfWaterCanFlowOut >= waterToRemove)
					return true;
			}
			// Sand is a special case here because it does not fall smoothly
			// (Liquid-like physics) due to its hourglass-like falling
			// behaviours
			// so more considerations are needed if only sand needs to be
			// removed
			else if (blockTypeN == SAND && dSand < sandToRemove)
			{
				int leftWallX = checkLeftForWall(x, y - 1);
				int rightWallX = checkRightForWall(x, y - 1);
				int noOfSandCanFall = noOfSandCanFall(leftWallX + 1, y - 1, x,
						rightWallX);
				if (noOfSandCanFall >= sandToRemove)
					return true;
				else if (gasToRemove == 0 && waterToRemove == 0
						&& noOfSandCanFall >= sandToRemove / wallsCanRemove)
					return true;
			}
			return false;
		}

		// Stores the Block types around this Wall for easier readability
		// Cardinal directions are used because it is shorter to read
		int blockTypeN = grid[x][y - 1].getType();
		int blockTypeE = grid[x + 1][y].getType();
		int blockTypeS = grid[x][y + 1].getType();
		int blockTypeW = grid[x - 1][y].getType();

		int blockTypeSE = grid[x + 1][y + 1].getType();
		int blockTypeSW = grid[x - 1][y + 1].getType();

		// Checks for walls near gas
		if (dSand < sandToRemove || dGas < gasToRemove)
		{
			if (blockTypeS == GAS && blockTypeN != WALL && blockTypeN != WATER)
			{
				// Checks above for possible Sand to remove unless too many
				// walls need to be
				// removed in order to eliminate Sand
				int noOfBlockingWalls = 1;
				for (int checkAbove = y - 1; checkAbove >= 0; checkAbove--)
				{
					int blockAbove = grid[x][checkAbove].getType();
					if (blockAbove == SAND && dSand < sandToRemove)
						return true;
					else if (blockAbove == WALL)
					{
						noOfBlockingWalls++;
						if (noOfBlockingWalls > wallsCanRemove % 3 + 1
								- wallsRemoved)
							return false;
					}
				}
				if (noOfBlockingWalls <= wallsCanRemove % 3 + 1 - wallsRemoved)
					return true;
			}
			else if (blockTypeE == GAS && blockTypeW != WALL
					&& blockTypeW != WATER)
			{
				if (blockTypeW == SAND && blockTypeSE == GAS)
					return true;

				// Checks for the amount of gas that will move out when
				// this Wall is removed
				int rightWallX = checkRightForWall(x + 1, y);
				int noOfGasCanEscape = noOfGasCanEscape(x + 1, y, rightWallX);

				// Checks left and upwards to confirm it does not lead to a dead
				// end
				int checkX = checkLeftUp(x, y, EMPTY);
				if (checkX == WALL)
					return false;
				// If there's a path directly out of the map, checks if it
				// will remove enough gas to solve the level
				else if (checkX == 0)
				{
					if (noOfGasCanEscape >= gasToRemove - dGas)
						return true;
					return false;
				}

				// Checks for the number of walls blocking Gas' path
				// to eliminate Sand or Gas' path to flow out of the level
				int noOfBlockingWalls = 1;
				for (int checkAbove = y - 1; checkAbove >= 0; checkAbove--)
				{
					int blockAbove = grid[checkX][checkAbove].getType();
					if (blockAbove == SAND && dSand < sandToRemove)
						return true;
					else if (blockAbove == WALL)
					{
						noOfBlockingWalls++;
						if (noOfBlockingWalls > wallsCanRemove % 3 + 1
								- wallsRemoved)
							return false;
					}
				}
				if (noOfBlockingWalls <= wallsCanRemove % 3 + 1 - wallsRemoved)
					return true;
			}
			else if (blockTypeW == GAS && blockTypeE != WALL
					&& blockTypeE != WATER)
			{
				if (blockTypeE == SAND && blockTypeSW == GAS)
					return true;

				// Checks the amount of Gas Blocks that will flow out
				int leftWallX = checkLeftForWall(x - 1, y);
				int noOfGasCanEscape = noOfGasCanEscape(leftWallX + 1, y, x);

				// Checks right and upwards to confirm there's no dead end
				int checkX = checkRightUp(x, y, EMPTY);
				if (checkX == WALL)
					return false;
				// Considers the possibility that Gas can flow directly out of
				// the level
				else if (checkX == MAX_X - 1)
				{
					if (noOfGasCanEscape >= gasToRemove - dGas)
						return true;
					return false;
				}

				// Checks for the number of walls blocking Gas' path
				// to eliminate Sand or Gas' path to flow out of the level
				int noOfBlockingWalls = 1;
				for (int checkAbove = y - 1; checkAbove >= 0; checkAbove--)
				{
					int blockAbove = grid[checkX][checkAbove].getType();
					if (blockAbove == SAND && dSand < sandToRemove)
						return true;
					else if (blockAbove == WALL)
					{
						noOfBlockingWalls++;
						if (noOfBlockingWalls > wallsCanRemove % 3 + 1
								- wallsRemoved)
							return false;
					}
				}
				if (noOfBlockingWalls <= wallsCanRemove % 3 + 1 - wallsRemoved)
					return true;
			}
		}

		// Checks for walls near sand
		if (dWater < waterToRemove || dSand < sandToRemove)
		{
			if (blockTypeN == SAND && blockTypeS != WALL && blockTypeS != GAS)
			{
				// Calculates the amount of Sand that will fall through the Wall
				// if the Wall is removed by the player
				int leftWallX = checkLeftForWall(x, y - 1);
				int rightWallX = checkRightForWall(x, y - 1);
				int noOfSandCanFall = noOfSandCanFall(leftWallX + 1, y - 1, x,
						rightWallX);

				// A special case if only sand needs to be removed
				if (dSand < sandToRemove && gasToRemove == 0
						&& waterToRemove == 0)
				{
					if (noOfSandCanFall >= (sandToRemove / wallsCanRemove)
							- dSand)
						return true;
				}

				// Checks for the number of walls blocking Sand' path
				// to eliminate Water or let Sand fall out of the level
				int noOfBlockingWalls = 1;
				for (int checkBelow = y + 1; checkBelow < MAX_Y; checkBelow++)
				{
					int blockBelow = grid[x][checkBelow].getType();

					// Considers the amount of water that can be potentially
					// removed,
					// however the calculation merely a theoretical value
					// because
					// there not all Water may not be removed depending on how
					// the Blocks
					// are updated; also considers if it should attempt to
					// remove the block
					if (blockBelow == WATER
							&& dWater < waterToRemove
							&& (noOfBlockingWalls <= (wallsCanRemove
									- (wallsCanRemove % 3 + 1) - wallsRemoved) || noOfBlockingWalls == wallsCanRemove
									- wallsRemoved))
					{
						int leftWaterWallX = checkLeftForWall(x, checkBelow);
						int rightWaterWallX = checkRightForWall(x, checkBelow);
						int endY = MAX_Y;
						// Looks below and finds the highest y value (y decrease
						// as it goes up)
						for (int checkY = y + 1; checkY < endY; checkY++)
							if (grid[x][checkY].getType() != WATER)
								endY = checkY;
						int noOfWaterCanEliminate = (rightWaterWallX
								- leftWaterWallX - 1)
								* (endY - y + 1);
						if (noOfWaterCanEliminate >= waterToRemove - dWater)
							return true;
					}
					else if (blockBelow == WALL)
					{
						noOfBlockingWalls++;
						// Stops the check if too many Walls are in the way
						if (noOfBlockingWalls > (wallsCanRemove
								- (wallsCanRemove % 3 + 1) - wallsRemoved))
							return false;
					}
				}
				// This check occurs when Sand can fall out of the level within
				// the Wall removal limit and there are no Water in the way
				if (noOfBlockingWalls <= (wallsCanRemove
						- (wallsCanRemove % 3 + 1) - wallsRemoved)
						&& noOfSandCanFall >= sandToRemove)
					return true;
			}
		}

		// Checks for walls near water
		if (dGas < gasToRemove || dWater < waterToRemove)
		{
			if (blockTypeN == WATER && blockTypeS != WALL)
			{
				int noOfBlockingWalls = 1;
				// Checks below to see if it can remove Gas Blocks or
				// it will be able to fall out of the level within
				// the given Wall removal limit
				for (int checkBelow = y + 1; checkBelow < MAX_Y; checkBelow++)
				{
					int blockBelow = grid[x][checkBelow].getType();

					// Considers the number of Gas Blocks that can be removed
					// to determine if removing this Wall is a good idea to
					// solve the level
					if (blockBelow == GAS
							&& dGas < gasToRemove
							&& (noOfBlockingWalls <= (wallsCanRemove
									- (wallsCanRemove % 3 + 1) - wallsRemoved) || noOfBlockingWalls == wallsCanRemove
									- wallsRemoved))
					{
						int leftGasWallX = checkLeftForWall(x, checkBelow);
						int rightGasWallX = checkRightForWall(x, checkBelow);
						int noOfGasCanEliminate = noOfGasCanEscape(
								leftGasWallX + 1, checkBelow, rightGasWallX);
						if (noOfGasCanEliminate >= gasToRemove - dGas)
							return true;
					}
					// Once it exceeds too many Walls to be removed
					else if (blockBelow == WALL)
					{
						noOfBlockingWalls++;
						if (noOfBlockingWalls >= (wallsCanRemove
								- (wallsCanRemove % 3 + 1) - wallsRemoved))
							return false;
					}
				}

				// Checks for the amount of water that can flow out
				int leftWallX = checkLeftForWall(x, y + 1);
				int rightWallX = checkRightForWall(x, 1 + 1);
				int noOfWaterCanFlowOut = noOfWaterCanFlowOut(leftWallX + 1,
						y + 1, rightWallX);
				if (noOfBlockingWalls <= (wallsCanRemove
						- (wallsCanRemove % 3 + 1) - wallsRemoved)
						&& noOfWaterCanFlowOut >= waterToRemove)
					return true;
			}
			else if (blockTypeE == WATER && blockTypeW != WALL)
			{
				// Considers the amount of Gas Blocks that can be eliminated
				// before checking for a dead end similarly to checks above for
				// Gas
				if (blockTypeW == GAS && dGas < gasToRemove)
				{
					int leftWallX = checkLeftForWall(x - 1, y);
					int noOfGasCanEscape = noOfGasCanEscape(leftWallX + 1, y, x);
					if (noOfGasCanEscape >= gasToRemove - dGas)
						return true;
				}

				// Looks for potential dead end or direct pathway to flow
				// out of the level which might be a solution to solve the game
				int rightWallX = checkRightForWall(x + 1, y);
				int noOfWaterCanFlowOut = noOfWaterCanFlowOut(x + 1, y,
						rightWallX);

				int checkX = checkLeftDown(x, y, EMPTY);
				if (checkX == WALL)
					return false;
				else if (checkX == 0)
				{
					if (noOfWaterCanFlowOut >= waterToRemove - dWater)
						return true;
					return false;
				}

				int noOfWallsInTheWay = 1;
				// Checks downwards from the result of checkLeftDown()
				// to determine if this Wall can solve the level
				for (int checkBelow = y + 1; checkBelow < MAX_Y; checkBelow++)
				{
					int blockBelow = grid[checkX][checkBelow].getType();
					// Considers the amount of Gas Blocks that will be
					// eliminated
					// in addition to the number of Walls that must be removed
					// to eliminate those Gas
					if (blockBelow == GAS
							&& dGas < gasToRemove
							&& (noOfWallsInTheWay <= (wallsCanRemove
									- (wallsCanRemove % 3 + 1) - wallsRemoved) || noOfWallsInTheWay == wallsCanRemove
									- wallsRemoved))
					{
						int leftGasWallX = checkLeftForWall(checkX, checkBelow);
						int rightGasWallX = checkRightForWall(checkX,
								checkBelow);
						int noOfGasCanEliminate = noOfGasCanEscape(
								leftGasWallX + 1, checkBelow, rightGasWallX);
						if (noOfGasCanEliminate >= gasToRemove - dGas)
							return true;
					}
					else if (blockBelow == WALL)
					{
						noOfWallsInTheWay++;
						if (noOfWallsInTheWay >= (wallsCanRemove
								- (wallsCanRemove % 3 + 1) - wallsRemoved))
							return false;
					}
				}
				if (noOfWallsInTheWay >= (wallsCanRemove
						- (wallsCanRemove % 3 + 1) - wallsRemoved)
						&& noOfWaterCanFlowOut >= waterToRemove)
					return true;
			}
			else if (blockTypeW == WATER && blockTypeE != WALL)
			{
				// Considers the amount of Gas Blocks that can be eliminated
				// before checking for a dead end similarly to checks above for
				// Gas
				if (blockTypeE == GAS && dGas < gasToRemove)
				{
					int rightWallX = checkRightForWall(x + 1, y);
					int noOfGasCanEscape = noOfGasCanEscape(x + 1, y,
							rightWallX);
					if (noOfGasCanEscape >= gasToRemove - dGas)
						return true;
				}

				int leftWallX = checkLeftForWall(x - 1, y);
				int noOfWaterCanFlowOut = noOfWaterCanFlowOut(leftWallX + 1, y,
						x);

				// Looks for potential dead end or direct pathway to flow
				// out of the level which might be a solution to solve the game
				int checkX = checkRightDown(x, y, EMPTY);
				if (checkX == WALL)
					return false;
				else if (checkX == MAX_X - 1)
				{
					if (noOfWaterCanFlowOut >= waterToRemove - dWater)
						return true;
					return false;
				}

				int noOfWallsInTheWay = 1;
				// Checks downwards from the result of checkRightDown()
				// to determine if this Wall can solve the level
				for (int checkBelow = y + 1; checkBelow < MAX_Y; checkBelow++)
				{
					int blockBelow = grid[checkX][checkBelow].getType();
					// Considers the amount of Gas Blocks that will be
					// eliminated
					// in addition to the number of Walls that must be removed
					// to eliminate those Gas
					if (blockBelow == GAS
							&& dGas < gasToRemove
							&& (noOfWallsInTheWay <= (wallsCanRemove
									- (wallsCanRemove % 3 + 1) - wallsRemoved) || noOfWallsInTheWay == wallsCanRemove
									- wallsRemoved))
					{
						int leftGasWallX = checkLeftForWall(checkX, checkBelow);
						int rightGasWallX = checkRightForWall(checkX,
								checkBelow);
						int noOfGasCanEliminate = noOfGasCanEscape(
								leftGasWallX + 1, checkBelow, rightGasWallX);
						if (noOfGasCanEliminate >= gasToRemove - dGas)
							return true;
					}
					// Quits early once too many Wall Blocks must be removed
					else if (blockBelow == WALL)
					{
						noOfWallsInTheWay++;
						if (noOfWallsInTheWay >= (wallsCanRemove
								- (wallsCanRemove % 3 + 1) - wallsRemoved))
							return false;
					}
				}
				// Occurs if a path has been found to allow water flow out of
				// the level
				if (noOfWallsInTheWay <= (wallsCanRemove
						- (wallsCanRemove % 3 + 1) - wallsRemoved)
						&& noOfWaterCanFlowOut >= waterToRemove)
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks left of the given position until it finds a wall then checks up if
	 * the Block to look for is there
	 * 
	 * @author Charles Shen
	 * @param startX the starting x position (exclusive)
	 * @param startY the starting y position (inclusive)
	 * @param blockToCheck the Block type to look for
	 * @return the x value of the Block to look for or -1 if it finds a Wall in
	 *         its wall but not the Block to look for or 0 if there are no Walls
	 *         and gaps in the way which will result in gas flowing out
	 */
	private int checkLeftUp(int startX, int startY, int blockToCheck)
	{
		int endCheckX = 0;
		for (int checkX = startX - 1; checkX >= endCheckX; checkX--)
		{
			// Gas will flow through gaps so the search will end early
			if (grid[checkX][startY - 1].getType() == EMPTY)
				return checkX;
			if (grid[checkX][startY].getType() == WALL)
			{
				// Looks at the NE Block of the Wall it hits
				if (grid[checkX + 1][startY - 1].getType() == blockToCheck)
					return checkX + 1;
				else
					return WALL;
			}
		}
		return endCheckX;
	}

	/**
	 * Checks left of the given position until it finds a wall then checks down
	 * if the Block to look for is there
	 * 
	 * @author Charles Shen
	 * @param startX the starting x position (exclusive)
	 * @param startY the starting y position (inclusive)
	 * @param blockToCheck the Block type to look for
	 * @return the x value of the Block to look for or -1 if it finds a Wall in
	 *         its wall but not the Block to look for or 0 if there are no Walls
	 *         and gaps in the way which will result in Water flowing out
	 */
	private int checkLeftDown(int startX, int startY, int blockToCheck)
	{
		int endCheckX = 0;
		for (int checkX = startX - 1; checkX >= endCheckX; checkX--)
		{
			// Water can flow through gaps even if it doesn't hit a Wall first
			if (grid[checkX][startY + 1].getType() == EMPTY)
				return checkX;
			if (grid[checkX][startY].getType() == WALL)
			{
				// Looks at the Block SE of the Wall it hits
				if (grid[checkX + 1][startY + 1].getType() == blockToCheck)
					return checkX + 1;
				else
					return WALL;
			}
		}
		return endCheckX;
	}

	/**
	 * Checks right of the given position until it finds a wall then checks up
	 * if the Block to look for is there
	 * 
	 * @author Charles Shen
	 * @param startX the starting x position (exclusive)
	 * @param startY the starting y position (inclusive)
	 * @param blockToCheck the Block type to look for
	 * @return the x value of the Block to look for or -1 if it finds a Wall in
	 *         its wall but not the Block to look for or 0 if there are no Walls
	 *         and gaps in the way which will result in Gas flowing out
	 */
	private int checkRightUp(int startX, int startY, int blockToCheck)
	{
		int endCheckX = MAX_X - 1;
		for (int checkX = startX + 1; checkX <= endCheckX; checkX++)
		{
			// Gas can flow through gaps
			if (grid[checkX][startY - 1].getType() == EMPTY)
				return checkX;
			if (grid[checkX][startY].getType() == WALL)
			{
				// Checks NW Block of the Wall that it found
				if (grid[checkX - 1][startY - 1].getType() == blockToCheck)
					return checkX - 1;
				else
					return WALL;
			}
		}
		return endCheckX;
	}

	/**
	 * Checks right of the given position until it finds a wall then checks down
	 * if the Block to look for is there
	 * 
	 * @author Charles Shen
	 * @param startX the starting x position (exclusive)
	 * @param startY the starting y position (inclusive)
	 * @param blockToCheck the Block type to look for
	 * @return the x value of the Block to look for or -1 if it finds a Wall in
	 *         its wall but not the Block to look for or 0 if there are no Walls
	 *         and gaps in the way which will result in Water flowing out
	 */
	private int checkRightDown(int startX, int startY, int blockToCheck)
	{
		int endCheckX = MAX_X - 1;
		for (int checkX = startX + 1; checkX <= endCheckX; checkX++)
		{
			// Water can flow through gaps
			if (grid[checkX][startY + 1].getType() == EMPTY)
				return checkX;
			if (grid[checkX][startY].getType() == WALL)
			{
				// Checks the block SW of the Wall it hit
				if (grid[checkX - 1][startY + 1].getType() == blockToCheck)
					return checkX - 1;
				else
					return WALL;
			}
		}
		return endCheckX;
	}

	/**
	 * Adds the block to the x and y position on the grid
	 * 
	 * @author Philip Liu
	 * @param type The block type assigned to each block class
	 * @param x The x position of the block on the grid
	 * @param y The y position of the block on the grid
	 */
	private void addBlock(int type, int x, int y)
	{
		if (type == EMPTY)
			grid[x][y] = (new Empty(x * BLOCK_SIZE, y * BLOCK_SIZE));
		else if (type == WALL)
			grid[x][y] = (new Wall(x * BLOCK_SIZE, y * BLOCK_SIZE, false));
		else if (type == GAS)
			grid[x][y] = (new Gas(x * BLOCK_SIZE, y * BLOCK_SIZE));
		else if (type == SAND)
			grid[x][y] = (new Sand(x * BLOCK_SIZE, y * BLOCK_SIZE));
		else if (type == WATER)
			grid[x][y] = (new Water(x * BLOCK_SIZE, y * BLOCK_SIZE));
	}

	/**
	 * Counts the number of blocks that exited the grid, each element has a
	 * different count
	 * 
	 * @author Vlad Zhurba & Philip Liu
	 */
	private void updateElementCount()
	{
		int noOfGas = 0;
		int noOfWater = 0;
		int noOfSand = 0;
		for (int y = 0; y < MAX_Y; y++)
		{
			for (int x = 0; x < MAX_X; x++)
			{
				if (grid[x][y].getType() == 3)
					noOfGas++;
				else if (grid[x][y].getType() == 2)
					noOfWater++;
				else if (grid[x][y].getType() == 1)
					noOfSand++;
			}
		}

		dSand = sandStarting - noOfSand;
		dWater = waterStarting - noOfWater;
		dGas = gasStarting - noOfGas;
	}

	/**
	 * Checks if the current map has meet its clear specifications
	 * 
	 * @author Philip Liu
	 * @return true if the map was cleared, false if it hasn't
	 */
	public boolean isCleared()
	{
		if (sandToRemove <= dSand && waterToRemove <= dWater
				&& gasToRemove <= dGas)
			return true;
		return false;
	}

	/**
	 * Starts the game
	 * 
	 * @author Philip Liu
	 */
	public void start()
	{
		while (!gameOver)
		{
			gameLoop();
		}
		repaint();
	}

	/**
	 * This method controls how many times the render and update method will be
	 * called per second
	 * 
	 * @author Philip Liu
	 */
	public void gameLoop()
	{
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();

		// Nanoseconds
		final double ns = 1000000000.0 / 10.0;
		double delta = 0;
		frames = 0;
		updates = 0;
		while (!gameOver)
		{
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			// The delta variable will reach 1, 60 times a second, this will
			// limit the updates to 60 per second
			while (delta >= 1)
			{
				update();
				updates++;
				delta--;

				// Putting the repaint inside the update loop, will limit the
				// amount of possible renders per second to the number of
				// updates per second.
				repaint();
				frames++;
			}

			// Once per second, display the number of frames
			if (System.currentTimeMillis() - timer > 1000)
			{
				timer += 1000;
				if (debug)
					this.setTitle(GAME_NAME + " v" + VERSION + " || " + frames
							+ "FPS " + updates + "updates "
							+ mapCache.get(currentMap));
				updates = 0;
				frames = 0;
			}
		}
	}

	/**
	 * Updates the states of all the element blocks on the grid
	 * 
	 * @author Vlad Zhurba
	 */
	public void update()
	{
		if (!pause && !menu)
		{
			updateElementCount();

			levelCleared = isCleared();

			if (wallsRemoved != wallsCanRemove && showSolution)
				findSolution();

			// Goes through the grid checking the change of behavior in gas
			// Note: Checks top to down, this allows for a smooth rising effect
			for (int y = 0; y < MAX_Y; y++)
				for (int x = 0; x < MAX_X; x++)
				{
					if (grid[x][y].getType() == GAS)
					{
						Liquid currentBlock = (Liquid) grid[x][y];

						// When the gas reaches the edge of the grid, we must
						// check if it flows out of the field of play
						if (currentBlock.getMovedStatus() == 0
								&& ((y == 0
										|| (x == 0 && currentBlock
												.getDirection() == 1) || (x == MAX_X - 1 && currentBlock
										.getDirection() == 2))))
							if (currentBlock.actBoolean() || y == 0
									|| grid[x][y - 1].getType() == WALL)
								grid[x][y] = new Empty(x * BLOCK_SIZE, y
										* BLOCK_SIZE);

						if (grid[x][y].getType() == GAS)
						{
							if (currentBlock.getMovedStatus() == 0)
							{
								int objectAbove = (grid[x][y - 1].getType());

								if (objectAbove == EMPTY)
								{
									// When gas surrounded by 2 substances that
									// act like walls,
									// reset the direction in order to simulate
									// the gas bouncing off walls
									if (shiftLeft(x, y, 0) == WALL
											&& shiftRight(y, x, 0) == WALL)
										currentBlock.setDirection((int) (Math
												.random() * 2) + 1);

									// Need to check if we are going to shift
									// horizontally during this free fall
									boolean shift = currentBlock.actBoolean();
									if (!shift)
									{
										// Move up
										grid[x][y].act();
										grid[x][y - 1] = grid[x][y];
										grid[x][y] = new Empty(x * BLOCK_SIZE,
												y * BLOCK_SIZE);
									}
									else
									{
										int move = currentBlock.getDirection();
										if (move == 1)
											move = -1;
										else
											move = 1;

										if (x + move < MAX_X && x + move > -1)
										{
											int blockBeside = grid[x + move][y]
													.getType();

											if (blockBeside != GAS
													&& blockBeside != WALL)
											{
												if (move == 1)
													currentBlock.moveAdd();

												if (blockBeside != WATER)
												{
													// Shift normally
													grid[x][y].act();
													grid[x + move][y] = grid[x][y];
												}

												grid[x][y] = new Empty(x
														* BLOCK_SIZE, y
														* BLOCK_SIZE);
											}
											else
											{
												// Since we are unable to shift,
												// we reset the shift value and
												// more up normally
												currentBlock.resetAct();
												grid[x][y].act();
												grid[x][y - 1] = grid[x][y];
												grid[x][y] = new Empty(x
														* BLOCK_SIZE, y
														* BLOCK_SIZE);
											}
										}
										else
											currentBlock.resetAct();
									}
								}
								else if (objectAbove == GAS)
								{
									currentBlock.resetAct();
									mergeWithElement(x, y, 1);
								}
								else if (objectAbove == SAND)
								{
									// Gas beats sand, thus we move up normally
									currentBlock.resetAct();
									grid[x][y].act();
									grid[x][y - 1] = grid[x][y];
									grid[x][y] = new Empty(x * BLOCK_SIZE, y
											* BLOCK_SIZE);
								}
								else if (objectAbove == WATER)
								{
									// Water beats gas, thus gas is eliminated
									grid[x][y] = new Empty(x * BLOCK_SIZE, y
											* BLOCK_SIZE);
								}
								else
								{
									// Since there is a wall above you, start
									// shifting around in order to find a new
									// block to interact with
									currentBlock.newfallSwitchPeriod();
									shiftOver(x, y);
								}
							}
						}
					}
					else if (grid[x][y].getMovedStatus() == 1)
					{
						// if this block was already moved, add another value to
						// move so it would be reset in the next statement,
						// thus allowing a continues flow for the element in the
						// next update cycle
						grid[x][y].moveAdd();
					}

					if (grid[x][y].getMovedStatus() == 2)
						grid[x][y].moveReset();
				}

			// water and sand
			for (int y = MAX_Y - 1; y >= 0; y--)
				for (int x = 0; x < MAX_X; x++)
				{
					// Check if blocks beside the edges of grid are to be
					// eliminated
					if (y == MAX_Y - 1 || x == 0 || x == MAX_X - 1)
					{
						if (grid[x][y].getType() == SAND)
							grid[x][y] = new Empty(x * BLOCK_SIZE, y
									* BLOCK_SIZE);

						if (grid[x][y].getType() == WATER)
							if (grid[x][y].getMovedStatus() == 0)
							{
								Liquid currentBlock = (Liquid) grid[x][y];
								if (y == MAX_Y - 1)
									grid[x][y] = new Empty(x * BLOCK_SIZE, y
											* BLOCK_SIZE);
								else if (((x == 0 && currentBlock
										.getDirection() == 1) || (x == MAX_X - 1 && currentBlock
										.getDirection() == 2))
										&& (currentBlock.actBoolean() || grid[x][y + 1]
												.getType() == WALL))
									// If water's direction of horizontal
									// displacement is meet with the outer grid
									// and if it is about to make this shift,
									// eliminate it now
									grid[x][y] = new Empty(x * BLOCK_SIZE, y
											* BLOCK_SIZE);
							}
					}
					// Starting point for sand checks
					if (grid[x][y].getType() == SAND
							&& grid[x][y].getMovedStatus() == 0)
					{
						int objectBelow = (grid[x][y + 1].getType());

						if (objectBelow == EMPTY || objectBelow == WATER)
						{
							// Fall normally
							grid[x][y].act();
							grid[x][y + 1] = grid[x][y];
							grid[x][y + 1].moveAdd();
							grid[x][y] = new Empty(x * BLOCK_SIZE, y
									* BLOCK_SIZE);
						}
						else if (objectBelow == SAND)
						{
							// Find out if we have a valid space bellow
							int moveL = shiftLeft(x, y, -1);
							int moveR = shiftRight(y, x, -1);

							// When both sides have a free space at bottom,
							// choose location to drop to at random and do so
							if ((moveL == 1 || moveL == 100)
									&& (moveR == 1 || moveR == 100))
							{
								int random = (int) (Math.random() * 2 + 1);

								if (random == 1)
									sandMerge(x, y, -1);
								else
									sandMerge(x, y, 1);
							}
							// If there is a valid space bellow, drop to it
							else if (moveL == 1 || moveL == 100)
								sandMerge(x, y, -1);
							else if (moveR == 1 || moveR == 100)
								sandMerge(x, y, 1);

						}
						else if (objectBelow == WALL)
						{
							// Must check if we can shift left or right in order
							// to drop next update cycle
							int objectRight = (grid[x + 1][y].getType());
							int objectLeft = (grid[x - 1][y].getType());

							int moveL = -2;
							int moveR = -2;

							// Find out if there is space to drop to
							if (objectRight == EMPTY || objectRight == WATER)
								moveR = shiftRight(y, x, -1);
							if (objectLeft == EMPTY || objectLeft == WATER)
								moveL = shiftLeft(x, y, -1);

							// Since we know which space is valid and which
							// isn't, we can shift the sand's location manually
							if ((moveL == 1 || moveL == 100)
									&& (moveR == 1 || moveR == 100))
							{
								// randomize the location
								int random = (int) (Math.random() * 2 + 1);
								if (random == 1)
								{
									grid[x][y].shift(-1);
									grid[x - 1][y] = grid[x][y];
									grid[x - 1][y].moveAdd();
									grid[x][y] = new Empty(x * BLOCK_SIZE, y
											* BLOCK_SIZE);
								}
								else
								{
									grid[x][y].shift(1);
									grid[x + 1][y] = grid[x][y];
									grid[x + 1][y].moveAdd();
									grid[x][y] = new Empty(x * BLOCK_SIZE, y
											* BLOCK_SIZE);
								}
							}
							// If there is a valid space bellow, drop to it
							else if (moveL == 1 || moveL == 100)
							{
								grid[x][y].shift(-1);
								grid[x - 1][y] = grid[x][y];
								grid[x - 1][y].moveAdd();
								grid[x][y] = new Empty(x * BLOCK_SIZE, y
										* BLOCK_SIZE);
							}
							else if (moveR == 1 || moveR == 100)
							{
								grid[x][y].shift(1);
								grid[x + 1][y] = grid[x][y];
								grid[x + 1][y].moveAdd();
								grid[x][y] = new Empty(x * BLOCK_SIZE, y
										* BLOCK_SIZE);
							}
						}
					}
					// Starting point for water checks
					else if (grid[x][y].getType() == WATER
							&& grid[x][y].getMovedStatus() == 0)
					{
						int objectBelow = grid[x][y + 1].getType();
						Liquid currentBlock = (Liquid) grid[x][y];

						if (objectBelow == EMPTY)
						{
							if (currentBlock.isMerged())
								currentBlock.switchMerged();

							boolean shift = currentBlock.actBoolean();

							int moveL = shiftLeft(x, y, 0);
							int moveR = shiftRight(y, x, 0);

							// Randomize the direction of the water's horizontal
							// shift while mid-air, simulates water bouncing off
							// a wall
							if (moveR == WALL && moveL == WALL && x != 0
									&& x != MAX_X - 1)
								currentBlock
										.setDirection((int) (Math.random() * 2) + 1);

							if (!shift)
							{
								// Have the water drop normally
								grid[x][y].act();
								grid[x][y + 1] = grid[x][y];
								grid[x][y + 1].moveAdd();
								grid[x][y] = new Empty(x * BLOCK_SIZE, y
										* BLOCK_SIZE);
							}
							else
							{
								// Find out where the water is going to shift to
								int move = currentBlock.getDirection();
								if (move == 1)
									move = -1;
								else
									move = 1;

								int blockBeside = grid[x + move][y].getType();
								// simplify to gas and empty?
								if (x + move >= 0
										&& x + move <= 38
										&& (blockBeside == EMPTY || blockBeside == GAS))
								{
									grid[x][y].act();
									grid[x + move][y] = grid[x][y];

									// Have the water's shifts cycle left and
									// right by 1 for effect
									if (move == 1)
									{
										grid[x + move][y].moveAdd();
										((Liquid) grid[x + move][y])
												.setDirection(1);
									}
									else
									{
										grid[x + move][y].moveAdd();
										((Liquid) grid[x + move][y])
												.setDirection(2);
									}
									grid[x][y] = new Empty(x * BLOCK_SIZE, y
											* BLOCK_SIZE);
								}
								else if (x + move >= 0 && x + move <= 38)
								{
									// If the water could not shift, reset the
									// values and move 1 block down
									((Water) grid[x][y]).resetAct();
									grid[x][y].act();
									grid[x][y + 1] = grid[x][y];
									grid[x][y] = new Empty(x * BLOCK_SIZE, y
											* BLOCK_SIZE);
								}
							}
						}
						else if (objectBelow == GAS)
						{
							// Reset water values and move down, water
							// destroyers gas
							currentBlock.resetAct();
							grid[x][y].act();
							grid[x][y].moveAdd();
							grid[x][y + 1] = grid[x][y];
							grid[x][y] = new Empty(x * BLOCK_SIZE, y
									* BLOCK_SIZE);
						}
						else if (objectBelow == WATER)
						{
							// Reset water values since it will shift its
							// location by a lot
							currentBlock.resetAct();
							if (currentBlock.isMerged())
								currentBlock.switchMerged();

							// Have water merge, also keep the value of the
							// horizontal direction of travel
							// accurate else it will give you a potential
							// unrealistic falling effect
							int i = mergeWithElement(x, y, -1);
							if (i < 0)
								((Liquid) grid[x + ~i][y + 1])
										.setDirection((int) (Math.random() * 2) + 1);
							else if (i > 0)
								((Liquid) grid[x - i][y + 1])
										.setDirection((int) (Math.random() * 2) + 1);
						}
						else
						{
							// Since there is a wall-like substance (sand/wall)
							// bellow you, shift
							// left/right in order to find a new element to
							// interact with
							currentBlock.newfallSwitchPeriod();
							shiftOver(x, y);

							if (currentBlock.isMerged())
								currentBlock.switchMerged();
						}
					}
					else if (grid[x][y].getMovedStatus() == 1)
					{
						// if this block was already moved, add another value to
						// move so it would be reset in the next statement,
						// thus allowing a continues flow for the element in the
						// next update cycle
						grid[x][y].moveAdd();
					}

					if (grid[x][y].getMovedStatus() == 2)
						grid[x][y].moveReset();
				}
		}
	}

	/**
	 * Merges the element with its like element bellow together if possible.
	 * This merge increases how much of the row the element is occupying. If
	 * merge fails, method shiftOver is called
	 * 
	 * @param x The x position of the grid, or the column value of the 2D array
	 * @param y The y position of the grid, or the row value of the 2D array
	 * @param i The spaces shifted up or down for the element to be merged
	 * @return The value of the required horizontal shift for a proper merge
	 */
	private int mergeWithElement(int x, int y, int i)
	{
		int moveL = shiftLeft(x, y, i);
		int moveR = shiftRight(y, x, i);
		Liquid currentBlock = (Liquid) grid[x][y];

		/*
		 * * <p> for the element to interact with. Meaning of the return value
		 * is as follows: <br> -1 = no space available <br> 100 to 3800 = the
		 * shifting block will remove an element at the new position <br> 10000
		 * to 38000 = the shifting block will be removed with the element if
		 * moved to the new position
		 */

		boolean right = false;
		boolean left = false;

		if (moveL == -1 && moveR == -1)
			;
		else if (moveL == -1)
			right = true;
		else if (moveR == -1)
			left = true;
		else
		{
			int l = moveL;
			int r = moveR;
			if (l > 40)
				l /= 10000;
			if (r > 40)
				r /= 10000;
			if (l > r)
				right = true;
			else
				left = true;

		}

		// Priorities the side that is closer to the location of merged
		// insertion
		if (right)
		{
			if (moveR > 9999)
			{
				grid[x][y] = new Empty(x * BLOCK_SIZE, y * BLOCK_SIZE);
			}
			else
			{
				// Places the block to a proper location that will give a
				// merging effect
				grid[x][y].act();
				grid[x][y].shift(moveR);
				currentBlock.switchMerged();

				// NOTE: 100%100 = 0, thus the gas is eliminating a gas,
				// 200%200=0 as well, same scenario

				grid[x + moveR][y - i] = grid[x][y];
				grid[x][y] = new Empty(x * BLOCK_SIZE, y * BLOCK_SIZE);

				// look in to this value's significance more
				return ~(moveR);
			}
		}
		else if (moveR == -1 && moveL == -1)
		{
			// Since there are no valid locations for a merge, shift the block
			currentBlock.newfallSwitchPeriod();
			shiftOver(x, y);
		}
		else if (left)
		{
			if (moveL > 9999)
			{
				grid[x][y] = new Empty(x * BLOCK_SIZE, y * BLOCK_SIZE);
			}
			else
			{
				// Places the block to a proper location that will give a
				// merging effect
				grid[x][y].act();
				grid[x][y].shift(-moveL);
				currentBlock.switchMerged();
				grid[x - moveL][y - i] = grid[x][y];
				grid[x][y] = new Empty(x * BLOCK_SIZE, y * BLOCK_SIZE);

				return moveL;
			}
		}
		return 0;
	}

	/**
	 * Moves the block on the given position left or right, based on its
	 * direction of travel, if such move is valid
	 * 
	 * @param x
	 * @param y
	 */
	private void shiftOver(int x, int y)
	{
		// Find out which direction it can shift to
		int moveL = shiftLeft(x, y, 0);
		int moveR = shiftRight(y, x, 0);

		Liquid currentBlock = (Liquid) grid[x][y];

		// If there is a wall like substance beside, change direction of travel
		if (moveR == -1 && moveL == -1)
			currentBlock.setDirection((int) (Math.random() * 2) + 1);
		else if (moveR == -1)
			currentBlock.setDirection(1);
		else if (moveL == -1)
			currentBlock.setDirection(2);

		// If we are able to move in the wanted direction, do so
		if (currentBlock.getDirection() == 2 && moveR < 40 && moveR > 0)
		{
			grid[x][y].shift(moveR);
			grid[x + moveR][y] = grid[x][y];
			grid[x + moveR][y].moveAdd();
			grid[x][y] = new Empty(x * BLOCK_SIZE, y * BLOCK_SIZE);
		}
		else if (currentBlock.getDirection() == 1 && moveL < 40 && moveL > 0)
		{
			grid[x][y].shift(-moveL);
			grid[x - moveL][y] = grid[x][y];
			grid[x - moveL][y].moveAdd();
			grid[x][y] = new Empty(x * BLOCK_SIZE, y * BLOCK_SIZE);
		}
		else if (moveL > 9999 || moveR > 9999)
		{
			grid[x][y] = new Empty(x * BLOCK_SIZE, y * BLOCK_SIZE);
		}
		// else if (moveL > 99)
		// {
		// moveL /= 100;
		// grid[x][y].shift(-moveL);
		// grid[x - moveL][y] = grid[x][y];
		// grid[x - moveL][y].moveAdd();
		// grid[x][y] = new Empty(x * BLOCK_SIZE, y * BLOCK_SIZE);
		// }
		// else if (moveR > 99)
		// {
		// moveR /= 100;
		// grid[x][y].shift(moveR);
		// grid[x + moveR][y] = grid[x][y];
		// grid[x + moveR][y].moveAdd();
		// grid[x][y] = new Empty(x * BLOCK_SIZE, y * BLOCK_SIZE);
		// }
	}

	/**
	 * Finds out what the first available spot the element can interact with to
	 * its left
	 * 
	 * @param x The x position of the grid, or the column value of the 2D array
	 * @param y The y position of the grid, or the row value of the 2D array
	 * @param amountUp The spaces shifted up or down for the valid space check
	 * @return The value for first available spot
	 *         <p>
	 *         for the element to interact with. Meaning of the return value is
	 *         as follows: <br>
	 *         -1 = no space available <br>
	 *         100 to 3800 = the shifting block will remove an element at the
	 *         new position <br>
	 *         10000 to 38000 = the shifting block will be removed with the
	 *         element if moved to the new position
	 */
	private int shiftLeft(int x, int y, int amountUp)
	{
		int moveL = 1;
		int newRow = x - 1;
		boolean valid = true;

		boolean destroy = false;
		boolean destroyed = false;

		while (newRow > -1 && valid)
		{
			int typeOnNewPosition = grid[newRow][y - amountUp].getType();
			int typeBeingUsed = grid[x][y].getType();

			if (grid[newRow][y - amountUp].isMerged()
					&& typeBeingUsed == typeOnNewPosition
					&& (amountUp == -1 || amountUp == 1))
			{
				// No shift possible because another block has already merged
				// with the element
				moveL = -1;
				valid = false;
			}
			else if (typeOnNewPosition == cycleOfLife[typeBeingUsed])
			{
				// The element at hand beats the element in position of interest
				valid = false;
			}
			else if (typeOnNewPosition == cycleOfLife[typeBeingUsed + 1])
			{
				// The element at hand loses to the element in position of
				// threat unless its water with sand
				if (typeBeingUsed == WATER && SAND == typeOnNewPosition)
					moveL = -1;
				else
					destroyed = true;
				valid = false;
			}
			else if (typeOnNewPosition == EMPTY)
			{
				// First empty spot is found
				valid = false;
			}
			else
			{
				moveL++;

				if (typeOnNewPosition == WALL
						|| (typeBeingUsed == typeOnNewPosition && amountUp == 0))
				{
					// Type on the position of interest is a wall, no shift is
					// possible
					moveL = -1;
					valid = false;
				}
			}
			newRow = x - moveL;
		}

		// Increase the value based on what interaction would be achieved
		if (destroy)
			moveL *= 100;
		else if (destroyed)
			moveL *= 10000;

		// Change the moveL value if the we didn't find a valid spot
		if (newRow == -1)
			moveL = -1;

		return moveL;
	}

	/**
	 * Finds out what the first available spot the element can interact with to
	 * its right
	 * 
	 * @param x The x position of the grid, or the column value of the 2D array
	 * @param y The y position of the grid, or the row value of the 2D array
	 * @param amountUp The spaces shifted up or down for the valid space check
	 * @return The value for first available spot for the element to interact
	 *         with. The return value meaning varied as this: -1 = no space
	 *         available 100 to 3800 = the shifting block will remove an element
	 *         at the new position 1000 to 38000 = the shifting block will be
	 *         removed with the element if moved to the new position
	 */
	private int shiftRight(int c, int r, int amountUp)
	{
		int moveR = 1;
		int newRow = r + 1;
		boolean valid = true;

		boolean destroy = false;
		boolean destroyed = false;

		while (newRow < MAX_X && valid)
		{
			int typeOnNewPosition = grid[newRow][c - amountUp].getType();
			int typeBeingUsed = grid[r][c].getType();

			if (grid[newRow][c - amountUp].isMerged()
					&& typeBeingUsed == typeOnNewPosition
					&& (amountUp == -1 || amountUp == 1))
			{
				// No shift possible because another block has already merged
				// with the element
				moveR = -1;
				valid = false;
			}
			else if (typeOnNewPosition == cycleOfLife[typeBeingUsed])
			{
				// The element at hand beats the element in position of interest
				valid = false;
			}
			else if (typeOnNewPosition == cycleOfLife[typeBeingUsed + 1])
			{
				// The element at hand loses to the element in position of
				// threat unless its water with sand
				if (typeBeingUsed == WATER && SAND == typeOnNewPosition)
					moveR = -1;
				else
					destroyed = true;
				valid = false;
			}
			else if (typeOnNewPosition == 0)
			{
				// First empty spot is found
				valid = false;
			}
			else
			{
				moveR++;
				if (typeOnNewPosition == -1
						|| (typeBeingUsed == typeOnNewPosition && amountUp == 0))
				{
					// Type on the position of interest is a wall, no shift is
					// possible
					moveR = -1;
					valid = false;
				}
			}
			newRow = r + moveR;
		}

		// Increase the value based on what interaction would be achieved
		if (destroy)
			moveR *= 100;
		else if (destroyed)
			moveR *= 10000;

		// Change the moveL value if the we didn't find a valid spot
		if (newRow == MAX_X)
			moveR = -1;

		return moveR;
	}

	/**
	 * Merges the sand together based on the values given
	 * 
	 * @param x The x position of the grid, or the column value of the 2D array
	 * @param y The y position of the grid, or the row value of the 2D array
	 * @param shiftValue The horizontal displacement value that the merging sand
	 *            will follow
	 */
	private void sandMerge(int x, int y, int shiftValue)
	{
		// Places the sand block to a new location based on parameters
		grid[x][y].act();
		grid[x][y].shift(shiftValue);
		grid[x][y].moveAdd();
		grid[x + shiftValue][y + 1] = grid[x][y];
		grid[x][y] = new Empty(x * BLOCK_SIZE, y * BLOCK_SIZE);
	}

	/**
	 * Renders object in its current state
	 * 
	 * @author Philip Liu
	 */
	public void render(Graphics2D g) throws Exception
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawImage(BACKGROUND, 0, 0, null);

		// Colors for the text
		Color sandColor = new Color(249, 234, 64);
		Color waterColor = new Color(57, 142, 246);
		Color gasColor = new Color(254, 69, 62);
		Color blockColor = new Color(87, 87, 87);

		if (menu)
		{
			font = font.deriveFont(Font.PLAIN, 48);
			g.setFont(font);

			if (randomIcon == SAND)
			{
				g.setColor(sandColor);
				g.drawString("SAND,", 225, 150);
				g.setColor(blockColor);
				g.drawString("WATER,", 335, 150);
				g.drawString("GAS!", 475, 150);
			}
			if (randomIcon == WATER)
			{
				g.setColor(waterColor);
				g.drawString("WATER,", 335, 150);
				g.setColor(blockColor);
				g.drawString("SAND,", 225, 150);
				g.drawString("GAS!", 475, 150);
			}
			if (randomIcon == GAS)
			{
				g.setColor(gasColor);
				g.drawString("GAS!", 475, 150);
				g.setColor(blockColor);
				g.drawString("SAND,", 225, 150);
				g.drawString("WATER,", 335, 150);
			}

			font = font.deriveFont(Font.PLAIN, 36);
			g.setColor(blockColor);

			// Animations
			// When the user hovers over the buttons, it will
			// be highlighted the same color as the JFrame icon
			if (mouseX >= 358 && mouseX <= 443 && mouseY <= 250
					&& mouseY >= 211)
			{
				if (randomIcon == SAND)
					g.setColor(sandColor);
				if (randomIcon == WATER)
					g.setColor(waterColor);
				if (randomIcon == GAS)
					g.setColor(gasColor);
				g.drawString("PLAY", 365, 250);
			}
			else
			{
				g.setColor(blockColor);
				g.drawString("PLAY", 360, 250);
			}

			if (mouseX >= 358 && mouseX <= 443 && mouseY <= 311
					&& mouseY >= 267)
			{
				if (randomIcon == SAND)
					g.setColor(sandColor);
				if (randomIcon == WATER)
					g.setColor(waterColor);
				if (randomIcon == GAS)
					g.setColor(gasColor);
				g.drawString("TUTORIAL", 325, 310);
			}
			else
			{
				g.setColor(blockColor);
				g.drawString("TUTORIAL", 320, 310);
			}

			if (mouseX >= 331 && mouseX <= 472 && mouseY <= 368
					&& mouseY >= 330)
			{
				if (randomIcon == SAND)
					g.setColor(sandColor);
				if (randomIcon == WATER)
					g.setColor(waterColor);
				if (randomIcon == GAS)
					g.setColor(gasColor);
				g.drawString("EXIT", 365, 370);
			}
			else
			{
				g.setColor(blockColor);
				g.drawString("EXIT", 360, 370);
			}
		}

		font = font.deriveFont(Font.PLAIN, 48);
		g.setColor(blockColor);
		g.setFont(font);

		// Highlights every solution block
		if (!menu)
		{
			for (int x = 0; x < MAX_X; x++)
				for (int y = 0; y < MAX_Y; y++)
				{
					if (grid[x][y].isSolution() && showSolution
							&& (wallsRemoved != wallsCanRemove))
					{
						grid[x][y].draw(g);
						g.drawImage(SOLUTION, x * 16 - 2, y * 16 - 2, null);
					}
					else
						grid[x][y].draw(g);
				}

			// Tutorial
			if (help && !debug)
			{
				int degrees = -90; // rotate text counter-clockwise
				AffineTransform vt = AffineTransform.getRotateInstance(Math
						.toRadians(degrees));

				if (currentMap == 0)
				{
					g.setFont(font.deriveFont(vt));
					g.drawString("REMOVE ALL WATER", 800, 480);

					g.setFont(font.deriveFont(Font.PLAIN, 36));

					if (wallsCanRemove != wallsRemoved)
					{
						g.setColor(blockColor);
						g.drawString("CLICK ON A BLOCK", 48, 176);
					}

					if (wallsRemoved == 1)
					{
						g.setFont(font.deriveFont(Font.PLAIN, 28));
						g.drawString("YOU HAVE A LIMITED", 390, 260);
						g.drawString("NUMBER OF CLICKS", 390, 300);
					}

					if (dWater >= 30)
					{
						g.setFont(font.deriveFont(Font.PLAIN, 28));
						g.drawString("WHEN ALL THE      ", 390, 150);
						g.setColor(waterColor);
						g.drawString("              WATER", 420, 150);
						g.setColor(blockColor);
						g.drawString("LEAVES, YOU PASS", 390, 190);
					}

				}
				else if (currentMap == 1)
				{
					g.setFont(font.deriveFont(vt));
					g.drawString("REMOVE ALL SAND", 800, 480);

					g.setFont(font.deriveFont(Font.PLAIN, 28));
					g.setColor(sandColor);
					g.drawString("SAND ", 365, 260);
					g.setColor(blockColor);
					g.drawString("    ACTS DIFFERENTLY", 390, 260);
					g.drawString("FROM     ", 365, 300);
					g.setColor(waterColor);
					g.drawString("WATER", 425, 300);

					if (dSand >= 30)
					{
						g.setColor(blockColor);
						g.setFont(font.deriveFont(Font.PLAIN, 28));
						g.drawString("IT FALLS IN AN", 390, 150);
						g.drawString("HOURGLASS PATTERN", 390, 190);
					}
				}
				else if (currentMap == 2)
				{
					g.setFont(font.deriveFont(vt));
					g.drawString("REMOVE ALL GAS", 800, 480);

					g.setColor(blockColor);
					g.setFont(font.deriveFont(Font.PLAIN, 28));
					g.drawString("THIS IS ", 10, 450);
					g.setColor(gasColor);
					g.drawString("GAS ", 90, 450);

					if (dGas >= 20)
					{
						g.setColor(blockColor);
						g.drawString("KINDA ", 260, 447);
					}
				}
				else if (currentMap == 3)
				{
					g.setFont(font.deriveFont(vt));
					g.drawString("REMOVE ALL BLOCKS", 800, 480);

					g.setFont(font.deriveFont(Font.PLAIN, 28));
					g.setColor(gasColor);
					g.drawString("GAS", 290, 80);
					g.setColor(blockColor);
					g.drawString("BEATS", 290, 110);
					g.setColor(sandColor);
					g.drawString("SAND", 290, 140);
					g.setColor(blockColor);
					g.drawString("BEATS", 290, 170);
					g.setColor(waterColor);
					g.drawString("WATER", 290, 200);
					g.setColor(blockColor);
					g.drawString("BEATS", 290, 230);
					g.setColor(gasColor);
					g.drawString("GAS", 290, 260);

				}
				else if (currentMap == 4)
				{
					if (levelCleared)
					{
						g.setFont(font.deriveFont(vt));
						g.drawString("TUTORIAL COMPLETE", 800, 480);
					}
					else if (!levelCleared)
					{
						g.setFont(font.deriveFont(vt));
						g.drawString("LAST LEVEL", 800, 480);
					}
				}
			}

			font = font.deriveFont(Font.PLAIN, 36);
			g.setFont(font);

			// Game UI
			if (!debug)
			{
				g.setColor(sandColor);
				g.drawString(dSand + "/" + sandToRemove, 670, 150);
				g.setColor(waterColor);
				g.drawString(dWater + "/" + waterToRemove, 670, 200);
				g.setColor(gasColor);
				g.drawString(dGas + "/" + gasToRemove, 670, 250);
				g.setColor(blockColor);
				g.drawString(wallsCanRemove - wallsRemoved + " LEFT", 670, 300);

				g.setColor(Color.WHITE);
				if (levelCleared && currentMap + 2 <= mapCache.size())
				{
					if (help)
						if (currentMap != 4)
						{
							g.drawString("NEXT", 670, 50);
							g.drawString("LEVEL", 670, 100);
						}
					if (!help)
					{
						g.drawString("NEXT", 670, 50);
						g.drawString("LEVEL", 670, 100);
					}
				}

				g.drawString("RESET", 670, 350);
				g.drawString("MENU", 670, 400);
				if (!help)
				{
					if (!showSolution && (wallsRemoved != wallsCanRemove))
						g.drawString("HINT", 670, 450);
					else if (showSolution && (wallsRemoved != wallsCanRemove))
						g.drawString("HIDE", 670, 450);
				}
			}

			// If the game is in debug mode, enable the debug UI
			if (debug)
			{
				g.drawImage(MAP_EDITOR_IMAGE, 625, 0, null);
				g.drawImage(EXPORT_BUTTON, 625, 60, null);

				if (currentMap - 1 >= 0)
					g.drawImage(PREVIOUS_BUTTON, 625, 120, null);
				if (currentMap + 2 <= mapCache.size() || randomMap)
					g.drawImage(NEXT_BUTTON, 625, 180, null);

				g.drawImage(RESET_BUTTON, 625, 240, null);
				g.drawImage(SAVE_BUTTON, 625, 360, null);

				if (!menu)
					g.drawImage(MENU_BUTTON, 625, 420, null);

				if (levelCleared)
				{
					g.drawImage(CLEAR_INDICATOR, 750, 430, null);
				}
				else
					g.drawImage(UNCLEARED_INDICATOR, 750, 430, null);

				if (pause)
				{
					g.drawImage(RESUME_BUTTON, 625, 300, null);
				}
				else if (!pause)
				{
					g.drawImage(PAUSE_BUTTON, 625, 300, null);
				}

				if (mapEditor)
				{
					String path = null;
					if (blockType == WALL)
					{
						path = "res/graphics/wall2.png";
					}
					else if (blockType == EMPTY)
					{
						path = "res/graphics/empty2.png";
					}
					else if (blockType == SAND)
					{
						path = "res/graphics/sand.png";
					}
					else if (blockType == WATER)
					{
						path = "res/graphics/water.png";
					}
					else if (blockType == GAS)
					{
						path = "res/graphics/gas.png";
					}

					Image image = new ImageIcon(path).getImage();
					g.drawImage(image, mouseX, mouseY, null);
					g.drawImage(MAP_EDITOR_ON_IMAGE, 625, 0, null);
				}

				if (!showSolution)
					g.drawImage(HINT_OFF, 685, 0, null);
				else
					g.drawImage(HINT_ON, 685, 0, null);
			}
		}
	}

	class GameCanvas extends JPanel implements MouseListener,
			MouseMotionListener, MouseWheelListener
	{
		public GameCanvas()
		{
			setFocusable(true);
			requestFocus();
			addMouseListener(this);
			addMouseMotionListener(this);
			addMouseWheelListener(this);
			setResizable(false);
		}

		public void paintComponent(Graphics g)
		{
			Graphics2D g2d = (Graphics2D) g;
			super.paintComponent(g2d);
			setBackground(Color.BLACK);

			// Draw the game objects
			try
			{
				render(g2d);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		/**
		 * Handles all the events in which the mouse is clicked
		 * 
		 * @author Philip Liu
		 * @param event The MouseEvent
		 */
		public void mouseClicked(MouseEvent event)
		{
			Point clickPoint = event.getPoint();

			// Grid x and y
			int y = (clickPoint.y) / BLOCK_SIZE;
			int x = (clickPoint.x) / BLOCK_SIZE;

			// UI x and y
			int yAccurate = (clickPoint.y);
			int xAccurate = (clickPoint.x);

			// Menu interactions
			if (menu)
			{
				// Play
				if (xAccurate >= 358 && xAccurate <= 443 && yAccurate <= 250
						&& yAccurate >= 211)
				{
					recache();

					if (mapCache.isEmpty())
					{
						generateMap(99, 99, 99, 1);
						randomMap = true;
					}
					recache();
					currentMap = 0;
					importMap(mapCache.get(currentMap));
					menu = false;
				}

				// Debug
				if (xAccurate >= 297 && xAccurate <= 316 && yAccurate <= 151
						&& yAccurate >= 110)
				{
					if (debug)
					{
						debug = false;
						setTitle(GAME_NAME);
					}
					else if (!debug)
						debug = true;
				}

				// Tutorial
				if (xAccurate >= 358 && xAccurate <= 443 && yAccurate <= 311
						&& yAccurate >= 267)
				{
					currentMap = 0;
					importMap(tutorialCache.get(currentMap));

					help = true;
					menu = false;
				}

				// Exit
				if (xAccurate >= 358 && xAccurate <= 443 && yAccurate <= 370
						&& yAccurate >= 330)
				{
					System.exit(0);
				}
			}

			else if (!menu)
			{
				// Debug interactions
				if (debug)
				{
					// Map Export
					if (xAccurate >= 624 && xAccurate < 674 && yAccurate <= 109
							&& yAccurate >= 59)
					{
						System.out.println(exportMap(99, 99, 99, 1));
					}

					// Map Editor
					if (xAccurate >= 624 && xAccurate < 674 && yAccurate < 50
							&& y * BLOCK_SIZE > 0)
					{
						if (mapEditor)
							mapEditor = false;
						else if (!mapEditor)
							mapEditor = true;
					}

					// Load Previous Map
					if (xAccurate >= 624 && xAccurate <= 674
							&& yAccurate <= 170 && yAccurate >= 120)
						loadPreviousMap();

					// Load Next Map
					if ((currentMap + 2 <= mapCache.size() || randomMap)
							&& xAccurate >= 624 && xAccurate <= 674
							&& yAccurate <= 230 && yAccurate > 180)
						loadNextMap();

					// Reset Map
					if (xAccurate > 624 && xAccurate < 674 && yAccurate <= 290
							&& yAccurate >= 240)
					{
						resetMap();
					}

					// Pause/Resume
					if (xAccurate >= 624 && xAccurate <= 674
							&& yAccurate <= 350 && yAccurate >= 300)
					{
						if (pause)
							pause = false;
						else
							pause = true;
					}

					// Map Save
					if (xAccurate >= 624 && xAccurate <= 674
							&& yAccurate <= 410 && yAccurate >= 360)
					{
						System.out.println(saveMap());
					}

					// Edit the grid
					if (mapEditor && y < MAX_Y && x < MAX_X)
					{
						System.out.println(grid[x][y]);
						addBlock(blockType, x, y);
					}

					// Menu
					if (xAccurate >= 628 && xAccurate < 668 && yAccurate <= 461
							&& yAccurate >= 419)
					{
						menu = true;
						if (help)
							help = false;
					}

					// Hint
					if (xAccurate >= 685 && xAccurate < 735 && yAccurate <= 50
							&& yAccurate >= 0)
					{
						if (!showSolution)
							showSolution = true;
						else
							showSolution = false;
					}
				}

				// Player UI interactions
				else if (!debug)
				{
					// Next Level
					if (xAccurate >= 670 && xAccurate <= 746
							&& yAccurate <= 101 && yAccurate >= 20
							&& levelCleared)
						loadNextMap();

					// Reset Map
					if (xAccurate >= 671 && xAccurate <= 751
							&& yAccurate >= 321 && yAccurate <= 349)
						resetMap();

					// Menu
					if (xAccurate >= 670 && xAccurate <= 744
							&& yAccurate >= 370 && yAccurate <= 400)
					{
						menu = true;
						if (help)
							help = false;
					}
					// Show solution
					if (xAccurate >= 670 && xAccurate <= 744
							&& yAccurate >= 420 && yAccurate <= 449 && !help)
						if (!showSolution && (wallsRemoved != wallsCanRemove))
							showSolution = true;
						else if (wallsRemoved != wallsCanRemove)
							showSolution = false;
				}

				// Grid Interaction
				if (!mapEditor && !menu)
				{
					if (y < MAX_Y && x < MAX_X && grid[x][y].isRemovable()
							&& wallsRemoved != wallsCanRemove)
					{
						addBlock(EMPTY, x, y);
						wallsRemoved++;

						if (debug)
							System.out.println("Removed " + grid[x][y]);
					}

					repaint();
					if (y < MAX_Y && x < MAX_X && grid[x][y].isRemovable()
							&& wallsRemoved == wallsCanRemove)
						if (debug)
							System.out
									.println("NO MORE WALLS CAN BE REMOVED. ");
				}

			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(MouseEvent arg0)
		{
			// TODO Auto-generated method stub

		}

		/**
		 * Allows the user to switch between elements if the scroll wheel is
		 * used
		 * 
		 * @author Philip Liu
		 * @param e the MouseWheelEvent
		 */
		public void mouseWheelMoved(MouseWheelEvent e)
		{

			// This feature is only present in debug mode
			int notches = e.getWheelRotation();

			if (notches < 0)
			{
				if (blockType + 1 > 3)
				{
					blockType = -2;
				}
				blockType++;
			}
			else if (notches > 0)
			{
				if (blockType - 1 < -1)
				{
					blockType = 4;
				}
				blockType--;
			}
		}

		/**
		 * Allows the user to hold down the mouse to place blocks
		 * 
		 * @author Philip Liu
		 * @param e the MouseEvent
		 */
		public void mouseDragged(MouseEvent e)
		{
			mouseX = e.getX();
			mouseY = e.getY();
			// Edit the grid
			if (!menu && !help && mapEditor && mouseY / BLOCK_SIZE < MAX_Y
					&& mouseX / BLOCK_SIZE < MAX_X)
			{
				System.out.println(grid[mouseX / BLOCK_SIZE][mouseY
						/ BLOCK_SIZE]);
				addBlock(blockType, mouseX / BLOCK_SIZE, mouseY / BLOCK_SIZE);
			}
		}

		/**
		 * Updates the current x and y position of the mouse
		 * 
		 * @author Philip Liu
		 * @param e the MouseEvent
		 */
		public void mouseMoved(MouseEvent e)
		{
			mouseX = e.getX();
			mouseY = e.getY();
		}

	}

	public static void main(String[] args) throws FontFormatException,
			IOException
	{
		// Starts the game
		new Game().start();
	}
}
