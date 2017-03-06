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

import gov.nasa.jpf.vm.StackFrame;

/**
 * @author Kasper Luckow
 */
public class Decision implements Serializable {
  private static final long serialVersionUID = -7153050127298410855L;
  private final BranchInstruction instr;
  private final int choice;
  
  //We don't want to serialize this guy
  private transient final StackFrame context;


  public Decision(BranchInstruction instr, int choice, StackFrame context) {
    this.choice = choice;
    this.instr = instr;
    this.context = context;
  }

  public BranchInstruction getInstruction() {
    return this.instr;
  }

  public int getChoice() {
    return this.choice;
  }
  
  public StackFrame getContext() {
    return this.context;
  }

  public Decision copy() {
    //copy to reference is copied here. This is dangerous
    Decision cp = new Decision(instr.copy(), this.choice, context);
    return cp;
  }
  
  @Override
  public int hashCode(){
    return new HashCodeBuilder()
        .append(instr)
        .append(choice)
        //.append(context)
        .toHashCode();
  }

  @Override
  public boolean equals(Object obj){
    if(obj instanceof Decision){
      final Decision other = (Decision)obj;
      return new EqualsBuilder()
          .append(instr, other.instr)
          .append(choice, other.choice)
          //.append(context, other.context)
          .isEquals();
    } else{
      return false;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<")  
       .append(this.getInstruction().toString())
       .append(", ") 
       .append(((this.getChoice() == 1) ? "T" : (this.getChoice() == 0) ? "F" : this.getChoice()+""))
       .append(">" /*+ cur.getContext() + "]"*/);
    
    return sb.toString();
  }
}
