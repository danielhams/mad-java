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

package test.uk.co.modularaudio.service.audioanalysis.beatdetection;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

import uk.co.modularaudio.service.audioanalysis.impl.analysers.beatdetection.BeatDetectionRT;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.beatdetection.BeatDetector;
import uk.co.modularaudio.util.audio.wavetablent.Oscillator;
import uk.co.modularaudio.util.audio.wavetablent.OscillatorFactory;
import uk.co.modularaudio.util.audio.wavetablent.OscillatorInterpolationType;
import uk.co.modularaudio.util.audio.wavetablent.OscillatorWaveShape;
import uk.co.modularaudio.util.audio.wavetablent.OscillatorWaveTableType;

public class TestBeatDetector extends TestCase
{
	private static Log log = LogFactory.getLog( TestBeatDetector.class.getName() );
	
	static
	{
		BasicConfigurator.configure();
	}
	
	public void testBeatDetector() throws Exception
	{
		int sampleRate = 44100;
		OscillatorFactory of = OscillatorFactory.getInstance( "wavetablecache" );
		Oscillator osc = of.createOscillator( OscillatorWaveTableType.SINGLE, OscillatorInterpolationType.LINEAR, OscillatorWaveShape.SINE );
		
		float[] input = new float[1024];
		for( int i = 0 ; i < 1024 ; i++ )
		{
			input[i] = 0.0f;
		}

		float[] oneval = new float[1];
		for( int i = 0 ; i < 512 ; i++ )
		{
//			float amp = 128.0f / (128 * (i + 1));
			osc.oscillate( input, 40.0f, 0.0f, 1.0f, 512 + i, 1, sampleRate );
			input[ i + 512 ] = oneval[0];
		}
		for( int i = 0 ; i < 1024 ; i++ )
		{
			log.debug("input["+i+"]=" + input[i]);
		}
		float[] output = new float[1024];
		BeatDetectionRT rt = new BeatDetectionRT( 1, 1024 );
		BeatDetector bd = new BeatDetector();
		bd.detect( rt, input, output );
		log.debug("Found bpm= " + rt.getBpm() + " " + rt.getConfidence());
		bd.detect( rt, input, output );
		log.debug("Found bpm= " + rt.getBpm() + " " + rt.getConfidence());
		bd.detect( rt, input, output );
		log.debug("Found bpm= " + rt.getBpm() + " " + rt.getConfidence());
		bd.detect( rt, input, output );
		log.debug("Found bpm= " + rt.getBpm() + " " + rt.getConfidence());
		bd.detect( rt, input, output );
		log.debug("Found bpm= " + rt.getBpm() + " " + rt.getConfidence());
		bd.detect( rt, input, output );
		log.debug("Found bpm= " + rt.getBpm() + " " + rt.getConfidence());
		bd.detect( rt, input, output );
		log.debug("Found bpm= " + rt.getBpm() + " " + rt.getConfidence());
		bd.detect( rt, input, output );
		log.debug("Found bpm= " + rt.getBpm() + " " + rt.getConfidence());
		bd.detect( rt, input, output );
		log.debug("Found bpm= " + rt.getBpm() + " " + rt.getConfidence());
		bd.detect( rt, input, output );
		log.debug("Found bpm= " + rt.getBpm() + " " + rt.getConfidence());
		bd.detect( rt, input, output );
		log.debug("Found bpm= " + rt.getBpm() + " " + rt.getConfidence());
	}
}
