package simplestJavaIDEpackage.Library.CodeStructure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import simplestJavaIDEpackage.ErrorPopupWindow;

/**
 * Reads and writes {@link CodingFile}s as human-readable JSON. (Earlier versions
 * used Java object serialization, which broke on every model change and could
 * not be inspected; the format is intentionally not backwards compatible.)
 */
public class FileManager {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  public static Boolean save(CodingFile codingFile) {
    Path path = Path.of(codingFile.getFilepath());
    try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      GSON.toJson(codingFile, writer);
      return true;
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
    return false;
  }

  public static CodingFile load(String className, String filepath, Boolean isNewFile) {
    if (isNewFile) {
      return load(className, createNewAndSave(className, filepath).getFilepath(), false);
    }
    try (Reader reader = Files.newBufferedReader(Path.of(filepath), StandardCharsets.UTF_8)) {
      CodingFile result = GSON.fromJson(reader, CodingFile.class);
      if (result != null) {
        // The path is not stored in the file; bind it to where it was loaded from.
        result.setFilepath(filepath);
      }
      return result;
    } catch (IOException | JsonParseException e) {
      ErrorPopupWindow.throwMessage("Could not open file: " + e.getMessage());
    }
    return null;
  }

  private static CodingFile createNewAndSave(String className, String filepath) {
    CodingFile codingFile = new CodingFile(className, filepath);
    FileManager.save(codingFile);
    return codingFile;
  }
}
