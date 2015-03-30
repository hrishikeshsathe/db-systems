package edu.buffalo.cse562.utility;

import net.sf.jsqlparser.statement.select.Limit;
import edu.buffalo.cse562.operators.Operator;

public class Printer {
	public static void print(Operator op, Limit limit){
		Tuple tuple = op.readOneTuple();

		if(limit!=null)
		{
			long lim=limit.getRowCount();
			long counter=lim;
			while(tuple != null && counter > 0){
					System.out.println(tuple.toString());
				counter--;
				tuple = op.readOneTuple();
			}
		}
		else
		{
			while(tuple != null){
					System.out.println(tuple.toString());
				tuple = op.readOneTuple();
			}
		}
	}
}
