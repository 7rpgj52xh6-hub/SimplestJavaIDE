package simplestJavaIDEpackage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A collapsible list of the current compiler errors. Each entry is clickable and
 * jumps to the matching class tab, method tab and line. Hidden when there are no
 * errors.
 *
 * @author Daniel Trageser
 */
public class ErrorListPanel extends JPanel {

  /** One error: where it is and what to show. */
  public record ErrorEntry(int classIndex, int methodIndex, int localLine, String display) {}

  private final DefaultListModel<ErrorEntry> model = new DefaultListModel<>();
  private final JList<ErrorEntry> list = new JList<>(model);
  private final JLabel header = new JLabel();
  private Consumer<ErrorEntry> onSelect = entry -> {};

  public ErrorListPanel() {
    super(new BorderLayout());
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.PANEL_BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));

    header.setForeground(Theme.STOP_RED);
    add(header, BorderLayout.NORTH);

    list.setCellRenderer(
        new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(
              JList<?> l, Object value, int index, boolean selected, boolean focused) {
            JLabel label =
                (JLabel)
                    super.getListCellRendererComponent(l, value, index, selected, focused);
            label.setText("●  " + ((ErrorEntry) value).display());
            if (!selected) {
              label.setForeground(Theme.STOP_RED);
            }
            return label;
          }
        });
    list.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ErrorEntry entry = list.getSelectedValue();
            if (entry != null) {
              onSelect.accept(entry);
            }
          }
        });
    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
    scrollPane.setPreferredSize(new Dimension(100, 110));
    add(scrollPane, BorderLayout.CENTER);

    setVisible(false);
  }

  public void setOnSelect(Consumer<ErrorEntry> onSelect) {
    this.onSelect = onSelect;
  }

  /** Replaces the shown errors; hides the panel when there are none. */
  public void setErrors(List<ErrorEntry> errors) {
    model.clear();
    for (ErrorEntry entry : errors) {
      model.addElement(entry);
    }
    header.setText(errors.size() + " Fehler");
    boolean show = !errors.isEmpty();
    if (isVisible() != show) {
      setVisible(show);
      revalidate();
    }
  }
}
