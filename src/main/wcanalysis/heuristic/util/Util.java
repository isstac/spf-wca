package wcanalysis.heuristic.util;

import java.io.File;

import gov.nasa.jpf.vm.MethodInfo;

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
  
  public static String normalizeJPFMethodName(MethodInfo methInfo) {
    int methBeginIdx = methInfo.getBaseName().lastIndexOf('.') + 1;
    String fullName = methInfo.getFullName();
    return fullName.substring(methBeginIdx, fullName.length());
  }
}
