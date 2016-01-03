package wcanalysis.heuristic;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public abstract class Policy implements Serializable {
  static enum ResolutionType {
    PERFECT,
    HISTORY, 
    INVARIANT,
    UNRESOLVED,
    NEW_CHOICE;
  }
  
  static class Resolution {
    public final ResolutionType type;
    public final int choice;
    public Resolution(int choice, ResolutionType type) {
      this.choice = choice;
      this.type = type;
    }
  }
  
  private static final long serialVersionUID = -2247935610676857237L;
  
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

  public static <T extends Policy> T fromObjStream(InputStream in, Class<T> polCls) {
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
