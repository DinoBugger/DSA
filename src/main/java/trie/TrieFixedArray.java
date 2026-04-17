package trie;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TrieFixedArray {

  private final int SIZE = 26;
  TrieNode root;


  public TrieFixedArray() {
    root = new TrieNode();
  }

  // ============= INSERT
  public void insert(String word) {
    if (word == null || word.isEmpty()) {
      return;
    }
    TrieNode cur = root;
    for (char c : word.toCharArray()) {
      int index = c - 'a';
      if (cur.children[index] == null) {
        cur.children[index] = new TrieNode();
      }
      cur = cur.children[index];
    }
    cur.isEndOfWord = true;
  }

  // ============ GET DEEPEST NODE OF STR
  private TrieNode getNode(String str) {
    TrieNode cur = root;
    for (char c : str.toCharArray()) {
      int index = c - 'a';
      if (cur == null) {
        return null;  // should check null before access
      }
      cur = cur.children[index];
    }
    return cur;
  }

  // ============= SEARCH
  public boolean search(String word) {
    if (word == null || word.isEmpty()) {
      return false;
    }
    var node = getNode(word);
    return node != null && !node.isEndOfWord;
  }

  // ============= STARTSWITH
  public boolean startsWith(String prefix) {
    if (prefix == null || prefix.isEmpty()) {
      return false;
    }
    return getNode(prefix) != null;
  }

  //====== DELETE
  public boolean delete(String word) {
    if (word == null || word.isEmpty()) {
      return false;
    }
    List<TrieNode> path = new ArrayList<>();
    TrieNode cur = root;
    path.add(cur);
    // Deep search
    for (int i = 0; i < word.length(); i++) {
      cur = cur.children[word.charAt(i)];
      if (cur == null) {
        return false;  // word is not found in the tree
      }
      path.add(cur);
    }
    if (!cur.isEndOfWord) {
      return false;  // on the path of other word but not a word. Ex ap -> application
    }
    cur.isEndOfWord = false;// delete if word is on the path of other word. Ex: app -> application

    // Backtracking
    // If you wonder why I subtract to 2. It is because
    // -1 for max size of a fixed array is n-1 with n is number of array items
    // another -1 because I want to get the parent of this current node to maybe delete it
    for (int i = path.size() - 2; i >= 0; i--) {
      TrieNode parent = path.get(i);
      TrieNode child = path.get(i + 1);
      // Condition to delete a node
      // never delete root
      // never delete node which has child
      // make sure deleting not disturb other word. Ex: on the going higher path check isEndOfWord
      // to not delete a shorter Ex. you delete apple not the word app
      // child.children.length == 0  is wrong because every children array default length is 26
      if (isEmpty(child) && !child.isEndOfWord) {
        int index = word.charAt(i) - 'a';
        parent.children[index] = null;
      } else {
        break; // if node is still in use
      }
      // apple

    }
    return true;
  }

  private boolean isEmpty(TrieNode node) {
    for (TrieNode n : node.children) {
      if (n != null) {
        return false;
      }
    }
    return true;
  }

  // ========= GET ALL WORDs START WITH PREFIX
  public List<String> getAllWordsStartWith(String prefix) {
    List<String> result = new ArrayList<>();
    if (prefix == null || prefix.isEmpty()) {
      return result;
    }
    // first, we get started at the end of prefix node
    TrieNode startNode = getNode(prefix);
    if (startNode == null) {
      return result;
    }

    Deque<DFSState> stack = new ArrayDeque<>();
    stack.push(new DFSState(startNode, new StringBuilder(prefix)));

    while (!stack.isEmpty()) {
      DFSState curState = stack.pop();

      // this also solve the case when prefix is actually a word itself
      if (curState.node.isEndOfWord) {
        result.add(curState.word.toString());
      }

      for (int i = 0; i < curState.node.children.length; i++) {
        if (curState.node.children[i] != null) {
          StringBuilder nextWord = new StringBuilder(curState.word);
          nextWord.append(
              (char) (i + 'a'));   // parse for the expression not solely for i like (char) i ...
          stack.push(new DFSState(curState.node.children[i], nextWord));
        }
      }
    }
    return result;
  }

  // ============== CLEAR ALL
  public void clear() {
    root.children = new TrieNode[26];
  }

  private static class TrieNode {

    TrieNode[] children;
    boolean isEndOfWord;

    public TrieNode() {
      children = new TrieNode[26];
      isEndOfWord = false;
    }
  }

  private record DFSState(TrieNode node, StringBuilder word) {

  }
}
