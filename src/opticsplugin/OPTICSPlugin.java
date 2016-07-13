package opticsplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Icon;

import com.treestar.lib.PluginHelper;
import com.treestar.lib.core.ExportFileTypes;
import com.treestar.lib.core.ExternalAlgorithmResults;
import com.treestar.lib.core.PopulationPluginInterface;
import com.treestar.lib.fjml.FJML;
import com.treestar.lib.xml.SElement;

public class OPTICSPlugin implements PopulationPluginInterface
{

	private List<String> fParameters = new ArrayList<String>();
	private SElement fOptions;

	@Override
	public SElement getElement()
	{
		SElement result = new SElement(getClass().getSimpleName());
		if (fOptions != null)
			result.addContent(new SElement(fOptions)); // create a copy of the
														// XML element
		// construct an XML element for each parameter name
		for (String pName : fParameters)
		{
			SElement pElem = new SElement(FJML.Parameter);
			pElem.setString(FJML.name, pName);
			result.addContent(pElem);
		}
		return result;

	}

	@Override
	public Icon getIcon()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return "OPTICSPlugin";
	}

	@Override
	public List<String> getParameters()
	{
		return fParameters;
	}

	@Override
	public String getVersion()
	{
		return "1.0";
	}

	@Override
	public ExternalAlgorithmResults invokeAlgorithm(SElement fcmlElem, File sampleFile, File outputFolder)
	{
		//results will be stored in here
		ExternalAlgorithmResults results = new ExternalAlgorithmResults();
		
		int minPts = fOptions.getInt("minPts");
		int epsilon = fOptions.getInt("epsilon");
		int dimensions = fOptions.getInt("numParams");
		double xi = fOptions.getDouble("xi");
		File outputFile = new File(outputFolder, "output." + sampleFile.getName());

		OPTICSAlgorithm myAlgorithm = new OPTICSAlgorithm(sampleFile, outputFile, minPts, (double) epsilon, dimensions);
		myAlgorithm.doIt();

		File clusterFile = new File(outputFolder, "cluster." + sampleFile.getName());
		// myAlgorithm.terriblexiClusterExtract(clusterFile, xi);
		// myAlgorithm.xiClusterExtract(clusterFile, xi);
		myAlgorithm.peakClusterSeparator(clusterFile, xi);
		File resultFile = new File(outputFolder, "result." + sampleFile.getName());

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(clusterFile));
			class MyPoint
			{
				int eventNum;
				int clusterNum;

				MyPoint()
				{
					super();
				}
			}

			ArrayList<MyPoint> eventList = new ArrayList<MyPoint>();

			String line = null;
			// br.readLine();
			while ((line = br.readLine()) != null)
			{
				String lineData[] = line.split(",");
				MyPoint p = new MyPoint();
				p.eventNum = Integer.parseInt(lineData[0]);
				p.clusterNum = Integer.parseInt(lineData[1]);
				eventList.add(p);
			}
			br.close();

			// We should sort this. Just in case.

			Collections.sort(eventList, new Comparator<MyPoint>()
			{
				public int compare(MyPoint p1, MyPoint p2)
				{
					if (p1.eventNum < p2.eventNum)
						return -1;
					else if (p1.eventNum > p2.eventNum)
						return 1;
					else
						return 0;
				}
			});

			/**
			 * resultWriter writes a single column CSV file. Each row
			 * corresponds to an event, and is populated with the cluster number
			 * of the event.
			 */
			FileWriter resultWriter = new FileWriter(resultFile);

			int pointCounter = 0;
			int numEvents = PluginHelper.getNumExportedEvents(fcmlElem);

			for (int i = 0; i < numEvents; i++)
			{

				if (pointCounter < eventList.size()) // Important...
				{
					MyPoint p = eventList.get(pointCounter);
					if (p.eventNum == i)
					{
						resultWriter.write(p.clusterNum + "\n");
						pointCounter++;
					} else
					{
						resultWriter.write("0\n");
					}
				} else
					resultWriter.write("0\n");

			}
			resultWriter.close();

		} catch (

		IOException e)
		{
			results.setErrorMessage("Something went wrong.");
		}

		// Return empty results!

		results.setCSVFile(resultFile);

		return results;
	}

	@Override
	public boolean promptForOptions(SElement fcmlQueryElement, List<String> parameterNames)
	{
		SElement algorithmElement = getElement();
		OPTICSOptionsPrompt prompt = new OPTICSOptionsPrompt(algorithmElement, parameterNames);

		if (!prompt.doIt())
			return false; // User cancelled operation

		setElement(prompt.getElement()); // set the plugin's element
		return true;
	}

	@Override
	public void setElement(SElement elem)
	{
		fOptions = elem.getChild("Options"); // could be null
		fParameters.clear();
		for (int i = 0; i < fOptions.getInt("numParams"); i++)
		{
			fParameters.add(fOptions.getChild(("Parameter" + i)).getTextContent());
		}
		// System.out.println(fOptions.toString());
		// System.out.println(fParameters.toString());
	}

	@Override
	public ExportFileTypes useExportFileType()
	{
		return ExportFileTypes.CSV_SCALE;
	}
}
