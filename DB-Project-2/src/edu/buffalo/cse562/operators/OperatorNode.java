package edu.buffalo.cse562.operators;

public class OperatorNode {
	
	Operator op;
	OperatorNode left;
	OperatorNode right;
	OperatorNode parent;
	
	public Operator getOp() {
		return op;
	}
	public void setOp(Operator op) {
		this.op = op;
	}
	public OperatorNode getLeft() {
		return left;
	}
	public void setLeft(OperatorNode left) {
		this.left = left;
	}
	public OperatorNode getRight() {
		return right;
	}
	public void setRight(OperatorNode right) {
		this.right = right;
	}
	public OperatorNode getParent() {
		return parent;
	}
	public void setParent(OperatorNode parent) {
		this.parent = parent;
	}

}
