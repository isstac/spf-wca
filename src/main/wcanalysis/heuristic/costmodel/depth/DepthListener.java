package wcanalysis.heuristic.costmodel.depth;

import java.util.HashMap;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;
import wcanalysis.heuristic.costmodel.*;

/**
 * @author Rody Kersten, Kasper Luckow
 */
public class DepthListener extends CostListener {

	HashMap<Integer,DepthState> sMap = new HashMap<Integer,DepthState>();
	
	@Override
	public void choiceGeneratorAdvanced (VM vm, ChoiceGenerator<?> currentCG) {
		ChoiceGenerator<?> cg = vm.getSystemState().getChoiceGenerator();
		if(cg instanceof PCChoiceGenerator) {
			PCChoiceGenerator pccg = (PCChoiceGenerator) cg;
			System.out.println("PCChoiceGenerator Advanced: " + pccg);
			
//			int choice = ((PCChoiceGenerator)currentCG).getNextChoice();
		    DepthState ds = sMap.get(cg.getStateId());
		    if(ds == null) {
		        //Convert JPF instruction to our own serializable
		        //Instruction representation
//		        Instruction jpfInstr = currentCG.getInsn();
//		        String clsName = jpfInstr.getMethodInfo().getClassName();
//		        String methodName = normalizeJPFMethodName(jpfInstr.getMethodInfo());
		        
//		        BranchInstruction instr = new BranchInstruction(jpfInstr.getMnemonic(), 
//		            clsName, methodName, jpfInstr.getInstructionIndex(), jpfInstr.getLineNumber());
		        ds = new DepthState(pccg);
		        sMap.put(cg.getStateId(), ds);
		        System.out.println("Current depth: " + ds.getDepth());
		    }
		}
	}
	
	@Override
	public DepthState getWCState() {
		DepthState wc = null;
		for (DepthState ds : sMap.values()) {
			if (ds.compareTo(wc) > 0)
				wc = ds;
		}
		return wc;
	}
}