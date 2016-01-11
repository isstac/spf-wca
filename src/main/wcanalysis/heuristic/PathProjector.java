package wcanalysis.heuristic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import isstac.structure.cfg.Block;
import isstac.structure.cfg.CFG;
import isstac.structure.cfg.CFGBuildException;
import isstac.structure.cfg.CFGGenerator;

/**
 * @author Kasper Luckow
 *
 */
public abstract class PathProjector {

  private final CFGGenerator cfgGenerator;
  private static final Logger logger = Logger.getLogger(PathProjector.class.getName());
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
        try {
          cfg = this.cfgGenerator.getCFG(currInstruction.getClassName(), currInstruction.getMethodName());
          transformedCFGs.add(cfg);
        } catch (CFGBuildException e) {
          logger.severe(e.getMessage());
          logger.severe("Is the classpath set up correctly for the CFG generator?");
        }

      }
      if(cfg != null) {
        int instrIdx = currInstruction.getInstructionIndex();      
        Block condBlock = cfg.getBlockWithIndex(instrIdx);

        this.projectDecision(condBlock, dec);
      }
      prevDecision = dec;
    }
    return transformedCFGs;
  }
  
  protected abstract void projectDecision(Block basicBlock, Decision dec);
}
