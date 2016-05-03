package wcanalysis.heuristic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.StackFrame;
import wcanalysis.heuristic.ContextManager.CGContext;

/**
 * @author Kasper Luckow
 * TODO: Clean up how (sub)paths can be generated (static methods, from CGs, ...)
 * both context preserving and not context preserving. 
 */
public class Path extends ArrayList<Decision> {
  
  private static final long serialVersionUID = -1691414612424473640L;
  
  public Path() { }

  public Path(Path other) {
    super(other);
  }
  
  //TODO: ugly
  public static Path generateCtxPreservingHistory(ChoiceGenerator<?> endCG, ContextManager ctxManager, int maxSize) {
    Path p = new Path();
    if(maxSize <= 0)
      return p;
    PCChoiceGenerator previousPCcg = endCG.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
    generatePath(p, previousPCcg, ctxManager, ctxManager.getContext(endCG), true, maxSize);
    return p;
  }
  
  private static void generatePath(Path path, ChoiceGenerator<?> endCG, ContextManager ctxManager, CGContext ctx, boolean ctxPreserving, int maxSize) {
    if(endCG == null)
      return;
    PCChoiceGenerator[] pcs = endCG.getAllOfType(PCChoiceGenerator.class);
    if(pcs.length == 0)
      return;
    for(int i = pcs.length - 1; i >= 0; i--) {
      if(maxSize > 0 && path.size() >= maxSize)
        break;
      PCChoiceGenerator currPc = pcs[i];
      CGContext currCtx = ctxManager.getContext(currPc);
      if(ctxPreserving && ctx.stackFrame != currCtx.stackFrame)
        break;
      Decision dec = new Decision(new BranchInstruction(currPc.getInsn()), currPc.getNextChoice(), currCtx.stackFrame);
      path.addFirst(dec);
    }
  }
  
  public void addFirst(Decision dec) {
    this.add(0, dec);
  }
  
  public void addLast(Decision dec) {
    this.add(this.size() - 1, dec);
  }
  
  public Decision getFirst() {
    return this.get(0);
  }
  
  public Decision getLast() {
    return this.get(this.size() - 1);
  }
  
  public Path(ChoiceGenerator<?> endCG, ContextManager ctxManager, boolean ctxPreserving, int maxSize) {
    generatePath(this, endCG, ctxManager, ctxManager.getContext(endCG), ctxPreserving, maxSize);
  }
  
  public Path(ChoiceGenerator<?> endCG, ContextManager ctxManager, boolean ctxPreserving) {
    this(endCG, ctxManager, ctxPreserving, -1);
  }
  
  public Path(ChoiceGenerator<?> endCG, ContextManager ctxManager) {
    this(endCG, ctxManager, false);
  }
  
  public Path generateCtxPreservingHistoryFromIdx(int idx, int maxSize) {
    return generateCtxPreservingHistoryFromIdx(idx, false, maxSize);
  }
  
  public Path generateCtxPreservingHistoryFromIdx(int idx) {
    return generateCtxPreservingHistoryFromIdx(idx, true, -1);
  }
  
  private Path generateCtxPreservingHistoryFromIdx(int idx, boolean full, int maxSize) {
    Path subPath = new Path();
    if(idx <= 0)
      return subPath;
    StackFrame ctx = this.get(idx).getContext();
    for(int i = idx-1, size = 0; i >= 0; i--, size++) {
      if(!full && size >= maxSize)
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
    pathBuilder.append("(");
    while(iter.hasNext()) {
      pathBuilder.append(iter.next().toString());
      if(iter.hasNext())
        pathBuilder.append(", ");
    }
    pathBuilder.append(")");
    return pathBuilder.toString();
  }
}