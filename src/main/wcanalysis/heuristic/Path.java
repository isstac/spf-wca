package wcanalysis.heuristic;

import java.util.Iterator;
import java.util.LinkedList;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.StackFrame;
import wcanalysis.heuristic.ContextManager.CGContext;

class Path extends LinkedList<Decision> {
  
  private static final long serialVersionUID = -1691414612424473640L;
  
  public Path() { }

  public Path(ChoiceGenerator<?> endCG, ContextManager ctxManager, boolean ctxPreserving) {
    PCChoiceGenerator[] pcs = endCG.getAllOfType(PCChoiceGenerator.class);
    StackFrame currCtx = ctxManager.getContext(pcs[pcs.length - 1]).stackFrame;
    for(int i = 0; i < pcs.length; i++) {
      PCChoiceGenerator currPc = pcs[i];
      CGContext cgCtx = ctxManager.getContext(currPc);
      if(ctxPreserving && currCtx != cgCtx.stackFrame)
        break;
      Decision dec = new Decision(new BranchInstruction(currPc.getInsn()), currPc.getNextChoice(), cgCtx.stackFrame);
      this.add(dec);
    }
  }
  
  public Path(ChoiceGenerator<?> endCG, ContextManager ctxManager) {
    this(endCG, ctxManager, false);
  }
  
  @Override
  public String toString() {
    StringBuilder pathBuilder = new StringBuilder();
    Iterator<Decision> iter = this.iterator();
    Decision cur = null;
    while(iter.hasNext()) {
      cur = iter.next();
      pathBuilder.append("[[" +  
            "l:" + cur.getInstruction().getLineNumber() + "(o:" +
            cur.getInstruction().getInstructionIndex() + "), " + 
            ((cur.getChoice() == 1) ? 'T' : (cur.getChoice() == 0) ? 'F' : cur.getChoice()) + "]" + cur.getContext() + "]");
      if(iter.hasNext())
        pathBuilder.append(", ");
    }
    return pathBuilder.toString();
  }
}