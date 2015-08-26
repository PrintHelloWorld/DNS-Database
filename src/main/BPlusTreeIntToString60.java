package main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implements a B+ tree in which the keys are integers and the values are
 * Strings (with maximum length 60 characters)
 */

public class BPlusTreeIntToString60 {

	private Node root;

	public static final int MAXLEAFKEYS = 15;
	public static final int MAX_INTERNAL_SIZE = 15;

	/**
	 * Returns the String associated with the given key, or null if the key is
	 * not in the B+ tree.
	 */
	public String find(int key) {
		if (root == null) {
			System.out.println("Tree is empty");
			return null;
		}
		/* case one root is leaf node - gets key from map */
		/* case two root is internal node - recurse until find leaf node */
		return root.find(key);
	}

	/**
	 * Stores the value associated with the key in the B+ tree. If the key is
	 * already present, replaces the associated value. If the key is not
	 * present, adds the key with the associated value
	 * 
	 * @param key
	 * @param value
	 * @return whether pair was successfully added.
	 */
	public boolean put(int key, String value) {
		System.out.println("Adding: " + key + " :: " + value);
		/* case one: tree is empty, root is created as leaf node */
		if (root == null) {
			root = new LeafNode(key, value);
			return true;
		} else {
			/* case two: root is not full, adds new value pair to root */
			Tuple<Integer, Node> pair = root.add(key, value);
			/* case three: root is full, make a new root node */
			if (pair != null) {
				InternalNode node = new InternalNode();
				node.addChild(0, root);
				node.addKey(0, pair.key());
				node.addChild(1, pair.value());
				root = node;
			}
			return true;
		}
	}

	public String printAll() {
		StringBuilder s = new StringBuilder();
		if (root == null)
			return null;
		Node node = this.root;
		while (!(node instanceof LeafNode))
			node = ((InternalNode) node).getChild(0);
		LeafNode leaf = (LeafNode) node;
		while (leaf != null) {
			s.append(leaf.toString());
			leaf = (LeafNode) leaf.getNext();
		}
		return s.toString();
	}

	private static class InternalNode implements Node {
		private Integer[] keys;
		private Node[] children;

		private int keySize;
		private int numChild;

		public InternalNode() {
			keys = new Integer[MAX_INTERNAL_SIZE + 1];
			children = new Node[MAX_INTERNAL_SIZE + 2];
		}

		@Override
		public String find(int key) {
			for (int i = 0; i < size(); i++) {
				if (key < getKey(i))
					return getChild(i).find(key);
			}
			return getChild(size()).find(key);
		}

		@Override
		public Tuple<Integer, Node> add(int key, String value) {
			for (int i = 0; i < size(); i++) {
				if (key < getKey(i)) {
					Tuple<Integer, Node> pair = getChild(i).add(key, value);
					if (pair == null)
						return null;
					else
						return promote(pair.key(), pair.value());
				}
			}
			Tuple<Integer, Node> pair = getChild(size()).add(key, value);
			if (pair == null)
				return null;
			else
				return promote(pair.key(), pair.value());
		}

		private Tuple<Integer, Node> promote(Integer key, Node rightChild) {
			if (rightChild == null || key == null)
				return null;
			if (key > getKey(size() - 1)) {
				addKey(size(), key);
				addChild(size(), rightChild);
			} else {
				for (int i = 0; i < size(); i++) {
					if (key < getKey(i)) {
						addKey(i, key);
						addChild(i + 1, rightChild);
						break;
					}
				}
			}
			if (size() <= MAX_INTERNAL_SIZE)
				return null;
			InternalNode sibling = new InternalNode();
			int pKey = splitToNode(sibling);
			return new Tuple<Integer, Node>(pKey, sibling);
		}

		private int splitToNode(InternalNode sibling) {
			int mid = (MAX_INTERNAL_SIZE + 1) / 2;
			int key = keys[mid];

			for (int i = mid + 1; i < keys.length; i++) {
				sibling.add(keys[i]);
				keys[i] = null;
			}
			keys[mid] = null;
			for (int i = mid + 1; i < children.length; i++) {
				sibling.addChild(children[i]);
				children[i] = null;
			}
			numChild = mid + 1;
			keySize = mid;
			return key;
		}

		public int size() {
			return keySize;
		}

		public void addChild(int index, Node node) {
			for (int i = children.length - 1; i > index; i--) {
				children[i] = children[i - 1];
			}
			children[index] = node;
			numChild++;
		}

		public void addKey(int index, int key) {
			for (int i = keys.length - 1; i > index; i--) {
				keys[i] = keys[i - 1];
			}
			keys[index] = key;
			keySize++;
		}

		public void setKey(int index, int key) {
			keys[index] = key;
			keySize++;
		}

		public int getKey(int index) {
			return keys[index];
		}

		public Node getChild(int index) {
			return children[index];
		}

		public void add(int key) {
			int index = keySize;
			for (int i = 0; i < size(); i++) {
				if (keys[i] != null && key == keys[i]) {
					keys[i] = key;
					return;
				}
				if (keys[i] == null || key < keys[i]) {
					index = i;
					break;
				}
			}
			for (int y = keys.length - 1; y > index; y--) {
				keys[y] = keys[y - 1];
			}
			keys[index] = key;
			keySize++;
		}

		public void addChild(Node child) {
			children[numChild] = child;
			numChild++;
		}

		public String toString() {
			return "Internal Node with keys from " + keys[0] + " through to "
					+ keys[keySize - 1];
		}
	}

	private static class LeafNode implements Node {

		private TreeMap<Integer, String> pairs = new TreeMap<Integer, String>();
		private Node next;

		/** root node constructor */
		public LeafNode(int k, String v) {
			pairs.put(k, v);
		}

		public LeafNode() {
			// empty node
		}

		@Override
		public String find(int key) {
			return pairs.get(key);
		}

		private void addPair(Integer k, String v) {
			pairs.put(k, v);
		}

		private String remove(Integer k) {
			return pairs.remove(k);
		}

		@Override
		public int size() {
			return pairs.size();
		}

		private Node getNext() {
			return next;
		}

		private void setNext(Node next) {
			this.next = next;
		}

		@Override
		public Tuple<Integer, Node> add(int k, String v) {
			if (size() < MAXLEAFKEYS) {
				pairs.put(k, v);
				return null;
			} else {
				return split(k, v);
			}
		}

		private Tuple<Integer, Node> split(int key, String value) {
			/* insert key and value into correct place */
			pairs.put(key, value);
			/* create a new sibling */
			LeafNode sibling = new LeafNode();
			/* move keys from mid to size out of node into sibling */
			int mid = (size() + 1) / 2;
			int count = 0;
			List<Integer> keysToRemove = new ArrayList<Integer>();
			for (Map.Entry<Integer, String> entry : pairs.entrySet()) {
				if (count < mid) {
					count++;
				} else {
					Integer k = entry.getKey();
					String v = entry.getValue();
					sibling.addPair(k, v);
					keysToRemove.add(k);
				}
			}
			for (Integer delete : keysToRemove) {
				remove(delete);
			}
			sibling.setNext(getNext());
			setNext(sibling);
			return new Tuple<Integer, Node>(sibling.pairs.firstKey(), sibling);
		}

		@Override
		public String toString() {
			return pairs.toString();
		}
	}

	/** Interface for a node of int to string */
	private interface Node {
		public String find(int key);

		public int size();

		public Tuple<Integer, Node> add(int k, String v);
	}
}
