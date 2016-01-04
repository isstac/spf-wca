package wcanalysis.heuristic.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
  
  public static Set<String> extractSimpleMethodNames(String[] jpfMethodSpecs) {
    //FIXME: This also means that we do not distinguish between overloaded methods
    String[] processedMethods = new String[jpfMethodSpecs.length];
    System.arraycopy(jpfMethodSpecs, 0, processedMethods, 0, jpfMethodSpecs.length);
    for(int i = 0; i < jpfMethodSpecs.length; i++) {
      String meth = jpfMethodSpecs[i];
      int sigBegin = meth.indexOf('(');
      if(sigBegin >= 0)
        processedMethods[i] = meth.substring(0, sigBegin);
    }
    return new HashSet<String>(Arrays.asList(processedMethods));
  }
}
