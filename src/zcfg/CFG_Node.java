package zcfg;

import java.util.ArrayList;

public class CFG_Node {
	private String signature;
	private ArrayList<CFG_Node> next;
	public CFG_Node(String signature) {
		this.signature = signature;
		this.next = new ArrayList<>();
	}
	public void AddNext(CFG_Node cfg_Node) {
		this.next.add(cfg_Node);
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public ArrayList<CFG_Node> getNext() {
		return next;
	}
	public void setNext(ArrayList<CFG_Node> next) {
		this.next = next;
	}
}
