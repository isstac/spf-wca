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

package challenge.challenge2.util;

import gov.nasa.jpf.symbc.Debug;

public class ModLinearHash extends AbstractBoundedUniformHash
{
    private static long HASH_STATE_INIT;
    private static long HASH_PARAM_A0;
    private static long HASH_PARAM_A1;
    private static long HASH_PARAM_AF;
    private static long HASH_PARAM_M0;
    private static long HASH_PARAM_M1;
    private static long HASH_PARAM_MF;
    protected long mHashState;
    
    public void reset() {
        this.mHashState = ModLinearHash.HASH_STATE_INIT;
    }
    
    public void update(final long n) {
      System.out.println("update in modlinearhash. input is symbolic: " + Debug.isSymbolicInteger((int)n));
      
        this.mHashState += (n + ModLinearHash.HASH_PARAM_A0) * ModLinearHash.HASH_PARAM_M0;
        this.mHashState = (this.mHashState + ModLinearHash.HASH_PARAM_A1) * ModLinearHash.HASH_PARAM_M1;
    }
    
    private static int symb_counter = 0;
    public long getHash() {
      System.out.println("in gethash of modlinearhash");
      
        long mLowerBound;
        long mUpperBound;
        long n;
        long n2;
        if (this.mLowerBound == this.mUpperBound) {
            mLowerBound = 0L;
            mUpperBound = 0L;
            n = this.mLowerBound;
            n2 = 0L;
        }
        else {
            if (this.mLowerBound > this.mUpperBound) {
                throw new RuntimeException(new StringBuilder().append("invalid bounds: [").append(this.mLowerBound).append(", ").append(this.mUpperBound).append("]").toString());
            }
            if (this.mLowerBound >= 0L) {
                mLowerBound = 0L;
                mUpperBound = this.mUpperBound - this.mLowerBound;
                n = this.mLowerBound;
                n2 = (Long.highestOneBit(mUpperBound) << 1) - 1L;
            }
            else if (this.mUpperBound < 0L) {
                mLowerBound = this.mLowerBound - this.mUpperBound - 1L;
                mUpperBound = -1L;
                n = this.mUpperBound + 1L;
                n2 = (Long.highestOneBit(~mLowerBound) << 1) - 1L;
            }
            else {
                mLowerBound = this.mLowerBound;
                mUpperBound = this.mUpperBound;
                n = 0L;
                final long max = Math.max(Long.highestOneBit(~mLowerBound), Long.highestOneBit(mUpperBound));
                if (max == 0L) {
                    n2 = 0L;
                }
                else {
                    n2 = (max << 1) - 1L;
                }
            }
        }
        long n3;
        //System.out.println("before do while loop in modlinearhash");
        //System.out.println("upper bound: " + mUpperBound + " is symbolic: " + Debug.isSymbolicInteger((int)mUpperBound));
        //System.out.println("lower bound: " + mLowerBound + " is symbolic: " + Debug.isSymbolicInteger((int)mLowerBound));
        
        //this.mHashState = (this.mHashState + ModLinearHash.HASH_PARAM_AF) * ModLinearHash.HASH_PARAM_MF;
        //n3 = n2;
        do {
        	System.out.println("do while");
            
            this.mHashState = (this.mHashState + ModLinearHash.HASH_PARAM_AF) * ModLinearHash.HASH_PARAM_MF;
            
            if (this.mHashState >= 0L) {
              //System.out.println("In first condition in do while");
                n3 = (this.mHashState & n2);
            }
            else {
              //System.out.println("In second condition in do while");
                n3 = (this.mHashState | ~n2);
            }
            //System.out.println("loop in gethash");
            //System.out.println("n3 is " + n3);
        } while (n3 < mLowerBound || n3 > mUpperBound);
        
        
        final long n4 = n3 + n;
        this.reset();
        return n4;
    }
    
    static {
        ModLinearHash.HASH_STATE_INIT = 3453881378648555325L;
        ModLinearHash.HASH_PARAM_A0 = -195385520352475319L;
        ModLinearHash.HASH_PARAM_A1 = -2660630309253685457L;
        ModLinearHash.HASH_PARAM_AF = -1138441404092675177L;
        ModLinearHash.HASH_PARAM_M0 = -4144643519569966049L;
        ModLinearHash.HASH_PARAM_M1 = -4394632577027451953L;
        ModLinearHash.HASH_PARAM_MF = -1428682232105743577L;
    }

	
}
