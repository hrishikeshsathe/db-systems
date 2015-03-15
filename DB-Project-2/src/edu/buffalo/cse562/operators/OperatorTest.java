package edu.buffalo.cse562.operators;

import java.util.ArrayList;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import edu.buffalo.cse562.utility.Tuple;


public class OperatorTest {

	static boolean isAggregate;


	/**
	 * Accepts an operator and dumps it to System.out
	 * @param op
	 */
	public static void dump(Operator op){
		Tuple tuple = op.readOneTuple();
		while(tuple != null){
			if(!tuple.isEmptyRecord())
				System.out.println(tuple.toString());
			tuple = op.readOneTuple();
		}
	}

	/**
	 * Accepts multiple parameters and returns an operator to be dumped.
	 * @param operator - 
	 * @param table - Table object containing name
	 * @param expression - where condition
	 * @param selectItems - items for projection
	 * @param joins - tables to be joined
	 * @param groupByColumnReferences - columns for group by clause
	 * @param having - having condition for group by
	 * @param allCol - if it's * or not
	 * @param limit - limit on number of tuples to dump
	 * @return
	 */
	public static Operator executeSelect(Operator oper, Table table,
			Expression condition, ArrayList<SelectExpressionItem> projectItems, ArrayList<Join> joins,
			ArrayList<Expression> groupByColumns, Expression having,
			boolean allCol, Limit limit, Distinct distinct) {

		isAggregate = false;
		Operator operator = oper;

		if(!allCol){
			for(int i=0; i<projectItems.size(); i++){
				if(projectItems.get(i).getExpression() instanceof Function){
					isAggregate = true;
					break;
				}				
			}
		}
		if(isAggregate){
			operator = new GroupByOperator(operator, table, groupByColumns, projectItems, false);
		}

		if(condition != null){
			operator = new SelectionOperator(operator, table, condition);
		}
		
		if(distinct != null && groupByColumns==null){
			operator = new GroupByOperator(operator, table, null, projectItems, true);
		}
		
		if(groupByColumns != null || isAggregate)
			operator = new ProjectOperator(operator, table, projectItems, allCol, true);
		else
			operator = new ProjectOperator(operator, table, projectItems, allCol, false);
		return operator;
	}
}
