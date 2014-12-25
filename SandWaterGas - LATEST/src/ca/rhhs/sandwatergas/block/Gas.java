package ca.rhhs.sandwatergas.block;

import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;

public class Gas extends Liquid
{

	final static Image GAS = new ImageIcon("res/graphics/gas.png").getImage();

	public Gas(int x, int y)
	{
		super(x, y);
		removable = false;
	}

	public void draw(Graphics2D g2d)
	{
		g2d.drawImage(GAS, x, y, null);
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
		else if (y > 0)
			y -= BLOCK_SIZE;
	}

	public int getType()
	{
		return 3;
	}

	public String toString() 
	{
		return x/BLOCK_SIZE + " " + y/BLOCK_SIZE + " " + "Gas";
	}

	public void resetAct()
	{
		fallCounter = 0;
	}
}
