package edu.buffalo.cse562.operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import edu.buffalo.cse562.utility.Tuple;

public class OrderByOperator implements Operator{

	Operator operator;
	Table table;
	int index;
	HashMap<String, Integer> tableSchema;
	ArrayList<Tuple> sortedTuples;
	ArrayList<Tuple> allTuples;
	List<OrderByElement> orderByColumns;
	
	public OrderByOperator(Operator operator, HashMap<String, Integer> schema, Table table, List<OrderByElement> orderByCols) {
		this.operator = operator;
		this.tableSchema = schema;
		this.orderByColumns = orderByCols;
		this.table = table;
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
		// order 1 indicates descending, order 0 indicates ascending
		ArrayList<Integer> orders = new ArrayList<Integer>();
		
		for(OrderByElement e : orderByColumns){
			if(e.toString().contains(" DESC") || e.toString().contains(" desc")){
				indexes.add(tableSchema.get(e.toString().replaceAll(" (?i)DESC", "")));
				orders.add(1);
			}else{
				indexes.add(tableSchema.get(e.toString().split(" ")[0]));
				orders.add(0);
			}
		}
		Tuple.sortTupleList(sortedTuples, indexes, orders, table.getAlias());		
	}

}
