package edu.buffalo.cse562.operators;

import edu.buffalo.cse562.utility.Tuple;
import net.sf.jsqlparser.schema.Table;


public interface Operator {

	public void reset();
	public Tuple readOneTuple();
	public Table getTable();
	public Operator getLeftChild();
	public Operator getRightChild();
	public Operator getParent();
	public void setLeftChild(Operator leftChild);
	public void setRightChild(Operator rightChild);
	public void setParent(Operator parent);
	
}
