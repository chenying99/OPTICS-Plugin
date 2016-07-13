package opticsplugin;

import java.util.Comparator;

final class OPTICSPointEventComparator implements Comparator<OPTICSPoint>
{
	public int compare(OPTICSPoint p1, OPTICSPoint p2)
	{
		if (p1.eventNum < p2.eventNum)
			return -1;
		else if (p1.eventNum > p2.eventNum)
			return 1;
		else
			return 0;
	}
}