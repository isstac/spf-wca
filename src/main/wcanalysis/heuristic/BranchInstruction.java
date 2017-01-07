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