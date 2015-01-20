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

package uk.co.modularaudio.util.audio.wavetablent;


public class BandLimitedWaveTableOscillator extends AbstractWavetableOscillator
{
	private BandLimitedWaveTableMap bandWaveTableMap;
	
	private float prevFreq = -1.0f;
	private CubicPaddedRawWaveTable waveTableForFreq = null;
	
	public BandLimitedWaveTableOscillator( BandLimitedWaveTableMap waveTableMap, WaveTableValueFetcher valueFetcher, PulseWidthMapper pulseWidthMapper )
	{
		super( valueFetcher, pulseWidthMapper );
		this.bandWaveTableMap = waveTableMap;
	}

	@Override
	public void oscillate( float[] output, float freq, float phase, float pulseWidth, int outputIndex, int length, int sampleRate )
	{
		float freqDiff = (prevFreq - freq);
		float absFreqDiff = ( freqDiff < 0.0f ? -freqDiff : freqDiff );
		// Only check for a new table every 20hz of difference
		if( absFreqDiff > 20.0f )
		{
			waveTableForFreq = bandWaveTableMap.getWaveTableForFrequency( freq );
			prevFreq = freq;
		}
		float incr = freq / sampleRate;
		phase = ( phase < 0 ? phase + 1 : phase );

		float pos = currentPosition;
		
		for( int i = 0 ; i < length ; i++ )
		{
			float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidth, pos );
			output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( waveTableForFreq, pwAdjustedPos );
			
			pos += incr;
			while( pos >= 1.0f ) pos -= 1.0f;
			while( pos < 0.0f ) pos += 1.0f;
		}
		currentPosition = pos;
	}
	
	@Override
	public void oscillate( float[] output, float freqs[], float phase, float pulseWidth, int outputIndex, int length, int sampleRate )
	{
		phase = ( phase < 0 ? phase + 1 : phase );

		float pos = currentPosition;
		
		for( int i = 0 ; i < length ; i++ )
		{
			int currentOutputIndex = outputIndex + i;
			float freq = freqs[ currentOutputIndex ];
			if( freq != prevFreq )
			{
				waveTableForFreq = bandWaveTableMap.getWaveTableForFrequency( freq );
				prevFreq = freq;
			}
			
			float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidth, pos );
			output[currentOutputIndex] = valueFetcher.getValueAtNormalisedPosition( waveTableForFreq, pwAdjustedPos );
			
			float incr = freq / sampleRate;
			pos += incr;
			while( pos >= 1.0f ) pos -= 1.0f;
			while( pos < 0.0f ) pos += 1.0f;
		}
		currentPosition = pos;
	}

	@Override
	public void oscillate( float[] output, float freq, float phase,
			float[] pulseWidths, int outputIndex, int length, int sampleRate )
	{
		if( freq != prevFreq )
		{
			waveTableForFreq = bandWaveTableMap.getWaveTableForFrequency( freq );
			prevFreq = freq;
		}
		float incr = freq / sampleRate;
		phase = ( phase < 0 ? phase + 1 : phase );

		float pos = currentPosition;
		
		for( int i = 0 ; i < length ; i++ )
		{
			int currentOutputIndex = outputIndex + i;
			float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidths[ currentOutputIndex ], pos );
			output[ currentOutputIndex ] = valueFetcher.getValueAtNormalisedPosition( waveTableForFreq, pwAdjustedPos );
			
			pos += incr;
			while( pos >= 1.0f ) pos -= 1.0f;
			while( pos < 0.0f ) pos += 1.0f;
		}
		currentPosition = pos;
	}

	@Override
	public void oscillate( float[] output, float[] freqs, float phase,
			float[] pulseWidths, int outputIndex, int length, int sampleRate )
	{
		phase = ( phase < 0 ? phase + 1 : phase );

		float pos = currentPosition;
		
		for( int i = 0 ; i < length ; i++ )
		{
			int currentOutputIndex = outputIndex + i;
			float freq = freqs[ currentOutputIndex ];
			if( freq != prevFreq )
			{
				waveTableForFreq = bandWaveTableMap.getWaveTableForFrequency( freq );
				prevFreq = freq;
			}
			
			float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidths[i], pos );
			output[ currentOutputIndex ] = valueFetcher.getValueAtNormalisedPosition( waveTableForFreq, pwAdjustedPos );
			
			float incr = freq / sampleRate;
			pos += incr;
			while( pos >= 1.0f ) pos -= 1.0f;
			while( pos < 0.0f ) pos += 1.0f;
		}
		currentPosition = pos;
	}
}
