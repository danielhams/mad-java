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

package uk.co.modularaudio.util.audio.oscillatortable;



public class SingleWaveTableOscillator extends AbstractWavetableOscillator
{
	private CubicPaddedRawWaveTable singleWaveTable = null;

	public SingleWaveTableOscillator( final CubicPaddedRawWaveTable waveTable, final WaveTableValueFetcher valueFetcher, final PulseWidthMapper pulseWidthMapper )
	{
		super( valueFetcher, pulseWidthMapper );
		singleWaveTable = waveTable;
	}

	@Override
	public void oscillate( final float[] output, final float freq, final float iPhase, final float pulseWidth, final int outputIndex, final int length, final int sampleRate )
	{
		final double incr = freq / sampleRate;
//		final float phase = ( iPhase < 0 ? iPhase + 1 : iPhase );

		double pos = currentPosition;

		for( int i = 0 ; i < length ; i++ )
		{
			final float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidth, (float)pos );
			output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pwAdjustedPos );

			pos += incr;
			while( pos >= 1.0 ) pos -= 1.0;
			while( pos < 0.0 ) pos += 1.0;
		}
		currentPosition = pos;
	}

	@Override
	public void oscillate( final float[] output, final float freqs[], final float iPhase, final float pulseWidth, final int outputIndex, final int length, final int sampleRate )
	{
//		final float phase = ( iPhase < 0 ? iPhase + 1 : iPhase );

		double pos = currentPosition;

		if( pulseWidth == 1.0f )
		{
			for( int i = 0 ; i < length ; i++ )
			{
				output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, (float)pos );

				final double incr = freqs[i] / sampleRate;
				pos += incr;
				while( pos >= 1.0 ) pos -= 1.0;
				while( pos < 0.0 ) pos += 1.0;
			}
		}
		else
		{
			for( int i = 0 ; i < length ; i++ )
			{
				final float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidth, (float)pos );
				output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pwAdjustedPos );

				final double incr = freqs[i] / sampleRate;
				pos += incr;
				while( pos >= 1.0 ) pos -= 1.0;
				while( pos < 0.0 ) pos += 1.0;
			}
		}
		currentPosition = pos;
	}

	@Override
	public void oscillate( final float[] output, final float freq, final float iPhase,
			final float[] pulseWidths, final int outputIndex, final int length, final int sampleRate )
	{
		final double incr = freq / sampleRate;
//		final float phase = ( iPhase < 0 ? iPhase + 1 : iPhase );

		double pos = currentPosition;

		for( int i = 0 ; i < length ; i++ )
		{
			final float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidths[i], (float)pos );
			output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pwAdjustedPos );

			pos += incr;
			while( pos >= 1.0 ) pos -= 1.0;
			while( pos < 0.0 ) pos += 1.0;
		}
		currentPosition = pos;
	}

	@Override
	public void oscillate( final float[] output, final float[] freqs, final float iPhase, final float[] pulseWidths, final int outputIndex, final int length, final int sampleRate )
	{
//		final float phase = ( iPhase < 0 ? iPhase + 1 : iPhase );

		double pos = currentPosition;

		for( int i = 0 ; i < length ; i++ )
		{
			final int currentIndex = outputIndex + i;
			final float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidths[i], (float)pos );
			output[currentIndex] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pwAdjustedPos );

			final double incr = freqs[currentIndex] / sampleRate;
			pos += incr;
			while( pos >= 1.0 ) pos -= 1.0;
			while( pos < 0.0 ) pos += 1.0;
		}
		currentPosition = pos;
	}
}
