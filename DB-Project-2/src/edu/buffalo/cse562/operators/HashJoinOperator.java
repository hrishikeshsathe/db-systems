package edu.buffalo.cse562.operators;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Table;
import edu.buffalo.cse562.evaluate.Evaluator;
import edu.buffalo.cse562.utility.Tuple;
import edu.buffalo.cse562.utility.Utility;

public class HashJoinOperator implements Operator{

	Operator leftOperator;
	Operator rightOperator;
	Expression leftColumn;
	Expression rightColumn;
	ArrayList<Tuple> leftTuples;
	Tuple rightTuple;
	static HashMap<String, Integer> leftTableSchema;
	static HashMap<String, Integer> rightTableSchema;
	Table table;
	static private HashMap<String,ArrayList<Tuple>> hashIndex;
	static int index = 0;

	public HashJoinOperator(Operator leftOperator,
			Operator rightOperator,Expression leftColumn, Expression rightColumn) {
		this.leftOperator=leftOperator;
		this.rightOperator=rightOperator;
		this.leftColumn=leftColumn;
		this.rightColumn=rightColumn;
		createNewJoinSchema(leftOperator.getTable(), rightOperator.getTable());

		rightTableSchema=Utility.tableSchemas.get(rightOperator.getTable().getName());
		leftTableSchema = Utility.tableSchemas.get(leftOperator.getTable().getName());

	}
	private void populateHashIndex() {

		hashIndex=new HashMap<String, ArrayList<Tuple>>();
		Tuple leftTuple;
		//initial condition
		leftTuple = leftOperator.readOneTuple();
		String key;

		while(leftTuple != null)
		{
			if(!leftTuple.isEmptyRecord()){
				Evaluator evaluator = new Evaluator(leftTableSchema, leftTuple, false);
				try{
					LeafValue columnValue = (LeafValue) evaluator.eval(leftColumn);
					key=columnValue.toString();
					ArrayList<Tuple> tuples;
					if(!hashIndex.containsKey(key))
					{
						tuples=new ArrayList<Tuple>();
						tuples.add(leftTuple);
						hashIndex.put(columnValue.toString(), tuples );
					}
					else
					{
						tuples=hashIndex.get(key);
						tuples.add(leftTuple);
						hashIndex.put(key, tuples);
					}
				}catch(SQLException e){
					System.out.println("Exception occured in SelectionOperator.readOneTuple()");
				}
				leftTuple = leftOperator.readOneTuple();
			}
		}//end of else


	}
	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public Tuple readOneTuple() {

		if(hashIndex == null)
			populateHashIndex();
		if(leftTuples == null){
			rightTuple=rightOperator.readOneTuple();
			if(rightTuple!=null){
				Evaluator evaluator = new Evaluator(rightTableSchema, rightTuple, false);
				try {
					LeafValue columnValue = (LeafValue) evaluator.eval(rightColumn);
					String key=columnValue.toString();
					if(hashIndex.containsKey(key))
					{
						leftTuples = hashIndex.get(key);
						return joinTuple(leftTuples.get(index),rightTuple);
					}
				} catch (SQLException e) {
					System.out.println("Exception occured in HashJoinOperator.readOneTuple()");
				}
			}
		}else{
			index++;
			if(index < leftTuples.size())
				return joinTuple(leftTuples.get(index),rightTuple);
			else{
				index = 0;
				leftTuples = null;
				return Utility.noResult;
			}
		}
		
		return null;

	}

	private Tuple joinTuple(Tuple leftTuple, Tuple rightTuple) {

		ArrayList<LeafValue> newTuple = new ArrayList<LeafValue>();
		newTuple.addAll(leftTuple.getTuple());
		newTuple.addAll(rightTuple.getTuple());
		return new Tuple(newTuple);
	}
	@Override
	public Table getTable() {
		// TODO Auto-generated method stub
		return table;
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
