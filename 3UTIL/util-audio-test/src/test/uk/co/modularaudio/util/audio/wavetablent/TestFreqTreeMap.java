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

package test.uk.co.modularaudio.util.audio.wavetablent;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.oscillatortable.Oscillator;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactory;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorInterpolationType;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveTableType;

public class TestFreqTreeMap extends TestCase
{
	private static Log log = LogFactory.getLog( TestFreqTreeMap.class.getName() );

	public void testInsertingIntoMap() throws Exception
	{
		final String waveTableCacheRoot = "wavetablecache";
		final OscillatorFactory of = OscillatorFactory.getInstance( waveTableCacheRoot );

		final Oscillator testOscillator = of.createOscillator( OscillatorWaveTableType.BAND_LIMITED, OscillatorInterpolationType.CUBIC, OscillatorWaveShape.SAW );

		final int outputLength = 2048;
		final float[] dumbOutput = new float[ outputLength ];

		final float[] testFrequencies = new float[] { -10.0f, 0.0f, 100.0f, 1000.0f, 2000.0f, 32000.0f };
//		final float[] testFrequencies = new float[] { 0.0f, 61.7f, 61.8f, 99.0f, 124.0f, 248.0f };
		for( final float testFrequency : testFrequencies )
		{
			log.info( "Using oscillator at frequency: " + testFrequency );
			testOscillator.oscillate( testFrequency, 0.0f, 1.0f, dumbOutput, 0, outputLength, 44100 );
		}
	}

}
