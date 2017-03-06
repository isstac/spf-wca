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

package wcanalysis.heuristic;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import gov.nasa.jpf.vm.Instruction;
import wcanalysis.heuristic.util.Util;

/**
 * @author Kasper Luckow
 * This is ugly, but we need a separate instructions class, because JPF's
 * is not serializable
 */
public class BranchInstruction implements Serializable {
  private static final long serialVersionUID = 7705704224210468320L;
  private final String mnemonic;
  private final int index;
  private final int lineNumber;
  private final String methodName;
  private final String className;

  public BranchInstruction(Instruction jpfInstruction) {
    this(jpfInstruction.getMnemonic(), 
        jpfInstruction.getMethodInfo().getClassName(), 
        Util.normalizeJPFMethodName(jpfInstruction.getMethodInfo()), 
        jpfInstruction.getInstructionIndex(), 
        jpfInstruction.getLineNumber());
  }
  
  public BranchInstruction(String mnemonic, String clsName, String methodName, int index, int linenumber) {
    this.mnemonic = mnemonic;
    this.index = index;
    this.lineNumber = linenumber;
    this.methodName = methodName;
    this.className = clsName;
  }
  
  public BranchInstruction copy() {
    return new BranchInstruction(this.mnemonic, this.className, this.methodName, this.index, this.lineNumber);
  }
  
  public String getMnemonic() {
    return mnemonic;
  }

  public String getClassName() {
    return className;
  }

  public int getInstructionIndex() {
    return index;
  }
  
  public String getMethodName() {
    return methodName;
  }

  public int getLineNumber() {
    return lineNumber;
  }
  
  @Override
  public int hashCode(){
    return new HashCodeBuilder()
        .append(mnemonic)
        .append(index)
        .append(lineNumber)
        .append(methodName)
        .toHashCode();
  }

  @Override
  public boolean equals(Object obj){
    if(obj instanceof BranchInstruction){
      final BranchInstruction other = (BranchInstruction)obj;
      return new EqualsBuilder()
          .append(mnemonic, other.mnemonic)
          .append(index, other.index)
          .append(lineNumber, other.lineNumber)
          .append(methodName, other.methodName)
          .isEquals();
    } else{
      return false;
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("l:" + this.getLineNumber())
      .append("(o:").append(this.getInstructionIndex()).append(")");
    return sb.toString();
  }
}