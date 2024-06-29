package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;

public class MethodTabsPanel extends JPanel {

	private static final long serialVersionUID = 1230287069025734089L;
	private CodingFile codingFile;
	private TerminalPanel terminal;
	private JTabbedPane tabbedPaneMethods;
	private CodingArea mainCodingArea;
	private List<CodingArea> listOfCodingAreas = new ArrayList<>();
	
	public MethodTabsPanel(CodingFile codingFile, TerminalPanel terminal) {
		super();
		this.codingFile = codingFile;
		this.terminal = terminal;
		// Coding input and load code if code is not null (from loading file)
	    mainCodingArea =
	        new CodingArea(
	            codingFile.methods.get(0), terminal.getRunButton(), terminal.getSaveButton(), null);

	    tabbedPaneMethods = new JTabbedPane(SwingConstants.TOP);
	    listOfCodingAreas.add(mainCodingArea);
	    for (int i = 1; i < codingFile.methods.size(); i++) {
	      listOfCodingAreas.add(
	          new CodingArea(
	              codingFile.methods.get(i),
	              terminal.getRunButton(),
	              terminal.getSaveButton(),
	              mainCodingArea.getFont()));
	    }
	    // Add a Tab for every coding area
	    for (CodingArea i : listOfCodingAreas) {
	      int index = tabbedPaneMethods.getTabCount();
	      tabbedPaneMethods.insertTab(i.getMethod().getName(), null, i, null, index);
	    }

	    MethodManagerPanel methodManagerPanel = new MethodManagerPanel(codingFile, mainCodingArea, listOfCodingAreas, tabbedPaneMethods, terminal);
	    JLabel labelManagingMethods = new JLabel("⚙");
	    Font currentFont = labelManagingMethods.getFont();
	    Font newFont = currentFont.deriveFont(currentFont.getSize() * 2F);
	    labelManagingMethods.setFont(newFont);
	    tabbedPaneMethods.addTab("[Managing Methods]", methodManagerPanel);
	    // panel
	    int indexOfManagingMethodsTab = -1;
	    for (int i = 1; i < tabbedPaneMethods.getTabCount(); i++) {
	      if (tabbedPaneMethods.getTitleAt(i).equals("[Managing Methods]")) {
	        indexOfManagingMethodsTab = i;
	      }
	    }
	    if (indexOfManagingMethodsTab != -1) {
	      tabbedPaneMethods.setTabComponentAt(indexOfManagingMethodsTab, labelManagingMethods);
	    }
	    tabbedPaneMethods.setBorder(BorderFactory.createLineBorder(new Color(50, 53, 55)));
	    tabbedPaneMethods.setBackground(new Color(55, 58, 60));
	    
	    this.setLayout(new BorderLayout());
	    this.add(tabbedPaneMethods, BorderLayout.CENTER);
	}
	
	public CodingArea getMainCodingArea() {
		return mainCodingArea;
	}
	
	public List<CodingArea> getListOfCodingAreas(){
		return listOfCodingAreas;
	}

}
