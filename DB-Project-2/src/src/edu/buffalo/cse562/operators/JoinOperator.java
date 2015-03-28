package edu.buffalo.cse562.operators;

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Table;
import edu.buffalo.cse562.utility.Tuple;
import edu.buffalo.cse562.utility.Utility;

public class JoinOperator implements Operator{

	Operator leftOperator;
	Operator rightOperator;
	Table table;
	Tuple leftTuple;
	Tuple rightTuple;

	public JoinOperator(Operator leftOperator, Operator rightOperator){
		this.leftOperator = leftOperator;
		this.rightOperator = rightOperator;
		createNewJoinSchema(leftOperator.getTable(), rightOperator.getTable());
	}

	public void reset(){
		leftOperator.reset();
		rightOperator.reset();
	}

	public Tuple readOneTuple(){

		if(leftTuple == null) //initial condition
			leftTuple = leftOperator.readOneTuple();

		if(leftTuple == null) //if leftTuple is null then reached end of file. Return null
			return null;
		else{
			rightTuple = rightOperator.readOneTuple();
			if(rightTuple == null){
				rightOperator.reset();
				rightTuple = rightOperator.readOneTuple();
				leftTuple = leftOperator.readOneTuple();
				if(leftTuple == null)
					return null;
			}
		}
		return joinTuple(leftTuple, rightTuple);
	}

	private Tuple joinTuple(Tuple leftTuple, Tuple rightTuple) {

		ArrayList<LeafValue> newTuple = new ArrayList<LeafValue>();
		newTuple.addAll(leftTuple.getTuple());
		newTuple.addAll(rightTuple.getTuple());
		return new Tuple(newTuple);
	}

	public Table getTable(){
		return this.table;
	}

	private void createNewJoinSchema(Table leftTable, Table rightTable) {
		this.table = new Table();
		this.table.setName(leftTable.getAlias() + " JOIN " + rightTable.getAlias());
		this.table.setAlias(leftTable.getAlias() + " JOIN " + rightTable.getAlias());
		
		HashMap<String, Integer> newSchema = new HashMap<String, Integer>();
		HashMap<String, Integer> leftTableSchema = Utility.tableSchemas.get(leftTable.getName());
		HashMap<String, Integer> rightTableSchema = Utility.tableSchemas.get(rightTable.getName());
		
		for(String column: leftTableSchema.keySet()){
			if(column.contains("."))
				newSchema.put(column, leftTableSchema.get(column));
			else
				newSchema.put(leftTable.getAlias() + "." + column, leftTableSchema.get(column));
		}
		
		for(String column: rightTableSchema.keySet()){
			if(column.contains("."))
				newSchema.put(column, rightTableSchema.get(column) + leftTableSchema.size());
			else
				newSchema.put(rightTable.getAlias() + "." + column, rightTableSchema.get(column) + leftTableSchema.size());
		}
		
		Utility.tableSchemas.put(table.getName(), newSchema);
	}
}
