package main;

public class Tuple<K,V>{
	private final K key;
	private final V value;
	
	public Tuple(K key, V value){
		this.key = key;
		this.value = value;
	}
	
	public K key(){
		return key;
	}
	
	public V value(){
		return value;
	}
	
	public String toString(){
		return("( Key: " + this.key + " Value: " + this.value + ")");
	}
}
