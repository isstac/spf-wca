package wcanalysis.heuristic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import isstac.structure.cfg.Block;
import isstac.structure.cfg.CFG;
import wcanalysis.heuristic.DecisionCollection.FalseDecisionCollection;
import wcanalysis.heuristic.DecisionCollection.TrueDecisionCollection;

/**
 * @author Kasper Luckow
 *
 */
public class HistoryBasedPolicy extends Policy { 
  private static final long serialVersionUID = 3311547338575590448L;

  private Map<BranchInstruction, Map<Path, Set<Integer>>> pol;
  private Map<BranchInstruction, Set<Integer>> historylessPol;
  
  public static final int NO_LIMIT = -1;
  public HistoryBasedPolicy(WorstCasePath wcPath, int maxHistorySize) {
    computePolicy(wcPath, maxHistorySize);
  }
  
  public HistoryBasedPolicy(WorstCasePath wcPath) {
    computePolicy(wcPath, NO_LIMIT);
  }
  
  private void computePolicy(WorstCasePath wcPath, int maxHistorySize) {
    this.pol = new HashMap<>();
    for(int i = wcPath.size(); i > 0; i--) {
      Decision currDecision = wcPath.get(i);
      Decision prevDecision;
      Path history = new Path();
      //We get the context preserving history from the current decision
      int currHistorySize = 0;
      for(int j = i - 1; j <= 0; j--, currHistorySize++) {
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
    
    BranchInstruction branchInstr = new BranchInstruction(cg.getInsn());
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
    decisions = this.pol.get(branchInstr).get(history);
    if(decisions.size() == 1) {
      return new Resolution(decisions.iterator().next(), ResolutionType.HISTORY);
    } else {
      return new Resolution(-1, ResolutionType.UNRESOLVED);
    }
    
    //do the invariant checking here
    
    
    
    Instruction ifInstr = ((PCChoiceGenerator) cg).getInsn();
    CFG cfg = getCfgExtractor().getCFG(ifInstr);      
    
    Block bl = cfg.getBlockWithIndex(ifInstr.getInstructionIndex());      
    
    boolean takeFalseBranch = bl.hasAttribute(FalseDecisionCollection.class);
    boolean takeTrueBranch = bl.hasAttribute(TrueDecisionCollection.class);
    int currChoice = ((PCChoiceGenerator) cg).getNextChoice();
    
    //Check if decision can be resolved "perfectly" i.e. only counts on one decision:
    if(ignoreState(takeTrueBranch, takeFalseBranch, currChoice)) {
      vm.getSystemState().setIgnored(true);
      this.resolvedPerfectChoices++;
      return;
    } else if(matchState(takeTrueBranch, takeFalseBranch, currChoice)) {
      State state = getCurrentState();
      HeuristicDecisionCounts counts = state.getBlockCounts(bl);
      counts.incrementForChoice(currChoice);
      return;
    }
    
    //counts on both branches
    if(takeTrueBranch && takeFalseBranch) { 
      //We have to call getPrev here on the previous decision, because in reality we haven't
      //made the prevDec decision yet -- specifically we may backtrack according to the decision
      //sets based on history. Agreed, this is pretty ugly, but seems we have to set prevDec in
      //choicegeneratoradvanced for everything to work correctly with JPFs exploration
      DecisionHistory currDecisionHistory = null;
      if(currDec != null)
        currDecisionHistory = currDec.generateCtxPreservingDecisionHistory(decisionHistorySize);
      else //bogus
        currDecisionHistory = new DecisionHistory(0); 
      
      FalseDecisionCollection falseDecColl = bl.getAttribute(FalseDecisionCollection.class);
      boolean matchFalseHistories = falseDecColl.contains(currDecisionHistory);
      
      TrueDecisionCollection trueDecColl = bl.getAttribute(TrueDecisionCollection.class);
      boolean matchTrueHistories = trueDecColl.contains(currDecisionHistory);
      
      //Check if decision can be resolved if the current decision history only matches recorded
      //decisions for one branch
      if(ignoreState(matchTrueHistories, matchFalseHistories, currChoice)) {
        vm.getSystemState().setIgnored(true);
        this.resolvedHistoryChoices++;
        return;
      } else if(matchState(matchTrueHistories, matchFalseHistories, currChoice)) {
        State state = getCurrentState();
        HeuristicDecisionCounts counts = state.getBlockCounts(bl);
        counts.incrementForChoice(currChoice);
        return;
      }
      
      //Check if decision can be resolved by checking
      //the count invariant for that decision
      State state = getCurrentState();
      HeuristicDecisionCounts counts = state.getBlockCounts(bl);
      Map<Long, CountsInvariant> blId2countsInv = countsInvariants.get(cfg.getFqMethodName());
      if(blId2countsInv != null) {
        CountsInvariant countsInvariant = blId2countsInv.get(bl.getId());
        if(countsInvariant != null) {
          int tmpFalseCount = counts.falseCount;
          int tmpTrueCount = counts.trueCount;
          if(((PCChoiceGenerator) cg).getNextChoice() == 1) //the decision will be true branch
            tmpTrueCount++;
          else
            tmpFalseCount++;
          
          if(!countsInvariant.apply(tmpTrueCount, tmpFalseCount)) {
            //Invariant does not hold, so we prune
            this.resolvedInvariantChoices++;
            vm.getSystemState().setIgnored(true);
            return;
          }
        }
      }
      
      counts.incrementForChoice(currChoice);
      this.unresolvedChoices++;
    } else if((!bl.hasAttribute(FalseDecisionCollection.class) && !bl.hasAttribute(TrueDecisionCollection.class)) ||
        (!takeTrueBranch && !takeFalseBranch)) { //branches we did not explore when obtaining counts
      this.newChoices++;
    }
    
    
    return 0;
  }
}
