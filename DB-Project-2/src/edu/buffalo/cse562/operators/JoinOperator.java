package edu.buffalo.cse562.operators;

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Table;
import edu.buffalo.cse562.utility.Schema;
import edu.buffalo.cse562.utility.StringUtility;
import edu.buffalo.cse562.utility.Tuple;
import edu.buffalo.cse562.utility.Utility;

public class JoinOperator implements Operator {

	private Operator leftChild;
	private Operator rightChild;
	private Operator parent;
	Table table;
	Tuple leftTuple;
	Tuple rightTuple;

	public JoinOperator(Operator leftOperator, Operator rightOperator) {
		this.leftChild = leftOperator;
		this.rightChild = rightOperator;
		createNewJoinSchema(leftOperator.getTable(), rightOperator.getTable());
	}

	@Override
	public void reset() {
		leftChild.reset();
		rightChild.reset();
	}

	@Override
	public Tuple readOneTuple() {

		if(leftTuple == null) //initial condition
			leftTuple = leftChild.readOneTuple();
		if(leftTuple == null) //if leftTuple is null then reached end of file. Return null
			return null;
		else{
			rightTuple = rightChild.readOneTuple();
			if(rightTuple == null){
				rightChild.reset();
				rightTuple = rightChild.readOneTuple();
				leftTuple = leftChild.readOneTuple();
				if(leftTuple == null)
					return null;
			}
		}
		return joinTuple(leftTuple, rightTuple);	
	}

	/**
	 * Take two tuples and join them and return the new tuple
	 * @param leftTuple
	 * @param rightTuple
	 * @return
	 */
	private Tuple joinTuple(Tuple leftTuple, Tuple rightTuple) {

		ArrayList<LeafValue> newTuple = new ArrayList<LeafValue>();
		newTuple.addAll(leftTuple.getTuple());
		newTuple.addAll(rightTuple.getTuple());
		return new Tuple(newTuple);
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
		return this.rightChild;
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
		this.rightChild = rightChild;
	}

	@Override
	public void setParent(Operator parent) {
		this.parent = parent;
	}

	private void createNewJoinSchema(Table leftTable, Table rightTable) {
		this.table = new Table();
		String newTableName = leftTable.getAlias() + StringUtility.JOIN + rightTable.getAlias();
		this.table.setName(newTableName);
		this.table.setAlias(newTableName);
		Schema schema = new Schema(table);
		HashMap<String, Integer> newSchema = new HashMap<String, Integer>();
		HashMap<String, Integer> leftTableSchema = Utility.tableSchemas.get(leftTable.getName()).getColumns();
		HashMap<String, Integer> rightTableSchema = Utility.tableSchemas.get(rightTable.getName()).getColumns();

		for(String column: leftTableSchema.keySet()){
			if(column.contains(StringUtility.DOT))
				newSchema.put(column, leftTableSchema.get(column));
			else
				newSchema.put(leftTable.getAlias() + StringUtility.DOT + column, leftTableSchema.get(column));
		}

		for(String column: rightTableSchema.keySet()){
			if(column.contains(StringUtility.DOT))
				newSchema.put(column, rightTableSchema.get(column) + leftTableSchema.size());
			else
				newSchema.put(rightTable.getAlias() + StringUtility.DOT + column, rightTableSchema.get(column) + leftTableSchema.size());
		}
		schema.setColumns(newSchema);
		Utility.tableSchemas.put(table.getName(), schema);
	}
}
