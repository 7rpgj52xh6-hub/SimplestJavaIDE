package simplestJavaIDEpackage;

/**
 * Runtime capability flags so the single codebase can serve both builds:
 *
 * <ul>
 *   <li><b>Desktop</b> — full JDK: system compiler, process execution, JDI debugger.
 *   <li><b>Web</b> — CheerpJ in the browser: no {@code javax.tools} system compiler,
 *       no second process/JVM (so student code runs in-process), no debugger.
 * </ul>
 *
 * <p>The web runtime is detected by CheerpJ's own JVM identity — deliberately NOT by
 * the absence of a system compiler, which would misclassify a Desktop jar launched on
 * a bare JRE as "web". An explicit {@code -Dsji.platform=web|desktop} overrides it.
 *
 * @author Daniel Trageser
 */
public final class Platform {

  /** True when running in the browser under CheerpJ. */
  public static final boolean WEB = detectWeb();

  /** The step debugger (JDI) needs a second JVM, which is impossible under CheerpJ. */
  public static final boolean DEBUGGER_SUPPORTED = !WEB;

  private Platform() {}

  private static boolean detectWeb() {
    String override = System.getProperty("sji.platform", "");
    if ("web".equalsIgnoreCase(override)) {
      return true;
    }
    if ("desktop".equalsIgnoreCase(override)) {
      return false;
    }
    String vm = System.getProperty("java.vm.name", "").toLowerCase();
    String arch = System.getProperty("os.arch", "").toLowerCase();
    return vm.contains("cheerpj") || arch.contains("cheerpj");
  }
}
