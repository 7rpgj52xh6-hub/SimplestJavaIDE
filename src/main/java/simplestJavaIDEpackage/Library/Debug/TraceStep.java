package simplestJavaIDEpackage.Library.Debug;

import java.util.Map;

/**
 * One execution step: the class and the line in its generated source that is
 * about to run, plus a snapshot of all visible local variables.
 *
 * @author Daniel Trageser
 */
public record TraceStep(String className, int generatedLine, Map<String, String> variables) {}
