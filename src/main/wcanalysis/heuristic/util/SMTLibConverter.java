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
