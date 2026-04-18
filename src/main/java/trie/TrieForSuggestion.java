package trie;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class TrieForSuggestion {

  private TrieNode root;

  public TrieForSuggestion() {
    root = new TrieNode();
  }

  // ================= INSERT

  public void insert(String word) {
    insert(word, 1);
  }

  /**
   *
   * @param word      word to insert
   * @param frequency word frequency
   */
  private void insert(String word, int frequency) {
    if (word == null || word.isEmpty()) {
      return;
    }
    TrieNode cur = root;
    for (int i = 0; i < word.length(); i++) {
      char curChar = word.charAt(i);
      cur.children.putIfAbsent(curChar, new TrieNode());
      cur = cur.children.get(curChar);
    }

    cur.isEndOfWord = true;
    cur.frequency += frequency;

  }

  //============= GET NODE
  private TrieNode getNode(String str) {
    TrieNode cur = root;
    for (int i = 0; i < str.length(); i++) {
      char curChar = str.charAt(i);

      cur = cur.children.get(curChar);  // Maybe this place can more to null point
      // So we check
      if (cur == null) {
        return null;
      }
    }
    return cur;
  }

  public boolean search(String word) {
    if (word == null || word.isEmpty()) {
      return false;
    }
    TrieNode node = getNode(word);
    return node != null && node.isEndOfWord;
  }

  public boolean startsWith(String prefix) {
    if (prefix == null || prefix.isEmpty()) {
      return false;
    }
    TrieNode node = getNode(prefix);
    return node != null;
  }

  public boolean delete(String word) {
    if (word == null || word.isEmpty()) {
      return false;
    }
    TrieNode cur = root;
    List<TrieNode> path = new ArrayList<>();
    for (int i = 0; i < word.length(); i++) {
      char c = word.charAt(i);
      if (cur.children.get(c) == null) {
        return false;
      }
      path.add(cur.children.get(c));
    }

    if (!cur.isEndOfWord) {
      return false;
    }
    cur.isEndOfWord = false;

    // isEmpty() is for checking if a map doesn't contain any key-value
    // in this
    for (int i = path.size() - 2; i >= 0; i--) {
      TrieNode parent = path.get(i);
      TrieNode child = path.get(i + 1);

      // check child condition
      if (child.children.isEmpty() && !child.isEndOfWord) {
        char key = word.charAt(i);
        parent.children.remove(key);
      } else {
        //in case still in use
        break;
      }
    }
    return true;
  }

  /**
   *
   * @param prefix prefix of word
   * @param limit  Top-K base on frequency
   * @return
   */
  public List<String> suggest(String prefix, int limit) {
    List<String> finalResult = new ArrayList<>();
    if (prefix == null || prefix.isEmpty() || limit <= 0) {
      return finalResult;
    }

    TrieNode startNode = getNode(prefix);
    if (startNode == null) {
      return finalResult;
    }

    // MIN-HEAP is standing out this scenario
    // when we want to access and alter when the data is updated constantly
    // MIN-HEAP is best for case of Top-K
    // Why not MAX-HEAP? you can wonder that we are finding top-k the strongest why not max-heap
    // When we use MIN-HEAP, the smallest always on top and we know exactly where is the smallest that
    // we can poll it easily is the top(the smallest), but when it comes to max-heap we only know
    // where is the strongest.
    PriorityQueue<Suggestion> minHeap = new PriorityQueue<>(
        (a, b) -> Integer.compare(a.frequency, b.frequency)
    );

    Deque<DFSState> stack = new ArrayDeque<>();
    stack.push(new DFSState(startNode, new StringBuilder(prefix)));

    while (!stack.isEmpty()) {
      DFSState curState = stack.pop();

      if (curState.node.isEndOfWord) {
        minHeap.add(new Suggestion(curState.word.toString(), curState.node.frequency));
        if (minHeap.size() > limit) {
          minHeap.poll(); // eliminate the top (the smallest)
        }
      }

      for (Map.Entry<Character, TrieNode> entry : curState.node.children.entrySet()) {
        stack.push(new DFSState(entry.getValue(),
            new StringBuilder(curState.word).append(entry.getKey())));
      }
    }

    // Another great tip here is the when you add sequently with the same index such 0
    // The previous value in index will be move toward like i+1
    while (!minHeap.isEmpty()) {
      finalResult.add(0, minHeap.poll().word);
    }
    return finalResult;
  }

  // =================== COLLECT ALL WORDS
  private void collectWords(TrieNode rootNode, String prefix, List<String> result) {
    Deque<DFSState> stack = new ArrayDeque<>();
    stack.push(new DFSState(rootNode, new StringBuilder(prefix)));

    while (!stack.isEmpty()) {
      DFSState current = stack.pop();

      if (current.node().isEndOfWord) {
        result.add(current.word().toString());
      }

      for (Map.Entry<Character, TrieNode> entry : current.node().children.entrySet()) {
        StringBuilder nextWord = new StringBuilder(current.word());
        nextWord.append(entry.getKey());
        stack.push(new DFSState(entry.getValue(), nextWord));
      }
    }
  }

  private static class Suggestion implements Comparable<Suggestion> {

    String word;
    int frequency;

    Suggestion(String word, int frequency) {
      this.word = word;
      this.frequency = frequency;
    }

    @Override
    public int compareTo(Suggestion other) {
      return Integer.compare(other.frequency, this.frequency);
    }
  }

  private record DFSState(TrieNode node, StringBuilder word) {

  }

  private static class TrieNode {

    Map<Character, TrieNode> children;
    boolean isEndOfWord;
    int frequency;

    public TrieNode() {
      this.children = new HashMap<>();
      this.isEndOfWord = false;
      this.frequency = 0;
    }
  }
}
