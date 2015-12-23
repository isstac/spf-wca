package wcanalysis.heuristic;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import gov.nasa.jpf.vm.StackFrame;

/**
 * @author Kasper Luckow
 */
public class Decision implements Serializable {
  private static final long serialVersionUID = -7153050127298410855L;
  private final BranchInstruction instr;
  private final int choice;
  
  //We don't want to serialize this guy
  private transient final StackFrame context;


  public Decision(BranchInstruction instr, int choice, StackFrame context) {
    this.choice = choice;
    this.instr = instr;
    this.context = context;
  }

  public BranchInstruction getInstruction() {
    return this.instr;
  }

  public int getChoice() {
    return this.choice;
  }
  
  public StackFrame getContext() {
    return this.context;
  }

  public Decision copy() {
    //copy to reference is copied here. This is dangerous
    Decision cp = new Decision(instr.copy(), this.choice, context);
    return cp;
  }
  
  public DecisionHistory generateDecisionHistory(int historySize) {
    DecisionHistory history = new DecisionHistory(historySize);
    Decision prevDec = this.getPrev().copy();
    for(int n = 0; prevDec != null && n < historySize; n++) {
      history.addFirst(prevDec);
      prevDec = prevDec.getPrev();
    }
    return history;
  }
  
  public DecisionHistory generateCtxPreservingDecisionHistory(int historySize) {
    DecisionHistory history = new DecisionHistory(historySize);
    
    Decision prevDec = (this.getPrev() != null) ? this.getPrev().copy() : null;
    for(int n = 0; prevDec != null && prevDec.frame.equals(this.frame) && n < historySize; n++) {
      history.addFirst(prevDec);
      prevDec = prevDec.getPrev();
    }
    return history;
  }
  
  public Path generatePath() {
    Path path = new Path();
    Decision cur = this.copy();
    while(cur != null) {
      path.prependDecision(cur);
      cur = cur.getPrev();
    }
    return path;
  }
  
  @Override
  public int hashCode(){
    //this could be dangerous since we are not taking 
    //into account the prev reference nor the state object
    //We are really only looking at the decision in isolation
    return new HashCodeBuilder()
        .append(instr)
        .append(choice)
        .toHashCode();
  }

  @Override
  public boolean equals(Object obj){
    //this could be dangerous since we are not taking 
    //into account the prev reference nor the state object
    //We are really only looking at the decision in isolation
    if(obj instanceof Decision){
      final Decision other = (Decision)obj;
      return new EqualsBuilder()
          .append(instr, other.instr)
          .append(choice, other.choice)
          .isEquals();
    } else{
      return false;
    }
  }

  @Override
  public String toString() {
    return "(s:" + instr.getLineNumber() + "," + ((choice == 1) ? 'T' : (choice == 0) ? 'F' : choice) + ")";
  }
}