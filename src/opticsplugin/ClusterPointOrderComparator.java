package opticsplugin;

import java.util.Comparator;

final class ClusterPointOrderComparator implements Comparator<ClusterPoint>
{
	public int compare(ClusterPoint p1, ClusterPoint p2)
	{
		if (p1.orderNum < p2.orderNum)
			return -1;
		else if (p1.orderNum > p2.orderNum)
			return 1;
		else
			return 0;
	}
}