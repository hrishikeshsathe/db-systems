package edu.buffalo.cse562.operators;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import edu.buffalo.cse562.evaluate.Evaluator;
import edu.buffalo.cse562.utility.Tuple;
import edu.buffalo.cse562.utility.Utility;

public class ProjectOperator implements Operator {

	Operator operator;
	ArrayList<SelectExpressionItem> toProject;
	Table table;
	HashMap<String, Integer> tableSchema;
	boolean allColumns;
	boolean isGroupBy;
	HashMap<String, Integer> projectSchema;
	
	public ProjectOperator(Operator operator, Table table,
			ArrayList<SelectExpressionItem> projectItems, boolean allCol, boolean isGroupBy) {
		this.operator = operator;
		this.toProject = projectItems;
		this.table = table;
		this.tableSchema = Utility.tableSchemas.get(table.getAlias());
		this.allColumns = allCol;
		this.isGroupBy = isGroupBy;
		projectSchema = new HashMap<String, Integer>();
		createProjectSchema();
	}

	private void createProjectSchema() {
		for(int i = 0; i < toProject.size(); i++){
			if(toProject.get(i).getAlias() == null){
				projectSchema.put(toProject.get(i).getExpression().toString(), i);
			}else
				projectSchema.put(toProject.get(i).getAlias().toString(), i);
			
		}
	}

	@Override
	public void reset() {
		operator.reset();
	}

	@Override
	public Tuple readOneTuple() {
		Tuple tempTuple = operator.readOneTuple();
		ArrayList<LeafValue> tuple = new ArrayList<LeafValue>();
		Evaluator evaluator = new Evaluator(tableSchema, tempTuple, false);
		
		if(tempTuple == null)
			return null;
		else if(allColumns || isGroupBy)
			return tempTuple;
		else if(tempTuple.isEmptyRecord())
			return tempTuple;
		else{
			for(SelectExpressionItem exp: toProject){
				try {
					tuple.add(evaluator.eval(exp.getExpression()));
				} catch (SQLException e) {
					System.out.println("Exception in ProjectOperator.readOneTuple()");
				}//end of catch
			}//end for
		}//end else
		return new Tuple(tuple);
	}

	@Override
	public Table getTable() {
		return table;
	}
	
	public HashMap<String, Integer> getProjectSchema(){
		return projectSchema;
	}
}
