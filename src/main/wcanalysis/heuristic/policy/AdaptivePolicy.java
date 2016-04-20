package wcanalysis.heuristic.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import wcanalysis.heuristic.Decision;
import wcanalysis.heuristic.Path;

/**
 * @author Kasper Luckow
 *
 */
public class AdaptivePolicy implements Serializable {
  private static final long serialVersionUID = -8230467461615793375L;

  private static class Node implements Serializable {
    private static final long serialVersionUID = 1657454520365014627L;
    
    private Set<Integer> choices = new HashSet<>();
    private BiMap<Decision, Node> next = HashBiMap.<Decision, Node>create();
    private Node parent;
    
    public Node(Node parent) {
      this.parent = parent;
    }
    
    public Node getParent() {
      return this.parent;
    }
    
    public Decision getDecision() {
      Node par = getParent();
      if(par == null)
        return null;
      return par.getDecisionForChild(this);
    }
    
    public Set<Integer> getChoices() {
      return this.choices;
    }
    
    public void addChoice(int choice) {
      this.choices.add(choice);
    }

    public Decision getDecisionForChild(Node child) {
      return this.next.inverse().get(child);
    }

    public Node getNext(Decision curr) {
      return next.get(curr);
    }

    public void addNext(Decision dec, Node next) {
      this.next.put(dec, next);
    }
  }
  
  
  public static class Builder {
    private Node root;      // root of trie

    private Map<Decision, Set<Node>> endNodes = new HashMap<>();
    private Map<Integer, Integer> choice2Counts = new HashMap<>();
    
    public Builder() { }

//    public Set<Integer> get(Path key) {
//      Node x = get(root, key, 0);
//      if (x == null)
//        return null;
//      return x.getChoices();
//    }
//
//    public boolean contains(Path key) {
//      return get(key) != null;
//    }
//
//    private Node get(Node x, Path key, int d) {
//      if(x == null)
//        return null;
//      if(d == key.size())
//        return x;
//      Decision c = key.get(d);
//      return get(x.getNext(c), key, d+1);
//    }
//  public void compact() {
//  compact(root);
//}
//
//private void compact(Node n) {
//  if(n.isEndNode() || n.childrenSize() > 1) {
//    return;
//  } else {
//    //ugly
//    Decision child = n.next.keySet().iterator().next();
//    Node newRoot = n.getNext(child);
//    n.next.put(child, null);
//    n = newRoot;
//    compact(n);
//  }
//}
    /**
     * Inserts the key-value pair into the symbol table, overwriting the old value
     * with the new value if the key is already in the symbol table.
     * If the value is <tt>null</tt>, this effectively deletes the key from the symbol table.
     * @param key the key
     * @param val the value
     * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
     */
    public Builder put(Path key, int choice) {
      root = put(root, null, key, choice, 0);
      
      if(!choice2Counts.containsKey(choice)) {
        choice2Counts.put(choice, 1);
      } else {
        int currentCount = choice2Counts.get(choice);
        choice2Counts.put(choice, ++currentCount);
      }
      
      return this;
    }

    private Node put(Node x, Node parent, Path key, int choice, int d) {
      if(x == null) {
        x = new Node(parent);
      }
      Decision dec;
      if(key.size() >= d)
        dec = key.get(d);
      else
        dec = null; //this is not very nice.
      if(d == key.size()) {
        assert x.getChoices() != null;
        x.addChoice(choice);
        Set<Node> endNodesForDec = this.endNodes.get(dec);
        if(endNodesForDec == null) {
          endNodesForDec = new HashSet<>();
          this.endNodes.put(dec, endNodesForDec);
        }
        endNodesForDec.add(x);
        return x;
      }
      Node nxt = put(x.getNext(dec), x, key, choice, d + 1);
      x.addNext(dec, nxt);
      return x;
    }
    
    public AdaptivePolicy build() {
      return new AdaptivePolicy(root, endNodes, choice2Counts);
    }
  }
  
  private final Map<Decision, Set<Node>> endNodes;
  private final Node root;
  private final Map<Integer, Integer> choice2Counts;
  
  private AdaptivePolicy(Node root, Map<Decision, Set<Node>> endNodes, Map<Integer, Integer> choice2Counts) {
    this.endNodes = endNodes;
    this.root = root;
    this.choice2Counts = choice2Counts;
  }
  
  public int getCountsForChoice(int choice) {
    if(this.choice2Counts.containsKey(choice)) {
      return this.choice2Counts.get(choice);
    } else {
      return 0;
    }
  }
  
  //This is pretty ugly.
  public Set<Integer> getChoicesForLongestSuffix(Path history) {
    Decision last;
    if(history.size() > 0) {
      last = history.get(history.size() - 1);
    } else {
      last = null;
    }
    
    Set<Node> ends = endNodes.get(last);
    Set<Node> maxSuffixNodes = new HashSet<>();
    int maxSuffix = -1;
    for(Node end : ends) {
      int index = 0;
      int suffixLength = 0;
      Node curr = end;
      boolean equal = true;
      while(curr != null) {
        Decision histDecision = history.get(history.size() - 1 - index);
        if(curr.getDecision().equals(histDecision)) {
          suffixLength++;
          curr = curr.getParent();
          index++;
        } else {
          equal = false;
          break;
        }
      }
      if(equal) {
        if(suffixLength >= maxSuffix) {
          if(suffixLength > maxSuffix) {
            maxSuffixNodes.clear();
          }
          maxSuffixNodes.add(end);
          maxSuffix = suffixLength;
        }
      }
    }
    Set<Integer> choices = new HashSet<>();
    for(Node maxSuffixNode : maxSuffixNodes) {
      choices.addAll(maxSuffixNode.getChoices());
    }
    return choices;
  }
}
