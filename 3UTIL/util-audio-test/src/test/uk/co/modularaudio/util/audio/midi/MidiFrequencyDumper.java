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

package test.uk.co.modularaudio.util.audio.midi;

import java.util.List;

import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;
import uk.co.modularaudio.util.math.MathFormatter;

public class MidiFrequencyDumper
{
//	private static Log log = LogFactory.getLog( MidiFrequencyDumper.class.getName() );

	public MidiFrequencyDumper()
	{
	}

	public void dumpEm()
	{
		final List<MidiNote> mns = MidiUtils.getOrderedMidiNotes();

		int numOutputCounter = 0;
		final int numMidiNotes = MidiUtils.getNumMidiNotes();

		System.out.print("\t");
		for( final MidiNote mn : mns )
		{
			System.out.print( MathFormatter.fastFloatPrint(mn.getFrequency(), 15, false ) );

			if( numOutputCounter < numMidiNotes - 1 )
			{
				System.out.print(", ");
			}

			numOutputCounter++;
			if( numOutputCounter % 4 == 0 )
			{
				System.out.println();
				System.out.print("\t");
			}
		}
	}

	public static void main(final String[] args)
	{
		final MidiFrequencyDumper d = new MidiFrequencyDumper();
		d.dumpEm();
	}

}
