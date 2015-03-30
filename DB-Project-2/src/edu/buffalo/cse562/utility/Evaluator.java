package edu.buffalo.cse562.utility;

import java.sql.SQLException;
import java.util.HashMap;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Column;
import edu.buffalo.cse562.Eval;
import edu.buffalo.cse562.utility.AggregateFunctions;
import edu.buffalo.cse562.utility.Tuple;

public class Evaluator extends Eval{

	private Schema schema;
	private Tuple tuple;
	private LeafValue column;
	private boolean isHaving;

	/**
	 * Constructor for project
	 * @param schema2
	 * @param tuple
	 */
	public Evaluator(Schema schema, Tuple tuple, boolean isHaving)
	{
		this.schema = schema;
		this.tuple = tuple;
		this.isHaving = isHaving;
	}


	/**
	 * Constructor for group by
	 * @param tableSchema
	 * @param tuple
	 * @param column
	 */
	public Evaluator(Schema schema, Tuple tuple,
			LeafValue column) {
		this.schema = schema;
		this.tuple = tuple;
		this.column = column;
	}


	/**
	 * returns LeafValue corresponding to a tuple given a column.
	 */
	public LeafValue eval(Column c) throws SQLException {

		HashMap<String, Integer> columns = schema.getColumns();
		String columnName = schema.getTable().getAlias() + StringUtility.DOT + c.getColumnName();

		//If schema contains original column name - A
		if(columns.containsKey(c.getColumnName()))
			return tuple.get(columns.get(c.getColumnName()));
		//if schema contains whole column name - R.A
		else if(columns.containsKey(c.getWholeColumnName()))
			return tuple.get(columns.get(c.getWholeColumnName()));
		else if(columns.containsKey(columnName)){
			return tuple.get(columns.get(columnName));
		}
		else if(c.getTable() != null){
			if(schema.getColumns().containsKey(c.getTable().getName().toUpperCase() + StringUtility.DOT + c.getColumnName())){
				return tuple.get(schema.getColumns().get(c.getTable().getName().toUpperCase() + StringUtility.DOT + c.getColumnName()));
			}
		}
		return null;
	}//end of eval


	/**
	 * Evaluates a function - SUM, MIN, MAX, AVG, COUNT and returns a LeafValue
	 */
	public LeafValue eval(Function function) throws SQLException{
		
		String functionName = function.getName().toLowerCase();
		if(isHaving){
			return eval(new Column(null, function.toString()));
		}
		else if(functionName.contains(StringUtility.DATE3)){
			return new DateValue(function.getParameters().getExpressions().get(0).toString());
		}
		else{
			if(functionName.contains(StringUtility.COUNT3))
				return AggregateFunctions.getCount(column);

			LeafValue functionParameter = eval((Expression) function.getParameters().getExpressions().get(0));

			if(functionName.contains(StringUtility.SUM3) || 
					functionName.contains(StringUtility.AVG3))
				return AggregateFunctions.calculateSum(functionParameter, column);

			else if(functionName.contains(StringUtility.MIN3))
				return AggregateFunctions.getMinimum(functionParameter, column);

			else if(functionName.contains(StringUtility.MAX3))
				return AggregateFunctions.getMaximum(functionParameter, column);
		}
		return null;
	}
}
