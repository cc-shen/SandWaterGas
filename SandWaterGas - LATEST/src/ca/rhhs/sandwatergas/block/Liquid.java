package ca.rhhs.sandwatergas.block;

/**
 * Abstract class responsible for giving specialized methods and variable for liquid based elements
 * @author Vlad Zhurba
 * @version January 15, 2014
 */
public abstract class Liquid extends Block
{
	protected int moved;
	protected int velocity;
	protected int fallSwitchPeriod;
	protected int fallCounter;
	
	/**
	 * Constructor for elements with liquid like behavior
	 * @param x	
	 * @param y
	 */
	public Liquid(int x, int y)
	{
		super(x, y);		
		velocity = (int) (Math.random() * 2) + 1;		
	}
	
	public void resetAct()
	{
		fallCounter = 0;    		
	}    	

	public boolean actBoolean() {	

		return (fallCounter+1==fallSwitchPeriod);
	}	
	
	public int getDirection() 
	{
		return velocity;
	}

	public void setDirection(int i) 
	{
		velocity = i;
	}

	public void newfallSwitchPeriod() {
		fallSwitchPeriod = (int) (Math.random() * 5) + 3;
		fallCounter = 0;
	}
	
	public void fallCounterAdd() {	
		fallCounter++;
	}
	

	public void switchMerged() 
	{
		if (wasMerged)
			wasMerged = false;
		else
			wasMerged = true;         
	}	
	
}
