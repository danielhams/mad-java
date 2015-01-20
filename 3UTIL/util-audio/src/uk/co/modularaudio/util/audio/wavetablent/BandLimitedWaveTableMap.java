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

package uk.co.modularaudio.util.audio.wavetablent;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;

public class BandLimitedWaveTableMap
{
//	private static Log log = LogFactory.getLog( BandLimitedWaveTableMap.class.getName() );

	private Set<CubicPaddedRawWaveTable> bandLimitedWaveTables = new HashSet<CubicPaddedRawWaveTable>();
	
	private FixedFreqTreeMap fixedFreqTreeMap = null;
	
	private final static MidiNote bandStartMidiNote = MidiUtils.getMidiNoteFromStringReturnNull( "C3" );
	private final static MidiNote bandEndMidiNote = MidiUtils.getMidiNoteFromStringReturnNull( "G10" );
	private final int notesBetweenBands = 1;
	
	public BandLimitedWaveTableMap( String waveCacheRoot, RawWaveTableGenerator waveTableBandGenerator, int cycleLength )
		throws IOException
	{
		FreqTreeMap<FreqTreeMapEntry> freqTreeMap = new FreqTreeMap<FreqTreeMapEntry>();
		
		FrequencyBander freqBander = new FrequencyBander( bandStartMidiNote, bandEndMidiNote, notesBetweenBands );
		int numBands = freqBander.getNumBands();
		float[] bandStartFreqs = freqBander.getBaseFreqPerBand();
		int[] bandNumHarms = freqBander.getNumHarmsPerBand();
		
		for( int i = 0 ; i < numBands ; i++ )
		{
			float bandStartFreq = bandStartFreqs[ i ];
			int numHarmonics = bandNumHarms[ i ];
			
			CubicPaddedRawWaveTable waveTableForBand = waveTableBandGenerator.readFromCacheOrGenerate( waveCacheRoot,
					cycleLength,
					numHarmonics );
			bandLimitedWaveTables.add(  waveTableForBand );

			FreqTreeMapEntry freqTreeMapEntry = new FreqTreeMapEntry( bandStartFreq, waveTableForBand );
			freqTreeMap.add( freqTreeMapEntry );
		}
		
		// And generate the quick lookup table
		fixedFreqTreeMap = freqTreeMap.fix();
	}

	
	public CubicPaddedRawWaveTable getWaveTableForFrequency( float freq )
	{
		return fixedFreqTreeMap.iterativeLookupWavetableForFreq( freq );
	}
}
