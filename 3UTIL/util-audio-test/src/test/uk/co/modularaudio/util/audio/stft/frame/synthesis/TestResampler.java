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

package test.uk.co.modularaudio.util.audio.stft.frame.synthesis;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.stft.frame.synthesis.StftStreamResampler;
import uk.co.modularaudio.util.audio.stft.frame.synthesis.StftStreamingCubicInterpolationResampler;

public class TestResampler
{
	private static Log log = LogFactory.getLog( TestResampler.class.getName() );
	
	public TestResampler()
	{
	}
	
	public void go() throws Exception
	{
//		float resampleRate = 0.9f;
//		float resampleRate = 1.000f;
		float resampleRate = 1.01f;
//		float resampleRate = 1.1f;
//		float resampleRate = 1.5f;
//		float resampleRate = 1.9f;
//		float resampleRate = 2.0f;
		
//		StftStreamResampler r = new StftStreamingLinearInterpolationResampler();
		StftStreamResampler r = new StftStreamingCubicInterpolationResampler();
//		float[] inputData = new float[] { 0.0f, 0.25f, 0.5f, 0.75f, 1.0f, 0.75f, 0.5f, 0.25f };
		
		int NUM_FLOATS = 8;
		float[] inputData = new float[ NUM_FLOATS ];
		for( int i = 0 ; i < NUM_FLOATS ; i++ )
		{
			inputData[ i ] = (float)i;
		}
		
//		float[] inputData = new float[] { 0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 3.0f, 2.0f, 1.0f, 0.0f };
		
		float[] outputData = new float[ 4096 ];
		Arrays.fill( outputData, 0.0f );
		
		for( int i = 0 ; i < 100 ; i++ )
		{
			int numSamples = r.streamResample( resampleRate, inputData, inputData.length, outputData );
			log.debug("Created " + numSamples + " samples");
			printIt( i, numSamples, outputData );
		}
	}
	
	protected void printIt( int roundNum, int numSamples, float[] outputData )
	{
		float[] fp = new float[ numSamples ];
		System.arraycopy( outputData, 0, fp, 0, numSamples );
		log.debug("Round " + roundNum + " produced " + numSamples +" samples : " + Arrays.toString( fp ) );
	}
	
	public static void main( String[] args )
		throws Exception
	{
		TestResampler t = new TestResampler();
		t.go();
	}

}
