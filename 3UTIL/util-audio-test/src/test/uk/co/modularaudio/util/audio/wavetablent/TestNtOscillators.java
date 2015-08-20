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

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.oscillatortable.NoWaveTableForShapeException;
import uk.co.modularaudio.util.audio.oscillatortable.Oscillator;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactory;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactoryException;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorInterpolationType;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveTableType;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestNtOscillators extends TestCase
{
	private static Log log = LogFactory.getLog( TestNtOscillators.class.getName() );

	public void testUsingSingleOscillator() throws Exception
	{
		final String pathToCacheRoot = "wavetablecache";
		log.debug("Obtaining oscillator reference");
		final OscillatorWaveShape[] shapes = OscillatorWaveShape.values();
		log.debug("Done");
		final OscillatorWaveTableType[] tableTypes = OscillatorWaveTableType.values();

		for( final OscillatorWaveShape shape : shapes )
		{
			for( final OscillatorWaveTableType tableType : tableTypes )
			{
				log.debug( "Doing " + tableType + " " + shape );
				onePassOfShapeAndType( pathToCacheRoot, tableType, shape );
			}
		}
	}

	private void onePassOfShapeAndType( final String pathToCacheRoot,
			final OscillatorWaveTableType waveTableType,
			final OscillatorWaveShape shapeToUse )
			throws IOException, NoWaveTableForShapeException,
			OscillatorFactoryException
	{
		final OscillatorFactory oscillatorFactory = OscillatorFactory.getInstance( pathToCacheRoot );

		final Oscillator truncatingSineOscillator = oscillatorFactory.createOscillator( waveTableType,
				OscillatorInterpolationType.TRUNCATING,
				shapeToUse );

		final float freqToGen = 200.0f;
		final int testBufferLength = 128;
		final float[] testBuffer = new float[ testBufferLength ];

		final float phase = 0.0f;
		final float pulseWidth = 1.0f;
		final int sampleRate = 44100;
		truncatingSineOscillator.oscillate( freqToGen, phase, pulseWidth, testBuffer, 0, testBufferLength, sampleRate );

		String results = MathFormatter.floatArrayPrint( testBuffer, 5 );

		log.debug("TR results are " + results );

		final Oscillator liSineOscillator = oscillatorFactory.createOscillator( waveTableType,
				OscillatorInterpolationType.LINEAR,
				shapeToUse );

		liSineOscillator.oscillate(  freqToGen, phase, pulseWidth, testBuffer, 0, testBufferLength, sampleRate );

		results = MathFormatter.floatArrayPrint( testBuffer, 5 );

		log.debug("LI results are " + results );

		final Oscillator ciSineOscillator = oscillatorFactory.createOscillator( waveTableType,
				OscillatorInterpolationType.CUBIC,
				shapeToUse );

		ciSineOscillator.oscillate( freqToGen, phase, pulseWidth, testBuffer, 0, testBufferLength, sampleRate );

		results = MathFormatter.floatArrayPrint( testBuffer, 5 );

		log.debug("CI results are " + results );
	}
}
