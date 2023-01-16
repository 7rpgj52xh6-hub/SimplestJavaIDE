package simplestJavaIDEpackage;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import java.awt.BorderLayout;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

public class ErrorPopupWindow {

  private JFrame frmErrorPopupWindow;

  /**
   * Launch the application.
   */
  public static void main(String[] args, String errorText) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          ErrorPopupWindow window = new ErrorPopupWindow(errorText);
          window.frmErrorPopupWindow.setVisible(true);
        } catch (Exception e) {
          // Can't be added to normal error handling. Danger of endless loop.
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the application.
   */
  public ErrorPopupWindow(String errorText) {
    initialize(errorText);
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize(String errorText) {
    List<String> errors = new ArrayList<String>();
    if (errorText != null) {
      errors.add(errorText);
    }
    frmErrorPopupWindow = new JFrame();
    frmErrorPopupWindow.setTitle("Error");
    frmErrorPopupWindow.setAlwaysOnTop(true);
    frmErrorPopupWindow.setBounds(100, 100, 1000, 300);
    frmErrorPopupWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frmErrorPopupWindow.setResizable(false);

    // Set Icon
    try {
      frmErrorPopupWindow
          .setIconImage(ImageIO.read(getClass().getClassLoader().getResource("favicon.png")));
    } catch (IOException e) {
      errors.add(e.getMessage());
    }

    JButton btnClose = new JButton("Close");
    btnClose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        frmErrorPopupWindow.dispose();
      }
    });
    frmErrorPopupWindow.getContentPane().add(btnClose, BorderLayout.SOUTH);

    JPanel panelError = new JPanel(new BorderLayout(0, 0));
    JTextPane textPaneErrors = new JTextPane();
    textPaneErrors.setEditable(false);
    panelError.add(textPaneErrors, BorderLayout.CENTER);
    textPaneErrors.setContentType("text/html");

    JScrollPane errorTextPaneScrollPane = new JScrollPane(textPaneErrors);
    JScrollBar errorTextPaneScrollPaneScrollBar = errorTextPaneScrollPane.getVerticalScrollBar();
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        errorTextPaneScrollPaneScrollBar.setValue(errorTextPaneScrollPaneScrollBar.getMinimum());
      }
    });
    errorTextPaneScrollPane
        .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    errorTextPaneScrollPane
        .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    frmErrorPopupWindow.getContentPane().add(errorTextPaneScrollPane, BorderLayout.CENTER);

    // Show all errors in textPane
    String output = "";
    for (String i : errors) {
      output = output + i + "\n";
    }
    textPaneErrors.setText(output);
  }
}
