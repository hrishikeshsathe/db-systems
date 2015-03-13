package edu.buffalo.cse562.operators;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.expression.LeafValue.InvalidLeaf;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import edu.buffalo.cse562.evaluate.Evaluator;
import edu.buffalo.cse562.utility.Tuple;
import edu.buffalo.cse562.utility.Utility;

public class GroupByOperator implements Operator {

	Operator operator;
	Table table;
	ArrayList<Expression> groupByColumns;
	ArrayList<SelectExpressionItem> projectItems;
	HashMap<String, Integer> tableSchema;
	HashMap<String, Tuple> allTuples;
	
	public GroupByOperator(Operator operator, Table table,
			ArrayList<Expression> groupByColumns, ArrayList<SelectExpressionItem> projectItems) {
		this.operator = operator;
		this.table = table;
		this.groupByColumns = groupByColumns;
		this.projectItems = projectItems;
		this.tableSchema = Utility.tableSchemas.get(table.getAlias());
		generateTuple();
	}

	@Override
	public void reset() {
		operator.reset();
	}

	@Override
	public Tuple readOneTuple() {
		return allTuples.get(null);
	}

	@Override
	public Table getTable() {
		return null;
	}

	private void generateTuple(){
		
		Evaluator evaluator = null;
		Tuple tuple = null;
		ArrayList<LeafValue> groupByTuple = new ArrayList<LeafValue>();
		int numberOfTuples=0;
		for(int i = 0; i < projectItems.size(); i++){
			if(projectItems.get(i).getExpression().toString().contains("MIN"))
				groupByTuple.add(new DoubleValue(Double.MAX_VALUE));
			else if(projectItems.get(i).getExpression().toString().contains("MAX"))
				groupByTuple.add(new DoubleValue(Double.MIN_VALUE));
			else
				groupByTuple.add(new DoubleValue("0"));
		}
			
		allTuples = new HashMap<String, Tuple>();
		tuple = operator.readOneTuple();
		
		while(tuple != null){
			numberOfTuples++;
			for(int i = 0; i < projectItems.size(); i++){
				try {
						evaluator = new Evaluator(tableSchema, tuple, groupByTuple.get(i));
						groupByTuple.set(i, evaluator.eval(projectItems.get(i).getExpression()));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//end catch
				
			}//end for
			tuple = operator.readOneTuple();
		}//end while
		for(int i = 0; i < projectItems.size(); i++){
			try {
				if(projectItems.get(i).getExpression().toString().contains("AVG"))
					groupByTuple.set(i, new DoubleValue(groupByTuple.get(i).toDouble()/numberOfTuples));
			} catch (InvalidLeaf e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		allTuples.put(null, new Tuple(groupByTuple));
	}
}

