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

package uk.co.modularaudio.util.audio.oscillatortable;

import java.util.Vector;

import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;

public class FrequencyBander
{
//	private static Log log = LogFactory.getLog( FrequencyBander.class.getName() );
	
	private int numBands = 0;
	private float[] baseFreqPerBand;
	private int[] numHarmsPerBand;
	
	private final MidiNote startMidiNote;
	private final MidiNote endMidiNote;
	private final int numNotesBetweenBand;

	
	public FrequencyBander( MidiNote startMidiNote, MidiNote endMidiNote, int numNotesBetweenBand )
	{
		this.startMidiNote = startMidiNote;
		this.endMidiNote = endMidiNote;
		this.numNotesBetweenBand = numNotesBetweenBand;
		decideBands();
	}
	
	private void decideBands()
	{
		Vector<Float> baseFreqForBands = new Vector<Float>();
		Vector<Integer> numHarmsForBands = new Vector<Integer>();
		// We'll start from the third octave effectively
		
		int startNoteNum = startMidiNote.getMidiNumber();
		int endNoteNum = endMidiNote.getMidiNumber();
		
		float prevFreq = 0.0f;
		int prevNumHarmonics = -1;

		for( int curNoteNum = startNoteNum ; curNoteNum < endNoteNum ; curNoteNum += numNotesBetweenBand )
		{
			MidiNote thisNote = MidiUtils.getMidiNoteFromNumberReturnNull( curNoteNum );
			float thisNoteFreq = thisNote.getFrequency();
			int numHarmonics = calcMaxHarmonicsForFreq( thisNoteFreq );
//			log.debug("So for note(" + thisNote.toString() + ") at freq(" + MathFormatter.fastFloatPrint( thisNoteFreq, 3, false) + ") harms(" +
//					numHarmonics +")");
			
			// Only create a band for it if it's a new number of harmonics (don't repeat)
			if( numHarmonics != prevNumHarmonics )
			{
//				log.debug("Thus baseFreq(" + MathFormatter.fastFloatPrint( prevFreq, 3, false ) + ") numHarms(" + numHarmonics + ")");
				baseFreqForBands.add( prevFreq );
				numHarmsForBands.add( numHarmonics );
				numBands++;
			}
			else
			{
//				log.debug("Skipping adding a band for (" + MathFormatter.fastFloatPrint( prevFreq, 3, false) + ")");
			}
			prevFreq = thisNoteFreq;
			prevNumHarmonics = numHarmonics;
		}
		
		baseFreqPerBand = new float[ numBands ];
		numHarmsPerBand = new int[ numBands ];
		// Stupid autoboxing/unboxing won't work with a toArray call...
		for( int i = 0 ; i < numBands ; i++ )
		{
			baseFreqPerBand[ i ] = baseFreqForBands.get( i );
			numHarmsPerBand[ i ] = numHarmsForBands.get( i );
		}
	}
	
	private int calcMaxHarmonicsForFreq( float inFreq )
	{
		int numHarmonics = 1;
		float curFreq = inFreq;
		boolean done = false;
		while( !done )
		{
			curFreq += inFreq;
			if( curFreq < 22050.0f )
			{
				numHarmonics++;
			}
			else
			{
				done = true;
			}
		}
		return numHarmonics;
	}
	
	public float[] getBaseFreqPerBand()
	{
		return baseFreqPerBand;
	}

	public int[] getNumHarmsPerBand()
	{
		return numHarmsPerBand;
	}
	
	public int getNumBands()
	{
		return numBands;
	}
}
