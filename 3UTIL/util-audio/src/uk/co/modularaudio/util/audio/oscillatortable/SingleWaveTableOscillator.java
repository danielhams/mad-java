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
	private final CubicPaddedRawWaveTable singleWaveTable;

	public SingleWaveTableOscillator( final CubicPaddedRawWaveTable waveTable, final WaveTableValueFetcher valueFetcher, final PulseWidthMapper pulseWidthMapper )
	{
		super( valueFetcher, pulseWidthMapper );
		singleWaveTable = waveTable;
	}

	@Override
	public void oscillate( final float freq,
			final float iPhase,
			final float pulseWidth,
			final float[] output, final int outputIndex, final int length, final int sampleRate )
	{
		final float incr = freq / sampleRate;
//		final float phase = ( iPhase < 0 ? iPhase + 1 : iPhase );

		float pos = currentPosition;

		for( int i = 0 ; i < length ; i++ )
		{
			final float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidth, pos );
			output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pwAdjustedPos );

			pos += incr;
			while( pos >= 1.0f ) pos -= 1.0f;
			while( pos < 0.0f ) pos += 1.0f;
		}
		currentPosition = pos;
	}

	@Override
	public void oscillate( final float freqs[], final int freqIndex,
			final float iPhase,
			final float pulseWidth,
			final float[] output, final int outputIndex, final int length, final int sampleRate )
	{
//		final float phase = ( iPhase < 0 ? iPhase + 1 : iPhase );

		float pos = currentPosition;

		if( pulseWidth == 1.0f )
		{
			for( int i = 0 ; i < length ; i++ )
			{
				output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pos );

				final float incr = freqs[freqIndex + i] / sampleRate;
				pos += incr;
				while( pos >= 1.0f ) pos -= 1.0f;
				while( pos < 0.0f ) pos += 1.0f;
			}
		}
		else
		{
			for( int i = 0 ; i < length ; i++ )
			{
				final float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidth, pos );
				output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pwAdjustedPos );

				final float incr = freqs[i] / sampleRate;
				pos += incr;
				while( pos >= 1.0f ) pos -= 1.0f;
				while( pos < 0.0f ) pos += 1.0f;
			}
		}
		currentPosition = pos;
	}

	@Override
	public void oscillate( final float freq,
			final float iPhase,
			final float[] pulseWidths, final int pwIndex,
			final float[] output, final int outputIndex, final int length, final int sampleRate )
	{
		final float incr = freq / sampleRate;
//		final float phase = ( iPhase < 0 ? iPhase + 1 : iPhase );

		float pos = currentPosition;

		for( int i = 0 ; i < length ; i++ )
		{
			final float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidths[pwIndex + i], pos );
			output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pwAdjustedPos );

			pos += incr;
			while( pos >= 1.0f ) pos -= 1.0f;
			while( pos < 0.0f ) pos += 1.0f;
		}
		currentPosition = pos;
	}

	@Override
	public void oscillate( final float[] freqs, final int freqIndex,
			final float iPhase,
			final float[] pulseWidths, final int pwIndex,
			final float[] output, final int outputIndex, final int length, final int sampleRate )
	{
//		final float phase = ( iPhase < 0 ? iPhase + 1 : iPhase );

		float pos = currentPosition;

		for( int i = 0 ; i < length ; i++ )
		{
			final float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidths[pwIndex + i], pos );
			output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pwAdjustedPos );

			final float incr = freqs[freqIndex + i] / sampleRate;
			pos += incr;
			while( pos >= 1.0f ) pos -= 1.0f;
			while( pos < 0.0f ) pos += 1.0f;
		}
		currentPosition = pos;
	}
}
