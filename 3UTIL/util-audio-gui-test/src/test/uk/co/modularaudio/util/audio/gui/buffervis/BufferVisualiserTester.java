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

package test.uk.co.modularaudio.util.audio.gui.buffervis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.math.NormalisedValuesMapper;

public class BufferVisualiserTester
{
	private static Log log = LogFactory.getLog( BufferVisualiserTester.class.getName() );

	public void testShowOne() throws Exception
	{
		final BufferVisualiser bv = new BufferVisualiser();
		final BufferVisualiserFrame f = new BufferVisualiserFrame( bv );

//		float[] testFloats = new float[] { -0.5f, 0.0f, 0.5f, 0.75f, 1.0f };
		final float[] testFloatsOne = new float[ 200 ];
		final float[] testFloatsTwo = new float[ 200 ];

//		BandLimitedWaveTable square = StandardBandLimitedWaveTables.getBandLimitedSquareWaveTable();
////
//		BandLimitedWaveTableOscillator osc = new BandLimitedWaveTableOscillator();
//		osc.oscillate( square, testFloats, 200.0f, 0.25f, 0, testFloats.length, 44100 );

		for( int i = 0 ; i < testFloatsOne.length ; i++ )
		{
			final float normalisedVal = ((float)i ) / (testFloatsOne.length - 1);

			// Straight X = Y line.
//			testFloats[ i ] = normalisedVal;

			testFloatsOne[ i ] = NormalisedValuesMapper.expMapF( normalisedVal );
//			testFloatsOne[ i ] = NormalisedValuesMapper.logMapF( normalisedVal );
//			testFloats[ i ] = NormalisedValuesMapper.cosMapF( normalisedVal );
//			testFloats[ i ] = NormalisedValuesMapper.sinMapF( normalisedVal );
//			testFloatsOne[ i ] = NormalisedValuesMapper.expMinMaxMapF( normalisedVal, 0.0f, 10000.0f );

//			testFloats[ i ] = NormalisedValuesMapper.circleQuadOneF( normalisedVal );
//			testFloats[ i ] = NormalisedValuesMapper.circleQuadTwoF( normalisedVal );
//			testFloats[ i ] = NormalisedValuesMapper.circleQuadThreeF( normalisedVal );
//			testFloats[ i ] = NormalisedValuesMapper.circleQuadOneF( normalisedVal );

			testFloatsTwo[ i ] = NormalisedValuesMapper.logMapF( normalisedVal );
//			testFloatsTwo[ i ] = NormalisedValuesMapper.expMinMaxMapF( normalisedVal, 0.0f, 10000.0f );

			log.debug("NV(" + normalisedVal + ") -> (" + testFloatsOne[i] + ")");
		}

//		bv.regenerateFromBuffers( "expMapF", testFloatsOne, testFloatsOne.length, "expMinMaxMapF(0.0,10000.0)", testFloatsTwo );
		bv.regenerateFromBuffers( "expMapF", testFloatsOne, testFloatsOne.length, "logMapF", testFloatsTwo );

		f.setVisible( true );

		while( f.isVisible() )
		{
			Thread.sleep( 100 );
		}
	}

	public static void main( final String[] args ) throws Exception
	{
		final BufferVisualiserTester bvt = new BufferVisualiserTester();
		bvt.testShowOne();
	}

}
