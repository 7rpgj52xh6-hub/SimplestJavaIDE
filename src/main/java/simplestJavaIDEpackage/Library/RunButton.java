package simplestJavaIDEpackage.Library;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class RunButton extends JPanel {
  private static final long serialVersionUID = -7383812177903148141L;
  private JButton button;

  public RunButton() {
    this.setPreferredSize(new Dimension(66, 36));
    this.setOpaque(false);
    this.setLayout(new GridBagLayout());

    try {
      Image tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnRun.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtn1 = new ImageIcon(tmpImage);
      tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnRunPressed.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtn2 = new ImageIcon(tmpImage);
      tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnRunDisabled.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtn3 = new ImageIcon(tmpImage);
      button = new JButton();
      this.add(button);
      button.setIcon(iconBtn1);
      button.setPressedIcon(iconBtn2);
      button.setDisabledIcon(iconBtn3);
      button.setText(null);
      button.setBorderPainted(false);
      button.setBorder(null);
      button.setMargin(new Insets(0, 0, 0, 0));
      button.setContentAreaFilled(false);
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage("Could not load run-button icon: " + e.getMessage());
    }
  }

  public JButton getButton() {
    return button;
  }
}
