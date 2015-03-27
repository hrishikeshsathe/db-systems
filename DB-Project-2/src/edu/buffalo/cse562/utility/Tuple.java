package edu.buffalo.cse562.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.expression.LeafValue.InvalidLeaf;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;


public class Tuple implements Comparable<Tuple>{

	private ArrayList<LeafValue> oneTuple;
	private static ArrayList<Integer> orderByIndexes;
	private static String tableName;
	/**
	 * Create a new tuple where each element is a LeafValue
	 * @param oneRow
	 * @param table
	 */
	public Tuple(String[] oneRow, String table){
		ArrayList<String> dataType = Utility.tableDataTypes.get(table);
		oneTuple = new ArrayList<LeafValue>();
		for(int i = 0; i < oneRow.length; i++){
			switch(dataType.get(i).toLowerCase()){
				case "int":
					oneTuple.add(new LongValue(oneRow[i])); 
					break;
				case "decimal":
				case "double":
					oneTuple.add(new DoubleValue(oneRow[i])); 
					break;
				case "date": 
					oneTuple.add(new DateValue(" "+oneRow[i]+" ")); 
					break;
				case "char": 
					oneTuple.add(new StringValue(" "+oneRow[i]+" ")); 
					break;
				case "string": 
					oneTuple.add(new StringValue(" "+oneRow[i]+" ")); 
					break;
				case "varchar":
					oneTuple.add(new StringValue(" "+oneRow[i]+" ")); 
					break;
				default:
				{
					if(dataType.get(i).contains("CHAR") || dataType.get(i).contains("char")){
						oneTuple.add(new StringValue(" "+oneRow[i]+" "));
					}
				}//default
			}//switch
		}//for
	}//end of constructor
	
	/**
	 * Constructor for null tuple
	 * @param size
	 */
	public Tuple(int size){
		this.oneTuple = new ArrayList<LeafValue>();
		for(int i = 0; i < size; i++)
			this.oneTuple.add(null);
	}
	
	public Tuple(ArrayList<LeafValue> tuple) {
		this.oneTuple = tuple;
	}

	public ArrayList<LeafValue> getTuple(){
		return this.oneTuple;
	}
	/**
	 * Constructor for empty record
	 */
	public Tuple(){
		oneTuple = new ArrayList<LeafValue>();
		oneTuple.add(new StringValue(" "+"NoResult"+" "));
	}

	/**
	 * Takes an index and returns the corresponding LeafValue from the tuple
	 * @param index
	 * @return LeafValue
	 */
	public LeafValue get(int index){
		if(oneTuple == null)
			return null;
		return oneTuple.get(index);
	}
	
	/**
	 * Return the tuple as a string
	 */
	public String toString(){
		String tuple = "";
		for(int i = 0; i < oneTuple.size() - 1; i++){
			tuple += checkIfStringValue(oneTuple.get(i)) + "|";
		}
		tuple += checkIfStringValue(oneTuple.get(oneTuple.size() - 1));
		return tuple;
	}
	
	/**
	 * Function to check if column is of type StringValue. Return without quotes if true
	 * @param column
	 * @return
	 */
	private String checkIfStringValue(LeafValue column) {
		if(column instanceof StringValue)
			return ((StringValue) column).getNotExcapedValue();
		else
			return column.toString();
	}

	/**
	 * Check if tuple is an empty record
	 * @return true if empty, false if not
	 */
	public boolean isEmptyRecord(){
		if(oneTuple.get(0).toString().equals("'NoResult'"))
			return true;
		else 
			return false;
	}


	
	@Override
	public int compareTo(Tuple t2) {
		// TODO Auto-generated method stub
		int value = getComparatorValue(this, t2, tableName, orderByIndexes.get(0));
      if (value == 0 && orderByIndexes.size()>1) {
           value = getComparatorValue(this, t2, tableName, orderByIndexes.get(1));
          if (value == 0 && orderByIndexes.size()>2) {
          	value = getComparatorValue(this, t2, tableName, orderByIndexes.get(2));
          	if (value == 0 && orderByIndexes.size()>3) {
              	value = getComparatorValue(this, t2, tableName, orderByIndexes.get(3));
              }
          }
      }	        
		return value;
	}
	
	public static ArrayList<Tuple> sortTupleList(ArrayList<Tuple> tuples, ArrayList<Integer> orderBy, String table){
		orderByIndexes = orderBy;
		tableName = table;
		Collections.sort(tuples);
		return tuples;
	}
	
	public int getComparatorValue(Tuple t1, Tuple t2, String table, int index){
		
		ArrayList<String> dataType = Utility.tableDataTypes.get(table);
		switch(dataType.get(index).toLowerCase()){
		case "int":
			try {
				return ((Long)t1.get(orderByIndexes.get(index)).toLong()).compareTo(((Long)t2.get(orderByIndexes.get(index)).toLong()));
			} catch (InvalidLeaf e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 			
		case "decimal":
		case "double":
			try {
				return ((Double)t1.get(orderByIndexes.get(index)).toDouble()).compareTo(((Double)t2.get(orderByIndexes.get(index)).toDouble()));
			} catch (InvalidLeaf e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case "date": 
			try {
				return ((Long)t1.get(orderByIndexes.get(index)).toLong()).compareTo(((Long)t2.get(orderByIndexes.get(index)).toLong()));
			} catch (InvalidLeaf e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case "char": 
			return ((String)t1.get(orderByIndexes.get(index)).toString()).compareTo(((String)t2.get(orderByIndexes.get(index)).toString()));			
		case "string": 
			return ((String)t1.get(orderByIndexes.get(index)).toString()).compareTo(((String)t2.get(orderByIndexes.get(index)).toString())); 
		case "varchar":
			return ((String)t1.get(orderByIndexes.get(index)).toString()).compareTo(((String)t2.get(orderByIndexes.get(index)).toString())); 
		default:
		{
			if(dataType.get(index).contains("CHAR") || dataType.get(index).contains("char")){
				return ((String)t1.get(orderByIndexes.get(index)).toString()).compareTo(((String)t2.get(orderByIndexes.get(index)).toString()));
			}
		}//default
	}//switch
		return 0;
	}

	

}
