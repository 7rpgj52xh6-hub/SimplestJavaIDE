package simplestJavaIDEpackage.Library;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class ZoomOutButton extends JPanel {
  private static final long serialVersionUID = -7383812177903148141L;
  private JButton button;

  public ZoomOutButton() {
    this.setPreferredSize(new Dimension(44, 36));
    this.setOpaque(false);
    this.setLayout(new GridBagLayout());
    try {
      Image tmpImage =
          ImageIO.read(
                  Objects.requireNonNull(getClass().getClassLoader().getResource("btnZoomOut.png")))
              .getScaledInstance(20, 20, Image.SCALE_SMOOTH);
      ImageIcon iconBtn1 = new ImageIcon(tmpImage);
      tmpImage =
          ImageIO.read(
                  Objects.requireNonNull(
                      getClass().getClassLoader().getResource("btnZoomOutPressed.png")))
              .getScaledInstance(20, 20, Image.SCALE_SMOOTH);
      ImageIcon iconBtn2 = new ImageIcon(tmpImage);
      button = new JButton();
      this.add(button);
      button.setIcon(iconBtn1);
      button.setPressedIcon(iconBtn2);
      button.setText(null);
      button.setBorderPainted(false);
      button.setBorder(null);
      button.setMargin(new Insets(0, 0, 0, 0));
      button.setContentAreaFilled(false);
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage("Could not load zoom-out-button icon: " + e.getMessage());
    }
  }

  public JButton getButton() {
    return button;
  }
}
