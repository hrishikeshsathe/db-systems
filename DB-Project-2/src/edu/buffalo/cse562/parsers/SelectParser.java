package edu.buffalo.cse562.parsers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import edu.buffalo.cse562.operators.JoinOperator;
import edu.buffalo.cse562.operators.Operator;
import edu.buffalo.cse562.operators.ReadOperator;
import edu.buffalo.cse562.operators.SelectionOperator;
import edu.buffalo.cse562.operators.TreeCreator;
import edu.buffalo.cse562.utility.Printer;
import edu.buffalo.cse562.utility.Schema;
import edu.buffalo.cse562.utility.StringUtility;
import edu.buffalo.cse562.utility.Utility;

public class SelectParser {

	/**
	 * Parse a select statement and execute it. Prints the result of the select query
	 * @param statement - statement of type SelectBody to be parsed
	 */
	public static void parseStatement(Statement statement) {
		SelectBody body = ((Select) statement).getSelectBody();
		if(body instanceof PlainSelect){
			Operator rootOperator = getOperator((PlainSelect) body);
			System.out.println("Free memory before optimization " + Runtime.getRuntime().freeMemory() / 1048576);
			optimizeTree(rootOperator, (PlainSelect) body);
			System.out.println("Free memory after optimization " + Runtime.getRuntime().freeMemory() / 1048576);
			Printer.print(rootOperator, ((PlainSelect) body).getLimit());
			System.gc();
			System.out.println("Free memory after execution" + Runtime.getRuntime().freeMemory() / 1048576);
		}//end if
	}//end parseStatement

	public static void optimizeTree(Operator rootOperator, PlainSelect body){
		List<Expression> whereExpressions = Utility.splitAndClauses(((PlainSelect) body).getWhere());
		Operator temp = rootOperator;
		while(!(temp instanceof SelectionOperator))
			temp = temp.getLeftChild();
		if(!(temp.getLeftChild() instanceof ReadOperator) && (temp.getLeftChild() instanceof JoinOperator)){
			for(Expression e: whereExpressions){
				if(Utility.isJoinCondition(e))
					Utility.optimizeJoin(temp.getLeftChild(), e, (Column)((BinaryExpression)e).getLeftExpression(), (Column)((BinaryExpression)e).getRightExpression());
				else{
					if(e instanceof Parenthesis){
						Utility.optimizeSelect(temp.getLeftChild(), ((Parenthesis) e).getExpression(), (Column) ((BinaryExpression)((OrExpression)((Parenthesis) e).getExpression()).getLeftExpression()).getLeftExpression());
					}
					else
						Utility.optimizeSelect(temp.getLeftChild(), e, (Column)((BinaryExpression)e).getLeftExpression());
				}
			}
			temp.getParent().setLeftChild(temp.getLeftChild());
			temp.getLeftChild().setParent(temp.getParent());
			temp = null;
			System.gc();
		}
	}

	/**
	 * Accept a PlainSelect object and return an operator. Operator can be dumped to print result.
	 * @param body - PlainSelect
	 * @return Operator
	 */
	private static Operator getOperator(PlainSelect body) {
		Table table = null;
		Operator operator = null;
		boolean allCol = false;

		//if there is a subQuery
		if(body.getFromItem() instanceof SubSelect){
			table = new Table();
			if(body.getFromItem().getAlias() == null){
				table.setName(StringUtility.SUBQUERY + Utility.subQueryCounter);
				table.setAlias(StringUtility.SUBQUERY + Utility.subQueryCounter);
				Utility.subQueryCounter++;
			}else{
				table.setName(body.getFromItem().getAlias());
				table.setAlias(body.getFromItem().getAlias());
			}
			operator = getOperator((PlainSelect) ((SubSelect) body.getFromItem()).getSelectBody());
			if(isAllColumns(body))
				allCol = true;
			createSchema(table, ((PlainSelect) ((SubSelect) body.getFromItem()).getSelectBody()), operator);
			operator = generateOperator(operator, table, body, allCol);
			return operator;
		}//end subQuery if
		else{
			table = (Table) body.getFromItem();
			table.setName(table.getName().toUpperCase());
			Utility.checkAndSetTableAlias(table);
			allCol = isAllColumns(body);
			String fileName = Utility.dataDir.toString() + File.separator + table.getName() + StringUtility.DAT;
			Operator readOperator = new ReadOperator(new File(fileName), table);
			operator = generateOperator(readOperator, table, body, allCol);
			return operator;
		}//end else
	}//end getOperator


	@SuppressWarnings(StringUtility.UNCHECKED)
	/**
	 * Check for allColumns. If true copy sub query schema as is.
	 * @param table
	 * @param body
	 * @param operator
	 */
	private static void createSchema(Table table, PlainSelect body, Operator operator) {
		if(isAllColumns(body)){
			Utility.tableSchemas.put(table.getAlias(), Utility.tableSchemas.get(operator.getTable().getAlias()));
		}else{
			createSchema(table, (ArrayList<SelectExpressionItem>) body.getSelectItems());
		}
	}

	/**
	 * Create a schema for sub queries
	 * @param table
	 * @param selectItems
	 */
	public static void createSchema(Table table,
			ArrayList<SelectExpressionItem> selectItems) {

		Schema schema = new Schema(table);
		HashMap<String, Integer> cols = new HashMap<String, Integer>();
		for(int colIndex = 0; colIndex < selectItems.size(); colIndex++)
		{
			// Put alias if it is present else put actual expression
			if(selectItems.get(colIndex).getAlias()!=null)
				cols.put(selectItems.get(colIndex).getAlias(), colIndex);
			else
				cols.put(selectItems.get(colIndex).getExpression().toString(), colIndex);
		}
		schema.setColumns(cols);
		//Add new schema to the list of schemas
		//if alias is present then with alias name else with actual name
		if(table.getAlias()!=null)
			Utility.tableSchemas.put(table.getAlias(), schema);
		else
			Utility.tableSchemas.put(table.getName(), schema);
	}


	/**
	 * Generate an operator via parameters
	 * @param readOperator
	 * @param table
	 * @param body
	 * @param allCol
	 * @return Operator object
	 */
	private static Operator generateOperator(Operator readOperator, Table table, PlainSelect body, Boolean allCol){
		@SuppressWarnings(StringUtility.UNCHECKED)
		Operator operator = TreeCreator.createTree(readOperator,
				table,
				body.getWhere(),
				(ArrayList<SelectExpressionItem>) body.getSelectItems(),
				(ArrayList<Join>) body.getJoins(),
				(ArrayList<Expression>) body.getGroupByColumnReferences(),
				body.getHaving(),
				allCol,
				body.getLimit(),
				body.getDistinct(),
				(ArrayList<OrderByElement>)body.getOrderByElements());
		return operator;
	}

	/**
	 * Check if project items are *
	 * @param body
	 * @return true if *, false otherwise
	 */
	private static boolean isAllColumns(PlainSelect body){
		if(((PlainSelect) body).getSelectItems().get(0) instanceof AllColumns)
			return true;
		else
			return false;
	}
}
