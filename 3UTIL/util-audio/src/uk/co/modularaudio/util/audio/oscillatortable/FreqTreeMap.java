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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.mahout.math.list.FloatArrayList;

public class FreqTreeMap<E extends FreqTreeMapEntry> extends TreeSet<E>
{
	private static final long serialVersionUID = -7405910914674103864L;
//	private static Log log = LogFactory.getLog( FreqTreeMap.class.getName() );

	public FreqTreeMap()
	{
	}

	public CubicPaddedRawWaveTable findLeafForFreq( final E entryForFinds, final float freq )
	{
		entryForFinds.setFreq( freq );

		final FreqTreeMapEntry foundEntry = floor( entryForFinds );
		if( foundEntry != null )
		{
			return foundEntry.getValue();
		}
		else
		{
			final FreqTreeMapEntry first = first();
			if( first != null )
			{
				return first.getValue();
			}
			else
			{
				return null;
			}
		}
	}

	public FixedFreqTreeMap fix()
	{
		final FloatArrayList floatPivots = new FloatArrayList();
		final List<CubicPaddedRawWaveTable> valuesAtPivots = new ArrayList<CubicPaddedRawWaveTable>();

		final Iterator<E> eIter = iterator();
		while( eIter.hasNext() )
		{
			final FreqTreeMapEntry entry = eIter.next();
			final float pivot = entry.getEntryPivot();
			final CubicPaddedRawWaveTable value = entry.getValue();
			floatPivots.add( pivot );
			valuesAtPivots.add( value );
		}

		final int numPivots = floatPivots.size();
		final float[] pivotsArray = new float[ numPivots ];
		final CubicPaddedRawWaveTable[] valuesArray = new CubicPaddedRawWaveTable[ numPivots ];
		for( int i =0 ; i < numPivots ; ++i )
		{
			pivotsArray[ i ] = floatPivots.get( i );
			valuesArray[ i ] = valuesAtPivots.get( i );
		}
		final FixedFreqTreeMap fixedMap = new FixedFreqTreeMap( pivotsArray, valuesArray );

		return fixedMap;
	}
}
