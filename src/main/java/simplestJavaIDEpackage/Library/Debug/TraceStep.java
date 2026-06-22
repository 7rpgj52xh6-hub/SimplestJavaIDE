package simplestJavaIDEpackage.Library.Debug;

import java.util.Map;

/**
 * One recorded execution step: the line in the generated source that is about to
 * run and a snapshot of all visible local variables and their values at that
 * moment.
 *
 * @author Daniel Trageser
 */
public record TraceStep(int generatedLine, Map<String, String> variables) {}
