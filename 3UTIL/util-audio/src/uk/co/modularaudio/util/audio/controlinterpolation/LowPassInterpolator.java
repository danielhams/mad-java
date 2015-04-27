/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.util.audio.controlinterpolation;

import java.util.Arrays;

import uk.co.modularaudio.util.audio.dsp.ButterworthFilter;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;

public class LowPassInterpolator implements ControlValueInterpolator
{
//	private static Log log = LogFactory.getLog( LowPassInterpolator.class.getName() );

	private float desVal;

	private int sampleRate;
	private final ButterworthFilter lpFilter = new ButterworthFilter();

	private static final float LP_FREQ = 30.0f;
//	private static final float LP_FREQ = 65.0f;

	private static final int TMP_LENGTH = 1024;
	private static final int NUM_RESET_ITERS = 10;

	public LowPassInterpolator()
	{
	}

	public void reset( final int sampleRate, final float valueChaseMillis )
	{
		this.sampleRate = sampleRate;
	}

	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		final int lastIndex = outputIndex + length;
		Arrays.fill( output, outputIndex, lastIndex, desVal );
		lpFilter.filter( output, outputIndex, length, LP_FREQ, 0.5f, FrequencyFilterMode.LP, sampleRate );
//		for( int i = outputIndex ; i < lastIndex ; ++i )
//		{
//			final float curVal = output[ i ];
//			output[ i ] = ( curVal < -1.0f ? -1.0f :
//				( curVal > 1.0f ? 1.0f :
//					curVal ) );
//		}
	}

	@Override
	public void notifyOfNewValue( final float newValue )
	{
		desVal = newValue;
	}

	@Override
	public boolean checkForDenormal()
	{
		return false;
	}

	@Override
	public void hardSetValue( final float value )
	{
		this.desVal = value;
		final float[] tmpArray = new float[TMP_LENGTH];
		Arrays.fill( tmpArray, desVal );

		for( int i = 0 ; i < NUM_RESET_ITERS ; ++i )
		{
			lpFilter.filter( tmpArray, 0, TMP_LENGTH, LP_FREQ, 0.5f, FrequencyFilterMode.LP, sampleRate );
		}
	}

	@Override
	public void resetLowerUpperBounds( final float lowerBound, final float upperBound )
	{
	}
}
