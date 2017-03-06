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

/**
 * @author corina pasareanu corina.pasareanu@sv.cmu.edu
 *
 */

package challenge.challenge2onebucket;

import challenge.challenge2onebucket.util.HashTable;
import gov.nasa.jpf.symbc.Debug;

public class TestApp {


  public static void main(final String[] args) {

    final int HASH_TABLE_SIZE = 1;
    int KEY_SIZE = 4;
    
    int N=Integer.parseInt(args[0]);
    final HashTable hashTable = new HashTable(HASH_TABLE_SIZE);
        
    
    for(int i=0;i<N;i++) {
      char[] input = new char[KEY_SIZE];
      for(int s = 0; s < input.length; s++) {
        input[s] = Debug.makeSymbolicChar("in"+i+s);
      }
//      System.out.println("calling put #" + i);  
      hashTable.put(new String(input), "value");
    }   

    char[] input = new char[KEY_SIZE];
    for(int s = 0; s < input.length; s++) {
      input[s] = Debug.makeSymbolicChar("get"+s);
    }
//    System.out.println("calling get");
//    System.out.println("size of hashtable " + hashTable.size());
    hashTable.get(new String(input));
//    System.err.println("Goodbye!");
  }
}
