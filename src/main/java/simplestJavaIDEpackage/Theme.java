package simplestJavaIDEpackage;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import java.awt.Color;
import java.awt.Insets;
import java.util.Map;
import javax.swing.UIManager;

/**
 * Central place for the application's look: a deliberate accent colour and a set
 * of FlatLaf tweaks (rounded components, calm tabs, slim scrollbars) so the UI
 * feels designed rather than default.
 *
 * @author Daniel Trageser
 */
public final class Theme {

  /** Accent used for focus, selection and the primary button. */
  public static final Color ACCENT = new Color(0x4C9AFF);
  /** Semantic action colours. */
  public static final Color RUN_GREEN = new Color(0x3FB950);
  public static final Color STOP_RED = new Color(0xE5534B);
  public static final Color HINT_AMBER = new Color(0xE3B341);

  /** Editor and console backgrounds (kept close for a cohesive code surface). */
  public static final Color EDITOR_BG = new Color(0x1E1F22);
  public static final Color CONSOLE_BG = new Color(0x191A1C);
  public static final Color PANEL_BORDER = new Color(0x33363B);

  private Theme() {}

  public static void setup() {
    FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#4C9AFF"));
    FlatDarkLaf.setup();

    UIManager.put("Button.arc", 14);
    UIManager.put("Component.arc", 12);
    UIManager.put("TextComponent.arc", 8);
    UIManager.put("CheckBox.arc", 6);
    UIManager.put("Component.focusWidth", 1);
    UIManager.put("Component.innerFocusWidth", 1);

    UIManager.put("ScrollBar.width", 12);
    UIManager.put("ScrollBar.thumbArc", 999);
    UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
    UIManager.put("ScrollBar.track", new Color(0, 0, 0, 0));

    UIManager.put("TabbedPane.showTabSeparators", false);
    UIManager.put("TabbedPane.tabHeight", 32);
    UIManager.put("TabbedPane.tabInsets", new Insets(4, 14, 4, 14));
    UIManager.put("TabbedPane.contentSeparatorHeight", 0);
    UIManager.put("TabbedPane.hoverColor", new Color(255, 255, 255, 18));

    UIManager.put("ToolBar.separatorColor", new Color(255, 255, 255, 28));
    UIManager.put("Component.hideMnemonics", false);
    UIManager.put("TitlePane.unifiedBackground", true);
  }
}
