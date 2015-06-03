package test.uk.co.modularaudio.util.audio.midi;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;
import uk.co.modularaudio.util.math.MathFormatter;

public class MidiFrequencyDumper
{
	private static Log log = LogFactory.getLog( MidiFrequencyDumper.class.getName() );
	
	
	public MidiFrequencyDumper()
	{
		List<MidiNote> mns = MidiUtils.getOrderedMidiNotes();
		
		int numOutputCounter = 0;
		int numMidiNotes = MidiUtils.getNumMidiNotes();
		
		System.out.print("\t");
		for( MidiNote mn : mns )
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

	public static void main(String[] args)
	{
		MidiFrequencyDumper d = new MidiFrequencyDumper();
	}

}
