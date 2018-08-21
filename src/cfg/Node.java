package cfg;

import java.util.HashSet;
import java.util.Set;

import soot.Unit;

public class Node {
	
	public Unit unit;
	
	public Set<Node> precursorNodes=new HashSet<>();
	public Set<Node> successorNodes=new HashSet<>();
	
	public Node(Unit unit){
		this.unit=unit;
	}
	
	

}
