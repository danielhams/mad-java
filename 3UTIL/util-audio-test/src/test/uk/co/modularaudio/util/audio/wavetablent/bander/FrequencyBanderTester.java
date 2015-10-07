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

package test.uk.co.modularaudio.util.audio.wavetablent.bander;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;
import uk.co.modularaudio.util.audio.oscillatortable.FrequencyBander;

public class FrequencyBanderTester extends TestCase
{
	private static Log log = LogFactory.getLog( FrequencyBanderTester.class.getName() );

	@SuppressWarnings("unused")
	public void testCreatingBands() throws Exception
	{
		final MidiNote startMidiNote = MidiUtils.getMidiNoteFromStringReturnNull( "C3" );
		final MidiNote endMidiNote = MidiUtils.getMidiNoteFromStringReturnNull( "G10" );
		final int numNotesBetweenBand = 1;
		final FrequencyBander fb = new FrequencyBander( startMidiNote, endMidiNote, numNotesBetweenBand );
		final int numBands = fb.getNumBands();
		final float[] baseFreqPerBand = fb.getBaseFreqPerBand();
		final int[] numHarmsPerBand = fb.getNumHarmsPerBand();
		log.debug("Would generate " + numBands + " bands of waveform");

		float prevFreq = -1.0f;

		for( int b = 0 ; b < numBands ; ++b )
		{
			final float bandFreq = baseFreqPerBand[b];

			if( prevFreq != -1.0f )
			{
				log.debug("For band " + b + " Low(" + prevFreq + ")High(" + bandFreq + ")");
			}

			prevFreq = bandFreq;
		}

	}
}
