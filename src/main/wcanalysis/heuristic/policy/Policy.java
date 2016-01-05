package wcanalysis.heuristic.policy;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Set;

import gov.nasa.jpf.vm.ChoiceGenerator;
import wcanalysis.heuristic.ContextManager;
import wcanalysis.heuristic.Resolution;

/**
 * @author Kasper Luckow
 *
 */
public abstract class Policy implements Serializable {
  
  private static final long serialVersionUID = -2247935610676857237L;
  
  
  private final Set<String> measuredMethods;
    
  public Policy(Set<String> measuredMethods) {
    this.measuredMethods = measuredMethods;
  }
  
  public Set<String> getMeasuredMethods() {
    return this.measuredMethods;
  }
  
  public abstract Resolution resolve(ChoiceGenerator<?> cg, ContextManager ctxManager);
  
  public void save(OutputStream out) {
    try {
      ObjectOutputStream o = new ObjectOutputStream(out);
      o.writeObject(this);
      o.close();
      out.close();
    } catch(IOException i) {
      throw new RuntimeException("Could not serialize policy", i);
    }
  }

  public static <T extends Policy> T load(InputStream in, Class<T> polCls) {
    T graph;
    try {
      ObjectInputStream i = new ObjectInputStream(in);
      graph = (T)i.readObject();
      i.close();
    } catch (ClassNotFoundException | IOException e1) {
      throw new RuntimeException("Could not deserialize policy", e1);
    }
    return graph;
  }
}
