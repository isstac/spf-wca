package wcanalysis.heuristic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import isstac.structure.cfg.Block;
import isstac.structure.cfg.CFG;

/**
 * @author Kasper Luckow
 *
 */
public class HistoryBasedPolicy extends Policy implements ChoiceListener { 
  private static final long serialVersionUID = 3311547338575590448L;

  private Map<BranchInstruction, Map<Path, Set<Integer>>> pol;
  private Map<BranchInstruction, Set<Integer>> historylessPol;
  
  public static final int NO_LIMIT = -1;
  private final int maxHistorySize;
  
  private InvariantChecker invariantChecker = null;
  
  public HistoryBasedPolicy(WorstCasePath wcPath, Set<String> measuredMethods, int maxHistorySize) {
    super(wcPath, measuredMethods);
    this.maxHistorySize = maxHistorySize;
  }
  
  public HistoryBasedPolicy(WorstCasePath wcPath, Set<String> measuredMethods, int maxHistorySize, InvariantChecker invariantChecker) {
    super(wcPath, measuredMethods);
    this.maxHistorySize = maxHistorySize;
    this.invariantChecker = invariantChecker; 
  }
  
  @Override
  protected void computePolicy(WorstCasePath wcPath) {
    this.pol = new HashMap<>();
    this.historylessPol = new HashMap<>();
    for(int i = wcPath.size() - 1; i >= 0; i--) {
      Decision currDecision = wcPath.get(i);
      Decision prevDecision;
      Path history = new Path();
      //We get the context preserving history from the current decision
      int currHistorySize = 0;
      for(int j = i - 1; j >= 0; j--, currHistorySize++) {
        prevDecision = wcPath.get(j);
        if(prevDecision.getContext() != currDecision.getContext())
          break;
        if(maxHistorySize > 0 && currHistorySize > maxHistorySize)
          break;
        history.addFirst(prevDecision);
      }
      BranchInstruction branchInstr = currDecision.getInstruction();
      Map<Path, Set<Integer>> branchPol = pol.get(branchInstr);
      if(branchPol == null) {
        branchPol = new HashMap<>();
        pol.put(currDecision.getInstruction(), branchPol);
      }
      
      Set<Integer> decisions = branchPol.get(history);
      if(decisions == null) {
        decisions = new HashSet<>();
        branchPol.put(history, decisions);
      }
      
      int policyChoice = currDecision.getChoice();
      decisions.add(policyChoice);
      
      //also add it to historyless policy
      Set<Integer> historylessDecisions = historylessPol.get(branchInstr);
      if(historylessDecisions == null) {
        historylessDecisions = new HashSet<>();
        historylessPol.put(branchInstr, historylessDecisions);
      }
      historylessDecisions.add(policyChoice);
    }
  }

  @Override
  public Resolution resolve(ChoiceGenerator<?> cg, ContextManager ctxManager) {
    assert cg instanceof PCChoiceGenerator;
    
    PCChoiceGenerator pcCg = (PCChoiceGenerator)cg;
    
    BranchInstruction branchInstr = new BranchInstruction(cg.getInsn());
    
    if(this.invariantChecker != null) {
      Set<Integer> allowedChoices = this.invariantChecker.getChoices(branchInstr, pcCg);
      if(allowedChoices.size() == 1) { //we can uniquely determine one choice
        return new Resolution(allowedChoices.iterator().next(), ResolutionType.INVARIANT);
      }
    }
    
    //Check whether it can be quickly resolved with historyless policy
    Set<Integer> decisions = this.historylessPol.get(branchInstr);
    //if the historyless policy has no choices stored, neither will the stateful one, so we cannot resolve it
    if(decisions.size() == 0)
      return new Resolution(-1, ResolutionType.NEW_CHOICE);
    //if there is ONLY one decision (i.e. one choice), then we can resolve the CG "perfectly"
    else if(decisions.size() == 1) {
      return new Resolution(decisions.iterator().next(), ResolutionType.PERFECT);
    }
    
    //TODO: history generation should be made prettier -- it is not obvious what is going on here
    Path history = new Path(cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class), ctxManager, true);
    
    //If we get here, there must be a history stored for the branch
    Map<Path, Set<Integer>> choices = this.pol.get(branchInstr);
    Set<Integer> decisionsWithHistory = choices.get(history);
    if(decisionsWithHistory != null && decisionsWithHistory.size() == 1) {
      return new Resolution(decisionsWithHistory.iterator().next(), ResolutionType.HISTORY);
    } else {
      return new Resolution(-1, ResolutionType.UNRESOLVED);
    }
  }

  @Override
  public void choiceMade(PCChoiceGenerator cg, int choiceMade) {
    if(this.invariantChecker != null) {
      this.invariantChecker.choiceMade(new BranchInstruction(cg.getInsn()), choiceMade);
    }
  }
}
