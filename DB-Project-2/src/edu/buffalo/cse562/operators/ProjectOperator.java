package edu.buffalo.cse562.operators;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import edu.buffalo.cse562.utility.Evaluator;
import edu.buffalo.cse562.utility.Schema;
import edu.buffalo.cse562.utility.Tuple;
import edu.buffalo.cse562.utility.Utility;

public class ProjectOperator implements Operator {

	Operator leftChild;
	Operator parent;
	ArrayList<SelectExpressionItem> projectItems;
	Table table;
	Schema schema;
	boolean allColumns;
	boolean isGroupBy;
	Schema newSchema;


	public ProjectOperator(Operator operator, Table table,
			ArrayList<SelectExpressionItem> projectItems, Boolean allCol,
			boolean isGroupBy) {

		this.leftChild = operator;
		this.projectItems = projectItems;
		this.table = table;
		this.schema = Utility.tableSchemas.get(table.getAlias());
		this.allColumns = allCol;
		this.isGroupBy = isGroupBy;
		createProjectSchema();

	}

	private void createProjectSchema() {
		if(!allColumns){
			newSchema = new Schema(table);
			HashMap<String, Integer> temp = new HashMap<String, Integer>();
			for(int projectItemIndex = 0; projectItemIndex < projectItems.size(); projectItemIndex++){
				String alias = projectItems.get(projectItemIndex).getAlias();
				if(alias == null)
					temp.put(projectItems.get(projectItemIndex).getExpression().toString(), projectItemIndex);
				else
					temp.put(alias, projectItemIndex);
			}
			newSchema.setColumns(temp);
			Utility.tableSchemas.put(table.getAlias(), newSchema);
		}
	}

	@Override
	public void reset() {
		leftChild.reset();
	}

	@Override
	public Tuple readOneTuple() {
		Tuple tempTuple = leftChild.readOneTuple();
		ArrayList<LeafValue> tuple = new ArrayList<LeafValue>();
		Evaluator evaluator = new Evaluator(schema, tempTuple, false);

		if(tempTuple == null)
			return null;
		else if(isGroupBy || allColumns)
			return tempTuple;
		else{
			for(SelectExpressionItem exp: projectItems){
				try {
					tuple.add(evaluator.eval(exp.getExpression()));
				} catch (SQLException e) {
					System.out.println("Exception in ProjectOperator.readOneTuple()");
				}//end of catch
			}//end for
		}//end else
		return new Tuple(tuple);	}

	@Override
	public Table getTable() {
		return table;
	}

	@Override
	public Operator getLeftChild() {
		return this.leftChild;
	}

	@Override
	public Operator getRightChild() {
		return null;
	}

	@Override
	public Operator getParent() {
		return this.parent;
	}

	@Override
	public void setLeftChild(Operator leftChild) {
		this.leftChild = leftChild;
	}

	@Override
	public void setRightChild(Operator rightChild) {
		//do nothing
	}

	@Override
	public void setParent(Operator parent) {
		this.parent = parent;
	}

}
