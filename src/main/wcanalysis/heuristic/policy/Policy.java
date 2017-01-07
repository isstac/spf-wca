/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
