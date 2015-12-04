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
  private final Decision prev;
  private final transient State state;
  private final BranchInstruction instr;
  private int choice = -1;
  
  //We don't want to serialize this guy
  public transient final StackFrame frame;

  public Decision(BranchInstruction instr, Decision prev, State state, StackFrame frame) {
    this.prev = prev;
    this.state = state;
    this.instr = instr;
    this.frame = frame;
  }

  public BranchInstruction getInstruction() {
    return this.instr;
  }

  public void setChoice(int choice) {
    this.choice = choice;
  }

  public int getChoice() {
    return this.choice;
  }  

  public Decision getPrev() {
    return this.prev;
  }

  public State getState() {
    return this.state;
  }

  public Decision copy() {
    //copy to reference is copied here. This is dangerous
    Decision cp = new Decision(instr, prev, this.state.copy(), frame);
    cp.setChoice(choice);
    return cp;
  }
  
  public DecisionHistory generateDecisionHistory(int historySize) {
    DecisionHistory history = new DecisionHistory(historySize);
    Decision prevDec = this.getPrev();
    for(int n = 0; prevDec != null && n < historySize; n++) {
      history.addFirst(prevDec.copy()); //TODO: check if we need to copy here
      prevDec = prevDec.getPrev();
    }
    return history;
  }
  
  public DecisionHistory generateCtxPreservingDecisionHistory(int historySize) {
    DecisionHistory history = new DecisionHistory(historySize);
    
    Decision prevDec = this.getPrev();
    for(int n = 0; prevDec != null && prevDec.frame.equals(this.frame) && n < historySize; n++) {
      history.addFirst(prevDec.copy()); //TODO: check if we need to copy here
      prevDec = prevDec.getPrev();
    }
    return history;
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
    return "(s:" + instr.getLineNumber() + "," + ((choice == 1) ? 'T' : 'F') + ")";
  }
}