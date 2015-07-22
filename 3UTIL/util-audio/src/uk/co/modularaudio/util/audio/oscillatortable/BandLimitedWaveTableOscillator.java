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


public class BandLimitedWaveTableOscillator extends AbstractWavetableOscillator
{
	private final BandLimitedWaveTableMap bandWaveTableMap;

	private float prevFreq = -1.0f;
	private CubicPaddedRawWaveTable waveTableForFreq = null;

	public BandLimitedWaveTableOscillator( final BandLimitedWaveTableMap waveTableMap, final WaveTableValueFetcher valueFetcher, final PulseWidthMapper pulseWidthMapper )
	{
		super( valueFetcher, pulseWidthMapper );
		this.bandWaveTableMap = waveTableMap;
	}

	@Override
	public void oscillate( final float[] output, final float freq, final float iPhase, final float pulseWidth, final int outputIndex, final int length, final int sampleRate )
	{
		final float freqDiff = (prevFreq - freq);
		final float absFreqDiff = ( freqDiff < 0.0f ? -freqDiff : freqDiff );
		// Only check for a new table every 20hz of difference
		if( absFreqDiff > 20.0f || waveTableForFreq == null )
		{
			waveTableForFreq = bandWaveTableMap.getWaveTableForFrequency( freq );
			prevFreq = freq;
		}
		final double incr = freq / sampleRate;
//		final float phase = ( iPhase < 0 ? iPhase + 1 : iPhase );

		double pos = currentPosition;

		for( int i = 0 ; i < length ; i++ )
		{
			final float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidth, (float)pos );
			output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( waveTableForFreq, pwAdjustedPos );

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

		for( int i = 0 ; i < length ; i++ )
		{
			final int currentOutputIndex = outputIndex + i;
			final float freq = freqs[ currentOutputIndex ];
			if( freq != prevFreq )
			{
				waveTableForFreq = bandWaveTableMap.getWaveTableForFrequency( freq );
				prevFreq = freq;
			}

			final float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidth, (float)pos );
			output[currentOutputIndex] = valueFetcher.getValueAtNormalisedPosition( waveTableForFreq, pwAdjustedPos );

			final double incr = freq / sampleRate;
			pos += incr;
			while( pos >= 1.0 ) pos -= 1.0;
			while( pos < 0.0 ) pos += 1.0;
		}
		currentPosition = pos;
	}

	@Override
	public void oscillate( final float[] output, final float freq, final float iPhase,
			final float[] pulseWidths, final int outputIndex, final int length, final int sampleRate )
	{
		if( freq != prevFreq )
		{
			waveTableForFreq = bandWaveTableMap.getWaveTableForFrequency( freq );
			prevFreq = freq;
		}
		final double incr = freq / sampleRate;
//		final float phase = ( iPhase < 0 ? iPhase + 1 : iPhase );

		double pos = currentPosition;

		for( int i = 0 ; i < length ; i++ )
		{
			final int currentOutputIndex = outputIndex + i;
			final float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidths[ currentOutputIndex ], (float)pos );
			output[ currentOutputIndex ] = valueFetcher.getValueAtNormalisedPosition( waveTableForFreq, pwAdjustedPos );

			pos += incr;
			while( pos >= 1.0 ) pos -= 1.0;
			while( pos < 0.0 ) pos += 1.0;
		}
		currentPosition = pos;
	}

	@Override
	public void oscillate( final float[] output, final float[] freqs, final float iPhase,
			final float[] pulseWidths, final int outputIndex, final int length, final int sampleRate )
	{
//		final float phase = ( iPhase < 0 ? iPhase + 1 : iPhase );

		double pos = currentPosition;

		for( int i = 0 ; i < length ; i++ )
		{
			final int currentOutputIndex = outputIndex + i;
			final float freq = freqs[ currentOutputIndex ];
			if( freq != prevFreq )
			{
				waveTableForFreq = bandWaveTableMap.getWaveTableForFrequency( freq );
				prevFreq = freq;
			}

			final float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidths[ currentOutputIndex ], (float)pos );
			output[ currentOutputIndex ] = valueFetcher.getValueAtNormalisedPosition( waveTableForFreq, pwAdjustedPos );

			final double incr = freq / sampleRate;
			pos += incr;
			while( pos >= 1.0 ) pos -= 1.0;
			while( pos < 0.0 ) pos += 1.0;
		}
		currentPosition = pos;
	}
}
