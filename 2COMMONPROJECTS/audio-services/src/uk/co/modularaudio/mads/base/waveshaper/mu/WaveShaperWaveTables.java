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

package uk.co.modularaudio.mads.base.waveshaper.mu;

import java.util.HashMap;
import java.util.Map;

import uk.co.modularaudio.util.audio.wavetable.raw.RawWaveTable;
import uk.co.modularaudio.util.audio.wavetable.waveshaper.StandardShaperTables;

public class WaveShaperWaveTables
{
	public enum WaveType
	{
		COMPRESSOR,
		TRIANGLE,
		SQUARE
	}
	
	private Map<WaveType, RawWaveTable> typeToTableMap = new HashMap<WaveType, RawWaveTable>();
	
	public WaveShaperWaveTables()
	{
		typeToTableMap.put( WaveType.COMPRESSOR, StandardShaperTables.getCompressorWaveTable() );
		typeToTableMap.put( WaveType.TRIANGLE, StandardShaperTables.getTriangleShaperTable() );
		typeToTableMap.put( WaveType.SQUARE, StandardShaperTables.getSquareShaperWaveTable() );
	}
	
	public RawWaveTable getTable( WaveType type )
	{
		return typeToTableMap.get( type );
	}
}
