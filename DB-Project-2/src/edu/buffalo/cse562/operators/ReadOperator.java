package edu.buffalo.cse562.operators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import net.sf.jsqlparser.schema.Table;
import edu.buffalo.cse562.utility.Tuple;


public class ReadOperator implements Operator {

	File file = null;
	BufferedReader br = null;
	Table table;

	public ReadOperator(File f, Table table){
		this.file = f;
		this.table = table;
		reset();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		try{
			br = new BufferedReader(new FileReader(file));
		}
		catch(IOException e){
			System.out.println("IO Exception in ReadOperator.reset()");
			System.exit(0);
		}
	}

	@Override
	/**Read One tuple at a time from the File.
	 * @return Tuple array for the row. 
	 */
	public Tuple readOneTuple() {
		
		if(br == null)
			return null;
		
		String line = "";
		Tuple tuple = null;
		
		try{
			line=br.readLine();
			if(line == null)
				return null;
			String[] cols = line.split("\\|");
			tuple = new Tuple(cols, table.getName());
		}catch(IOException e){
			System.out.println("IOException in ReadOperator.readOneTuple()");
			System.exit(0);
		}
		return tuple;
	}

	@Override
	public Table getTable() {
		return table;
	}

}
