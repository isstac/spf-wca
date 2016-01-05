package wcanalysis.heuristic.costmodel.depth;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import wcanalysis.heuristic.costmodel.*;


/**
 * The only thing this class does over the superclass is optimize.
 * It stores a local int with the depth, instead of calculating it 
 * from the path of PCChoiceGenerators each time it is used.
 * @author Rody Kersten
 */
public class DepthState extends CostState {
	
	private int depth;
	
	/** Constructor */
	public DepthState(PCChoiceGenerator pccg) {
		super(pccg);
		depth = getDepth();
	}
	
	/** Just the depth in this case. */
	public int intValue() {
		return depth;
	}

	@Override
	public int compareTo(CostState other) {
		if(other == null)
			return 1;
		
		if (other instanceof DepthState)
			return this.depth - ((DepthState) other).depth;
		else
			throw new RuntimeException("Trying to compare states of different cost models");
	}
	
	@Override
	public String toString() {
		return "DepthState = " + depth;
	}
}
