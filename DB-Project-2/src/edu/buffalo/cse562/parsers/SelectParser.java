package edu.buffalo.cse562.parsers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import edu.buffalo.cse562.operators.Operator;
import edu.buffalo.cse562.operators.OperatorTest;
import edu.buffalo.cse562.operators.ReadOperator;
import edu.buffalo.cse562.utility.Utility;

public class SelectParser {

	/**
	 * Parse a select statement and execute it. Prints the result of the select query
	 * @param statement - statement of type SelectBody to be parsed
	 */
	public static void parseStatement(Statement statement) {
		SelectBody body = ((Select) statement).getSelectBody();
		
		if(body instanceof PlainSelect){
			Operator operator = getOperator((PlainSelect) body);
			OperatorTest.dump(operator);
		}//end if
	}//end parseStatement

	/**
	 * Accept a PlainSelect object and return an operator. Operator can be dumped to print result.
	 * @param body - PlainSelect
	 * @return Operator
	 */
	@SuppressWarnings("unchecked")
	private static Operator getOperator(PlainSelect body) {
		Table table = null;
		Operator operator = null;
		boolean allCol = false;
		
		//if there is a subQuery
		if(body.getFromItem() instanceof SubSelect){
			table = new Table();
			
			if(body.getFromItem().getAlias() == null){
				table.setName("SubQuery" + Utility.subQueryCounter);
				table.setAlias("SubQuery" + Utility.subQueryCounter);
				Utility.subQueryCounter++;
			}else{
				table.setName(body.getFromItem().getAlias());
				table.setAlias(body.getFromItem().getAlias());
			}
			
			createSchema(table, 
					(ArrayList<SelectExpressionItem>) ((PlainSelect) ((SubSelect) 
							body.getFromItem()).getSelectBody()).getSelectItems());
			operator = getOperator((PlainSelect) ((SubSelect) body.getFromItem()).getSelectBody());
			operator = OperatorTest.executeSelect(operator,
					table,
					body.getWhere(),
					(ArrayList<SelectExpressionItem>) body.getSelectItems(),
					(ArrayList<Join>) body.getJoins(),
					(ArrayList<Expression>) body.getGroupByColumnReferences(),
					body.getHaving(),
					allCol,
					body.getLimit());
			return operator;
		}//end subQuery if
		else{
			table = (Table) body.getFromItem();
			if(table.getAlias() == null){
				table.setAlias(table.getName());
			}
			if(((PlainSelect) body).getSelectItems().get(0) instanceof AllColumns)
				allCol = true;
			else
				allCol = false;
			String tableFile = Utility.dataDir.toString() + File.separator + table.getName() + ".dat";
			Operator readOperator = new ReadOperator(new File(tableFile), table);
			operator = OperatorTest.executeSelect(readOperator,
					table,
					body.getWhere(),
					(ArrayList<SelectExpressionItem>) body.getSelectItems(),
					(ArrayList<Join>) body.getJoins(),
					(ArrayList<Expression>) body.getGroupByColumnReferences(),
					body.getHaving(),
					allCol,
					body.getLimit());
			return operator;
		}
	}

	private static void createSchema(Table table,
			ArrayList<SelectExpressionItem> selectItems) {
		
		HashMap<String, Integer> schema = new HashMap<String, Integer>();
		for(int i=0; i<selectItems.size(); i++)
		{
			// Put alias if it is present else put actual expression
			if(selectItems.get(i).getAlias()!=null)
				schema.put(selectItems.get(i).getAlias(), i);
			else
				schema.put(selectItems.get(i).getExpression().toString(), i);
		}
		//Add new schema to the list of schemas
		//if alias is present then with alias name else with actual name
		if(table.getAlias()!=null)
			Utility.tableSchemas.put(table.getAlias(), schema);
		else
			Utility.tableSchemas.put(table.getName(), schema);
	}
}
