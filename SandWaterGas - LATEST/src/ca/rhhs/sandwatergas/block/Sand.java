package ca.rhhs.sandwatergas.block;

import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;

public class Sand extends Block {

	final static Image SAND = new ImageIcon("res/graphics/sand.png").getImage();

	public Sand(int x, int y) {
		super(x, y);
		removable = false;
	}

	public void draw(Graphics2D g2d) {
		g2d.drawImage(SAND, x, y, null);
	}

	public void act() {
		if (this.y < MAX_Y * BLOCK_SIZE)
			y+=BLOCK_SIZE;	
	}

	public String toString() 
	{
		return x/BLOCK_SIZE + " " + y/BLOCK_SIZE + " " + "Sand";
	}

	public int getType() {
		return 1;
	}
}
