package wcanalysis.heuristic.policy;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
public class TrieStorage implements BranchPolicyStorage {
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
    
    public boolean hasChoices() {
      return this.choices.size() > 0;
    }
    
    public void addChoice(int choice) {
      this.choices.add(choice);
    }
    
    public Set<Node> getChildren() {
      return this.next.values();
    }

    public Decision getDecisionForChild(Node child) {
      return this.next.inverse().get(child);
    }

    public Node getNext(Decision curr) {
      if(next.containsKey(curr))
        return next.get(curr);
      else
        return null;
    }

    public void addNext(Decision dec, Node next) {
      this.next.put(dec, next);
    }
  }
  
  
  public static class Builder {
    private Node root;      // root of trie
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

    public Builder put(Path key, int choice) {
      Path keyCpy = new Path(key);
      Collections.reverse(keyCpy);
      root = put(root, null, keyCpy, choice, 0);
      
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
      Decision dec = null;
      if(key.size() > d)
        dec = key.get(d);
      if(d >= key.size() - 1) {
        assert x.getChoices() != null;
        x.addChoice(choice);
        return x;
      }
      Node nxt = put(x.getNext(dec), x, key, choice, d + 1);
      x.addNext(dec, nxt);
      return x;
    }
    
    public TrieStorage build() {
      return new TrieStorage(root, choice2Counts);
    }
  }
  
  private final Node root;
  private final Map<Integer, Integer> choice2Counts;
  
  private TrieStorage(Node root, Map<Integer, Integer> choice2Counts) {
    this.root = root;
    this.choice2Counts = choice2Counts;
  }
  
  @Override
  public int getCountsForChoice(int choice) {
    if(this.choice2Counts.containsKey(choice)) {
      return this.choice2Counts.get(choice);
    } else {
      return 0;
    }
  }
  
  public Set<Integer> getChoicesForLongestPrefix(Path key) {
      Node x = getNodeWithLongestPrefix(root, key, 0, null);
      if (x == null) return null;
      return x.getChoices();
  }
  
  private Node getNodeWithLongestPrefix(Node x, Path key, int d, Node longestSuffixNode) {
    if(x == null)
      return null;
    if(x.hasChoices()) {
      longestSuffixNode = x;
    }
    if(d == key.size()) {
      return longestSuffixNode;
    }
    Decision c = key.get(d);
    return getNodeWithLongestPrefix(x.getNext(c), key, d+1, longestSuffixNode);
  }
  
  @Override
  public String toString() {
    Set<String> paths = new HashSet<>();
    collectPaths(root, new StringBuilder(), paths);
    StringBuilder pathStringBuilder = new StringBuilder();
    Iterator<String> pathIter = paths.iterator();
    while(pathIter.hasNext()) {
      pathStringBuilder.append(pathIter.next());
      if(pathIter.hasNext())
        pathStringBuilder.append("\n");
    }
    return pathStringBuilder.toString();
  }
  
  private void collectPaths(Node node, StringBuilder sb, Set<String> paths) {
    Decision curr = node.getDecision();
    if(curr != null)
      sb.append(curr.toString());
    if(node.hasChoices()) {
      StringBuilder pathSb = new StringBuilder(sb);
      if(curr == null)
        pathSb.append("Empty");
      pathSb.append(" --> {");
      Iterator<Integer> choiceIter = node.getChoices().iterator();
      while(choiceIter.hasNext()) {
        pathSb.append(choiceIter.next());
        if(choiceIter.hasNext())
          pathSb.append(",");
      }
      pathSb.append("}");
      paths.add(pathSb.toString());
    }
    if(curr != null && node.getChildren().size() > 0)
      sb.append(","); 
    for(Node child : node.getChildren()) {
      collectPaths(child, new StringBuilder(sb), paths);
    }
  }
}
