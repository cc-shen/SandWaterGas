package ca.rhhs.sandwatergas.block;

import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;

public class Water extends Liquid
{

	final static Image WATER = new ImageIcon("res/graphics/water.png")
			.getImage();

	public Water(int x, int y)
	{
		super(x, y);
		removable = false;
	}

	public void draw(Graphics2D g2d)
	{
		g2d.drawImage(WATER, x, y, null);
	}

	public void act()
	{
		fallCounter++;
		if (fallCounter == fallSwitchPeriod)
		{
			if (velocity == 1)
				x -= BLOCK_SIZE;
			else if (velocity == 2)
				x += BLOCK_SIZE;

			fallCounter = 0;
		}
		else
			y += BLOCK_SIZE;
	}

	public boolean actBoolean()
	{
		return (fallCounter + 1 == fallSwitchPeriod);
	}

	public void moveLeft()
	{
		if (this.x > 0)
			this.x -= BLOCK_SIZE;
	}

	public void moveRight()
	{
		if (this.x < MAX_X * BLOCK_SIZE)
			this.x += BLOCK_SIZE;
	}

	public String toString()
	{
		return x / BLOCK_SIZE + " " + y / BLOCK_SIZE + " " + "Water";
	}

	public int getType()
	{
		return 2;
	}

	public void resetAct()
	{
		fallCounter = 0;
	}
}
