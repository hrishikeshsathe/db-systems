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
		//If schema contains original column name - A
		if(columns.containsKey(c.getColumnName()))
			return tuple.get(columns.get(c.getColumnName()));
		//if schema contains whole column name - R.A
		if(columns.containsKey(c.getWholeColumnName()))
			return tuple.get(columns.get(c.getWholeColumnName()));
		else if(c.getTable() != null){
			if(schema.getColumns().containsKey(c.getTable().getName().toUpperCase() + StringUtility.DOT + c.getColumnName())){
				return tuple.get(schema.getColumns().get(c.getTable().getName().toUpperCase() + StringUtility.DOT + c.getColumnName()));
			}
		}

		String tableName = schema.getTable().getAlias();
		String[] joinTables = tableName.split(StringUtility.SPACE);
		for(int i = 0; i < joinTables.length; i++){
			if(columns.containsKey(joinTables[i] + StringUtility.DOT + c.getColumnName()))
				return tuple.get(columns.get(joinTables[i] + StringUtility.DOT + c.getColumnName()));
		}
		return null;
	}//end of eval


	/**
	 * Evaluates a function - SUM, MIN, MAX, AVG, COUNT and returns a LeafValue
	 */
	public LeafValue eval(Function function) throws SQLException{

		if(isHaving){
			return eval(new Column(null, function.toString()));
		}
		else if(function.getName().contains(StringUtility.DATE1) || function.getName().contains(StringUtility.DATE2) || function.getName().contains(StringUtility.DATE3)){
			return new DateValue(function.getParameters().getExpressions().get(0).toString());
		}
		else{
			if(function.getName().contains(StringUtility.COUNT1) || function.getName().contains(StringUtility.COUNT2) || function.getName().contains(StringUtility.COUNT3))
				return AggregateFunctions.getCount(column);

			LeafValue functionParameter = eval((Expression) function.getParameters().getExpressions().get(0));

			if(function.getName().contains(StringUtility.SUM1) || function.getName().contains(StringUtility.SUM2) || function.getName().contains(StringUtility.SUM3) || 
					function.getName().contains(StringUtility.AVG1) || function.getName().contains(StringUtility.AVG2) || function.getName().contains(StringUtility.AVG3))
				return AggregateFunctions.calculateSum(functionParameter, column);

			else if(function.getName().contains(StringUtility.MIN1) || function.getName().contains(StringUtility.MIN2) || function.getName().contains(StringUtility.MIN3))
				return AggregateFunctions.getMinimum(functionParameter, column);

			else if(function.getName().contains(StringUtility.MAX1) || function.getName().contains(StringUtility.MAX2) || function.getName().contains(StringUtility.MAX3))
				return AggregateFunctions.getMaximum(functionParameter, column);
		}
		return null;
	}
}
