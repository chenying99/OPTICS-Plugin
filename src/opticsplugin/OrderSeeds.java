package opticsplugin;

import java.util.PriorityQueue;

public class OrderSeeds extends PriorityQueue<OPTICSPoint>
{
	private static final long serialVersionUID = 1L;

	/**
	 * synonymous with priority queue add
	 * 
	 * @param p
	 */
	public void insert(OPTICSPoint p)
	{
		this.add(p);
	}

	/**
	 * Synonymous with removing the head./
	 * 
	 * @return
	 */
	public OPTICSPoint next()
	{
		return this.poll(); // remove the top of the queue
	}

	/**
	 * This just involves removing and reinserting the element. This ensures
	 * that the point is in the correct location.
	 * 
	 * @param p
	 */
	public void decrease(OPTICSPoint p)
	{
		if (this.contains(p))
		{
			this.remove(p);
			this.add(p);
		}
	}
}