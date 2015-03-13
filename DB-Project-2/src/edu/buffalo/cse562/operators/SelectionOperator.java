package edu.buffalo.cse562.operators;

import java.sql.SQLException;
import java.util.HashMap;

import net.sf.jsqlparser.expression.BooleanValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import edu.buffalo.cse562.evaluate.Evaluator;
import edu.buffalo.cse562.utility.Tuple;
import edu.buffalo.cse562.utility.Utility;

public class SelectionOperator implements Operator {

	Operator operator;
	HashMap<String, Integer> tableSchema;
	Expression condition;
	Table table;

	public SelectionOperator(Operator operator, Table table,
			Expression condition) {
		this.table = table;
		this.operator = operator;
		this.condition = condition;
		this.tableSchema = Utility.tableSchemas.get(table.getAlias());
	}

	@Override
	public void reset() {
		operator.reset();
	}

	@Override
	public Tuple readOneTuple() {
		Tuple tuple = null;
		tuple = operator.readOneTuple();

		if(tuple == null)
			return null;
		else{
			Evaluator evaluator = new Evaluator(tableSchema, tuple);
			try{
				BooleanValue bool = (BooleanValue) evaluator.eval(condition);
				if(!bool.getValue())
					tuple = Utility.noResult;
			}catch(SQLException e){
				System.out.println("Exception occured in SelectionOperator.readOneTuple()");
			}
			return tuple;
		}//end of else
	}//end of readOneTuple

	@Override
	public Table getTable() {
		return table;
	}

}
