package edu.buffalo.cse562.utility;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.expression.LeafValue.InvalidLeaf;
import net.sf.jsqlparser.expression.LongValue;

public class AggregateFunctions {

	/**
	 * Function that accepts two LeafValues and returns their sum
	 * @param a
	 * @param b
	 * @return
	 */
	public static LeafValue calculateSum(LeafValue a, LeafValue b){
		Double sum = 0.0;
		try {
			sum = a.toDouble() + b.toDouble();
		} catch (InvalidLeaf e) {
			e.printStackTrace();
		}
		return new DoubleValue(sum.toString());
	}

	/**
	 * Function to accept two LeafValues and return the smaller one
	 * @param a
	 * @param b
	 * @return
	 */
	public static LeafValue getMinimum(LeafValue a,	LeafValue b) {
		Double minimum = 0.0;
		try {
			minimum = Math.min(a.toDouble(), b.toDouble());
		} catch (InvalidLeaf e) {
			e.printStackTrace();
		}
		return new DoubleValue(minimum.toString());
	}
	
	/**
	 * Function to accept two LeafValues and return the larger one
	 * @param a
	 * @param b
	 * @return
	 */
	public static LeafValue getMaximum(LeafValue a,	LeafValue b) {
		Double maximum = 0.0;
		try {
			maximum = Math.max(a.toDouble(), b.toDouble());
		} catch (InvalidLeaf e) {
			e.printStackTrace();
		}
		return new DoubleValue(maximum.toString());
	}

	/**
	 * Function to return count
	 * @param column
	 * @return
	 */
	public static LeafValue getCount(LeafValue column) {
		Long count = 0L;
		try {
			count = column.toLong() + 1;
		} catch (InvalidLeaf e) {
			e.printStackTrace();
		}
		return new LongValue(count.toString());
	}
}
