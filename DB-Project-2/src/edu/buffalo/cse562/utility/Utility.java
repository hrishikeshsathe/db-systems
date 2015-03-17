package edu.buffalo.cse562.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Utility {

	public static File dataDir = null;
	public static ArrayList<File> sqlFiles;
	public static HashMap<String, HashMap<String, Integer>> tableSchemas = null;
	public static HashMap<String, ArrayList<String>> tableDataTypes = null;
	public static int subQueryCounter = 0;
	public static Tuple noResult = new Tuple();
	public static int grpByCounter=0;
}
