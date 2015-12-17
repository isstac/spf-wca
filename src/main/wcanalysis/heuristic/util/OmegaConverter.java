package wcanalysis.heuristic.util;

import java.util.HashSet;
import java.util.Iterator;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import sidechannel.util.SymbolicVariableCollector;

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
