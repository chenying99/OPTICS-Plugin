package opticsplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OPTICSPoint implements Comparable<OPTICSPoint>
{

	boolean processed = false;
	boolean coreDefined = false;
	boolean reachDefined = false;
	double coreDist;
	double reachDist;
	double[] loc;
	double refDist;
	int eventNum;
	int clusterNum;
	int orderNum;

	public OPTICSPoint()
	{
		super();
		clusterNum = -1;
	}

	public void setCoreDistance(ArrayList<OPTICSPoint> neighbors, double epsilon, int minPts)
	{
		if (neighbors.size() < minPts)
		{
			this.coreDefined = false;
			return;
		}

		ArrayList<OPTICSPoint> copyNeighbors = new ArrayList<OPTICSPoint>();
		for (int i = 0; i < neighbors.size(); i++)
		{
			OPTICSPoint p = new OPTICSPoint();
			p.refDist = neighbors.get(i).refDist;
			copyNeighbors.add(p);
		}

		Collections.sort(copyNeighbors, new Comparator<OPTICSPoint>()
		{
			public int compare(OPTICSPoint p1, OPTICSPoint p2)
			{
				if(p1.refDist < p2.refDist) return -1;
				else if (p1.refDist > p2.refDist) return 1;
				else return 0;
			}
		});
		for (int i = 0; i < copyNeighbors.size(); i++)
		{
			if (copyNeighbors.get(i).refDist <= epsilon && (i + 1) == minPts)
			{
				this.coreDist = copyNeighbors.get(i).refDist;
				this.coreDefined = true;
				copyNeighbors.clear();
				copyNeighbors = null;
				break;
			}
		}
	}

	@Override
	public int compareTo(OPTICSPoint p)
	{
		if (this.reachDist < p.reachDist)
			return -1;
		else if (this.reachDist > p.reachDist)
			return 1;
		else
			return 0;
	}
}