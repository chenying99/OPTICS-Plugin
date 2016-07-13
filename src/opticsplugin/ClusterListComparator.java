package opticsplugin;

import java.util.ArrayList;
import java.util.Comparator;

final class ClusterListComparator implements Comparator<ArrayList<OPTICSPoint>>
{
	public int compare(ArrayList<OPTICSPoint> a, ArrayList<OPTICSPoint> b)
	{
		if (a.size() > b.size())
			return -1;
		else if (a.size() < b.size())
			return 1;
		else
			return 0;
	}
}