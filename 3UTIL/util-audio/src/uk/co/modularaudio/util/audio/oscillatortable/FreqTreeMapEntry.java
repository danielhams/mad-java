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


public class FreqTreeMapEntry implements Comparable<FreqTreeMapEntry>
{
//	private static Log log = LogFactory.getLog( FreqTreeMapEntry.class.getName() );
	
	private float entryPivot = 0.0f;

	private CubicPaddedRawWaveTable value = null;
	
	public FreqTreeMapEntry( float pivot, CubicPaddedRawWaveTable value )
	{
		this.entryPivot = pivot;
		this.value = value;
	}

	public float getEntryPivot()
	{
		return entryPivot;
	}

	public CubicPaddedRawWaveTable getValue()
	{
		return value;
	}

	@Override
	public int compareTo( FreqTreeMapEntry o )
	{
		float diff = entryPivot - o.entryPivot;
		return( diff < 0.0f ? -1 : (diff > 0.0f ? 1 : 0 ) );
	}

	public void setFreq( float freq )
	{
		entryPivot = freq;		
	}

}
