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



public class SingleWaveTableOscillator extends AbstractWavetableOscillator
{
	private CubicPaddedRawWaveTable singleWaveTable = null;

	public SingleWaveTableOscillator( CubicPaddedRawWaveTable waveTable, WaveTableValueFetcher valueFetcher, PulseWidthMapper pulseWidthMapper )
	{
		super( valueFetcher, pulseWidthMapper );
		singleWaveTable = waveTable;
	}

	@Override
	public void oscillate( float[] output, float freq, float phase, float pulseWidth, int outputIndex, int length, int sampleRate )
	{
		float incr = freq / sampleRate;
		phase = ( phase < 0 ? phase + 1 : phase );

		float pos = currentPosition;
		
		for( int i = 0 ; i < length ; i++ )
		{
			float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidth, pos );
			output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pwAdjustedPos );
			
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
			float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidth, pos );
			output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pwAdjustedPos );
			
			float incr = freqs[i] / sampleRate;
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
		float incr = freq / sampleRate;
		phase = ( phase < 0 ? phase + 1 : phase );

		float pos = currentPosition;
		
		for( int i = 0 ; i < length ; i++ )
		{
			float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidths[i], pos );
			output[outputIndex + i] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pwAdjustedPos );
			
			pos += incr;
			while( pos >= 1.0f ) pos -= 1.0f;
			while( pos < 0.0f ) pos += 1.0f;
		}
		currentPosition = pos;
	}

	@Override
	public void oscillate( float[] output, float[] freqs, float phase, float[] pulseWidths, int outputIndex, int length, int sampleRate )
	{
		phase = ( phase < 0 ? phase + 1 : phase );

		float pos = currentPosition;
		
		for( int i = 0 ; i < length ; i++ )
		{
			int currentIndex = outputIndex + i;
			float pwAdjustedPos = pulseWidthMapper.adjustPwPos( pulseWidths[i], pos );
			output[currentIndex] = valueFetcher.getValueAtNormalisedPosition( singleWaveTable, pwAdjustedPos );
			
			float incr = freqs[currentIndex] / sampleRate;
			pos += incr;
			while( pos >= 1.0f ) pos -= 1.0f;
			while( pos < 0.0f ) pos += 1.0f;
		}
		currentPosition = pos;
	}
}
