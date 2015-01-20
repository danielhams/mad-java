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

package test.uk.co.modularaudio.util.audio.fileio;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.fileio.WaveFileReader;

public class TestWaveFileReading extends TestCase
{
	private static Log log = LogFactory.getLog( TestWaveFileReading.class.getName() );
	
	public void testWriteThenReadAndCompare()
		throws Exception
	{
//		String filename = "/tmp/cppwavout.wav";
//		String filename = "/media/663099F83099D003/Music/PhaseVocoderAudioFiles/secondearth_beats_mono.wav";
		String filename = "/home/dan/tmp/TestExampleBeats96000.wav";

		WaveFileReader wif = new WaveFileReader( filename );
		
		int numReadChannels = wif.getNumChannels();
		log.debug("Found " + numReadChannels + " channel(s) in file");
		long numTotalFloats = wif.getNumTotalFloats();
		long numTotalFrames = wif.getNumTotalFrames();
		assertTrue( (numTotalFloats / numReadChannels) == numTotalFrames );
		
		int numFloatsAsInt = (int)numTotalFloats;
		float[] resultData = new float[ numFloatsAsInt ];
		wif.read( resultData, 0, 0, numFloatsAsInt );

		log.debug("Read " + numFloatsAsInt + " floats from file.");
		
		wif.close();
	}
}
