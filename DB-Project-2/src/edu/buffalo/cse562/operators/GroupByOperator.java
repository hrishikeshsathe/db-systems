package edu.buffalo.cse562.operators;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
	HashMap<String, Tuple> groupedTuples;
	ArrayList<Tuple> allTuples;
	int index;
	HashMap<String, Integer> groupedTupleCount = new HashMap<String, Integer>();

	public GroupByOperator(Operator operator, Table table,
			ArrayList<Expression> groupByColumns, ArrayList<SelectExpressionItem> projectItems) {
		this.operator = operator;
		this.table = table;
		this.groupByColumns = groupByColumns;
		this.projectItems = projectItems;
		this.tableSchema = Utility.tableSchemas.get(table.getAlias());
		generateTuple();
		index = -1;
		allTuples = new ArrayList<Tuple>(groupedTuples.values());
	}

	@Override
	public void reset() {
		operator.reset();
		index = -1;
	}

	@Override
	public Tuple readOneTuple() {
		index++;
		if(index < allTuples.size())
			return allTuples.get(index);
		else 
			return null;
	}

	@Override
	public Table getTable() {
		return null;
	}

	private void generateTuple(){

		Evaluator groupByEvaluator = null;
		Evaluator columnEvaluator = null;
		Tuple tuple = null;
		ArrayList<LeafValue> groupByTuple = null;
		int numberOfTuples=0;
		String keyGroupByColumns = null;

		groupedTuples = new HashMap<String, Tuple>();
		tuple = operator.readOneTuple();

		while(tuple != null){
			if(groupByColumns != null){
				columnEvaluator = new Evaluator(tableSchema, tuple);
				keyGroupByColumns = getColumnValue(columnEvaluator, groupByColumns);
				if(!groupedTuples.containsKey(keyGroupByColumns))
				{
					groupedTuples.put(keyGroupByColumns, new Tuple(projectItems.size()));				
					groupedTupleCount.put(keyGroupByColumns, 1);
				}
				else
				{
					groupedTupleCount.put(keyGroupByColumns, groupedTupleCount.get(keyGroupByColumns)+1);
				}
			}
			else{
				if(!groupedTuples.containsKey(null))
				{	
					groupedTuples.put(null, new Tuple(projectItems.size()));
					groupedTupleCount.put(null, 1);
				}
				else
				{
					groupedTupleCount.put(null, groupedTupleCount.get(null)+1);
				}
			}
			groupByTuple = groupedTuples.get(keyGroupByColumns).getTuple();
			for(int i = 0; i < projectItems.size(); i++){
				try {
					groupByEvaluator = new Evaluator(tableSchema, tuple, groupByTuple.get(i));
					groupByTuple.set(i, groupByEvaluator.eval(projectItems.get(i).getExpression()));
				} catch (SQLException e) {
					System.out.println("SQLException in generateTuple() - GroupByOperator");
				}//end catch
			}//end for
			tuple = operator.readOneTuple();
		}//end while

		Iterator<Entry<String, Tuple>> it= groupedTuples.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry e=it.next();
			for(int i=0;i<projectItems.size();i++)
			{
				if(projectItems.get(i).getExpression().toString().contains("AVG"))
				{
					String groupByKey=(String) e.getKey();
					Integer count=groupedTupleCount.get(groupByKey);
					ArrayList<LeafValue> groupedTuple = groupedTuples.get(groupByKey).getTuple();
					try {
						groupedTuple.set(i, new DoubleValue(groupedTuples.get(groupByKey).get(i).toDouble()/count));
					} catch (InvalidLeaf e1) {
						System.out.println("InvalidLeaf Exception while calculating average");
					}
					groupedTuples.put(groupByKey,new Tuple(groupedTuple));
				}
			}
		}
	}

	/**
	 * Return the group by columns as a String
	 * @param eval
	 * @param groupByColumns
	 * @return String
	 */
	private static String getColumnValue(Evaluator eval, ArrayList<Expression> groupByColumns){
		StringBuffer value = new StringBuffer();;
		try {
			for(int i = 0; i < groupByColumns.size(); i++){
				value.append(eval.eval(groupByColumns.get(i)).toString());
				value.append(",");
			}
		} catch (SQLException e) {
			System.out.println("SQLException in getColumnValue() - GroupByOperator");
		}
		return value.toString();
	}
}

