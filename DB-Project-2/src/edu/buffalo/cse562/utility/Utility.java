package edu.buffalo.cse562.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import edu.buffalo.cse562.operators.HashJoinOperator;
import edu.buffalo.cse562.operators.JoinOperator;
import edu.buffalo.cse562.operators.Operator;
import edu.buffalo.cse562.operators.ReadOperator;
import edu.buffalo.cse562.operators.SelectionOperator;

public class Utility {

	public static File dataDir = null;
	public static File swapDir = null;
	public static ArrayList<File> sqlFiles;
	public static HashMap<String, Schema> tableSchemas = null;
	public static HashMap<String, ArrayList<String>> tableDataTypes = null;
	public static int subQueryCounter = 0;
	public static int grpByCounter = 0;

	/**
	 * Function to set table alias as table name if table alias is null
	 * @param table
	 */
	public static void checkAndSetTableAlias(Table table){
		if(table.getAlias() == null)
			table.setAlias(table.getName());
	}

	/**
	 * Set children for parent and set parent for children
	 * @param parent
	 * @param leftChild
	 * @param rightChild
	 */
	public static void setParentAndChild(Operator parent,
			Operator leftChild, Operator rightChild) {
		parent.setLeftChild(leftChild);
		parent.setRightChild(rightChild);
		if(leftChild != null)
			leftChild.setParent(parent);
		if(rightChild != null)
			rightChild.setParent(parent);
	}//end of setParentAndChild

	/**
	 * Function to split AND clauses in the where clause
	 * @param e
	 * @return
	 */
	public static List<Expression> splitAndClauses(Expression e) 
	{
		List<Expression> ret = 
				new ArrayList<Expression>();
		if(e instanceof AndExpression){
			AndExpression a = (AndExpression)e;
			ret.addAll(
					splitAndClauses(a.getLeftExpression())
					);
			ret.addAll(
					splitAndClauses(a.getRightExpression())
					);
		} else {
			ret.add(e);
		}
		return ret;
	}

	public static boolean optimizeJoin(Operator root, Expression expression, Column leftColumn, Column rightColumn){
		HashMap<String, Integer> leftSchema = getSchema(root, StringUtility.LEFT);
		HashMap<String, Integer> rightSchema = getSchema(root, StringUtility.RIGHT);
		if((containsColumn(leftSchema, leftColumn) && containsColumn(rightSchema, rightColumn))){
			if(root instanceof JoinOperator){
				Operator hashJoinOperator = createHashJoin(root, leftColumn, rightColumn);
				changeParentsForJoin(hashJoinOperator, root);
				return true;
			}
			else 
				return false;
		}
		if(containsColumn(leftSchema, rightColumn) && containsColumn(rightSchema, leftColumn)){
			if(root instanceof JoinOperator){
				Operator hashJoinOperator = createHashJoin(root, rightColumn, leftColumn);
				changeParentsForJoin(hashJoinOperator, root);
				return true;
			}
			else return false;
		}
		if(containsColumn(leftSchema, leftColumn) && containsColumn(leftSchema, rightColumn))
			return optimizeJoin(root.getLeftChild(), expression, leftColumn, rightColumn);
		else
			return optimizeJoin(root.getRightChild(), expression, leftColumn, rightColumn);
	}

	public static boolean optimizeSelect(Operator root, Expression expression, Column column) {
		if(root instanceof ReadOperator){
			createSelectOperator(root, expression);
			return true;
		}
		HashMap<String, Integer> leftSchema = getSchema(root, StringUtility.LEFT);
		HashMap<String, Integer> rightSchema = null;
		if(root.getRightChild() != null)
			rightSchema = getSchema(root, StringUtility.RIGHT);
		if(containsColumn(leftSchema, column))
			return optimizeSelect(root.getLeftChild(), expression, column);
		else if(rightSchema != null && containsColumn(rightSchema, column))
			return optimizeSelect(root.getRightChild(), expression, column);
		return false;
	}

	public static void createSelectOperator(Operator root, Expression where){

		if(root.getParent() instanceof SelectionOperator)
		{
			SelectionOperator select = (SelectionOperator) root.getParent();
			select.setWhere(new AndExpression(select.getWhere(), where) );
		}
		else
		{
			Operator selectionOperator = new SelectionOperator(root, root.getTable(), where, false);
			selectionOperator.setParent(root.getParent());
			root.setParent(selectionOperator);
			if((selectionOperator.getParent() instanceof JoinOperator) || (selectionOperator.getParent() instanceof HashJoinOperator)){
				if(selectionOperator.getParent().getLeftChild() == root)
					selectionOperator.getParent().setLeftChild(selectionOperator);
				else
					selectionOperator.getParent().setRightChild(selectionOperator);
			}
			else
				selectionOperator.getParent().setLeftChild(selectionOperator);
		}
	}

	public static void changeParentsForJoin(Operator newOperator, Operator old){
		newOperator.setParent(old.getParent());
		newOperator.getLeftChild().setParent(newOperator);
		newOperator.getRightChild().setParent(newOperator);
		old.getParent().setLeftChild(newOperator);
		old = null;
//		System.gc();
	}

	public static boolean isJoinCondition(Expression e)
	{
		if(e instanceof EqualsTo)
			if(((EqualsTo) e).getLeftExpression() instanceof Column && ((EqualsTo) e).getRightExpression() instanceof Column)
				return true;
		return false;
	}

	public static Operator createHashJoin(Operator root, Column leftColumn, Column rightColumn){
		return new HashJoinOperator(root.getLeftChild(), root.getRightChild(), leftColumn, rightColumn);
	}

	public static boolean containsColumn(HashMap<String, Integer> columns, Column c){
		c.getTable().setName(c.getTable().getName().toUpperCase());
		if(columns.containsKey(c.getWholeColumnName()))
			return true;
		return false;
	}

	public static HashMap<String, Integer> getSchema(Operator temp, String which){
		if(which.equals(StringUtility.LEFT))
			return tableSchemas.get(temp.getLeftChild().getTable().getAlias()).getColumns();
		return tableSchemas.get(temp.getRightChild().getTable().getAlias()).getColumns();
	}

	/*public static void printTree(Operator root){
		if(root != null){
			System.out.println("-----------------------------------------------------");
			System.out.println(root.getClass());
			if(root instanceof SelectionOperator)
				System.out.println(((SelectionOperator) root).getWhere());
			else if(root instanceof ReadOperator)
				System.out.println(root.getTable().getAlias());
			else if(root instanceof JoinOperator || root instanceof HashJoinOperator){
				System.out.println(root.getLeftChild().getTable().getAlias());
				System.out.println(root.getRightChild().getTable().getAlias());
				if(root instanceof HashJoinOperator){
					System.out.println(((HashJoinOperator) root).getLeftColumn());
					System.out.println(((HashJoinOperator) root).getRightColumn());
				}
			}
			printTree(root.getLeftChild());
			printTree(root.getRightChild());
		}
	}*/

}
