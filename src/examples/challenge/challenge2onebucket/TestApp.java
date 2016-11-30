/**
 * @author corina pasareanu corina.pasareanu@sv.cmu.edu
 *
 */

package challenge.challenge2onebucket;

import challenge2onebucket.util.HashTable;
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
