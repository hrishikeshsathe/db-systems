package edu.buffalo.cse562.utility;

import java.util.HashMap;

import net.sf.jsqlparser.schema.Table;

public class Schema {

	private HashMap<String, Integer> columns;
	private Table table;
	
	public Schema(Table table){
		this.columns = new HashMap<String, Integer>();
		this.table = table;
	}
	public HashMap<String, Integer> getColumns() {
		return columns;
	}
	public void setColumns(HashMap<String, Integer> columns) {
		this.columns = columns;
	}
	public Table getTable() {
		return table;
	}
	public void setTable(Table table) {
		this.table = table;
	}
	
	
}
