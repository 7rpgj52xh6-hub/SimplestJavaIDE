package simplestJavaIDEpackage;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Persists small bits of UI state between sessions (font size, main window
 * bounds, recently opened files) via {@link Preferences}.
 *
 * @author Daniel Trageser
 */
public final class AppPreferences {

  private static final Preferences PREFS = Preferences.userNodeForPackage(AppPreferences.class);
  private static final String KEY_FONT_SIZE = "fontSize";
  private static final String KEY_WINDOW_BOUNDS = "windowBounds";
  private static final String KEY_RECENT_FILES = "recentFiles";
  private static final int MAX_RECENT = 6;

  private AppPreferences() {}

  public static int getFontSize(int fallback) {
    return PREFS.getInt(KEY_FONT_SIZE, fallback);
  }

  public static void setFontSize(int size) {
    PREFS.putInt(KEY_FONT_SIZE, size);
  }

  public static Rectangle getWindowBounds() {
    String value = PREFS.get(KEY_WINDOW_BOUNDS, "");
    String[] parts = value.split(",");
    if (parts.length != 4) {
      return null;
    }
    try {
      return new Rectangle(
          Integer.parseInt(parts[0]),
          Integer.parseInt(parts[1]),
          Integer.parseInt(parts[2]),
          Integer.parseInt(parts[3]));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static void setWindowBounds(Rectangle bounds) {
    PREFS.put(
        KEY_WINDOW_BOUNDS, bounds.x + "," + bounds.y + "," + bounds.width + "," + bounds.height);
  }

  /** Returns recently opened file paths (most recent first) that still exist. */
  public static List<String> getRecentFiles() {
    List<String> result = new ArrayList<>();
    for (String path : PREFS.get(KEY_RECENT_FILES, "").split("\n")) {
      if (!path.isBlank() && new File(path).exists()) {
        result.add(path);
      }
    }
    return result;
  }

  public static void addRecentFile(String path) {
    List<String> recent = getRecentFiles();
    recent.remove(path);
    recent.add(0, path);
    while (recent.size() > MAX_RECENT) {
      recent.remove(recent.size() - 1);
    }
    PREFS.put(KEY_RECENT_FILES, String.join("\n", recent));
  }
}
