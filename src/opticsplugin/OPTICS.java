package opticsplugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.treestar.lib.PluginHelper;
import com.treestar.lib.core.ExportFileTypes;
import com.treestar.lib.core.ExternalAlgorithmResults;
import com.treestar.lib.core.PopulationPluginInterface;
import com.treestar.lib.fjml.FJML;
import com.treestar.lib.xml.SElement;

public class OPTICS implements PopulationPluginInterface
{

	private List<String> fParameters = new ArrayList<String>();
	private SElement fOptions;

	@Override
	public SElement getElement()
	{
		SElement result = new SElement(getClass().getSimpleName());
		if (fOptions != null)
			result.addContent(new SElement(fOptions)); // create a copy of the
														// // XML element
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
		Icon myIcon = null;
		File iconFile = new File("OPTICSIcon.gif");
		URL imgURL = getClass().getResource("OPTICSIcon.gif");
		if(iconFile.exists())
			myIcon = new ImageIcon("OPTICSIcon.gif");
		else if(imgURL != null)
			myIcon = new ImageIcon(imgURL);
		return myIcon;
	}

	@Override
	public String getName()
	{
		// This determines the name of the node, too
		if (fOptions == null)
			return "OPTICS";

		String name = "OPTICS";
		name += "_minPts_" + fOptions.getAttribute("minPts");
		name += "_epsilon_" + fOptions.getAttribute("epsilon");
		name += "_xi_" + fOptions.getAttribute("xi");
		name += "_numParams_" + fOptions.getAttribute("numParams");
		return name;

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

	/**
	 * Invokes the algorithm and returns the results.
	 */
	@Override
	public ExternalAlgorithmResults invokeAlgorithm(SElement fcmlElem, File sampleFile, File outputFolder)
	{
		// results will be stored in here
		ExternalAlgorithmResults results = new ExternalAlgorithmResults();

		int minPts = fOptions.getInt("minPts");
		int epsilon = fOptions.getInt("epsilon");
		int dimensions = fOptions.getInt("numParams");
		double xi = fOptions.getDouble("xi");

		File outputFile = new File(outputFolder, "output." + sampleFile.getName());

		OPTICSAlgorithm myAlgorithm = new OPTICSAlgorithm(sampleFile, outputFile, minPts, (double) epsilon, dimensions);
		try
		{
			myAlgorithm.doIt();
		} catch (IOException e)
		{
			results.setErrorMessage("There was a problem.");
			return results;
		}
		
		String ext = "_minPts_" + fOptions.getAttribute("minPts");
		ext += "_epsilon_" + fOptions.getAttribute("epsilon");
		ext += "_xi_" + fOptions.getAttribute("xi");
		ext += "_numParams_" + fOptions.getAttribute("numParams");
		File clusterFile = new File(outputFolder, "cluster" +ext+ sampleFile.getName());

		// Do the cluster extraction
		myAlgorithm.peakClusterSeparator(clusterFile, xi);
		// myAlgorithm.terriblexiClusterExtract(clusterFile, xi);
		// myAlgorithm.xiClusterExtract(clusterFile, xi);

		String doClustering = fOptions.getAttribute("doClustering");

		// We're going to copy the cluster data into a single-column CSV for
		// use in the ExternalAlgorithmResult setCSV method.
		
		File clusterCopy = new File(outputFolder, "clusterCopy" + ext + sampleFile.getName());
		File orderNumFile = new File(outputFolder, "orderNum" + ext + sampleFile.getName());
		File reachabilityFile = new File(outputFolder, "reachability" + ext + sampleFile.getName());

		try
		{
			BufferedReader in = new BufferedReader(new FileReader(clusterFile));
			BufferedWriter clusterOut = new BufferedWriter(new FileWriter(clusterCopy));
			BufferedWriter orderNumOut = new BufferedWriter(new FileWriter(orderNumFile));
			BufferedWriter reachOut = new BufferedWriter(new FileWriter(reachabilityFile));

			String line = null;
			in.readLine();
			clusterOut.write("\n");

			orderNumOut.write("orderNum" + ext + "\n");
			reachOut.write("reachability" + ext + "\n");
			line = in.readLine();
			boolean readLine = true;
			int numEvents = PluginHelper.getNumTotalEvents(fcmlElem);
			String[] lineData = null;
			for(int i = 1; i <= numEvents; i++)
			{
				if(readLine)
				{
					readLine = false;
					if((line = in.readLine()) != null)
					{
						lineData = line.split(",");
						clusterOut.write(lineData[0] + "\n");
					}
					else break;
						
				}
				
				if(Integer.parseInt(lineData[3]) == i)
				{
					orderNumOut.write(lineData[1] + "\n");
					reachOut.write(lineData[2] + "\n");
					readLine = true;
				}
				else
				{
					orderNumOut.write("0\n");
					reachOut.write("0\n");
				}
			}
			
			in.close();
			clusterOut.close();
			orderNumOut.close();
			reachOut.close();

		} catch (IOException e)
		{
			results.setErrorMessage("something went wrong");
			return results;
		}

		if (doClustering.equals("true"))
			results.setCSVFile(clusterCopy);

		// Create new parameters!
		PluginHelper.createClusterParameter(results, "orderNum" + ext, orderNumFile);
		PluginHelper.createClusterParameter(results, "reachability" + ext, reachabilityFile);

		return results;
	}

	@Override
	public boolean promptForOptions(SElement fcmlQueryElement, List<String> parameterNames)
	{
		SElement algorithmElement = getElement();

		OPTICSOptionsPrompt prompt;
		if (fOptions != null)
			prompt = new OPTICSOptionsPrompt(algorithmElement, parameterNames, fOptions);
		else
			prompt = new OPTICSOptionsPrompt(algorithmElement, parameterNames);
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
