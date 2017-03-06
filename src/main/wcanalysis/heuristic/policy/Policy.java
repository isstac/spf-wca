/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

  public abstract void unify(Policy otherPolicy) throws PolicyUnificationException;
  
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
