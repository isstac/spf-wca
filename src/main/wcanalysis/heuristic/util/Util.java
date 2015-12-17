package wcanalysis.heuristic.util;

import java.io.File;

public class Util {

  public static File createDirIfNotExist(File root, String subDir) {
    File sub = new File(root, subDir);
    if(!sub.exists())
      sub.mkdirs();
    return sub;
  }

  public static File createDirIfNotExist(String root) {
    File sub = new File(root);
    if(!sub.exists())
      sub.mkdirs();
    return sub;
  }
  
  public static File createDirIfNotExist(File dir) {
    if(!dir.exists())
      dir.mkdirs();
    return dir;
  }
}
