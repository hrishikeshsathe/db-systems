package edu.buffalo.cse562.operators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import edu.buffalo.cse562.utility.Tuple;
import edu.buffalo.cse562.utility.Utility;

public class OrderByOperator implements Operator{

	Operator operator;
	Table table;
	int index;
	HashMap<String, Integer> tableSchema;
	ArrayList<Tuple> sortedTuples;
	ArrayList<Tuple> allTuples;
	List<OrderByElement> orderByColumns;
	
	public OrderByOperator(Operator operator, Table table, List<OrderByElement> orderByCols) {
		this.operator = operator;
		this.table = table;		
		this.tableSchema = Utility.tableSchemas.get(table.getAlias());
		this.orderByColumns = orderByCols;
		generateTuple();
		index = -1;
		
//		allTuples = sortedTuples;
		
	}

	
	@Override
	public void reset() {
		operator.reset();
		index = -1;
	}

	@Override
	public Tuple readOneTuple() {
		index++;
		if(index < sortedTuples.size())
			return sortedTuples.get(index);
		else 
			return null;
	}

	@Override
	public Table getTable() {
		return null;
	}
	
	private void generateTuple(){
		Tuple tuple = null;
		tuple = operator.readOneTuple();
		sortedTuples = new ArrayList<Tuple>();
		
		while(tuple != null){
			sortedTuples.add(tuple);
			tuple = operator.readOneTuple();
		}
		
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		for(OrderByElement e : orderByColumns){
			indexes.add(tableSchema.get(e.toString()));
		}
		Tuple.sortTupleList(sortedTuples, indexes, table.getAlias());		
	}

}
