package wcanalysis.heuristic.costmodel;

import java.util.LinkedList;
import java.util.List;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Rody Kersten
 */
public abstract class CostState implements Comparable<CostState> {
	
	// holds the path leading up to this state
	protected PCChoiceGenerator pccg;
	
	/** Constructor */
	public CostState(PCChoiceGenerator pccg) {
		this.pccg = pccg;
	}
	
	/** @return an integer representation of the costs */
	public abstract int intValue();
	
	/** Compares two CostStates.
	 * @param other state to compare to
	 * @return -1 if this is smaller, 0 if equal, 1 if this is larger
	 **/
	public abstract int compareTo(CostState other);
	
	/** @return the depth of the path up to this state */
	public int getDepth() {
		return calcDepth(pccg);
	}
	
	/** Returns the PCChoiceGenerator that represents the path up to this state. */
	public PCChoiceGenerator getPath() {
		return pccg;
	}
	
	/** Returns a chronological list of PCChoiceGenerators. */
	public List<PCChoiceGenerator> getPathAsOrderedList() {
		List<PCChoiceGenerator> l = new LinkedList<PCChoiceGenerator>();
		PCChoiceGenerator cur = pccg;
		while(cur != null) {
			l.add(0,cur);
			cur = cur.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
		}
		return l;
	}
	
	/** @return the path up to this state as a string */
	public String pathToString() {
		StringBuilder pathBuilder = new StringBuilder();
		PCChoiceGenerator cur = pccg;
		while(cur != null) {
			if (pathBuilder.length()>0)
				pathBuilder.insert(0,", ");
			pathBuilder.insert(0, "[[" +  
					"l:" + cur.getInsn().getLineNumber() + "(o:" +
					cur.getInsn().getInstructionIndex() + "), " + 
					((cur.getNextChoice() == 1) ? 'T' : 'F') + "]"+/*cur.frame +*/ "]");
			cur = cur.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
		}
	    return pathBuilder.toString();
	}
	
	/** Calculate the depth of the path up to this state */
	private int calcDepth(PCChoiceGenerator pccg) {
		if (pccg==null) {
			return 0;
		} else {
			ChoiceGenerator<?> prev = pccg.getPreviousChoiceGenerator();
			if (prev instanceof PCChoiceGenerator)
				return 1 + calcDepth((PCChoiceGenerator) prev);
			else
				return 1;
		}
	}
}