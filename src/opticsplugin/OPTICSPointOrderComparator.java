package opticsplugin;

import java.util.Comparator;

final class OPTICSPointOrderComparator implements Comparator<OPTICSPoint>
{
	public int compare(OPTICSPoint p1, OPTICSPoint p2)
	{
		if (p1.orderNum < p2.orderNum)
			return -1;
		else if (p1.orderNum > p2.orderNum)
			return 1;
		else
			return 0;
	}
}