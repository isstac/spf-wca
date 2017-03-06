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
