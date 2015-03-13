package edu.buffalo.cse562.utility;

import java.util.ArrayList;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;


public class Tuple {

	ArrayList<LeafValue> oneTuple;

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
	
	
	public Tuple(ArrayList<LeafValue> tuple) {
		this.oneTuple = tuple;
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
			tuple += oneTuple.get(i) + "|";
		}
		tuple += oneTuple.get(oneTuple.size() - 1);
		return tuple;
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
}
