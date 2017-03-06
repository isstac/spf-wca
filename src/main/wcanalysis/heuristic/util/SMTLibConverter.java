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

import gov.nasa.jpf.symbc.numeric.PathCondition;
import wcanalysis.util.SymbolicVariableCollector;

/**
 * @author Kasper Luckow
 */
public class SMTLibConverter implements ConstraintsConverter {

  @Override
  public String convert(PathCondition pc) {
    if(pc == null || pc.header == null)
      return "";
    StringBuilder smtLib = new StringBuilder();
    HashSet<String> setOfSymVar = new HashSet<String>();
    SymbolicVariableCollector collector = new SymbolicVariableCollector(setOfSymVar);
    collector.collectVariables(pc);
    //Add declarations
    for (String var : collector.getListOfVariables())
      smtLib.append("(declare-const " + var + " Int)\n");
    //make assertion
    smtLib.append('\n');
    smtLib.append("(assert ").append(pc.header.prefix_notation()).append(")\n\n");
    smtLib.append("(check-sat)\n");
    smtLib.append("(get-model)");
    return smtLib.toString();
  }
}
