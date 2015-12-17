package wcanalysis.heuristic.util;

import java.util.HashSet;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import sidechannel.util.SymbolicVariableCollector;

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
