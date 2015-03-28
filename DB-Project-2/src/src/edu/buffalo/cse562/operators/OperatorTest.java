package edu.buffalo.cse562.operators;

import java.io.File;
import java.util.ArrayList;


import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.OrderByElement;
import edu.buffalo.cse562.utility.Tuple;
import edu.buffalo.cse562.utility.Utility;


public class OperatorTest {

	static boolean isAggregate;

	/**
	 * Accepts an operator and dumps it to System.out
	 * @param op
	 * @param limit 
	 */
	public static void dump(Operator op, Limit limit){
		Tuple tuple = op.readOneTuple();

		if(limit!=null)
		{
			long lim=limit.getRowCount();
			int counter=0;
			while(tuple != null && counter<lim){
				if(!tuple.isEmptyRecord())
					System.out.println(tuple.toString());
				counter++;
				tuple = op.readOneTuple();
			}
		}
		else
		{
			while(tuple != null){
				if(!tuple.isEmptyRecord())
					System.out.println(tuple.toString());
				tuple = op.readOneTuple();
			}
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
	 * * @param distinct - boolean value to indicate if select items are distinct
	 * @return
	 */
	public static Operator executeSelect(Operator oper, Table table,
			Expression condition, ArrayList<SelectExpressionItem> projectItems, ArrayList<Join> joins,
			ArrayList<Expression> groupByColumns, Expression having,
			boolean allCol, Limit limit, Distinct distinct, List<OrderByElement> orderByColumns) {

		isAggregate = false;
		Operator operator = oper;

		if(joins != null){
			for(int i = 0; i < joins.size(); i++){
				Table rightTable = (Table) joins.get(i).getRightItem();
				rightTable.setName(rightTable.getName().toUpperCase());
				Utility.checkAndSetTableAlias(rightTable);
				String tableFile = Utility.dataDir.toString() + File.separator + rightTable.getName().toString() + ".dat";
				Operator rightOperator = new ReadOperator(new File(tableFile), rightTable);
				operator = new JoinOperator(operator, rightOperator);
				table = operator.getTable();
			}
		}

		if(!allCol){
			for(int i=0; i<projectItems.size(); i++){
				if(projectItems.get(i).getExpression() instanceof Function){
					isAggregate = true;
					break;
				}				
			}
		}
		if(condition != null)
			operator = new SelectionOperator(operator, table, condition, false);

		if(isAggregate){
			operator = new GroupByOperator(operator, table, groupByColumns, projectItems, false, null);
		}

		if(having != null){
			if(operator instanceof GroupByOperator)
				operator = new SelectionOperator(operator, ((GroupByOperator) operator).getNewSchema(), having, true);
		}

		if(distinct != null && groupByColumns==null){
			operator = new GroupByOperator(operator, table, null, projectItems, true, null);
		}

		if(groupByColumns != null || isAggregate)
			operator = new ProjectOperator(operator, table, projectItems, allCol, true);
		else
			operator = new ProjectOperator(operator, table, projectItems, allCol, false);
		
		if(orderByColumns !=null){
			operator = new OrderByOperator(operator, ((ProjectOperator)operator).getProjectSchema(), table, orderByColumns);
		}
		return operator;
	}
}
