package simplestJavaIDEpackage;

/**
 * Thin compatibility shim: existing call sites still call {@code throwMessage},
 * but messages now go to the unobtrusive {@link Notifications} / status bar
 * instead of spawning an always-on-top window.
 *
 * @author Daniel Trageser
 */
public class ErrorPopupWindow {

  /** Reports an error message to the user (status bar, or dialog as fallback). */
  public static void throwMessage(String errorText) {
    Notifications.error(errorText);
  }
}
