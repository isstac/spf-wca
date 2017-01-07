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

/**
 * @author corina pasareanu corina.pasareanu@sv.cmu.edu
 *
 */

package challenge.challenge2.app;

import challenge.challenge2.util.HashTable;
import gov.nasa.jpf.symbc.Debug;

public class TestApp {


  public static void main(final String[] args) {

    final int HASH_TABLE_SIZE = 2;
    int keyLength = 2;
    
    int N=Integer.parseInt(args[0]);
    final HashTable hashTable = new HashTable(HASH_TABLE_SIZE);
        
    

    
    for(int i=0;i<N;i++) {
      char[] input = new char[keyLength];
      for(int s = 0; s < input.length; s++) {
        input[s] = Debug.makeSymbolicChar("in"+i+s);
      }
      System.out.println("calling put #" + i);  
      hashTable.put(new String(input), "value");
    }   

    char[] input = new char[keyLength];
    for(int s = 0; s < input.length; s++) {
      input[s] = Debug.makeSymbolicChar("get"+s);
    }
    System.out.println("calling get");    
    hashTable.get(new String(input));
    System.err.println("Goodbye!");
  }
}