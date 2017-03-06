/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
