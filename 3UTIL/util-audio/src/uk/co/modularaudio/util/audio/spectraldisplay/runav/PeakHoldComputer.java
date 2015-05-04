package uk.co.modularaudio.util.audio.spectraldisplay.runav;

import java.util.Arrays;

public class PeakHoldComputer implements RunningAverageComputer
{
	private final float[] binValues = new float[ 16384 ];

	public PeakHoldComputer()
	{
		reset();
	}

	@Override
	public void computeNewRunningAverages( final int currentNumBins, final float[] valuesToAdd, final float[] runningValues )
	{
		for( int i = 0 ; i < currentNumBins ; ++i )
		{
			if( valuesToAdd[i] > binValues[i] )
			{
				binValues[i] = valuesToAdd[i];
			}
		}

		System.arraycopy( binValues, 0, runningValues, 0, currentNumBins );
	}

	public final void reset()
	{
		Arrays.fill( binValues, 0.0f );
	}
}
