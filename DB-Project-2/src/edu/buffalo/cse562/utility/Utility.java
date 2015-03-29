package edu.buffalo.cse562.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import edu.buffalo.cse562.operators.Operator;
import net.sf.jsqlparser.schema.Table;

public class Utility {

	public static File dataDir = null;
	public static File swapDir = null;
	public static ArrayList<File> sqlFiles;
	public static HashMap<String, Schema> tableSchemas = null;
	public static HashMap<String, ArrayList<String>> tableDataTypes = null;
	public static int subQueryCounter = 0;
	public static Tuple noResult = new Tuple();
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
	
}
