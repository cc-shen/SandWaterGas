package ca.rhhs.sandwatergas.block;

import java.awt.Graphics2D;

/**
 * Object responsible for keeping track of key variables and graphics about the
 * block at hand.
 * 
 * @author Vlad Zhurba
 * @version January 15, 2014
 */
public class Block
{
	protected int x, y;
	protected boolean removable;
	protected int moved;
	protected boolean wasMerged;
	
	protected boolean solution;

	final static protected int BLOCK_SIZE = 16;
	final static protected int MAX_Y = 30;
	final static protected int MAX_X = 39;

	/**
	 * Block constructor that initializes the blocks location on the grid and
	 * its first key characteristic
	 * 
	 * @param x The actual x position of the block on grid, the x coordinate on
	 *            the console
	 * @param y The actual y position of the block on grid, the x coordinate on
	 *            the console
	 */
	public Block(int x, int y)
	{
		this.x = x;
		this.y = y;
		removable = true;
	}

	/**
	 * Returns the information about if the block was already merged
	 * 
	 * @return True = block was already merged <br>
	 *         False = block was not merged
	 */
	public boolean isMerged()
	{
		return wasMerged;
	}

	/**
	 * Returns information about actions taken by the block
	 * 
	 * @return 0 = Has yet to move <br>
	 *         1 = Block was already moved <br>
	 *         2 = Block has been checked for actions when it was already moved
	 */
	public int getMovedStatus()
	{
		return moved;
	}

	/**
	 * Changes the moved status value of the block by 1
	 */
	public void moveAdd()
	{
		moved++;
	}

	/**
	 * Resets the moved status value to 0
	 */
	public void moveReset()
	{
		moved = 0;
	}

	/**
	 * Changes the block's position horizontally 
	 * @param amount	The amount of horizontal shifts to be made by each block size
	 */
	public void shift(int amount) 
	{
		x += amount * BLOCK_SIZE;
	}
	
	/**
	 * Draws the image on the console based on the objects location
	 * 
	 * @param g2d The console to draw the image at
	 */
	public void draw(Graphics2D g2d)
	{
	}

	/**
	 * Performs a visual shift based on the block type
	 */
	public void act()
	{
	}

	/**
	 * Returns information based on if the block is removable
	 * 
	 * @return True = block can be removed through selection <br>
	 *         False = block can't be removed through selection
	 */
	public boolean isRemovable()
	{
		return removable;
	}

	/**
	 * Returns information about the blocks position and status
	 * @return	The information about the blocks position and status
	 */
	public String toString() 
	{
		return x + " " + y + " " + "has no block type";
	}

	/**
	 * Returns the value that corresponds to the type
	 * 
	 * @return Value of 9, meaning this is a placeholder for a potential element
	 */
	public int getType()
	{
		return 9;
	}
	
	public boolean isSolution()
	{
		return solution;
	}

	public void setSolution(boolean solution)
	{
		this.solution = solution;
	}
}
