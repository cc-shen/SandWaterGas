package ca.rhhs.sandwatergas.block;

import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;

public class Empty extends Block
{
	private int blockColor;

	final static Image DUMMY_IMAGE_1 = new ImageIcon("res/graphics/empty1.png").getImage();
	final static Image DUMMY_IMAGE_2 = new ImageIcon("res/graphics/empty2.png").getImage();
	final static Image DUMMY_IMAGE_3 = new ImageIcon("res/graphics/empty3.png").getImage();

	public Empty(int x, int y)
	{
		super(x, y);
		removable = false;
		blockColor = (int) (Math.random() * 3);
	}

	public void draw(Graphics2D g2d)
	{
		if (blockColor == 0)
			g2d.drawImage(DUMMY_IMAGE_1, x, y, null);
		else if (blockColor == 1)
			g2d.drawImage(DUMMY_IMAGE_2, x, y, null);
		else if (blockColor == 2)
			g2d.drawImage(DUMMY_IMAGE_3, x, y, null);

	}

	public String toString()
	{
		return x/BLOCK_SIZE + " " + y/BLOCK_SIZE + " " + "Empty";
	}

	public int getType()
	{
		return 0;
	}

}
