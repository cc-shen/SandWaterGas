package ca.rhhs.sandwatergas.block;

import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;

public class Wall extends Block
{
	private int blockColor;

	final static private Image WALL_1 = new ImageIcon("res/graphics/wall1.png")
			.getImage();
	final static private Image WALL_2 = new ImageIcon("res/graphics/wall2.png")
			.getImage();
	final static private Image WALL_3 = new ImageIcon("res/graphics/wall3.png")
			.getImage();

	public Wall(int x, int y, boolean solution)
	{
		super(x, y);
		removable = true;
		blockColor = (int) (Math.random() * 3);
		this.solution = solution;
	}

	public void draw(Graphics2D g2d)
	{
		if (blockColor == 0)
			g2d.drawImage(WALL_1, x, y, null);
		else if (blockColor == 1)
			g2d.drawImage(WALL_2, x, y, null);
		else if (blockColor == 2)
			g2d.drawImage(WALL_3, x, y, null);
	}
	
	public String toString()
	{
		if (solution == false)
			return x / BLOCK_SIZE + " " + y / BLOCK_SIZE + " " + "Wall";
		else
			return x / BLOCK_SIZE + " " + y / BLOCK_SIZE + " " + "Solution";
	}

	public int getType()
	{
		return -1;
	}
}
