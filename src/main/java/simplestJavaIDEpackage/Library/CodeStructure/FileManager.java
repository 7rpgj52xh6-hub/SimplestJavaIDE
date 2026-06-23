package simplestJavaIDEpackage.Library.CodeStructure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import simplestJavaIDEpackage.ErrorPopupWindow;

/**
 * Reads and writes {@link CodingFile}s as human-readable JSON. Files from the
 * earlier single-class schema (imports + methods + javaClass) are migrated to the
 * current multi-class schema on load.
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
      JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
      CodingFile result = json.has("classes") ? GSON.fromJson(json, CodingFile.class) : migrate(json, className, filepath);
      if (result == null || result.classes == null || result.classes.isEmpty()) {
        ErrorPopupWindow.throwMessage("Diese Datei ist beschädigt oder leer.");
        return null;
      }
      // The path is not stored in the file; bind it to where it was loaded from.
      result.setFilepath(filepath);
      return result;
    } catch (IOException | JsonParseException | IllegalStateException e) {
      ErrorPopupWindow.throwMessage("Could not open file: " + e.getMessage());
    }
    return null;
  }

  /** Migrates an old single-class file (imports + methods + javaClass) to the new schema. */
  private static CodingFile migrate(JsonObject json, String className, String filepath) {
    String name = className;
    if (json.has("javaClass") && json.getAsJsonObject("javaClass").has("className")) {
      name = json.getAsJsonObject("javaClass").get("className").getAsString();
    }
    CodingFile result = new CodingFile(name, filepath);
    result.imports.clear();
    if (json.has("imports")) {
      for (JsonElement element : json.getAsJsonArray("imports")) {
        result.imports.add(element.getAsString());
      }
    }
    JavaClass main = result.classes.get(0);
    if (json.has("methods")) {
      main.methods.clear();
      JsonArray methods = json.getAsJsonArray("methods");
      for (JsonElement element : methods) {
        JsonObject method = element.getAsJsonObject();
        main.methods.add(
            new Methods(method.get("name").getAsString(), method.get("content").getAsString()));
      }
    }
    return result;
  }

  private static CodingFile createNewAndSave(String className, String filepath) {
    CodingFile codingFile = new CodingFile(className, filepath);
    FileManager.save(codingFile);
    return codingFile;
  }
}
