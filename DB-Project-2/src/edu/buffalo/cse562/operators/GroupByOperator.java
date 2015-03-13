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

		Evaluator groupByEvaluator = null;
		Evaluator columnEvaluator = null;
		Tuple tuple = null;
		ArrayList<LeafValue> groupByTuple = null;
		int numberOfTuples=0;
		String keyGroupByColumns = null;

		allTuples = new HashMap<String, Tuple>();
		tuple = operator.readOneTuple();

		while(tuple != null){
			columnEvaluator = new Evaluator(tableSchema, tuple);
			keyGroupByColumns = getColumnValue(columnEvaluator, groupByColumns);
			if(!allTuples.containsKey(keyGroupByColumns))
				allTuples.put(keyGroupByColumns, new Tuple(projectItems.size()));

			groupByTuple = allTuples.get(keyGroupByColumns).getTuple();
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

		for(int i = 0; i < projectItems.size(); i++){
			try {
				if(projectItems.get(i).getExpression().toString().contains("AVG"))
					groupByTuple.set(i, new DoubleValue(groupByTuple.get(i).toDouble()/numberOfTuples));
			} catch (InvalidLeaf e) {
				e.printStackTrace();
			}// end of catch
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

