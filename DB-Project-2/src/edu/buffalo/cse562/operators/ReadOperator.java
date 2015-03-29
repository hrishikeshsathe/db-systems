package edu.buffalo.cse562.operators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.sf.jsqlparser.schema.Table;
import edu.buffalo.cse562.utility.StringUtility;
import edu.buffalo.cse562.utility.Tuple;

public class ReadOperator implements Operator {
	
	File tableData = null;
	BufferedReader br = null;
	Table table = null;
	Operator parent = null;
	
	public ReadOperator(File file, Table table) {
		this.tableData = file;
		this.table = table;
		reset();
	}

	@Override
	public void reset() {
		try{
			if(br != null)
				br.close();
			br = new BufferedReader(new FileReader(tableData));
		}catch(IOException ex){
			System.out.println("IOException in ReadOperator.reset()");
			System.exit(0);
		}
	}

	@Override
	public Tuple readOneTuple() {
		
		if(br == null)
			return null;
		String line = new String("");
		Tuple tuple = null;
		try{
			line = br.readLine();
			if(line == null)
				return null;
			tuple = new Tuple(line.split(StringUtility.PIPE), table.getName());
		}catch(IOException ex){
			System.out.println("IOException in ReadOperator.readOneTuple()");
			System.exit(0);
		}
		return tuple;
	}

	@Override
	public Table getTable() {
		return table;
	}

	@Override
	public Operator getLeftChild() {
		return null;
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
		
	}

	@Override
	public void setRightChild(Operator rightChild) {

	}

	@Override
	public void setParent(Operator parent) {
		this.parent = parent;
	}

}
