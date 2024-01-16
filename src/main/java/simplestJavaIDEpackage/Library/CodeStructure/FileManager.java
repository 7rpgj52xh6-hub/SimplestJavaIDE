package simplestJavaIDEpackage.Library.CodeStructure;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class FileManager {
  public static Boolean save(CodingFile codingFile) {
    FileOutputStream fos;
    ObjectOutputStream oos;
    try {
      fos = new FileOutputStream(codingFile.getFilepath());
      oos = new ObjectOutputStream(fos);
      oos.writeObject(codingFile);
      oos.close();
      fos.close();
      return true;
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
    return false;
  }

  public static CodingFile load(String className, String filepath, Boolean isNewFile) {
    if (isNewFile) {
      return load(className, createNewAndSave(className, filepath).getFilepath(), false);
    } else {
      FileInputStream fis;
      ObjectInputStream ois;
      try {
        fis = new FileInputStream(filepath);
        ois = new ObjectInputStream(fis);
        try {
          CodingFile result = (CodingFile) ois.readObject();
          fis.close();
          ois.close();
          return result;
        } catch (ClassNotFoundException e) {
          ErrorPopupWindow.throwMessage(e.getMessage());
        }
        fis.close();
        ois.close();
      } catch (IOException e) {
        ErrorPopupWindow.throwMessage(e.getMessage());
      }
    }
    return null;
  }

  private static CodingFile createNewAndSave(String className, String filepath) {
    CodingFile codingFile = new CodingFile(className, filepath);
    FileManager.save(codingFile);
    return codingFile;
  }
}
