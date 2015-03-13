package edu.buffalo.cse562.evaluate;

import java.sql.SQLException;
import java.util.HashMap;

import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Column;
import edu.buffalo.cse562.Eval;
import edu.buffalo.cse562.utility.Tuple;

public class Evaluator extends Eval{

	private HashMap<String, Integer> schema;
	private Tuple tuple;

	public Evaluator(HashMap<String, Integer> table, Tuple tuple)
	{
		this.schema=table;
		this.tuple=tuple;
	}
	
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
}
