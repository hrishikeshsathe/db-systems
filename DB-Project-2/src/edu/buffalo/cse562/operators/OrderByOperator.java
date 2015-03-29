package edu.buffalo.cse562.operators;

import java.util.ArrayList;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import edu.buffalo.cse562.utility.Schema;
import edu.buffalo.cse562.utility.Tuple;
import edu.buffalo.cse562.utility.Utility;

public class OrderByOperator implements Operator {

	private Operator leftChild;
	private Operator parent;
	Table table;
	int index;
	Schema schema;
	ArrayList<Tuple> sortedTuples;
	ArrayList<Tuple> allTuples;
	ArrayList<OrderByElement> orderByColumns;

	public OrderByOperator(Operator operator, Table table,
			ArrayList<OrderByElement> orderByColumns) {
		this.leftChild = operator;
		this.schema = Utility.tableSchemas.get(table.getAlias());
		this.orderByColumns = orderByColumns;
		this.table = table;
		index = -1;
	}

	@Override
	public void reset() {
		leftChild.reset();
		index = -1;
	}

	@Override
	public Tuple readOneTuple() {
		if(sortedTuples == null)
			generateTuple();
		index++;
		if(index < sortedTuples.size())
			return sortedTuples.get(index);

		return null;
	}

	private void generateTuple() {
		Tuple tuple = null;
		tuple = leftChild.readOneTuple();
		sortedTuples = new ArrayList<Tuple>();

		while(tuple != null){
			sortedTuples.add(tuple);
			tuple = leftChild.readOneTuple();
		}

		ArrayList<Integer> indexes = new ArrayList<Integer>();
		// order 1 indicates descending, order 0 indicates ascending
		ArrayList<Integer> orders = new ArrayList<Integer>();

		for(OrderByElement e : orderByColumns){
			if(e.toString().contains(" DESC") || e.toString().contains(" desc")){
				indexes.add(schema.getColumns().get(e.toString().replaceAll(" (?i)DESC", "")));
				orders.add(1);
			}else{
				indexes.add(schema.getColumns().get(e.toString().split(" ")[0]));
				orders.add(0);
			}
		}
		Tuple.sortTupleList(sortedTuples, indexes, orders, table.getAlias());
	}

	@Override
	public Table getTable() {
		return this.table;
	}

	@Override
	public Operator getLeftChild() {
		return this.leftChild;
	}

	@Override
	public Operator getRightChild() {
		return null;
	}

	@Override
	public Operator getParent() {
		return this.parent;
	}

	@Override
	public void setLeftChild(Operator leftChild) {
		this.leftChild = leftChild;
	}

	@Override
	public void setRightChild(Operator rightChild) {
		//do nothing
	}

	@Override
	public void setParent(Operator parent) {
		this.parent = parent;
	}

}
