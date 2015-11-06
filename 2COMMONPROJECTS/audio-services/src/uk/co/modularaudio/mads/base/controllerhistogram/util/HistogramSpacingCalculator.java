package uk.co.modularaudio.mads.base.controllerhistogram.util;

public class HistogramSpacingCalculator
{

	public static int calculateEventMarkerSpacing( final int height )
	{
		// Work out rounded num pixels per marker so we have integer number
		// and don't exceed the specified height.
		final int roundedNum = (int)Math.floor(
				(height-HistogramDisplay.MARKER_PADDING) /
				(HistogramDisplay.NUM_EVENT_MARKERS-1) );

		return roundedNum;
	}

	public static int calculateBinMarkerSpacing( final int width )
	{
		final int roundedNum = (int)Math.floor(
				(width-HistogramDisplay.MARKER_PADDING) /
				(HistogramDisplay.NUM_BIN_MARKERS-1) );
		return roundedNum;
	}

}
