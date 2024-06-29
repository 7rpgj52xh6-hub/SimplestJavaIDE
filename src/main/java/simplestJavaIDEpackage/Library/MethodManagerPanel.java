package simplestJavaIDEpackage.Library;

import java.awt.Dimension;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.FileManager;
import simplestJavaIDEpackage.Library.CodeStructure.Methods;

public class MethodManagerPanel extends JPanel{
	private static final long serialVersionUID = 4457723430958322668L;
	 private final DefaultListModel<String> listModel = new DefaultListModel<>();
	  private List<CodingArea> listOfCodingAreas;
	  private JTabbedPane tabbedPaneMethods;
	  private TerminalPanel terminal;
	public MethodManagerPanel(CodingFile codingFile, CodingArea mainCodingArea, List<CodingArea> listOfCodingAreas, JTabbedPane tabbedPaneMethods, TerminalPanel terminal) {
		super();
		this.listOfCodingAreas = listOfCodingAreas;
		this.tabbedPaneMethods = tabbedPaneMethods;
		this.terminal = terminal;
		// Panel for managing methods
	    JSplitPane splitPaneManagingMethods = new JSplitPane();
	    splitPaneManagingMethods.setOrientation(JSplitPane.VERTICAL_SPLIT);
	    JPanel inputPanel = new JPanel();
	    splitPaneManagingMethods.setLeftComponent(inputPanel);

	    JLabel labelImport = new JLabel("Method name:");
	    inputPanel.add(labelImport);

	    JTextField textFieldMethodName = new JTextField();
	    textFieldMethodName.setHorizontalAlignment(SwingConstants.CENTER);
	    textFieldMethodName.setText("");
	    inputPanel.add(textFieldMethodName);
	    textFieldMethodName.setColumns(25);

	    JList<String> listOfMethods = new JList<>(listModel);
	    for (Methods i : codingFile.methods) {
	      listModel.add(codingFile.methods.indexOf(i), i.getName());
	    }
	    splitPaneManagingMethods.setRightComponent(listOfMethods);

	    JButton btnAddMethod = getBtnAddMethod(codingFile, textFieldMethodName, mainCodingArea);
	    inputPanel.add(btnAddMethod);

	    JButton btnDeleteMethod = getBtnDeleteMethod(codingFile, listOfMethods);
	    inputPanel.add(btnDeleteMethod);
	    
	    this.add(splitPaneManagingMethods);
	}
	
	private JButton getBtnDeleteMethod(CodingFile codingFile, JList<String> listOfMethods) {
	    JButton btnDeleteMethod = new JButton("Delete");
	    btnDeleteMethod.addActionListener(
	        arg0 -> {
	          int toDeleteIndex = listOfMethods.getSelectedIndex();
	          if (toDeleteIndex != 0) {
	            listModel.remove(toDeleteIndex); // remove listModel entry
	            codingFile.methods.remove(toDeleteIndex); // remove method
	            listOfCodingAreas.remove(toDeleteIndex); // remove CodingArea
	            tabbedPaneMethods.removeTabAt(toDeleteIndex); // remove tabbed pane
	          }
	        });
	    btnDeleteMethod.setPreferredSize(new Dimension(100, 36));
	    return btnDeleteMethod;
	  }

	  private JButton getBtnAddMethod(
	      CodingFile codingFile, JTextField textFieldMethodName, CodingArea mainCodingArea) {
	    JButton btnAddMethod = new JButton("Add");
	    btnAddMethod.addActionListener(
	        arg0 -> {
	          String newMethodName = textFieldMethodName.getText();
	          if (!newMethodName.isEmpty()) {
	            if (!newMethodName.contains(" ")) {
	              if ((!listModel.contains(newMethodName))
	                  && (!codingFile.checkIfMethodWithSameNameExists(newMethodName))) {
	                listModel.addElement(textFieldMethodName.getText());
	                codingFile.methods.add(
	                    new Methods(
	                        newMethodName, "public static void " + newMethodName + "(){\n\t\n}"));
	                int index = tabbedPaneMethods.getTabCount() - 1;
	                CodingArea newCodingArea =
	                    new CodingArea(
	                        codingFile.returnMethodFromName(newMethodName),
	                        terminal.getRunButton(),
	                        terminal.getSaveButton(),
	                        mainCodingArea.getTextArea().getFont());
	                listOfCodingAreas.add(newCodingArea);
	                tabbedPaneMethods.insertTab(newMethodName, null, newCodingArea, null, index);
	                FileManager.save(codingFile);
	              }
	            }
	          }
	        });
	    btnAddMethod.setPreferredSize(new Dimension(100, 36));
	    return btnAddMethod;
	  }
}
