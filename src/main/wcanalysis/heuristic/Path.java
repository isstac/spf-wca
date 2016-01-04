package wcanalysis.heuristic;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.StackFrame;
import wcanalysis.heuristic.ContextManager.CGContext;

class Path extends LinkedList<Decision> {
  
  private static final long serialVersionUID = -1691414612424473640L;
  
  public Path() { }

  public Path(ChoiceGenerator<?> endCG, ContextManager ctxManager, boolean ctxPreserving) {
    if(endCG == null)
      return;
    PCChoiceGenerator[] pcs = endCG.getAllOfType(PCChoiceGenerator.class);
    if(pcs.length == 0)
      return;
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
  
  public Path generateCtxPreservingHistoryFromIdx(int idx, int maxSize) {
    Path subPath = new Path();
    if(idx <= 0)
      return subPath;
    StackFrame ctx = this.get(idx).getContext();
    for(int i = idx-1, size = 0; i >= 0; i--, size++) {
      if(size >= maxSize)
        return subPath;
      Decision curr = this.get(i);
      if(curr.getContext() == ctx) {
        subPath.addFirst(curr);
      } else
        return subPath;
    }
    return subPath;
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
            ((cur.getChoice() == 1) ? "T" : (cur.getChoice() == 0) ? "F" : cur.getChoice()) + "]" /*+ cur.getContext()*/ + "]");
      if(iter.hasNext())
        pathBuilder.append(", ");
    }
    return pathBuilder.toString();
  }
}