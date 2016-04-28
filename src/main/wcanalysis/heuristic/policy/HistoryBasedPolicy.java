package wcanalysis.heuristic.policy;

import java.util.Set;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import wcanalysis.heuristic.BranchInstruction;
import wcanalysis.heuristic.ContextManager;
import wcanalysis.heuristic.Decision;
import wcanalysis.heuristic.InvariantChecker;
import wcanalysis.heuristic.Path;
import wcanalysis.heuristic.Resolution;
import wcanalysis.heuristic.WorstCasePath;
import wcanalysis.heuristic.Resolution.ResolutionType;

/**
 * @author Kasper Luckow
 *
 */
public class HistoryBasedPolicy extends Policy implements ChoiceListener { 
  private static final long serialVersionUID = 3311547338575590448L;
  
  public static class Builder {
    private boolean adaptive = false;
    private int maxHistorySize = 0;
    private InvariantChecker invariantChecker = null;
    
    public Builder() {}
    
    public Builder setAdaptive(boolean adaptive) {
      this.adaptive = adaptive;
      return this;
    }

    public Builder setMaxHistorySize(int maxHistorySize) {
      this.maxHistorySize = maxHistorySize;
      return this;
    }
    
    public Builder addInvariantChecker(InvariantChecker invChecker) {
      this.invariantChecker = invChecker;
      return this;
    }
    
    public HistoryBasedPolicy build(WorstCasePath wcPath, Set<String> measuredMethods) {
      if(adaptive)
        return new HistoryBasedPolicy(wcPath, measuredMethods, invariantChecker, true);
      else {
        return new HistoryBasedPolicy(wcPath, measuredMethods, invariantChecker, maxHistorySize);
      }
    }
  }
  
  private HistoryBasedBranchPolicy historyPolicy;
  private HistoryLessBranchPolicy historyLessPolicy;
  
  public static final int ADAPTIVE = -1;
  private boolean adaptive = false;
  
  private int maxHistorySize = 0;
  
  private InvariantChecker invariantChecker = null;
  
  private HistoryBasedPolicy(WorstCasePath wcPath, Set<String> measuredMethods, InvariantChecker invariantChecker, int maxHistorySize) {
    super(measuredMethods);
    this.maxHistorySize = maxHistorySize;
    this.invariantChecker = invariantChecker; 
    computePolicy(wcPath);
  }
  
  private HistoryBasedPolicy(WorstCasePath wcPath, Set<String> measuredMethods, InvariantChecker invariantChecker, boolean adaptive) {
    super(measuredMethods);
    this.adaptive = adaptive;
    this.invariantChecker = invariantChecker; 
    computePolicy(wcPath);
  }
  
  private void computePolicy(WorstCasePath wcPath) {
    HistoryBasedBranchPolicy.Builder historyPolicyBuilder = new HistoryBasedBranchPolicy.Builder();
    HistoryLessBranchPolicy.Builder historyLessPolicyBuilder = new HistoryLessBranchPolicy.Builder();
    
    for(int i = wcPath.size() - 1; i >= 0; i--) {
      Decision currDecision = wcPath.get(i);
      int currentChoice = currDecision.getChoice();
      Decision prevDecision;
      Path history = new Path();
      //We get the context preserving history from the current decision
      int currHistorySize = 0;
      for(int j = i - 1; j >= 0; j--, currHistorySize++) {
        prevDecision = wcPath.get(j);
        if(prevDecision.getContext() != currDecision.getContext())
          break;
        if(!adaptive && currHistorySize >= maxHistorySize)
          break;
        history.addFirst(prevDecision);
      }
      BranchInstruction branchInstr = currDecision.getInstruction();
      
      historyPolicyBuilder.addPolicy(branchInstr, history, currentChoice);
      historyLessPolicyBuilder.addPolicy(branchInstr, currentChoice);
    }
    this.historyPolicy = historyPolicyBuilder.build();
    this.historyLessPolicy = historyLessPolicyBuilder.build();
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
    Set<Integer> decisions = this.historyLessPolicy.resolve(branchInstr);
    //if the historyless policy has no choices stored, neither will the stateful one, so we cannot resolve it
    if(decisions == null || decisions.size() == 0)
      return new Resolution(-1, ResolutionType.NEW_CHOICE);
    //if there is ONLY one decision (i.e. one choice), then we can resolve the CG "perfectly"
    else if(decisions.size() == 1) {
      return new Resolution(decisions.iterator().next(), ResolutionType.PERFECT);
    }
    
    //TODO: history generation should be made prettier -- it is not obvious what is going on here
    Path history = Path.generateCtxPreservingHistory(cg, ctxManager, maxHistorySize);
    
    //If we get here, there must be a history stored for the branch
    Set<Integer> choices = this.historyPolicy.resolve(branchInstr, history);
    if(choices != null && choices.size() == 1) {
      return new Resolution(choices.iterator().next(), ResolutionType.HISTORY);
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
  
  @Override
  public String toString() {
    return this.historyPolicy.toString();
  }
}
