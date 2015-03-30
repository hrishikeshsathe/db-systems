package edu.buffalo.cse562.operators;

import java.sql.SQLException;

import net.sf.jsqlparser.expression.BooleanValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import edu.buffalo.cse562.utility.Evaluator;
import edu.buffalo.cse562.utility.Schema;
import edu.buffalo.cse562.utility.Tuple;
import edu.buffalo.cse562.utility.Utility;

public class SelectionOperator implements Operator {
	
	private Operator leftChild;
	private Operator parent;
	Schema schema;
	Expression where;
	
	public Expression getWhere() {
		return where;
	}

	public void setWhere(Expression where) {
		this.where = where;
	}

	Table table;
	boolean isHaving;

	
	public SelectionOperator(Operator operator, Table table, Expression where,
			boolean isHaving) {
		this.leftChild = operator;
		this.table = table;
		this.where = where;
		this.schema = Utility.tableSchemas.get(table.getAlias());
		this.isHaving = isHaving;

	}

	@Override
	public void reset() {
		leftChild.reset();
	}

	@Override
	public Tuple readOneTuple() {
		Tuple tuple = null;
		tuple = leftChild.readOneTuple();
		boolean tupleSatisfiesCondition;
		
		if(tuple == null)
			return null;
		while(tuple != null){
			Evaluator evaluator = new Evaluator(schema, tuple, isHaving);
			try{
				tupleSatisfiesCondition = ((BooleanValue)evaluator.eval(where)).getValue();
				if(tupleSatisfiesCondition)
					return tuple;
				else
					tuple = leftChild.readOneTuple();
			}catch(SQLException e){
				System.out.println("Exception occured in SelectionOperator.readOneTuple()");
			}
		}//end while
		return tuple;
	}//end of readOneTuple

	@Override
	public Table getTable() {
		return table;
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
