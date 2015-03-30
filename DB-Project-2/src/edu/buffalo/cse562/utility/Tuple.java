package edu.buffalo.cse562.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.expression.LeafValue.InvalidLeaf;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;


public class Tuple implements Comparable<Tuple>{

	private ArrayList<LeafValue> oneTuple;
	private static ArrayList<Integer> orderByIndexes;
	private static ArrayList<Integer> orderByOrders;
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
			case StringUtility.INT:
				oneTuple.add(new LongValue(oneRow[i])); 
				break;
			case StringUtility.DECIMAL:
			case StringUtility.DOUBLE:
				oneTuple.add(new DoubleValue(oneRow[i])); 
				break;
			case StringUtility.DATE3: 
				oneTuple.add(new DateValue(StringUtility.SPACE + oneRow[i] + StringUtility.SPACE)); 
				break;
			case StringUtility.CHAR1: 
			case StringUtility.STRING: 
			case StringUtility.VARCHAR:
				oneTuple.add(new StringValue(StringUtility.SPACE + oneRow[i] + StringUtility.SPACE)); 
				break;
			default:
			{
				if(dataType.get(i).contains(StringUtility.CHAR1) || dataType.get(i).contains(StringUtility.CHAR2)){
					oneTuple.add(new StringValue(StringUtility.SPACE + oneRow[i] + StringUtility.SPACE));
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
		oneTuple.add(new StringValue(StringUtility.SPACE + StringUtility.NORESULT1+ StringUtility.SPACE));
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
			tuple += checkIfStringValue(oneTuple.get(i)) + StringUtility.PIPE2;
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


	@Override
	public int compareTo(Tuple t2) {
		int value = getComparatorValue(this, t2, tableName, orderByIndexes.get(0), 0);
		if (value == 0 && orderByIndexes.size()>1) {
			value = getComparatorValue(this, t2, tableName, orderByIndexes.get(1), 1);
			if (value == 0 && orderByIndexes.size()>2) {
				value = getComparatorValue(this, t2, tableName, orderByIndexes.get(2), 2);
				if (value == 0 && orderByIndexes.size()>3) {
					value = getComparatorValue(this, t2, tableName, orderByIndexes.get(3), 3);
				}
			}
		}	        
		return value;
	}

	public static ArrayList<Tuple> sortTupleList(ArrayList<Tuple> tuples, ArrayList<Integer> orderBy, ArrayList<Integer> orders, String table){
		orderByIndexes = orderBy;
		tableName = table;
		orderByOrders = orders;
		Collections.sort(tuples);
		return tuples;
	}

	public int getComparatorValue(Tuple t1, Tuple t2, String table, int index, int order){

		String dataType = t1.get(index).getClass().getSimpleName();
		switch(dataType){
		case StringUtility.LONGVALUE:
			try {
				if(orderByOrders.get(order) == 0)
					return ((Long)t1.get(index).toLong()).compareTo(((Long)t2.get(index).toLong()));
				else
					return -1 * ((Long)t1.get(index).toLong()).compareTo(((Long)t2.get(index).toLong()));
			} catch (InvalidLeaf e1) {
				e1.printStackTrace();
			} 			
		case StringUtility.DOUBLEVALUE:
			try {
				if(orderByOrders.get(order) == 0)
					return Double.compare((Double)t1.get(index).toDouble(), (Double)t2.get(index).toDouble());
				else
					return -1 * Double.compare((Double)t1.get(index).toDouble(), (Double)t2.get(index).toDouble());

			} catch (InvalidLeaf e) {
				e.printStackTrace();
			}
			break;
		case StringUtility.DATEVALUE: 
			if(orderByOrders.get(order) == 0){					
				Date d1 = ((DateValue) t1.get(index)).getValue();
				Date d2 = ((DateValue) t2.get(index)).getValue();
				return d1.compareTo(d2);
			}
			else{
				Date d1 = ((DateValue) t1.get(index)).getValue();
				Date d2 = ((DateValue) t2.get(index)).getValue();
				return -1 * d1.compareTo(d2);
			}
		case StringUtility.STRINGVALUE: 
			if(orderByOrders.get(order) == 0)
				return ((String)t1.get(index).toString()).compareTo(((String)t2.get(index).toString()));
			else
				return -1 * ((String)t1.get(index).toString()).compareTo(((String)t2.get(index).toString()));
		}//switch
		return 0;
	}




}
