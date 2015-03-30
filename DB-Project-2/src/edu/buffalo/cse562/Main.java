package edu.buffalo.cse562;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.Select;
import edu.buffalo.cse562.parsers.CreateTableParser;
import edu.buffalo.cse562.parsers.SelectParser;
import edu.buffalo.cse562.utility.StringUtility;
import edu.buffalo.cse562.utility.Utility;
import edu.buffalo.cse562.utility.Schema;

public class Main {

	public static void main(String args[]){
		initialize(args);
		resetAll();
		for(File sql: Utility.sqlFiles){
			FileReader fr = getFileReader(sql);
			parseWithJsql(fr);		
			
		}
	}// end of main

	/**
	 * Load the Data Directory and SQL Files
	 * @param args - the arguments that are passed to the program.
	 */
	private static void initialize(String args[]) {
		int argIndex;
		Utility.dataDir = new File(args[1]);
		if(args[2].equals(StringUtility.SWAP)){
			Utility.swapDir = new File(args[3]);
			argIndex = 4;
		}
		else
			argIndex = 2;
		Utility.sqlFiles = new ArrayList<File>();
		
		for(;argIndex < args.length; argIndex++){ 
			try{
				File sql = new File(args[argIndex]);
				Utility.sqlFiles.add(sql);
			}
			catch(NullPointerException e){
				System.out.println("Null pointer exception encountered in initialize()");
			}
		}//end for
	}//end of initialize

	/**
	 * Takes a FileReader object and parses it using JsqlParser
	 * @param inputFile The FileReader object to parse
	 * @return void
	 */
	private static void parseWithJsql(FileReader inputFile) {
		try{
			CCJSqlParser parser = new CCJSqlParser(inputFile);
			Statement statement = null;
			while((statement  = parser.Statement()) != null){
				if(statement instanceof Select){
//					long startTime = System.currentTimeMillis();
					SelectParser.parseStatement(statement);
//					System.out.println(System.currentTimeMillis() - startTime);
				}
				else if(statement instanceof CreateTable){
					CreateTableParser.parseStatement(statement);
				}
			}
		}catch(ParseException e){
			System.out.println("Invalid statement to parse or null. Encountered in parseWithJsql()");
		}
	}

	/**
	 * Takes String arguments from the console and returns a FileReader object
	 * @param args The String arguments
	 * @return inputFile The FileReader object
	 * @exception FileNotFoundException e
	 */
	private static FileReader getFileReader(File file){
		FileReader inputFile = null;
		try{
			inputFile = new FileReader(file);
		}catch(FileNotFoundException e){
			System.out.println("The file could not be found. Encountered in getFileReader()");
			System.exit(0);
		}
		return inputFile;
	}
	
	private static void resetAll(){
		Utility.tableSchemas = new HashMap<String, Schema>();
		Utility.tableDataTypes = new HashMap<String, ArrayList<String>>();
		Utility.subQueryCounter = 0;
		Utility.grpByCounter = 0;
	}
}

