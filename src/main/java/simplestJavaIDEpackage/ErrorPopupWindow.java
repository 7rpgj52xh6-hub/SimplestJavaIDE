package simplestJavaIDEpackage;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ErrorPopupWindow {

  private JFrame frmErrorPopupWindow;

  /** Create the application. */
  public ErrorPopupWindow(String errorText) {
    initialize(errorText);
  }

  /** Launch the application only with error. */
  public static void throwMessage(String errorText) {
    EventQueue.invokeLater(
        () -> {
          try {
            ErrorPopupWindow window = new ErrorPopupWindow(errorText);
            window.frmErrorPopupWindow.setVisible(true);
          } catch (Exception e) {
            // Can't be added to normal error handling. Danger of endless loop.
            System.out.println(e.getMessage());
          }
        });
  }

  static void setScrollPaneSettings(JTextPane textPaneErrors, JFrame frmErrorPopupWindow) {
    JScrollPane errorTextPaneScrollPane = new JScrollPane(textPaneErrors);
    JScrollBar errorTextPaneScrollPaneScrollBar = errorTextPaneScrollPane.getVerticalScrollBar();
    SwingUtilities.invokeLater(
        () ->
            errorTextPaneScrollPaneScrollBar.setValue(
                errorTextPaneScrollPaneScrollBar.getMinimum()));
    errorTextPaneScrollPane.setHorizontalScrollBarPolicy(
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    errorTextPaneScrollPane.setVerticalScrollBarPolicy(
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    frmErrorPopupWindow.getContentPane().add(errorTextPaneScrollPane, BorderLayout.CENTER);
  }

  /** Initialize the contents of the frame. */
  private void initialize(String errorText) {
    List<String> errors = new ArrayList<>();
    if (errorText != null) {
      errors.add(errorText);
    }
    frmErrorPopupWindow = new JFrame();
    frmErrorPopupWindow.setTitle("Error");
    frmErrorPopupWindow.setAlwaysOnTop(true);
    frmErrorPopupWindow.setBounds(100, 100, 1000, 300);
    frmErrorPopupWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frmErrorPopupWindow.setResizable(false);

    // Set Icon
    try {
      frmErrorPopupWindow.setIconImage(
          ImageIO.read(
              Objects.requireNonNull(getClass().getClassLoader().getResource("favicon.png"))));
    } catch (IOException e) {
      errors.add(e.getMessage());
    }

    JButton btnClose = new JButton("Close");
    btnClose.addActionListener(e -> frmErrorPopupWindow.dispose());
    frmErrorPopupWindow.getContentPane().add(btnClose, BorderLayout.SOUTH);

    JPanel panelError = new JPanel(new BorderLayout(0, 0));
    JTextPane textPaneErrors = new JTextPane();
    textPaneErrors.setEditable(false);
    panelError.add(textPaneErrors, BorderLayout.CENTER);
    textPaneErrors.setContentType("text/html");

    setScrollPaneSettings(textPaneErrors, frmErrorPopupWindow);

    // Show all errors in textPane
    StringBuilder output = new StringBuilder();
    for (String i : errors) {
      output.append(i).append("\n");
    }
    textPaneErrors.setText(output.toString());
  }
}
