package edu.buffalo.cse562.evaluate;

import java.sql.SQLException;
import java.util.HashMap;

import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Column;
import edu.buffalo.cse562.Eval;
import edu.buffalo.cse562.utility.AggregateFunctions;
import edu.buffalo.cse562.utility.Tuple;

public class Evaluator extends Eval{

	private HashMap<String, Integer> schema;
	private Tuple tuple;
	private LeafValue column;

	
	/**
	 * Constructor for project
	 * @param tableSchema
	 * @param tuple
	 */
	public Evaluator(HashMap<String, Integer> tableSchema, Tuple tuple)
	{
		this.schema = tableSchema;
		this.tuple = tuple;
	}
	
	
	/**
	 * Constructor for group by
	 * @param tableSchema
	 * @param tuple
	 * @param column
	 */
	public Evaluator(HashMap<String, Integer> tableSchema, Tuple tuple,
			LeafValue column) {
		this.schema = tableSchema;
		this.tuple = tuple;
		this.column = column;
	}

	
	/**
	 * returns LeafValue corresponding to a tuple given a column.
	 */
	public LeafValue eval(Column c) throws SQLException {
		//If schema contains original column name - A
		if(schema.containsKey(c.getColumnName())){
			return tuple.get(schema.get(c.getColumnName()));
		}
		//if schema contains whole column name - R.A
		else if(schema.containsKey(c.getWholeColumnName())){
			return tuple.get(schema.get(c.getWholeColumnName()));
		}
		//Search after split on '.'
		else {
			for(String column: schema.keySet()){
				if(c.getColumnName().equals(column.split("\\.")[1])){
					return tuple.get(schema.get(column));
				}
			}
		}//end else
		return null;
	}//end of eval
	
	
	/**
	 * Evaluates a function - SUM, MIN, MAX, AVG, COUNT and returns a LeafValue
	 */
	public LeafValue eval(Function function) throws SQLException{
		
		if(function.getName().contains("COUNT")){
			return AggregateFunctions.getCount(column);
		}
		
		LeafValue functionParameter = eval((Column) function.getParameters().getExpressions().get(0));

		if(function.getName().contains("SUM") || function.getName().contains("AVG")){
			return AggregateFunctions.calculateSum(functionParameter, column);
		}
		else if(function.getName().contains("MIN")){
			return AggregateFunctions.getMinimum(functionParameter, column);
		}
		else if(function.getName().contains("MAX")){
			return AggregateFunctions.getMaximum(functionParameter, column);
		}
		return null;
	}
}
