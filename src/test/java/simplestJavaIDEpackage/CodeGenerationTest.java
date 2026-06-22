package simplestJavaIDEpackage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.junit.Test;
import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.FileManager;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedSource;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedSource.MethodLocation;
import simplestJavaIDEpackage.Library.CodeStructure.Methods;
import simplestJavaIDEpackage.Library.Commands.JavaCompilerService;

public class CodeGenerationTest {

  @Test
  public void mapsGeneratedLinesBackToMethods() {
    CodingFile file = new CodingFile("Demo", "/tmp/Demo.sji");
    file.methods.add(new Methods("foo", "public static void foo(){\n\t\n}"));
    GeneratedSource source = file.buildSource();

    // Line 1 is the generated class head, not part of any user method.
    assertNull(source.locate(1));

    // The main method occupies lines 2..4; line 3 is its 2nd local line.
    MethodLocation main = source.locate(3);
    assertNotNull(main);
    assertEquals("Main Method", main.methodName());
    assertEquals(2, main.localLine());

    // foo occupies lines 5..7; line 6 is its 2nd local line.
    MethodLocation foo = source.locate(6);
    assertNotNull(foo);
    assertEquals("foo", foo.methodName());
    assertEquals(2, foo.localLine());
  }

  @Test
  public void mapsCompilerErrorToMethodAndLine() throws Exception {
    Path dir = Files.createTempDirectory("sji");
    CodingFile file = new CodingFile("Demo", dir.resolve("Demo.sji").toString());
    file.methods.set(
        0,
        new Methods(
            "Main Method", "public static void main(String[] args){\n\tundefinedCall();\n}"));
    GeneratedSource source = file.buildSource();
    file.tmpSaveAndRunJavaCode();

    JavaCompilerService.Result result =
        JavaCompilerService.compile(file.getJavaTmpFilePath(), dir.toString());
    assertTrue(result.compilerAvailable());
    assertFalse(result.success());

    MethodLocation firstError = null;
    for (Diagnostic<? extends JavaFileObject> diagnostic : result.diagnostics()) {
      if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
        firstError = source.locate((int) diagnostic.getLineNumber());
        break;
      }
    }
    assertNotNull(firstError);
    assertEquals("Main Method", firstError.methodName());
    assertEquals(2, firstError.localLine());
  }

  @Test
  public void compilesAndRunsHelloWorld() throws Exception {
    Path dir = Files.createTempDirectory("sji");
    CodingFile file = new CodingFile("Demo", dir.resolve("Demo.sji").toString());
    file.tmpSaveAndRunJavaCode(); // default main prints "Hello World"

    JavaCompilerService.Result result =
        JavaCompilerService.compile(file.getJavaTmpFilePath(), dir.toString());
    assertTrue(result.success());

    String javaExecutable =
        System.getProperty("java.home") + java.io.File.separator + "bin" + java.io.File.separator
            + "java";
    Process process =
        new ProcessBuilder(javaExecutable, "-cp", dir.toString(), "Demo")
            .redirectErrorStream(true)
            .start();
    String output = new String(process.getInputStream().readAllBytes());
    process.waitFor();
    assertTrue("expected greeting, got: " + output, output.contains("Hello World"));
  }

  @Test
  public void jsonRoundTripPreservesContent() throws Exception {
    Path dir = Files.createTempDirectory("sji");
    String path = dir.resolve("Demo.sji").toString();
    CodingFile original = new CodingFile("Demo", path);
    original.imports.add("java.util.*");
    original.methods.add(new Methods("foo", "public static void foo(){}"));
    assertTrue(FileManager.save(original));

    CodingFile loaded = FileManager.load("Demo", path, false);
    assertNotNull(loaded);
    assertEquals(original.javaClass.className(), loaded.javaClass.className());
    assertEquals(original.imports, loaded.imports);
    assertEquals(original.methods, loaded.methods);
    // The path is not serialized; it is rebound to where the file was loaded from.
    assertEquals(path, loaded.getFilepath());
  }
}
