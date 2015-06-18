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
			System.out.print( MathFormatter.slowFloatPrint(mn.getFrequency(), 15, false ) );

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
