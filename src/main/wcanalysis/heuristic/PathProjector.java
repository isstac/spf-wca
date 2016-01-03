package wcanalysis.heuristic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import isstac.structure.cfg.Block;
import isstac.structure.cfg.CFG;
import isstac.structure.cfg.CFGGenerator;

/**
 * @author Kasper Luckow
 *
 */
public abstract class PathProjector {

  private final CFGGenerator cfgGenerator;
  public PathProjector(CFGGenerator cfgGenerator) {
    this.cfgGenerator = cfgGenerator;
  }
  
  public Collection<CFG> projectPath(Path path) {
    if(path.size() < 1)
      return null;
    CFG cfg = null;
    Decision prevDecision = null;
    
    Set<CFG> transformedCFGs = new HashSet<>();
    
    for(Decision dec : path) {
      
      BranchInstruction currInstruction = dec.getInstruction();
      //We optimize cfg extraction a bit here...
      if(prevDecision == null ||
          (prevDecision != null && !currInstruction.getMethodName().equals(prevDecision.getInstruction().getMethodName()))) {
        cfg = this.cfgGenerator.getCFG(currInstruction.getClassName(), currInstruction.getMethodName());
        transformedCFGs.add(cfg);
      }
      assert cfg != null;
      
      int instrIdx = currInstruction.getInstructionIndex();      
      Block condBlock = cfg.getBlockWithIndex(instrIdx);

      this.projectDecision(condBlock, dec);

      prevDecision = dec;
    }
    return transformedCFGs;
  }
  
  protected abstract void projectDecision(Block basicBlock, Decision dec);
}
