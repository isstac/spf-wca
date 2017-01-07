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

package wcanalysis.heuristic.util;

import java.util.HashSet;
import java.util.Iterator;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import wcanalysis.util.SymbolicVariableCollector;

/**
 * @author Kasper Luckow
 * Omega crashes (core dump, syntax errors,...) when the constraints string
 * becomes "too" large (in terms of characters). Strange.
 * Can we split up the constraints?
 */
public class OmegaConverter implements ConstraintsConverter {

  @Override
  public String convert(PathCondition pc) {
    if(pc == null)
      return "";
    PathCondition convertPc = pc.make_copy(); //probably not necessary...

    HashSet<String> vars = new HashSet<>();
    //TODO: does the collector *only* collect integer vars? That's weird
    SymbolicVariableCollector collector = new SymbolicVariableCollector(vars);
    collector.collectVariables(convertPc);

    StringBuilder omegaVarDecl = new StringBuilder();
    Iterator<String> iter = vars.iterator();
    while(iter.hasNext()) {
      omegaVarDecl.append(clean(iter.next()));
      if(iter.hasNext())
        omegaVarDecl.append(',');
    }

    StringBuilder omegaStr = new StringBuilder();
    omegaStr.append("{[").append(omegaVarDecl.toString()).append("] : ").append(clean(convertPc)).append("};");

    return omegaStr.toString();
  }

  private static String clean(PathCondition pc) {
    return (pc.header == null) ? "TRUE" : clean(pc.header.toString());
  }

  private static String clean(String constraintsString) {
    String clean = constraintsString.replaceAll("\\s+", "");
    clean = clean.replaceAll("CONST_(\\d+)", "$1");
    clean = clean.replaceAll("CONST_-(\\d+)", "-$1");
    clean = clean.replaceAll("[a-zA-Z]", "s");
    return clean;
  }
}
