package edu.buffalo.cse562.operators;

import java.io.File;
import java.util.ArrayList;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import edu.buffalo.cse562.utility.Utility;

public class TreeCreator {

	/**
	 * Accepts multiple parameters and returns an operator to be dumped.
	 * @param operator - 
	 * @param table - Table object containing name
	 * @param expression - where condition
	 * @param projectItems - items for projection
	 * @param joins - tables to be joined
	 * @param groupByColumnReferences - columns for group by clause
	 * @param having - having condition for group by
	 * @param allCol - if it's * or not
	 * @param limit - limit on number of tuples to dump
	 * * @param distinct - boolean value to indicate if select items are distinct
	 * @return
	 */
	public static Operator createTree(Operator readOperator, Table table,
			Expression where, ArrayList<SelectExpressionItem> projectItems,
			ArrayList<Join> joins,
			ArrayList<Expression> groupByColumns, Expression having,
			Boolean allCol, Limit limit, Distinct distinct,
			ArrayList<OrderByElement> orderByColumns) {

		boolean isAggregate = false;
		boolean isGroupBy = false;
		Operator operator = readOperator;

		if(joins != null){
			for(int joinIndex = 0; joinIndex < joins.size(); joinIndex++){
				Table rightTable = (Table) joins.get(joinIndex).getRightItem();
				rightTable.setName(rightTable.getName().toUpperCase());
				Utility.checkAndSetTableAlias(rightTable);
				String fileName = Utility.dataDir.toString() + File.separator + rightTable.getName().toString() + ".dat";
				Operator rightOperator = new ReadOperator(new File(fileName), rightTable);
				Operator joinOperator = new JoinOperator(operator, rightOperator);
				Utility.setParentAndChild(joinOperator, operator, rightOperator);
				operator = joinOperator;
				table = operator.getTable();
			}//end of loop
		}//end if

		if(!allCol){
			for(int projectItemIndex = 0; projectItemIndex < projectItems.size(); projectItemIndex++){
				if(projectItems.get(projectItemIndex).getExpression() instanceof Function){
					isAggregate = true;
					break;
				}//end if
			}//end loop
		}//end if

		if(where != null){
			Operator selectionOperator = new SelectionOperator(operator, table, where, false);
			Utility.setParentAndChild(selectionOperator, operator, null);
			operator = selectionOperator;
		}//end if

		if(isAggregate){
			Operator groupByOperator = new GroupByOperator(operator, table, groupByColumns, projectItems, false, null);
			Utility.setParentAndChild(groupByOperator, operator, null);
			operator = groupByOperator;
			table = operator.getTable();
		}//end if

		if(having != null){
			Operator selectionOperator = new SelectionOperator(operator, table, having, true);
			Utility.setParentAndChild(selectionOperator, operator, null);
			operator = selectionOperator;
		}//end if

		if(distinct != null && groupByColumns == null){
			Operator groupByOperator = new GroupByOperator(operator, table, null, projectItems, true, null);
			Utility.setParentAndChild(groupByOperator, operator, null);
			operator = groupByOperator;
			table = operator.getTable();
		}//end if

		if(groupByColumns != null || isAggregate)
			isGroupBy = true;

		Operator projectOperator = new ProjectOperator(operator, table, projectItems, allCol, isGroupBy);
		Utility.setParentAndChild(projectOperator, operator, null);
		operator = projectOperator;
		table = operator.getTable();
		
		if(orderByColumns != null){
			Operator orderByOperator = new OrderByOperator(operator, table, orderByColumns);
			Utility.setParentAndChild(orderByOperator, operator, null);
			operator = orderByOperator;
		}
		return operator;
	}

}

