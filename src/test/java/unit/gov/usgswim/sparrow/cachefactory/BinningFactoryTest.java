package gov.usgswim.sparrow.cachefactory;

import static gov.usgswim.sparrow.action.CalcBinning.digitAccuracyMultipliers;
import static gov.usgswim.sparrow.action.CalcBinning.getUniqueValueCount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.action.CalcBinning;
import gov.usgswim.sparrow.util.BigDecimalUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Ignore;
import org.junit.Test;

public class BinningFactoryTest {

	public final static boolean ENABLE_ALL_LOG_OUTPUT = false;
	public final static boolean ENABLE_FAILURE_LOG_OUTPUT = true;

	/**
	 * Max is redundant, but there is no max negative value double constant,
	 * so we create them both here.
	 */
	public final static double MAX_DOUBLE = Double.MAX_VALUE;
	public final static double MIN_DOUBLE = Double.MAX_VALUE * -1d;



	public static Double[] sortedData2 = { 2d, 4d };
	public static Double[] sortedData2a = { 0d, 0d };
	public static Double[] sortedData2b = { 4d, 4d };
	public static Double[] sortedData2c = { 0d, 4d };

	public static Double[] sortedData2d = { -4d, -2d };
	public static Double[] sortedData2e = { -4d, -4d };
	public static Double[] sortedData2f = { -4d, 0d };

	public static Double[] sortedData2g = { -4d, 4d };

	//Two values w/ decimals
	public static Double[] sortedData2_ = { 2.1d, 4.3d };
	public static Double[] sortedData2a_ = { 0.1d, 0.1d };
	public static Double[] sortedData2b_ = { 4.1d, 4.1d };
	public static Double[] sortedData2c_ = { 0d, 4.1d };

	public static Double[] sortedData2d_ = { -4.1d, -2.1d };
	public static Double[] sortedData2e_ = { -4.1d, -4.1d };
	public static Double[] sortedData2f_ = { -4.1d, 0d };

	public static Double[] sortedData2g_ = { -4.1d, 4.1d };

	public static Double[] sortedData3SignZero = { 0d, 100d, 110d, 120d };

	//empty array
	public static Double[] sortedDataEmpty_ = new Double[0];

	//Non sorted data tables with 'odd' values
	public static SimpleDataTableWritable tblEmpty =
		new SimpleDataTableWritable();
	static {
		StandardNumberColumnDataWritable col = new StandardNumberColumnDataWritable();
		tblEmpty.addColumn(col);
	}
	public static DataTable tblOneNAN =
		new SimpleDataTableWritable(transpose(Double.NaN), null);
	public static DataTable tblAllNAN =
		new SimpleDataTableWritable(transpose(Double.NaN, Double.NaN, Double.NaN), null);
	public static DataTable tblMixNAN =
		new SimpleDataTableWritable(transpose(4d, Double.NaN, -4, Double.NaN), null);

	public static DataTable tblMixAll_1 =
		new SimpleDataTableWritable(
		transpose(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN), null);
	public static DataTable tblMixAll_2 =
		new SimpleDataTableWritable(
		transpose(Double.NaN, 1.2d, Double.POSITIVE_INFINITY, -9d, Double.NEGATIVE_INFINITY, Double.NaN), null);
	public static DataTable tblMixAll_3 =
		new SimpleDataTableWritable(
		transpose(Double.NaN, 1.2d, Double.POSITIVE_INFINITY, -9d, Double.NaN, Double.NaN), null);
	public static DataTable tblMixAll_4 =
		new SimpleDataTableWritable(
		transpose(Double.NaN, Double.NEGATIVE_INFINITY, -9d, Double.NaN, Double.NaN), null);

	// ==============

	@Test public void testInfinityAssumptions() {
		assertTrue(Double.isInfinite(Double.POSITIVE_INFINITY));
		assertTrue(Double.isInfinite(Double.NEGATIVE_INFINITY));
		assertFalse(Double.isInfinite(Double.MAX_VALUE));
		assertFalse(Double.isInfinite(Double.MAX_VALUE * -1d));
	}


	////////////////////////////////////
	// Try small datasets
	////////////////////////////////////
	@Test public void testEqualCount_SmallDataset_OneBin() {

		final int bins = 1;
		final boolean round = true;

		//Comments following the asserts indicate a preferred binning result,
		//though the tested values are acceptable.  Who's to say??
		doEqualCountAssert(sortedData2, bins, round, 0d, 5d);	//2 to 4
		doEqualCountAssert(sortedData2_, bins, round, 0d, 5d); //2 to 5
		doEqualCountAssert(sortedData2a, bins, round, 0d, 1d);
		doEqualCountAssert(sortedData2a_, bins, round, 0d, .1d);
		doEqualCountAssert(sortedData2b, bins, round, 0d, 4d);
		doEqualCountAssert(sortedData2b_, bins, round, 0d, 4.1d);	//0 to 5
		doEqualCountAssert(sortedData2c, bins, round, 0d, 5d);	//0 to 4
		doEqualCountAssert(sortedData2c_, bins, round, 0d, 5d);
		doEqualCountAssert(sortedData2d, bins, round, -5d, -0d);
		doEqualCountAssert(sortedData2d_, bins, round, -5d, -2d);
		doEqualCountAssert(sortedData2e, bins, round, -4d, 0d);
		doEqualCountAssert(sortedData2e_, bins, round, -4.1d, 0d);
		doEqualCountAssert(sortedData2f, bins, round, -5d, 0d);	//-4 to 0
		doEqualCountAssert(sortedData2f_, bins, round, -5d, 0d);
		doEqualCountAssert(sortedData2g, bins, round, -10d, 10d);	//-4 to 4
		doEqualCountAssert(sortedData2g_, bins, round, -10d, 10d);	//-5 to 5
		doEqualCountAssert(sortedDataEmpty_, bins, round, 0d, 1d);

		//Test datatables with non-standard double values
		doEqualCountAssert(tblEmpty, bins, round, 0d, 1d);


		//This is an strange binning:  Why would zero be included in the generated bins?
		doEqualCountAssert(new Double[] {100000D, 1000000D}, bins, round, 0d, 1000000D);
	}

	// NaNs are now removed instead of "handled", but left test in in case reversion desired
	@Test @Ignore public void testNaNHandling() {
		final int bins = 1;
		final boolean round = true;

		doEqualCountAssert(tblOneNAN, bins, round, 0d, 1d);
		doEqualCountAssert(tblAllNAN, bins, round, 0d, 1d);
		doEqualCountAssert(tblMixNAN, bins, round, -10d, 10d);
	}

	// Infinitys are now removed instead of "handled", but left test in in case reversion desired
	@Test @Ignore public void testInfinityHandling() {
		final int bins = 1;
		final boolean round = true;
		//These Tests all require that INFINITE values be considered in
		//creating bins.
		//TODO:  This test is broken until we actually figure out how to handle
		//infinite values.

		doEqualCountAssert(tblMixAll_1, bins, round, MIN_DOUBLE, MAX_DOUBLE);
		doEqualCountAssert(tblMixAll_2, bins, round, MIN_DOUBLE, MAX_DOUBLE);
		doEqualCountAssert(tblMixAll_3, bins, round, -10d, MAX_DOUBLE);
		doEqualCountAssert(tblMixAll_4, bins, round, MIN_DOUBLE, 0d);
	}

	private static double[][] transpose(double... in) {
		int size = in.length;
		double[][] out = new double[size][1];

		for (int i=0; i < size; i++) {
			out[i][0] = in[i];
		}
		return out;
	}

	@Test public void testEqualCount_SmallDataset_TwoBins() {
		final int bins = 2;
		final boolean round = true;

		//Comments following the asserts indicate a preferred binning result,
		//though the tested values are acceptable.  Who's to say??
		doEqualCountAssert(sortedData2, bins, round, 2d, 3d, 4d);
		doEqualCountAssert(sortedData2_, bins, round, 2.1d, 3.2d, 4.3d);
		doEqualCountAssert(sortedData2a, bins, round, 0d, 1d, 2d);
		doEqualCountAssert(sortedData2a_, bins, round, 0d, .05d, .1d);
		doEqualCountAssert(sortedData2b, bins, round, 0d, 2d, 4d);
		doEqualCountAssert(sortedData2b_, bins, round, 0d, 2.05d, 4.1d);
		doEqualCountAssert(sortedData2c, bins, round, 0d, 2d, 4d);
		doEqualCountAssert(sortedData2c_, bins, round, 0d, 2.05d, 4.1d);
		doEqualCountAssert(sortedData2d, bins, round, -4d, -3d, -2d);

		//Some specific values that have caused issues
		doEqualCountAssert(sortedData2d_, bins, round, -4.1, -3.1, -2.1);
		doEqualCountAssert(new Double[] {-4.1d, -2.1d}, bins, round, -4.1d, -3.1d, -2.1d);
		doEqualCountAssert(new Double[] {-4.3d, -2.1d}, bins, round, -4.3d, -3.2d, -2.1d);


		doEqualCountAssert(sortedData2e, bins, round, -4d, -2d, 0d);
		doEqualCountAssert(sortedData2e_, bins, round, -4.1d, -2.05d, 0d);
		doEqualCountAssert(sortedData2f, bins, round, -4d, -2d, 0d);
		doEqualCountAssert(sortedData2f_, bins, round, -4.1d, -2.05d, 0d);
		doEqualCountAssert(sortedData2g, bins, round, -4d, 0d, 4d);
		doEqualCountAssert(sortedData2g_, bins, round, -4.1d, 0d, 4.1d);
		doEqualCountAssert(sortedDataEmpty_, bins, round, 0d, 1d, 2d);

		doEqualCountAssert(sortedData3SignZero, bins, round, 0d, 110d, 120d);
	}


	@Test public void testGetEqualCountBinsOfThreeSmall() {
		final int bins = 3;
		final boolean round = true;

		doEqualCountAssert(sortedData2, bins, round, 2d, 2.67d, 3.34d, 4.01d);
		doEqualCountAssert(sortedData2_, bins, round, 2.1d, 2.84d, 3.58d, 4.32d);
		doEqualCountAssert(sortedData2a, bins, round, 0d, 1d, 2d, 3d);
		doEqualCountAssert(sortedData2a_, bins, round, 0d, .034d, .068d, .102d);
		doEqualCountAssert(sortedData2b, bins, round, 0d, 1.34d, 2.68d, 4.02d);
		doEqualCountAssert(sortedData2b_, bins, round, 0d, 1.37d, 2.74d, 4.11d);
		doEqualCountAssert(sortedData2c, bins, round, 0d, 1.34d, 2.68d, 4.02d);
		doEqualCountAssert(sortedData2c_, bins, round, 0d, 1.37d, 2.74d, 4.11d);
		doEqualCountAssert(sortedData2d, bins, round, -4d, -3.33d, -2.66d, -1.99d);
		doEqualCountAssert(sortedData2d_, bins, round, -4.1d, -3.43d, -2.76d, -2.09d);
		doEqualCountAssert(sortedData2e, bins, round, -4d, -2.66d, -1.32d, .02d);
		doEqualCountAssert(sortedData2e_, bins, round, -4.1d, -2.73d, -1.36d, .01d);
		doEqualCountAssert(sortedData2f, bins, round, -4d, -2.66d, -1.32d, .02d);
		doEqualCountAssert(sortedData2f_, bins, round, -4.1d, -2.73d, -1.36d, .01d);
		doEqualCountAssert(sortedData2g, bins, round, -4d, -1.33d, 1.34d, 4.01d);
		doEqualCountAssert(sortedData2g_, bins, round, -4.1d, -1.36d, 1.38d, 4.12d);
		doEqualCountAssert(sortedDataEmpty_, bins, round, 0d, 1d, 2d, 3d);
	}


	@Test public void testSimpleRound() {

		//Round UP to the nearest 1000, treat any value less than or equal to 10 as 'small' (2 places right of 1000)
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(1000d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(1000.000001d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(1010d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("2000", BigDecimalUtils.roundToScale(new BigDecimal(1010.000000001d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("2000", BigDecimalUtils.roundToScale(new BigDecimal(1100d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(999.999999d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(9.999999d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(10d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(10.0000001d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-1000d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-1000.000001d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-1010d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("-2000", BigDecimalUtils.roundToScale(new BigDecimal(-1010.000000001d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("-2000", BigDecimalUtils.roundToScale(new BigDecimal(-1100d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-999.999999d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(-9.999999d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(-10d), 3, 2, RoundingMode.UP).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-10.0000001d), 3, 2, RoundingMode.UP).toPlainString());

		//Round Down to the nearest 1000, treat any value less than or equal to 10 as 'small' (2 places right of 1000)
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(1000d), 3, 2, RoundingMode.DOWN).toPlainString());
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(1100d), 3, 2, RoundingMode.DOWN).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(989.999999999d), 3, 2, RoundingMode.DOWN).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(990d), 3, 2, RoundingMode.DOWN).toPlainString());
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(990.0000000001), 3, 2, RoundingMode.DOWN).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-1000d), 3, 2, RoundingMode.DOWN).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-1100d), 3, 2, RoundingMode.DOWN).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(-989.999999999d), 3, 2, RoundingMode.DOWN).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(-990d), 3, 2, RoundingMode.DOWN).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-990.0000000001), 3, 2, RoundingMode.DOWN).toPlainString());

		//Round CEILING to the nearest 1000, treat any value less than or equal to 10 as 'small' (2 places right of 1000)
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(1000d), 3, 2, RoundingMode.CEILING).toPlainString());
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(1010d), 3, 2, RoundingMode.CEILING).toPlainString());
		assertEquals("2000", BigDecimalUtils.roundToScale(new BigDecimal(1010.000000001d), 3, 2, RoundingMode.CEILING).toPlainString());
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(999.999999d), 3, 2, RoundingMode.CEILING).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(10d), 3, 2, RoundingMode.CEILING).toPlainString());
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(10.0000001d), 3, 2, RoundingMode.CEILING).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-1000d), 3, 2, RoundingMode.CEILING).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-1011d), 3, 2, RoundingMode.CEILING).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-1990d), 3, 2, RoundingMode.CEILING).toPlainString());
		assertEquals("-2000", BigDecimalUtils.roundToScale(new BigDecimal(-1990.000000001d), 3, 2, RoundingMode.CEILING).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-999.999999d), 3, 2, RoundingMode.CEILING).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(-10d), 3, 2, RoundingMode.CEILING).toPlainString());

		//Round FLOOR to the nearest 1000, treat any value less than or equal to 10 as 'small' (2 places right of 1000)
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(1000d), 3, 2, RoundingMode.FLOOR).toPlainString());
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(1990d), 3, 2, RoundingMode.FLOOR).toPlainString());
		assertEquals("2000", BigDecimalUtils.roundToScale(new BigDecimal(1990.000000001d), 3, 2, RoundingMode.FLOOR).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(10d), 3, 2, RoundingMode.FLOOR).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-1000d), 3, 2, RoundingMode.FLOOR).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-1010d), 3, 2, RoundingMode.FLOOR).toPlainString());
		assertEquals("-2000", BigDecimalUtils.roundToScale(new BigDecimal(-1010.00001d), 3, 2, RoundingMode.FLOOR).toPlainString());
		assertEquals("-1000", BigDecimalUtils.roundToScale(new BigDecimal(-999.999999d), 3, 2, RoundingMode.FLOOR).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(-10d), 3, 2, RoundingMode.FLOOR).toPlainString());

		//Zero rounding test
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal("0"), -2, 2, RoundingMode.FLOOR).toPlainString());


		//Some random samples
		assertEquals("1346000", BigDecimalUtils.roundToScale(new BigDecimal(1345456d), 3, 3, RoundingMode.UP).toPlainString());
		assertEquals("1345500000", BigDecimalUtils.roundToScale(new BigDecimal(1345456863d), 5, 6, RoundingMode.UP).toPlainString());
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(.0002345d), 3, 8, RoundingMode.UP).toPlainString());
		assertEquals("0", BigDecimalUtils.roundToScale(new BigDecimal(.0000001d), 3, 10, RoundingMode.UP).toPlainString());
		assertEquals("1000", BigDecimalUtils.roundToScale(new BigDecimal(.00000011d), 3, 10, RoundingMode.UP).toPlainString());
	}


	@Test public void testMakeBigDecimalWith0Digit() {

		BigDecimal bd = BigDecimalUtils.makeBigDecimal(1, digitAccuracyMultipliers[0], 0);
		assertEquals("1 scale: 0", bd2String(bd));

		bd = BigDecimalUtils.makeBigDecimal(0, digitAccuracyMultipliers[0], 0);
		assertEquals("0 scale: 0", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(-1, digitAccuracyMultipliers[0], 0);
		assertEquals("-1 scale: 0", bd2String(bd));

		bd = BigDecimalUtils.makeBigDecimal(1, digitAccuracyMultipliers[0], 1);
		assertEquals("1E+1 scale: -1", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(0, digitAccuracyMultipliers[0], 1);
		assertEquals("0E+1 scale: -1", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(-1, digitAccuracyMultipliers[0], 1);
		assertEquals("-1E+1 scale: -1", bd2String(bd));

		bd = BigDecimalUtils.makeBigDecimal(1, digitAccuracyMultipliers[0], -1);
		assertEquals("0.1 scale: 1", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(0, digitAccuracyMultipliers[0], -1);
		assertEquals("0.0 scale: 1", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(-1, digitAccuracyMultipliers[0], -1);
		assertEquals("-0.1 scale: 1", bd2String(bd));
	}


	@Test public void testMakeBigDecimalWithHalfDigit() {

		BigDecimal bd = BigDecimalUtils.makeBigDecimal(2, digitAccuracyMultipliers[1], 0);
		assertEquals("1 scale: 0", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(1, digitAccuracyMultipliers[1], 0);
		assertEquals("0.5 scale: 1", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(0, digitAccuracyMultipliers[1], 0);
		assertEquals("0 scale: 0", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(-1, digitAccuracyMultipliers[1], 0);
		assertEquals("-0.5 scale: 1", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(-2, digitAccuracyMultipliers[1], 0);
		assertEquals("-1 scale: 0", bd2String(bd));

		bd = BigDecimalUtils.makeBigDecimal(2, digitAccuracyMultipliers[1], 1);
		assertEquals("1E+1 scale: -1", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(1, digitAccuracyMultipliers[1], 1);
		assertEquals("5 scale: 0", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(0, digitAccuracyMultipliers[1], 1);
		assertEquals("0E+1 scale: -1", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(-1, digitAccuracyMultipliers[1], 1);
		assertEquals("-5 scale: 0", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(-2, digitAccuracyMultipliers[1], 1);
		assertEquals("-1E+1 scale: -1", bd2String(bd));

		bd = BigDecimalUtils.makeBigDecimal(2, digitAccuracyMultipliers[1], -1);
		assertEquals("0.1 scale: 1", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(1, digitAccuracyMultipliers[1], -1);
		assertEquals("0.05 scale: 2", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(0, digitAccuracyMultipliers[1], -1);
		assertEquals("0.0 scale: 1", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(-1, digitAccuracyMultipliers[1], -1);
		assertEquals("-0.05 scale: 2", bd2String(bd));
		bd = BigDecimalUtils.makeBigDecimal(-2, digitAccuracyMultipliers[1], -1);
		assertEquals("-0.1 scale: 1", bd2String(bd));
	}

	@Test public void testUniqueValueCount() {
		Double[] data0 = new Double[] {};
		Double[] data1 = new Double[] { 1D };
		Double[] data2a = new Double[] { 0D, 0d };
		Double[] data2b = new Double[] { 0d, 1d };
		Double[] data2c = new Double[] { -4d, 4d };
		Double[] data4a = new Double[] { 0d, 0d, 1d, 1d };
		Double[] data4b = new Double[] { 0d, 1d, 2d, 3d };

		assertEquals(0, getUniqueValueCount(data0, 0));
		assertEquals(0, getUniqueValueCount(data0, 10));

		assertEquals(1, getUniqueValueCount(data1, 0));
		assertEquals(1, getUniqueValueCount(data1, 2));

		assertEquals(1, getUniqueValueCount(data2a, 0));
		assertEquals(1, getUniqueValueCount(data2a, 1));
		assertEquals(2, getUniqueValueCount(data2b, 0));
		assertEquals(1, getUniqueValueCount(data2b, 1));
		assertEquals(2, getUniqueValueCount(data2b, 2));
		assertEquals(1, getUniqueValueCount(data2c, 1));
		assertEquals(2, getUniqueValueCount(data2c, 2));
		assertEquals(2, getUniqueValueCount(data2c, 3));

		assertEquals(2, getUniqueValueCount(data4a, 0));
		assertEquals(1, getUniqueValueCount(data4a, 1));
		assertEquals(2, getUniqueValueCount(data4a, 2));
		assertEquals(2, getUniqueValueCount(data4a, 3));
		assertEquals(4, getUniqueValueCount(data4b, 0));
		assertEquals(1, getUniqueValueCount(data4b, 1));
		assertEquals(2, getUniqueValueCount(data4b, 2));
		assertEquals(3, getUniqueValueCount(data4b, 3));
		assertEquals(4, getUniqueValueCount(data4b, 4));
		assertEquals(4, getUniqueValueCount(data4b, 5));
	}

	/**
	 * The <code>extractSortedData()</code> method will remove all the zeroes in
	 * the data and add back a single zero if any were removed
	 */
	@Test public void testExtractSortedData_RestoresASingleZero() {
		double[] unSortedData = {0.1, 0.11, 0, 0, 0.13, 0.15, 0.151, 0, 0.1522, 0.15223, 0.1523, 0.154, 0.1542, 0.16, 6000.1};
		ColumnDataWritable dataCol = makeColumnData(unSortedData, "", "");
		SimpleDataTableWritable table = new SimpleDataTableWritable();
		table.addColumn(dataCol);
		Double[] result = CalcBinning.extractSortedFilteredDoubleValues(table, 0, false);
		assertEquals("Three zeroes were removed and one added back in", unSortedData.length - 3 + 1, result.length);
	}
	
	public ColumnDataWritable makeColumnData(double[] data, String heading, String units ) {
		StandardNumberColumnDataWritable<Double> result = new StandardNumberColumnDataWritable<Double>();
		result.setName(heading);
		for (int i=0; i<data.length; i++) {
			result.setValue(data[i], i);
		}
		return result;
	}
	
    @Test public void testBuildEqualCountBinsSmallSet_HighestValueRoundDownPrevention() {

        Double[] sortedData = {0.1, 0.11, 0.13, 0.15, 0.151, 0.1522, 0.15223, 0.1523, 0.154, 0.1542, 0.16, 6000.1};
        int binCount = 3;
        BigDecimal[] result = CalcBinning.buildEqualCountBins(sortedData, binCount, true);
//        for (BigDecimal bin: result) {
//            System.out.println(bin);
//        }
        BigDecimal largestBin = result[result.length-1];
        assertTrue(sortedData[sortedData.length - 1] <= largestBin.doubleValue());
    }
    
    @Test public void testBuildEqualCountBinsSmallSet_BinRounding() {

        Double[] sortedData = {0.1, 0.11, 0.13, 0.15, 0.151, 0.1522, 0.15223, 0.1523, 0.154, 0.1542, 0.16, 6000.1};
        int binCount = 3;
        BigDecimal[] result = CalcBinning.buildEqualCountBins(sortedData, binCount, true);
        for (BigDecimal bin: result) {
            String stringResult = bin.toPlainString();
            assertTrue(stringResult.length() <=6);
        }
    }

    @Test public void testBuildEqualCountBins_HighestValueRoundDownPrevention() {

        Double[] sortedData = {0.1, 0.11, 0.13, 0.15, 0.151, 0.1522, 0.15223, 0.1523, 0.154, 0.1542, 0.16, 6000.1, 6000.1, 6000.1, 6000.1};
        int binCount = 5;
        BigDecimal[] result = CalcBinning.buildEqualCountBins(sortedData, binCount, true);
        BigDecimal largestBin = result[result.length-1];
        assertTrue(sortedData[sortedData.length - 1] <= largestBin.doubleValue());
        assertEquals("6000.1", largestBin.toPlainString());
    }
    
    @Test public void testBuildEqualCountBins_BinRounding() {

        Double[] sortedData = {0.1, 0.11, 0.13, 0.15, 0.151, 0.1522, 0.15223, 0.1523, 0.154, 0.1542, 0.16, 6000.1, 6000.1, 6000.1, 6000.1};
        int binCount = 5;
        BigDecimal[] result = CalcBinning.buildEqualCountBins(sortedData, binCount, true);
        for (BigDecimal bin: result) {
            String stringResult = bin.toPlainString();
            assertTrue(stringResult.length() <=7);
        }
    }

    @Test public void testBigDecimalDouble_RoundtripBehavior() {
    	for (double value = 0; value < 2; value += .001) {
    		BigDecimal bd = new BigDecimal(value);
    		assertTrue("Assuming absolute equality for " + value, value >= bd.doubleValue());
    		assertTrue("Assuming absolute equality for " + value, value <= bd.doubleValue());
    	}
    }

    @Test public void testBigDecimalFloat_RoundtripBehavior() {
    	for (float value = 0; value < 2; value += .001) {
    		BigDecimal bd = new BigDecimal(value);
    		assertTrue("Assuming absolute equality for " + value, value >= bd.floatValue());
    		assertTrue("Assuming absolute equality for " + value, value <= bd.floatValue());
    	}
    }

    @Test public void testBigDecimalFloatDouble_RoundtripBehavior() {
    	for (float value = 0; value < 2; value += .001) {
    		BigDecimal bd = new BigDecimal(value);
    		assertTrue("Assuming absolute equality for " + value, value >= bd.doubleValue());
    		assertTrue("Assuming absolute equality for " + value, value <= bd.doubleValue());
    	}
    }

    @Ignore // TEST fails, as expected
    @Test public void testBigDecimalDoubleFloat_RoundtripBehavior() {
    	for (double value = 0; value < 2; value += .001) {
    		BigDecimal bd = new BigDecimal(value);
    		assertTrue("Assuming absolute equality for " + value, value >= bd.floatValue());
    		assertTrue("Assuming absolute equality for " + value, value <= bd.floatValue());
    	}
    }

    @Test public void testBigDecimalDoubleStringParse_RoundtripBehavior() {
    	for (double value = 0; value < 2; value += .001) {
    		BigDecimal bd = new BigDecimal(value);
    		Double doubleValue = Double.parseDouble(bd.toPlainString());

    		assertTrue("Even the String parsing roundtrip behavior should be equal", doubleValue >= bd.doubleValue());
    		assertTrue("Even the String parsing roundtrip behavior should be equal", doubleValue <= bd.doubleValue());
    	}
    }

    @Ignore // TEST fails, as expected
    @Test public void testBigDecimalFloatStringParse_RoundtripBehavior() {
    	for (double value = 0; value < 2; value += .001) {
    		BigDecimal bd = new BigDecimal(value);
    		Float floatValue = Float.parseFloat(bd.toPlainString());

    		assertTrue("Even the String parsing roundtrip behavior should be equal for " + floatValue, floatValue >= bd.doubleValue());
    		assertTrue("Even the String parsing roundtrip behavior should be equal for " + floatValue, floatValue <= bd.doubleValue());
    	}
    }

	@SuppressWarnings("unused")
	private void printBinningResult(String message, BigDecimal[] result) {
		//System.out.println(message);
		for (int i=0; i<result.length; i++) {
			System.out.println(i + ": " + result[i]);
		}
	}

	@SuppressWarnings("unused")
	private void print(BigDecimal bd) {
		//System.out.println(bd2String(bd));
	}

	private String bd2String(BigDecimal bd) {
		return bd + " scale: " + bd.scale();
	}

	private void doEqualCountAssert(DataTable data, int binCount,
			boolean round, Double... expectedBins) {

		assertEquals(
				"TEST IS INCORRECTLY SETUP: " +
				"THE NUMBER OF BINS SHOULD MATCH (n + 1) THE EXPECTED BINS",
				binCount + 1, expectedBins.length);

		BigDecimal[] result = CalcBinning.buildEqualCountBins(data, 0, binCount, true, true);
		String desc = buildDescription(data, 0, expectedBins);

		doAssert(desc, result, expectedBins);
	}

	private void doEqualCountAssert(Double[] sorted, int binCount,
			boolean round, Double... expectedBins) {

		assertEquals(
				"TEST IS INCORRECTLY SETUP: " +
				"THE NUMBER OF BINS SHOULD MATCH (n + 1) THE EXPECTED BINS",
				binCount + 1, expectedBins.length);

		BigDecimal[] result = CalcBinning.buildEqualCountBins(sorted, binCount, round);
		String desc = buildDescription(sorted, expectedBins);

		doAssert(desc, result, expectedBins);
	}

	private static String buildDescription(DataTable data, int columnIndex, Double[] expectedBins) {
		String dataDesc = null;
		String binDesc = null;

		int rowCount = data.getRowCount();

		if (rowCount == 0) {
			dataDesc = "[empty]";
		} else if (rowCount < 10) {
			StringBuffer sb = new StringBuffer();
			for (int r=0; r< data.getRowCount(); r++) {
				sb.append(data.getDouble(r, columnIndex));
				sb.append(", ");
			}
			dataDesc = "(" + sb.substring(0, sb.length() - 1) + ")";
		} else {
			dataDesc =
				"{" + data.getDouble(0, columnIndex) +
				" to " + data.getDouble(rowCount - 1, columnIndex) + "} " +
				"(" + rowCount + " values)";
		}

		binDesc = "" + (expectedBins.length - 1) + " bins: " +
			ArrayUtils.toString(expectedBins);

		return "Data " + dataDesc + " Eq. Cnt. binned to " + binDesc;
	}

	private static String buildDescription(Double[] data, Double[] expectedBins) {
		String dataDesc = null;
		String binDesc = null;
		if (data.length < 10) {
			dataDesc = ArrayUtils.toString(data);
		} else {
			dataDesc =
				"{" + data[0] + " to " + data[data.length - 1] + "} " +
				"(" + data.length + " values)";
		}

		binDesc = "" + (expectedBins.length - 1) + " bins: " +
			ArrayUtils.toString(expectedBins);

		return "Data " + dataDesc + " Eq. Cnt. binned to " + binDesc;
	}


	private void doAssert(String description, BigDecimal[] calculatedBins, Double... expectedBins) {

		if (ENABLE_FAILURE_LOG_OUTPUT || ENABLE_ALL_LOG_OUTPUT) {

		}

		boolean testFailed = false;

		try {
			for (int i=0; i<calculatedBins.length; i++) {
				double calced = calculatedBins[i].doubleValue();
				double expect = expectedBins[i];
				double precision = expect / 10000d;

				try {
					assertEquals(expect, calced, precision);
				} catch (AssertionFailedError e) {
					testFailed = true;
					throw e;
				}
			}
		} finally {
			if (testFailed && (ENABLE_FAILURE_LOG_OUTPUT || ENABLE_ALL_LOG_OUTPUT)) {
				System.out.println("FAILED: " + description + ".  Calculated Bins:");
				for (int i=0; i<calculatedBins.length; i++) {
					System.out.println(i + ": " + calculatedBins[i]);
				}
			} else if (ENABLE_ALL_LOG_OUTPUT) {
				System.out.println(description + ".  Calculated Bins:");
				for (int i=0; i<calculatedBins.length; i++) {
					System.out.println(i + ": " + calculatedBins[i]);
				}
			}
		}
	}
	

	@Test public void testRoundToZero() {
		assertEquals("0", CalcBinning.round(.011, -.1, .44).toString());
	}

	@Test public void testRoundToSingleDigit() {
		//rounding up

		BigDecimal bd = CalcBinning.round(8.77, 5.2, 10.1);
		bd = bd.setScale(0);
		assertEquals("10", bd.toString());
		assertEquals("1", CalcBinning.round(.877, .52, 1.01).toString());
		assertEquals("0.1", CalcBinning.round(.0877, .052, 1.01).toString());
		assertEquals("1000", CalcBinning.round(870.7, 520, 1010).toString());

		// rounding down
		assertEquals("10", CalcBinning.round(9.47, 5.2, 10.1).toString());
		assertEquals("1", CalcBinning.round(.947, .52, 1.01).toString());
		assertEquals("0.1", CalcBinning.round(.0947, .052, 1.01).toString());
		assertEquals("1000", CalcBinning.round(940.7, 520, 1010).toString());
	}

	@Test public void testRoundTo1_5Digits() {
		// rounding up
		assertEquals("8.5", CalcBinning.round(8.67, 8.2, 8.91).toString());
		assertEquals("0.85", CalcBinning.round(.867, .82, .891).toString());
		assertEquals("0.085", CalcBinning.round(.0867, .082, .0891).toString());
		assertEquals("850", CalcBinning.round(860.7, 820, 891).toString());
	}

	@Test public void testRoundTo2Digits() {
		//rounding up
		assertEquals("8.8", CalcBinning.round(8.77, 8.52, 8.91).toString());
		assertEquals("0.88", CalcBinning.round(.877, .852, .891).toString());
		assertEquals("0.088", CalcBinning.round(.0877, .0852, .0891).toString());
		assertEquals("0.000088", CalcBinning.round(.0000877, .0000852, .0000891).toString());
		assertEquals("880", CalcBinning.round(877.77, 852, 891).toString());

		// rounding down
		assertEquals("9.4", CalcBinning.round(9.44, 9.25, 9.49).toString());
		assertEquals("0.94", CalcBinning.round(.944, .925, .949).toString());
		assertEquals("0.094", CalcBinning.round(.0944, .0925, .0949).toString());
		assertEquals("0.000094", CalcBinning.round(.0000944, .0000925, .0000949).toString());
		assertEquals("940", CalcBinning.round(940.7, 925, 949).toString());
	}

	@Test public void testRoundTo4Digits() {
		//rounding up
		assertEquals("8.888", CalcBinning.round(8.8877, 8.8871, 8.8887).toString());
		assertEquals("0.8888", CalcBinning.round(.88877, .88852, .88891).toString());
		assertEquals("0.08888", CalcBinning.round(.088877, .088852, .088891).toString());
		assertEquals("0.00008888", CalcBinning.round(.000088877, .000088852, .000088891).toString());
		assertEquals("8.888E+4", CalcBinning.round(88877.77, 88852, 88891).toString());

		// rounding down
		assertEquals("9.488", CalcBinning.round(9.4884, 9.48725, 9.4889).toString());
		assertEquals("0.9488", CalcBinning.round(.94884, .948725, .94889).toString());
		assertEquals("0.09488", CalcBinning.round(.09488, .0948725, .094889).toString());
		assertEquals("0.00009488", CalcBinning.round(.000094884, .0000948725, .000094889).toString());
		assertEquals("9.488E+4", CalcBinning.round(94880.7, 94872.5, 94889).toString());
	}

	@Test public void testRoundTo6Digits() {
		// 6-digit rounding now supported
		System.out.println(CalcBinning.round(8.770009000001, 8.770005000001, 8.770091).toString());
		assertTrue(CalcBinning.round(8.770009000001, 8.770005000001, 8.770091).toString().startsWith("8.77001"));
	}

	@Test public void testTooNarrowRoundingRange() {
		// String.startsWith() used since representation of value is not truncated,
		// (i.e., 8.770009000001 is returned as 8.770009000001[2342393758713480])
		System.out.println(CalcBinning.round(8.7700019000001, 8.7700015000001, 8.77000191).toString());
		assertTrue(CalcBinning.round(8.7700019000001, 8.7700015000001, 8.77000191).toString().startsWith("8.77000190000"));
	}

}
