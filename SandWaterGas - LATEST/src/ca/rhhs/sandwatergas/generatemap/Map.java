package ca.rhhs.sandwatergas.generatemap;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import ca.rhhs.sandwatergas.Game;


public class Map
{


	private int[][] newMap;
	private int mainProblemLayout;
	private LinkedList<Point> blocksToRemove;
	private int[] conflictingSolutions;
	private Point enteryFrom;
	private Point enteryTo;

	final static private int EMPTY = 0;
	final static private int SAND = 1;
	final static private int WATER = 2;
	final static private int GAS = 3;
	final static private int WALL = -1;

	final static private int BLOCK_SIZE = 16;
	final static private int MAX_Y = 30;
	final static private int MAX_X = 39;


	// will probably need to implement and linked list of these points for when showing solutions

	// complexity
	public Map ()
	{               
		newMap = new int[39][30];       

		// takes 1 main layout (big problem) from our pool
		mainProblemLayout = (int) (Math.random()*10+1);
		// eventually used for the listing of correct moves
		blocksToRemove = new LinkedList<Point>();
		// explains more later on, it helps minimize the false blocksToRemove chances 
		conflictingSolutions = new int[20];


		closeGrid((int)(Math.random()*3 + 1));                   

		//              Form may look something like this when inserting main layout
		//              by looking at what we got from the mainProblemLayout
		//              if ()
		//                      
		//              else if () 
		//                      
		//              else if ()

		this.createMoreProblems();


		this.implementProblem1();
		// mapGeneration2(20,5,6);

		this.createMoreProblems();
		//	smallProblem1();

	}       
	
	private void createMoreProblems()
	{
		for (int y = 29;y>=0;y--)
			for (int x = 1;x<=38;x++)                       
				if (newMap [x][y] == 7)
					if (Math.random()*10<4 && conflictingSolutions [1] == 0)
						smallProblem1(x,y);
					else if (Math.random()*10<2 && conflictingSolutions [3] == 0)
						smallProblem3();

	}

	private void smallProblem3()
	{
		int problem = 3;
		int topLocation = enteryTo.y;
		
		int shiftCount = 0;
		boolean directionLeft = false;
		
		if (Math.random()*2>1)
			directionLeft = true;
					
		while (newMap[enteryTo.x][topLocation] != 4)		
			topLocation--;
		
		if (topLocation-1 < 1)		
			return;
		
		conflictingSolutions [problem]++;
	}
	
	public int[][] getMap ()
	{
		return newMap;          

	}

	/**
	 * Creates Sand on top and gas on bottom (opposite corners)
	 */
	private void implementProblem1()
	{
		// Implement with the randomization later on (based on how the blocks are created)

		// randomize locations later on

		int sandWidth = (int) (Math.random()*3+3);
		int sandHight = (int) (Math.random()*3+3);

		int gasWidth = 0;
		int gasHight = 0;
		while (gasWidth*gasHight <= sandHight)
		{
			gasWidth = (int) (Math.random()*3+3);
			gasHight = (int) (Math.random()*3+3);           
		}

		Point sandStart = new Point ( (int) (Math.random()*27+1), (int) (Math.random()*2+6));
		Point gasStart = new Point ( (int) (Math.random()*27+1), (int) (Math.random()*4+24));


		enteryFrom = new Point ((int) (Math.random()*(gasWidth+1)+gasStart.x),gasStart.y-gasHight-1);
		enteryTo = new Point ((int) (Math.random()*(sandWidth+1)+sandStart.x),sandStart.y+1);


		// gas block at bottom left
		for (int y = gasStart.y; y >= gasStart.y-gasHight; y--) 
		{
			for (int x = gasStart.x; x <= gasStart.x+gasWidth; x++)
			{
				addBlock(3, x, y);                              
			}
		}               
		for (int y = gasStart.y; y >= gasStart.y - gasHight; y--) 
		{                       
			addBlock(4,  gasStart.x-1, y);
			addBlock(4,  gasStart.x+gasWidth+1, y);
		}

		for (int x = gasStart.x-1; x <= gasStart.x + gasWidth +1; x++)
		{               

			addBlock(4, x, gasStart.y+1);
			addBlock(4, x, gasStart.y-gasHight-1);
		}

		// sand block at top right
		for (int y= sandStart.y; y >= sandStart.y-sandHight; y--)
		{
			for (int x = sandStart.x; x <= sandStart.x+sandWidth; x++)
			{
				addBlock(1, x, y);
			}
		}
		for (int y = sandStart.y; y >= sandStart.y - sandHight; y--) 
		{
			addBlock(4,  sandStart.x-1, y);
			addBlock(4,  sandStart.x+sandWidth+1, y);
		}               
		for (int x = sandStart.x-1; x <= sandStart.x + sandWidth +1; x++)
		{               
			addBlock(4, x, sandStart.y+1);
			addBlock(4, x, sandStart.y-sandHight-1);
		}

		// marks the big moves needed to be made (in this case it is to move gas to sand)
		addBlock(9,enteryTo.x,enteryTo.y);
		addBlock(9,enteryFrom.x,enteryFrom.y);

		// connects the locations of the two initially marked points
		connectPoints1 (enteryTo,enteryFrom);
	}

	/**
	 * A second variation on generating a map
	 * @param noOfSand
	 * @param noOfWater
	 * @param noOfGas
	 */
	private void mapGeneration2 (int noOfSand, int noOfWater, int noOfGas)
	{

		//		int gasWidth = (int) (Math.random() * (Math.sqrt(noOfGas))) + 1;
		//		while (noOfGas % gasWidth != 0) {
		//			gasWidth = (int) (Math.random() * (int) Math.sqrt(noOfGas)) + 1;
		//		}
		//		int gasLength = noOfGas % gasWidth;
		//
		//		int waterWidth = (int) (Math.random() * (Math.sqrt(noOfWater))) + 1;
		//		while (noOfWater % waterWidth != 0) {
		//			waterWidth = (int) (Math.random() * (int) Math.sqrt(noOfWater)) + 1;
		//		}
		//		int waterLength = noOfWater % waterWidth;

		int sandWidth = (int) (Math.random() * (Math.sqrt(noOfSand))) + 1;
		while (noOfSand % sandWidth != 0) {
			sandWidth = (int) (Math.random() * (int) Math.sqrt(noOfSand)) + 1;
		}
		int sandLength = noOfSand % sandWidth;

		int randomX = (int) (Math.random() * MAX_X) / 4 + 1;
		int randomY = (int) (Math.random() * MAX_Y) / 8 + 1;

		System.out.println (randomX);
		System.out.println (sandLength);

		for (int x = randomX; x < randomX + sandLength; x++) {
			addBlock(WALL, x, randomY + sandWidth);
			for (int y = randomY; y < randomY + sandWidth; y++) {
				addBlock(SAND, x, y);
			}			
		}

	}



	/**
	 * Creates an initial tunnel between 2 points, current space available is 1 layer
	 * Must also modify later for potential use for all problem sets 
	 * OR have multiple connectPoints methods where each one is associated with their big problem layer
	 * SECOND OPTION IS PROBEBLY EASIER BUT REPETITIVE (will most likely do second option due to time)
	 * @param enteryTo              end
	 * @param enteryFrom    start
	 */
	private void connectPoints1(Point enteryTo, Point enteryFrom)
	{               
		// the starting and ending location of our open grid
		Point travelPoint = new Point (enteryFrom.x,enteryFrom.y-1);
		Point endPoint = new Point (enteryTo.x,enteryTo.y+1);




		// not putting much thought in the order so this will be the only comment relating to that
		blocksToRemove.add (enteryTo);
		blocksToRemove.add (enteryFrom);

		// marks the first part as our travel point 
		addBlock(7,travelPoint.x,travelPoint.y);

		// places walls around this point
		if (newMap[travelPoint.x-1][travelPoint.y] == 0)
			addBlock(4,travelPoint.x-1,travelPoint.y);
		if (newMap[travelPoint.x+1][travelPoint.y] == 0)
			addBlock(4,travelPoint.x+1,travelPoint.y);
		if (newMap[travelPoint.x][travelPoint.y-1] == 0)
			addBlock(4,travelPoint.x,travelPoint.y-1);




		// must make the two points connect
		while (travelPoint.x != endPoint.x || travelPoint.y != endPoint.y)                      
		{               
			//                      System.out.println (travelPoint);
			//                      System.out.println ("------");
			//                      System.out.println (endPoint);


			// next two if statements find out if we are either moving up or right from this side
			if ((int) (Math.random()*4) > 1 && travelPoint.x < endPoint.x)                  
			{                               
				// shift right marking our main path
				travelPoint.x++;
				addBlock(7,travelPoint.x,travelPoint.y);

				// add walls around
				if (newMap[travelPoint.x][travelPoint.y-1] == 0)
					addBlock(4,travelPoint.x,travelPoint.y-1);
				if (newMap[travelPoint.x][travelPoint.y+1] == 0)
					addBlock(4,travelPoint.x,travelPoint.y+1);                              
				if (newMap[travelPoint.x+1][travelPoint.y] == 0)
					addBlock(4,travelPoint.x+1,travelPoint.y);
			}

			if ((int) (Math.random()*10) <2 && travelPoint.y > endPoint.y)                  
			{
				// travels upwards
				travelPoint.y--;
				addBlock(7,travelPoint.x,travelPoint.y);                                


				// add walls around
				if (newMap[travelPoint.x][travelPoint.y-1] == 0)
					addBlock(4,travelPoint.x,travelPoint.y-1);
				if (newMap[travelPoint.x-1][travelPoint.y] == 0)
					addBlock(4,travelPoint.x-1,travelPoint.y);                              
				if (newMap[travelPoint.x+1][travelPoint.y] == 0)
					addBlock(4,travelPoint.x+1,travelPoint.y);

			}

			if ((int) (Math.random()*4) > 1 && travelPoint.x > endPoint.x)                  
			{                               
				// shift right marking our main path
				travelPoint.x--;
				addBlock(7,travelPoint.x,travelPoint.y);

				// add walls around
				if (newMap[travelPoint.x][travelPoint.y-1] == 0)
					addBlock(4,travelPoint.x,travelPoint.y-1);
				if (newMap[travelPoint.x][travelPoint.y+1] == 0)
					addBlock(4,travelPoint.x,travelPoint.y+1);                              
				if (newMap[travelPoint.x-1][travelPoint.y] == 0)
					addBlock(4,travelPoint.x-1,travelPoint.y);
			}


			// make problem implemented random



			//                      System.out.println("infi");
			//                      System.out.println(endPoint);
			//                      System.out.println(travelPoint);                        
		}

	}

	/**
	 * Have sand be on the way of the path 
	 * @param firstPoint
	 */
	private void smallProblem1 (int x, int y)
	{       
		int problem = 1;
		boolean valid = true;

		conflictingSolutions [problem]++;

		if (newMap[x+1][y] == 4 && newMap[x-1][y] == 4)
		{                                                                                               
			conflictingSolutions [problem]--;
		}
		else if (newMap[x][y+1] == 4 && newMap[x][y+2] == 4)
		{                                                                                               
			conflictingSolutions [problem]--;
		}
		else
		{       
			for (int check = 0; check <= 38;check++)
				if (newMap [check][y+1] == 9)
					valid = false;

			if (valid)
			{       
				while (newMap[x][y] == 7)
				{
					// add water block and shift left for the next check
					addBlock(2,x,y);
					x--;    
					//System.out.println("infi");
				}

				if (newMap[x+1][y+1] == 4)
				{
					//      blocksToRemove.add(new Point (x+1,y+1));
					addBlock(9,x+1,y+1);
				}
				if (newMap[x+1][y+1] == 7)
				{
					//              blocksToRemove.add(new Point (x+1,y+1));
					addBlock(9,x+1,y+1);
				}

				x++;
				while (newMap[x][y] == 7 || newMap[x][y] == 2)
				{
					// add water block and shift left for the next check
					addBlock(2,x,y);
					x++;    
					//System.out.println("infi");
				}

				if (newMap[x-1][y+1] == 4)
				{
					//      blocksToRemove.add(new Point (x+1,y+1));
					addBlock(9,x-1,y+1);
				}
				if (newMap[x-1][y+1] == 7)
				{
					//              blocksToRemove.add(new Point (x+1,y+1));
					addBlock(9,x-1,y+1);
				}
				x =39;
				y = -1;
			}
		}
	}


	/**
	 * Creates a wall around the map
	 * @param thickness
	 */
	private void closeGrid (int thickness)
	{
		// Implement thickness later, probably based on the bigProblem given

		for (int y = 0; y < MAX_Y; y++) 
		{
			addBlock(4,  0, y);
			addBlock(4, MAX_X - 1, y);
		}               
		for (int x = 0; x < MAX_X; x++)
		{               
			addBlock(4, x,  0);
			addBlock(4, x, MAX_Y - 1);                     
		}
		
		if (thickness > 1)
		{
			for (int y = 1; y < MAX_Y - 1; y++) 
			{
				addBlock(4,  1, y);
				addBlock(4, MAX_X - 2, y);
			}               
			for (int x = 1; x < MAX_X - 1; x++)
			{               
				addBlock(4, x,  1);
				addBlock(4, x, MAX_Y - 2);                     
			}
			if (thickness == 3)
			{
				for (int y = 2; y < MAX_Y - 2; y++) 
				{
					addBlock(4,  2, y);
					addBlock(4, MAX_X - 3, y);
				}               
				for (int x = 2; x < MAX_X - 2; x++)
				{               
					addBlock(4, x,  2);
					addBlock(4, x, MAX_Y - 3);                     
				}
			}
		}
	}


	/**
	 * Adds block of a type, smiler to what we have in game.java
	 * @param type
	 * @param x
	 * @param y
	 */
	private void addBlock(int type, int x, int y) 
	{                       
		// 0 = Empty
		// 1 = Sand
		// 2 = Water
		// 3 = Gas
		// 4 = Wall     
		// 5 = Security Space (in order to prevent 2 small problems from conflicting each other) [may or may not need this]
		// 7 = Main Path                
		// 9 = Block to Remove (for solving)
		newMap[x][y] = type;
	}
	
	/**
	 * Returns an ok 2d grid of numbers to our console 
	 * @return
	 */
	public String toString ()
	{
		StringBuilder grid = new StringBuilder ();

		for (int y = 0; y < 30; y++)
		{
			for (int x = 0; x < 39; x++)                    
				grid.append (String.valueOf (newMap[x][y]));                      
			grid.append("\n");
		}

		return grid.toString();                         
	}
}
