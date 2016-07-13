package opticsplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;

import com.treestar.lib.xml.SElement;

public class OPTICSOptionsPrompt implements WindowListener, ActionListener, ListSelectionListener, KeyListener
{
	boolean cancelled = true; //Setting to true by default prevents issues when 'x' is hit.
	private JLabel numParametersLabel;
	private JDialog myWindow;
	private List<String> parameterList;
	// private List<String> selectedParameters;
	SElement algorithmElement;
	private JTextField epsField;
	private JTextField minPtsField;
	private JRadioButton defaultButton;
	private JRadioButton customButton;
	private JButton cancelButton;
	private JButton runButton;
	private ButtonGroup minPtsGroup;
	private JList parameterJList;
	private JTextField xiField;

	OPTICSOptionsPrompt(SElement algorithmElement, List<String> parameterList)
	{
		this.parameterList = parameterList;
		this.algorithmElement = algorithmElement;
	}

	boolean doIt()
	{
		int labelWidth = 50;
		// First, build up the window to display.
		JFrame myFrame = new JFrame();
		myWindow = new JDialog(myFrame, "OPTICS Plugin Parameters", true);
		JPanel myPanel = new JPanel();
		myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
		myPanel.setBorder(BorderFactory.createEmptyBorder());

		// copy List parameterList to vector parameters
		String parameters[] = new String[parameterList.size()];
		for (int i = 0; i < parameters.length; i++)
		{
			parameters[i] = parameterList.get(i);
		}

		parameterJList = new JList(parameters);
		parameterJList.addListSelectionListener(this);
		JScrollPane listScrollPane = new JScrollPane(parameterJList);
		listScrollPane.setPreferredSize(new Dimension(150, 150));

		JLabel listLabel = new JLabel("Please select parameters: ");
		numParametersLabel = new JLabel("Num Parameters: 0");
		Box listBox = Box.createVerticalBox();
		Box scrollPaneBox = Box.createHorizontalBox();
		scrollPaneBox.add(Box.createRigidArea(new Dimension(10, 0)));
		scrollPaneBox.add(listScrollPane);
		scrollPaneBox.add(Box.createRigidArea(new Dimension(10, 0)));

		listBox.add(listLabel);
		listBox.add(scrollPaneBox);
		listBox.add(numParametersLabel);

		// Epsilon label and text field
		NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);

		String epsToolTipText = "The epsilon-max value sets a maximum distance for which OPTICS will search for neighbors. "
				+ "Setting higher values will yield better results at the cost of time, while values that are too low may"
				+ " miss some less dense clusters.";
		epsField = new JFormattedTextField(formatter);
		epsField.setText("10000");
		epsField.setEditable(true);
		epsField.setPreferredSize(new Dimension(100, 25));
		epsField.setToolTipText(epsToolTipText);
		epsField.setVerifyInputWhenFocusTarget(true);
		epsField.addActionListener(this);
		epsField.addKeyListener(this);

		JLabel epsLabel = new JLabel("\u03B5-max:");
		epsLabel.setToolTipText(epsToolTipText);
		epsLabel.setPreferredSize(new Dimension(labelWidth, 25));

		Box epsBox = Box.createHorizontalBox();

		epsBox.add(Box.createRigidArea(new Dimension(10, 0)));
		epsBox.add(epsLabel, BorderLayout.WEST);
		epsBox.add(Box.createRigidArea(new Dimension(10, 0)));
		epsBox.add(epsField, BorderLayout.EAST);
		epsBox.add(Box.createRigidArea(new Dimension(10, 0)));

		// MinPts label and text field
		String minPtsToolTipText = "Minimum # of points to define a cluster. Recommended at least # parameters selected.";
		JLabel minPtsLabel = new JLabel("minPts:");
		minPtsLabel.setToolTipText(minPtsToolTipText);
		minPtsLabel.setPreferredSize(new Dimension(labelWidth, 25));

		minPtsField = new JFormattedTextField(formatter);
		minPtsField.setText("0");
		minPtsField.setEditable(false);
		minPtsField.setPreferredSize(new Dimension(100, 25));
		minPtsField.setToolTipText(minPtsToolTipText);
		minPtsField.addActionListener(this);
		minPtsField.addKeyListener(this);

		Box minPtsBox = Box.createHorizontalBox();
		minPtsBox.add(Box.createHorizontalStrut(10));
		minPtsBox.add(minPtsLabel);
		minPtsBox.add(Box.createHorizontalStrut(10));
		minPtsBox.add(minPtsField);
		minPtsBox.add(Box.createHorizontalGlue());
		minPtsBox.add(Box.createHorizontalStrut(10));

		String xiToolTipText = "%drop to define a down slope. Must be between 0 and 1.";
		JLabel xiLabel = new JLabel("xi:");
		xiLabel.setToolTipText(xiToolTipText);
		xiLabel.setPreferredSize(new Dimension(labelWidth, 25));

		xiField = new JTextField();
		xiField.setText("0.2");
		xiField.setPreferredSize(new Dimension(100, 25));
		xiField.setToolTipText(xiToolTipText);
		xiField.addActionListener(this);
		xiField.addKeyListener(this);

		Box xiBox = Box.createHorizontalBox();
		xiBox.add(Box.createHorizontalStrut(10));
		xiBox.add(xiLabel);
		xiBox.add(Box.createHorizontalStrut(10));
		xiBox.add(xiField);
		xiBox.add(Box.createHorizontalGlue());
		xiBox.add(Box.createHorizontalStrut(10));

		defaultButton = new JRadioButton("Default", true);
		customButton = new JRadioButton("Custom");
		defaultButton.addActionListener(this);
		customButton.addActionListener(this);
		minPtsGroup = new ButtonGroup();
		minPtsGroup.add(defaultButton);
		minPtsGroup.add(customButton);

		Box radioBox = Box.createHorizontalBox();
		radioBox.add(Box.createHorizontalStrut(10));
		radioBox.add(defaultButton);
		radioBox.add(Box.createHorizontalStrut(10));
		radioBox.add(customButton);
		radioBox.add(Box.createHorizontalStrut(10));

		// cancel and run buttons
		cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(new Dimension(100, 25));
		cancelButton.addActionListener(this);

		runButton = new JButton("Run");
		runButton.setEnabled(false);
		runButton.setPreferredSize(new Dimension(100, 25));
		runButton.addActionListener(this);

		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(runButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(cancelButton);
		buttonBox.add(Box.createHorizontalStrut(10));

		// Put it all together now
		myPanel.add(Box.createVerticalStrut(10));
		myPanel.add(listBox);
		myPanel.add(Box.createVerticalStrut(5));
		myPanel.add(epsBox);
		myPanel.add(Box.createVerticalStrut(5));
		myPanel.add(minPtsBox);
		myPanel.add(Box.createVerticalStrut(5));
		myPanel.add(radioBox);
		myPanel.add(Box.createVerticalStrut(5));
		myPanel.add(xiBox);
		myPanel.add(Box.createVerticalStrut(5));
		myPanel.add(buttonBox);
		myPanel.add(Box.createVerticalStrut(10));
		myWindow.add(myPanel);
		myWindow.addWindowListener(this);
		myWindow.pack();
		// myWindow.setResizable(false);
		myWindow.setVisible(true);

		return !cancelled;
	}

	@Override
	public void windowActivated(WindowEvent e)
	{

	}

	@Override
	public void windowClosed(WindowEvent e)
	{

	}

	@Override
	public void windowClosing(WindowEvent e)
	{
		myWindow.setVisible(false);
		myWindow.dispose();
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{

	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{

	}

	@Override
	public void windowIconified(WindowEvent e)
	{

	}

	@Override
	public void windowOpened(WindowEvent e)
	{

	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		Object source = arg0.getSource();
		// System.out.println(source.toString());
		// If one of the radio buttons is the source...
		if (source.equals(customButton) || source.equals(defaultButton))
		{
			if (source.equals(defaultButton))
			{
				// Make the field not editable, set default value
				minPtsField.setEditable(false);
				int numSelected = parameterJList.getSelectedIndices().length;
				minPtsField.setText(Integer.toString(numSelected * 2));
			} else
			{
				minPtsField.setEditable(true);
			}
		}

		if (source.equals(minPtsField))
		{
			int minPts = Integer.MIN_VALUE;
			try
			{
				minPts = Integer.parseInt(minPtsField.getText().replaceAll(",", ""));
			} catch (NumberFormatException nfe)
			{
				runButton.setEnabled(false);
			}
			if (minPts < parameterJList.getSelectedIndices().length)
			{
				runButton.setEnabled(false);
			} else
			{
				runButton.setEnabled(true);
			}
		}

		// Case if cancelled
		if (source.equals(cancelButton))
		{
			cancelled = true;
			myWindow.setVisible(false);
			myWindow.dispose();
		}

		// Case if Run - check for valid inputs
		if (source.equals(runButton))
		{
			runButtonCheck();
			if (!runButton.isEnabled())
				return;
			int minPts;
			int epsilon;
			double xi;
			try
			{
				minPts = Integer.parseInt(minPtsField.getText().replaceAll(",", ""));
				epsilon = Integer.parseInt(epsField.getText().replaceAll(",", ""));
				xi = Double.parseDouble(xiField.getText());
			} catch (Exception e)
			{
				return;
			}
			// Add values into the element
			SElement options = new SElement("Options");
			options.setInt("minPts", minPts);
			options.setInt("epsilon", epsilon);
			options.setDouble("xi", xi);

			// How to make this meet the standard for creating the list?

			int[] selectedIndices = parameterJList.getSelectedIndices();
			options.setInt("numParams", selectedIndices.length);
			for (int i = 0; i < selectedIndices.length; i++)
			{
				String parameter = parameterList.get(selectedIndices[i]);
				// if the parameter has a colon, FlowJo gets mad. This fixes it.
				if (parameter.contains(":"))
				{
					parameter = parameter.substring(0, parameter.indexOf(':')).trim();
				}
				options.addContent(new SElement(("Parameter" + i), parameter));
			}
			algorithmElement.addContent(options);

			cancelled = false;
			myWindow.setVisible(false);
			myWindow.dispose();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0)
	{
		JList myList = (JList) arg0.getSource();
		int numSelected = myList.getSelectedIndices().length;
		numParametersLabel.setText("Num Parameters: " + numSelected);

		// If radio button is selected, make sure
		if (defaultButton.isSelected())
		{
			minPtsField.setText(Integer.toString(numSelected * 2));
		}

		runButtonCheck();
	}

	public SElement getElement()
	{
		return algorithmElement;
	}

	public void runButtonCheck()
	{
		String minPtsText = minPtsField.getText().replaceAll(",", "");
		String epsText = epsField.getText().replaceAll(",", "");
		String xiText = xiField.getText();
		int numParams = parameterJList.getSelectedIndices().length;

		if (minPtsText.length() > 0 && epsText.length() > 0)
		{
			int minPts = Integer.MIN_VALUE;
			int epsilon = Integer.MIN_VALUE;
			double xi = Double.NEGATIVE_INFINITY;

			try
			{
				minPts = Integer.parseInt(minPtsText);
				epsilon = Integer.parseInt(epsText);
				xi = Double.parseDouble(xiText);
			} catch (NumberFormatException nfe)
			{
				minPts = Integer.MIN_VALUE;
				epsilon = Integer.MIN_VALUE;
				xi = Double.NEGATIVE_INFINITY;
				runButton.setEnabled(false);
				return;
			}

			if (minPts < numParams)
			{
				runButton.setEnabled(false);
				return;
			}
			if (epsilon < 1)
			{
				runButton.setEnabled(false);
				return;
			}
			if (numParams < 2)
			{
				runButton.setEnabled(false);
				return;
			}
			if (xi < 0 || xi > 1)
			{
				runButton.setEnabled(false);
				return;
			}
		} else
		{
			runButton.setEnabled(false);
			return;
		}

		runButton.setEnabled(true);
	}

	@Override
	public void keyPressed(KeyEvent arg0)
	{
		//Don't care.
	}

	@Override
	public void keyReleased(KeyEvent arg0)
	{
		//This makes sure that the input is valid on each keystroke
		runButtonCheck();
	}

	@Override 
	public void keyTyped(KeyEvent arg0)
	{
		runButtonCheck();
	}
}
