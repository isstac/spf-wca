package wcanalysis.heuristic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Kasper Luckow
 *
 */
public class PolicyManager {

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
    File policyFile = new File(this.baseDir, policyFileName + POLICY_EXTENSION);
    try(FileOutputStream fo = new FileOutputStream(policyFile)) {
      policy.save(fo);
    }
  }
  
  public <T extends Policy> T loadPolicy(Collection<String> measuredMethods, Class<T> type) throws FileNotFoundException, IOException, PolicyManagerException {
    ArrayList<T> policies = new ArrayList<>();
    for(File f : this.baseDir.listFiles()) {
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
    } else {
      return policies.get(0);
    }
  }
}
