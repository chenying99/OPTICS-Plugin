package opticsplugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OPTICSAlgorithm
{
	private int numParams;
	private int minPts;
	private int outputCount = 0;
	private double epsilon;
	private ArrayList<OPTICSPoint> pointList = new ArrayList<OPTICSPoint>();
	private File inputFile;
	private File outputFile;
	private BufferedReader inputReader;
	private BufferedWriter outputWriter;
	private double xi;

	/**
	 * The constructor sets the arguments as fields, and reads in a point list
	 * from the specified input file.
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @param minPts
	 * @param epsilon
	 * @param numParams
	 */
	public OPTICSAlgorithm(File inputFile, File outputFile, int minPts, double epsilon, int numParams)
	{
		this.inputFile = inputFile;
		this.minPts = minPts;
		this.epsilon = epsilon;
		this.numParams = numParams;
		this.outputFile = outputFile;

		// Build the pointList first!
		try
		{
			String line;
			inputReader = new BufferedReader(new FileReader(this.inputFile));
			inputReader.readLine();
			while ((line = inputReader.readLine()) != null)
			{
				String[] lineData = line.split(",");
				OPTICSPoint p = new OPTICSPoint();
				p.loc = new double[this.numParams];
				for (int i = 0; i < this.numParams; i++)
				{
					p.loc[i] = Double.parseDouble(lineData[i]);
				}
				// For some reason the event number gets written as a double,
				// too?
				p.eventNum = (int) Double.parseDouble(lineData[numParams]);
				pointList.add(p);
			}
			inputReader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * doIt() starts the OPTICS algorithm proper. The output is stored as a csv
	 * to the outputFile specified in the class constructor.
	 * 
	 * The first column of output corresponds to the event number The second
	 * column corresponds to the order it is output in the OPTICS algorithm The
	 * third column is the point's reachability distance
	 */
	public void doIt()
	{
		try
		{
			outputWriter = new BufferedWriter(new FileWriter(outputFile));
			outputWriter.write("eventNum,orderNum,reachability\n");

			// Top view of the algorithm.
			for (int i = 0; i < pointList.size(); i++)
				if (!pointList.get(i).processed)
					expandClusterOrder(pointList.get(i));

			outputWriter.close();

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Calculates the Euclidean distance between 2 k-dimensional points
	 * 
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	public double calcEuclideanDist(double[] loc1, double[] loc2)
	{
		double result = 0;
		for (int i = 0; i < loc1.length; i++)
		{
			double temp = loc1[i] - loc2[i];
			result += temp * temp;
		}
		result = Math.sqrt(result);
		return result;
	}

	/**
	 * Finds all of the neighbors of a point within a radius of epsilon The
	 * reference distance of each point is also set as the neighbors are added
	 * to the neighbor list.
	 * 
	 * @param point
	 * @return
	 */
	public ArrayList<OPTICSPoint> findNeighbors(OPTICSPoint point)
	{
		ArrayList<OPTICSPoint> neighbors = new ArrayList<OPTICSPoint>();
		double dist;
		for (OPTICSPoint p : pointList)
		{
			dist = calcEuclideanDist(point.loc, p.loc);
			if (dist <= epsilon)
			{
				p.refDist = dist;
				neighbors.add(p);
			}
		}
		return neighbors;
	}

	/**
	 * This method does most of the heavy lifting of the algorithm.
	 * 
	 * @param point
	 */
	private void expandClusterOrder(OPTICSPoint point)
	{
		ArrayList<OPTICSPoint> neighbors = findNeighbors(point);
		point.processed = true;
		point.reachDefined = false;
		point.setCoreDistance(neighbors, epsilon, minPts);
		write(point);

		if (point.coreDefined)
		{
			OrderSeeds orderSeeds = new OrderSeeds();
			updateOrderSeeds(orderSeeds, neighbors, point);
			while (!orderSeeds.isEmpty())
			{
				OPTICSPoint currentObject = orderSeeds.next();
				neighbors = findNeighbors(currentObject);
				currentObject.processed = true;
				currentObject.setCoreDistance(neighbors, epsilon, minPts);
				write(currentObject);
				if (currentObject.coreDefined)
					updateOrderSeeds(orderSeeds, neighbors, currentObject);
			}
		}
	}

	/**
	 * Updates orderSeeds...
	 * 
	 * @param orderSeeds
	 * @param neighbors
	 * @param point
	 */
	private void updateOrderSeeds(OrderSeeds orderSeeds, ArrayList<OPTICSPoint> neighbors, OPTICSPoint point)
	{
		double coreDist = point.coreDist;
		for (int i = 0; i < neighbors.size(); i++)
		{
			OPTICSPoint p = neighbors.get(i);
			if (!p.processed)
			{
				double newReachDist = max(coreDist, p.refDist);
				if (!p.reachDefined)
				{
					p.reachDist = newReachDist;
					p.reachDefined = true;
					orderSeeds.insert(p);
				} else
				{
					if (newReachDist < p.reachDist)
					{
						p.reachDist = newReachDist;
						orderSeeds.decrease(p);
					}
				}
			}
		}
	}

	/**
	 * Writes the point to CSV. Only writes the event num and the reachability
	 * dist
	 * 
	 * @param point
	 */
	private void write(OPTICSPoint point)
	{
		try
		{
			outputWriter.write(point.eventNum + ",");
			outputWriter.write(++outputCount + ",");
			outputWriter.write(point.reachDist + "\n");

		} catch (IOException e)
		{
			e.printStackTrace();
		}

		point.orderNum = outputCount;
	}

	/**
	 * This is a very simple attempt to separate clusters by detecting peaks in
	 * the reachability index.
	 * 
	 * @param resultsFile
	 * @param xi
	 */
	public void peakClusterSeparator(File resultsFile, double xi)
	{
		this.xi = xi;
		Collections.sort(pointList, new OPTICSPointOrderComparator());

		ArrayList<Integer> peakIndices = new ArrayList<Integer>();

		int size = pointList.size();
		peakIndices.add(new Integer(0)); // Seed with index 0.
		/*double prev2 = pointList.get(0).reachDist;
		double prev = pointList.get(1).reachDist;
		double curr = pointList.get(2).reachDist;
		double next = pointList.get(3).reachDist;
		double next2 = pointList.get(4).reachDist;*/

		boolean wentDown = true;
		int countDown = minPts;
		for (int i = 1; i < size - 1; i++)
		{
			if (wentDown)
			{
				countDown--;
				if (countDown == 0)
					wentDown = false;
			} else if (downPoint(pointList.get(i), pointList.get(i + 1)))
			{
				wentDown = true;
				peakIndices.add(i);
				countDown = minPts;
			}
			/*
			 * if (curr >= prev && prev >= prev2) { if (curr * (1 - xi) >= next
			 * && next >= next2) if ((i - 2 - peakIndices.get(peakIndices.size()
			 * - 1)) > minPts) if (pointList.get(i - 2).reachDist > 0)
			 * peakIndices.add(i - 2); } prev2 = prev; prev = curr; curr = next;
			 * next = next2; next2 = pointList.get(i + 1).reachDist;
			 */
		}
		peakIndices.add(pointList.indexOf(pointList.get(size - 1)));
		System.out.println(peakIndices);
		int numClusters = 1;
		for (int i = 0; i < peakIndices.size() - 1; i++)
		{
			int start = peakIndices.get(i);
			int end = peakIndices.get(i + 1);
			for (int j = start; j < end; j++)
			{
				pointList.get(j).clusterNum = numClusters;
			}
			numClusters++;
		}

		Collections.sort(pointList, new OPTICSPointEventComparator());
		try
		{
			BufferedWriter resultsWriter = new BufferedWriter(new FileWriter(resultsFile));
			//Write the 1 column CSV
			resultsWriter.write("\n"); //FlowJo wants this...
			for (OPTICSPoint p : pointList)
			{
				//resultsWriter.write(p.eventNum + ",");
				// resultsWriter.write(p.orderNum + ",");
				// resultsWriter.write(p.reachDist + ",");
				resultsWriter.write(p.clusterNum + "\n");
			}
			resultsWriter.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Attempt at the algorithm described in Figure 19 of the OPTICS paper.
	 * 
	 * @param resultsFile
	 * @param xi
	 */
	public void xiClusterExtract(File resultsFile, double xi)
	{
		// sort pointList by orderNum
		Collections.sort(pointList, new OPTICSPointOrderComparator());
		ArrayList<ArrayList<OPTICSPoint>> steepDownAreas = new ArrayList<ArrayList<OPTICSPoint>>();
		ArrayList<ArrayList<OPTICSPoint>> clusters = new ArrayList<ArrayList<OPTICSPoint>>();
		ArrayList<Double> mibVals = new ArrayList<Double>();

		int index = 0;
		double mib = 0;
		int size = pointList.size();

		while (index < size - 1)
		{
			mib = max(mib, pointList.get(index).reachDist);
			if (downPoint(pointList.get(index), pointList.get(index + 1)))
			{
				updateMibFilterSDA(steepDownAreas, mib, mibVals);
				ArrayList<OPTICSPoint> D = new ArrayList<OPTICSPoint>();
				mibVals.add(0.0); // set D.mib 0
				// Find end of steep down area D
				while (index < size - 1 && downPoint(pointList.get(index), pointList.get(index + 1)))
				{
					D.add(pointList.get(index));
					index++;
				}
				// add D to set of steep down areas
				steepDownAreas.add(D);
				mib = pointList.get(index).reachDist;
			} else if (upPoint(pointList.get(index), pointList.get(index + 1)))
			{
				updateMibFilterSDA(steepDownAreas, mib, mibVals);
				ArrayList<OPTICSPoint> U = new ArrayList<OPTICSPoint>();
				// Find end of steep up area U
				while (index < size - 1 && upPoint(pointList.get(index), pointList.get(index + 1)))
				{
					U.add(pointList.get(index));
					index++;
				}
				mib = pointList.get(index).reachDist;
				for (ArrayList<OPTICSPoint> D : steepDownAreas)
				{
					// if combination of D and U is valid, and satisfies 1, 2,
					// 3a
					// compute [s,e], add cluster to set of clusters
					int startD = pointList.indexOf(D.get(0)); // first index of
																// D
					int endU = pointList.indexOf(U.get(U.size() - 1)); // last
																		// index
																		// of U
					if (endU - startD < minPts)
						continue; // Not enough points to call a
									// cluster-actually takes care of two cases
					if (mib * (1 - xi) < mibVals.get(steepDownAreas.indexOf(D)))
						continue; // Doesn't pass the xi-test

					// build up the new cluster
					ArrayList<OPTICSPoint> newCluster = new ArrayList<OPTICSPoint>();
					for (int i = startD; i <= endU; i++)
					{
						newCluster.add(pointList.get(i));
					}
					clusters.add(newCluster);

				}
			} else
				index++;
		}

		int numClusters = 0;
		// sort clusters from large to small.
		Collections.sort(clusters, new ClusterListComparator());

		/**
		 * Assign cluster numbers to the points in each cluster. Because the
		 * clusters are sorted large to small, subclusters end up overwriting
		 * the larger clusters. The cluster hierarchy is lost, but the smaller
		 * subclusters are preserved.
		 */
		for (ArrayList<OPTICSPoint> cluster : clusters)
		{
			for (OPTICSPoint p : cluster)
			{
				p.clusterNum = numClusters;
			}
			/*
			 * System.out.println("cluster " + numClusters);
			 * System.out.print(pointList.indexOf(cluster.get(0)));
			 * System.out.print(" to ");
			 * System.out.print(pointList.indexOf(cluster.get(cluster.size() -
			 * 1))); System.out.print("\n\n");
			 */
			numClusters++;
		}

		Collections.sort(pointList, new OPTICSPointEventComparator());
		try
		{
			BufferedWriter resultsWriter = new BufferedWriter(new FileWriter(resultsFile));
			// Write 2 columns, column 1 contains event number,
			// column 2 contains cluster number
			for (OPTICSPoint p : pointList)
			{
				resultsWriter.write(p.eventNum + ",");
				resultsWriter.write(p.clusterNum + "\n");
			}
			resultsWriter.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void updateMibFilterSDA(ArrayList<ArrayList<OPTICSPoint>> steepDownAreas, double mib,
			ArrayList<Double> mibVals)
	{
		int index = 0;
		while (mibVals.size() > index)
		{
			if (steepDownAreas.get(index).get(0).reachDist * (1 - xi) < mib)
			{
				mibVals.remove(index);
				steepDownAreas.remove(index);
			} else
			{
				double temp = mibVals.remove(index);
				mibVals.add(index, max(mib, temp)); // ensure mibval is set to
													// the higher of the two.
				index++;
			}
		}
	}

	private boolean downPoint(OPTICSPoint p1, OPTICSPoint p2)
	{
		return (p1.reachDist * (1 - xi) >= p2.reachDist);
	}

	private boolean upPoint(OPTICSPoint p1, OPTICSPoint p2)
	{
		return (p1.reachDist <= p2.reachDist * (1 - xi));
	}

	/**
	 * This is the simplest clustering algorithm following OPTICS It gets the
	 * same results that DBScan would, using a single epsilon value to set the
	 * cutoff for clusters.
	 * 
	 * @param epsPrime
	 */
	public void extractDBScanClusters(File resultsFile, double epsPrime)
	{
		// first, sort pointList by orderNum
		Collections.sort(pointList, new OPTICSPointOrderComparator());
		final int NOISE = -1;
		int clusterId = NOISE;
		for (OPTICSPoint p : pointList)
		{
			if (p.reachDist > epsPrime)
			{
				if (p.coreDist <= epsPrime)
				{
					clusterId++;
					p.clusterNum = clusterId;
				} else
					p.clusterNum = NOISE;
			} else
				p.clusterNum = clusterId;
		}

		// re-sort by event num
		Collections.sort(pointList, new OPTICSPointEventComparator());
		try
		{
			BufferedWriter resultsWriter = new BufferedWriter(new FileWriter(resultsFile));
			// Write 2 columns, column 1 contains event number,
			// column 2 contains cluster number
			for (OPTICSPoint p : pointList)
			{
				//resultsWriter.write(p.eventNum + ",");
				resultsWriter.write(p.clusterNum + "\n");
			}
			resultsWriter.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * This is the start of an attempt to separate clusters by detecting
	 * xi-steep down areas. It does not represent the solution presented in the
	 * original OPTICS paper.
	 * 
	 * @param resultsFile
	 * @param xi
	 */
	public void terriblexiClusterExtract(File resultsFile, double xi)
	{
		ArrayList<ClusterPoint> myList = new ArrayList<ClusterPoint>();
		try
		{
			BufferedWriter resultsWriter = new BufferedWriter(new FileWriter(resultsFile));
			inputReader = new BufferedReader(new FileReader(outputFile));

			inputReader.readLine(); // Clear out the headers
			String line = null;
			int clusterNum = 0;
			double previous = Double.MAX_VALUE;
			double current;
			while ((line = inputReader.readLine()) != null)
			{
				String[] lineData = line.split(",");
				current = Double.parseDouble(lineData[2]);
				if (current == 0)
					current = epsilon;
				ClusterPoint p = new ClusterPoint();
				// This is where it goes. Good luck.
				if (current < previous * (1 - xi))
				{
					++clusterNum;
				}
				previous = current;
				p.eventNum = Integer.parseInt(lineData[0]);
				p.clusterNum = clusterNum;
				myList.add(p);
			}
			inputReader.close();

			Collections.sort(myList, new Comparator<ClusterPoint>()
			{
				public int compare(ClusterPoint p1, ClusterPoint p2)
				{
					if (p1.eventNum < p2.eventNum)
						return -1;
					else if (p1.eventNum > p2.eventNum)
						return 1;
					else
						return 0;
				}
			});

			for (ClusterPoint p : myList)
			{
				//resultsWriter.write(p.eventNum + ",");
				resultsWriter.write(p.clusterNum + "\n");
			}
			resultsWriter.close();

		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private double max(double a, double b)
	{
		if (a > b)
			return a;
		else
			return b;
	}
}