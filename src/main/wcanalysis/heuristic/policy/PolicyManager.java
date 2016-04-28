package wcanalysis.heuristic.policy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import gov.nasa.jpf.JPF;

/**
 * @author Kasper Luckow
 *
 */
public class PolicyManager {

  private static final Logger logger = JPF.getLogger("policymanager");
  
  private final File baseDir;
  
  private static final String POLICY_EXTENSION = ".pol";
  
  public PolicyManager(File baseDir) {
    this.baseDir = baseDir;
  }
  
  public void savePolicy(Policy policy) throws FileNotFoundException, IOException {
    String policyFileName = "";
    for(String meas : policy.getMeasuredMethods()) {
      policyFileName += meas;
    }
    
    // Prune to prevent FileNotFoundException (File name too long) (max 255 chars)
    if (policyFileName.length()>251)
    	policyFileName = policyFileName.substring(0, 250);
    
    File policyFile = new File(this.baseDir, policyFileName + POLICY_EXTENSION);
    try(FileOutputStream fo = new FileOutputStream(policyFile)) {
      policy.save(fo);
    }
    logger.info("Saved policy: " + policy.toString());
  }
  
  public <T extends Policy> T loadPolicy(Collection<String> measuredMethods, Class<T> type) throws FileNotFoundException, IOException, PolicyManagerException {
    ArrayList<T> policies = new ArrayList<>();
    File[] baseDirFiles = this.baseDir.listFiles();
    if(baseDirFiles == null)
      throw new PolicyManagerException("The provided base dir for loading policies is invalid. Received: " + this.baseDir.getPath());
    for(File f : baseDirFiles) {
      if(f.getName().endsWith(POLICY_EXTENSION)) {
        try(InputStream in = new FileInputStream(f)) {
          T pol = Policy.load(in, type);
          if(pol.getMeasuredMethods().equals(measuredMethods)) {
            policies.add(pol);
          }
        }
      }
    }
    if(policies.size() > 1) {
      String measMethodsStr = "";
      for(String meas : measuredMethods) {
        measMethodsStr += meas;
      }
      throw new PolicyManagerException("Multiple policies found for measured methods: " + measMethodsStr);
    } else if(policies.size() == 0) {
        String measMethodsStr = "";
        for(String meas : measuredMethods) {
          measMethodsStr += meas;
        }
        throw new PolicyManagerException("No policies found for measured methods: " + measMethodsStr);
      } {
      return policies.get(0);
    }
  }
}
