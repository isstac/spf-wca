/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wcanalysis.util;

import java.util.HashSet;
import java.util.Set;

import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;

/**
 * A visitor to collect all symbolic variables
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class SymbolicVariableCollector extends ConstraintExpressionVisitor {

	public HashSet<String> setOfSymVar;
	
	public SymbolicVariableCollector(HashSet<String> set){
		setOfSymVar = set;
	}
	
	@Override
	public void preVisit(SymbolicInteger expr) {
		String name = cleanSymbol(expr.toString());
		setOfSymVar.add(name);
	}

	public void collectVariables(PathCondition pc){
		Constraint c = pc.header;
		while(c != null){
			c.accept(this);
			c = c.getTail();
		}
	}
	
	private static String cleanSymbol(String str) {
		return str.replaceAll("\\[(.*?)\\]", ""); // remove e.g. [-1000000]
	}
	
	public Set<String> getListOfVariables(){
		return setOfSymVar;
	}
	
	public int size(){
		return setOfSymVar.size();
	}
}
