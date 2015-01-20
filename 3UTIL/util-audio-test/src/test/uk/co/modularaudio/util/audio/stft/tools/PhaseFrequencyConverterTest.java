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

package test.uk.co.modularaudio.util.audio.stft.tools;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.fft.FftWindow;
import uk.co.modularaudio.util.audio.fft.HannFftWindow;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.tools.PhaseFrequencyConverter;
import uk.co.modularaudio.util.math.MathDefines;
import uk.co.modularaudio.util.math.MathFormatter;

public class PhaseFrequencyConverterTest extends TestCase
{
	private static Log log = LogFactory.getLog( PhaseFrequencyConverterTest.class.getName() );
	
	private StftParameters parameters;
	private PhaseFrequencyConverter phaseFrequencyConverter;

	DataRate outputRate = DataRate.SR_44100;
	int sampleRate = outputRate.getValue();
	int numChannels = 1;
	int windowLength = 1024;
	int numOverlaps = 4;
	int numReals = 1024;
	int analysisStepSize = numReals / numOverlaps;
	
	FftWindow fftWindow;
	
	protected void setUp() throws Exception
	{
		fftWindow = new HannFftWindow( numReals );
		
		parameters = new StftParameters( outputRate, numChannels, windowLength, numOverlaps, numReals, fftWindow );
		
		phaseFrequencyConverter = new PhaseFrequencyConverter( parameters );
	}

	protected void tearDown() throws Exception
	{
	}
	
	public void testPhaseToFrequency() throws Exception
	{
		log.debug( "Beginning phase to frequency tests" );
		
//		int peakBinIndex = 256;
//		int peakBinIndex = 255;
//		int peakBinIndex = 232;
		int peakBinIndex = 128;
		
		float binCenterFrequency = (float)phaseFrequencyConverter.getBinCenterFreqs()[ peakBinIndex ];
		float freqPerBin = (float)phaseFrequencyConverter.getFreqPerBin();
		
		float numRevsPerSample = binCenterFrequency / sampleRate;
		float radiansRotationForLength = numRevsPerSample * MathDefines.TWO_PI_F * analysisStepSize;
		float degreesRotationForLength = absolutertod( radiansRotationForLength );
		
		log.debug("Bin " + peakBinIndex + " center frequency is " + MathFormatter.slowFloatPrint( binCenterFrequency ) );
		log.debug("Freq per bin is " + freqPerBin );
		float minFreq = binCenterFrequency - (freqPerBin / 2 );
		float maxFreq = binCenterFrequency + (freqPerBin / 2 );
		log.debug("So min(" + MathFormatter.slowFloatPrint( minFreq ) + ") max(" + MathFormatter.slowFloatPrint( maxFreq ) + ")");
		
		// Test calculation starting from various phases with differences of -PI -> +PI
		int NUM_SEGMENTS = 5;
		
		int numTotalTests = NUM_SEGMENTS * NUM_SEGMENTS;
		
		float[] segmentPhasesToTest = new float[ numTotalTests * 2];
		
		float[] startDegrees = new float[] { -179.0f, -90.0f, 0.0f, 90.0f, 179.0f };
		float[] changeDegrees = new float[] { -179.0f, -90.0f, 0.0f, 90.0f, 179.0f };
		
		int radIndex = 0;
		for( int i = 0 ; i < startDegrees.length ; i++ )
		{
			float start = startDegrees[ i ];
			for( int j = 0 ; j < changeDegrees.length ; j++ )
			{
				float change = changeDegrees[ j ];
				
				float scaledChangeDegrees = change * ((float)analysisStepSize / windowLength);
				
				float end = start + degreesRotationForLength + scaledChangeDegrees;
				while( end < -180.0f ) end += 360.0f;
				while( end > 180.0f ) end -= 360.0f;
				
				float startRadians = plusMinus180dtor( start );
				float endRadians = plusMinus180dtor( end );
				segmentPhasesToTest[ radIndex++ ] = startRadians;
				segmentPhasesToTest[ radIndex++ ] = endRadians;
			}
		}

		int curInputIndex = 0;
		for( int i = 0 ; i < numTotalTests ; i++ )
		{
			float phaseStart = segmentPhasesToTest[ curInputIndex ];
			float phaseEnd = segmentPhasesToTest[ curInputIndex + 1 ];
			
			curInputIndex += 2;

			log.debug("Test " + i + " calculating frequency for oldPhase(" + MathFormatter.slowFloatPrint( phaseStart, 5, true ) + ") to newPhase(" +
					MathFormatter.slowFloatPrint( phaseEnd, 5, true ) + ")");
			double phaseStartDegrees = phaseFrequencyConverter.radiansPmPiToDegrees( phaseStart );
			double phaseEndDegrees = phaseFrequencyConverter.radiansPmPiToDegrees( phaseEnd );
			log.debug("In degrees oldPhase(" + MathFormatter.slowFloatPrint( (float)phaseStartDegrees, 5, true ) + ") to newPhase(" +
					MathFormatter.slowFloatPrint( (float)phaseEndDegrees, 5, true ) + ")");
			double degreesTurn = phaseEndDegrees - phaseStartDegrees - (degreesRotationForLength);
			while( degreesTurn < -180.0 ) degreesTurn += 360.0;
			while( degreesTurn > 180.0 ) degreesTurn -= 360.0;
			log.debug("Which is a rotation of " + MathFormatter.slowFloatPrint( (float)degreesTurn, 5, true ) + " degrees");
			
			float freq = phaseFrequencyConverter.phaseToFreq( phaseEnd,
				phaseStart,
				peakBinIndex );
		
			log.debug( "Calculated the frequency: " + freq );
		
			if( freq < minFreq )
			{
				log.error( "Frequency falls under expected min for bin" );
			}
			else if( freq > maxFreq )
			{
				log.error( "Frequency falls over expected max for bin" );
			}
		}

		log.debug( "Phase to frequency tests done" );
	}
	
	public float plusMinus180dtor( float degrees )
	{
		return (degrees / 180.0f) * MathDefines.ONE_PI_F;
	}
	
	public float absolutertod( float radians )
	{
		return (radians * 360.0f / MathDefines.TWO_PI_F);
	}

}
