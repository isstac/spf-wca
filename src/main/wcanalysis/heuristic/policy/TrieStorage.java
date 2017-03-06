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

package wcanalysis.heuristic.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import wcanalysis.heuristic.Decision;
import wcanalysis.heuristic.Path;

/**
 * @author Kasper Luckow
 * TODO: Get rid of endnodes -- they are not used for anything but matching longest suffix
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
    private Node root; // root of trie

    private Map<Decision, Set<Node>> endNodes = new HashMap<>();
    private Map<Integer, Integer> choice2Counts = new HashMap<>();
    
    public Builder() { }

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

    public void addStorage(TrieStorage storage) {
      Set<PathChoicesPair> pathChoicePairs = storage.getPaths();
      for(PathChoicesPair pcp : pathChoicePairs) {
        for(int choice : pcp.choices) {
          this.put(pcp.path, choice);
        }
      }
    }

    private Node put(Node x, Node parent, Path key, int choice, int d) {
      if(x == null) {
        x = new Node(parent);
      }
      Decision dec = null;
      if(key.size() > d)
        dec = key.get(d);
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
    
    private void performTruncation() {
      throw new RuntimeException("Adaptive analysis coming up!");
    }
    
    public TrieStorage build(boolean makeAdaptive) {
      if(makeAdaptive) {
        performTruncation();
      }
      return new TrieStorage(root, endNodes, choice2Counts);
    }
    
    public TrieStorage build() {
      return new TrieStorage(root, endNodes, choice2Counts);
    }
  }

  private static class PathChoicesPair {
    public final Path path;
    public final Set<Integer> choices;

    public PathChoicesPair(Path path, Set<Integer> choices) {
      this.path = path;
      this.choices = choices;
    }
  }
  
  @Deprecated
  private final Map<Decision, Set<Node>> endNodes;
  
  private final Node root;
  private int height = -1;
  private final Map<Integer, Integer> choice2Counts;
  
  private TrieStorage(Node root, Map<Decision, Set<Node>> endNodes, Map<Integer, Integer> choice2Counts) {
    this.endNodes = endNodes;
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
  
  public boolean containsChoices(Path key) {
    return getChoices(key) != null;
  }

  private Node get(Node x, Path key, int d) {
    if(x == null)
      return null;
    if(d == key.size())
      return x;
    Decision c = key.get(d);
    return get(x.getNext(c), key, d+1);
  }

  @Override
  public Set<Integer> getChoices(Path history) {
    Node x = get(root, history, 0);
    if(x == null)
      return null;
    return x.getChoices();
  }
  
  //This is pretty messy. Too tired to clean it up now...
  @Deprecated
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
        Decision histDecision = null;
        int historyIdx = history.size() - 1 - index;
        if(historyIdx > 0)
          histDecision = history.get(historyIdx);
        else
          break;
        Decision policyDecision = curr.getDecision();
        if(policyDecision == null)
          break;
        
        if(policyDecision.equals(histDecision)) {
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
  
  //seems a bit insane
  @Override
  public String toString() {
    Set<String> paths = new HashSet<>();
    createPathRepresentation(root, new StringBuilder(), paths);
    StringBuilder pathStringBuilder = new StringBuilder();
    Iterator<String> pathIter = paths.iterator();
    while(pathIter.hasNext()) {
      pathStringBuilder.append(pathIter.next());
      if(pathIter.hasNext())
        pathStringBuilder.append("\n");
    }
    return pathStringBuilder.toString();
  }

  private Set<PathChoicesPair> getPaths() {
    Set<PathChoicesPair> pathChoicePairs = new HashSet<>();
    getDecisions(root, new Path(), pathChoicePairs);
    return pathChoicePairs;
  }

  private void getDecisions(Node node, Path currentPath, Set<PathChoicesPair> pathsDecisions) {
    Decision curr = node.getDecision();
    if(curr != null) {
      currentPath.addLast(curr);
    }
    if(node.hasChoices()) {
      Path completePath = new Path(currentPath);
      pathsDecisions.add(new PathChoicesPair(completePath, node.choices));
    }
    for(Node child : node.getChildren()) {
      getDecisions(child, new Path(currentPath), pathsDecisions);
    }
  }
  
  private void createPathRepresentation(Node node, StringBuilder sb, Set<String> paths) {
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
      createPathRepresentation(child, new StringBuilder(sb), paths);
    }
  }

  @Override
  public int getMaxHistoryLength() {
    if(height < 0)
      height = getMaxHeight(root, 0);
    return height;
  }
  
  private int getMaxHeight(Node node, int currHeight) {
    int childMax = currHeight;
    for(Node child : node.getChildren()) {
      int height = getMaxHeight(child, currHeight + 1);
      if(height > childMax) {
        childMax = height;
      }
    }
    return childMax;
  }
}
