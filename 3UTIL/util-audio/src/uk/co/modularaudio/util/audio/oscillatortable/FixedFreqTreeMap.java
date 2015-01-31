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


public class FixedFreqTreeMap
{
//	private static Log log = LogFactory.getLog( FixedFreqTreeMap.class.getName() );
	
	private int lastPivotIndex = -1;
	private float[] pivotsArray = null;
	private CubicPaddedRawWaveTable[] valuesArray = null;

	public FixedFreqTreeMap( float[] pivotsArray, CubicPaddedRawWaveTable[] valuesArray )
	{
		this.pivotsArray = pivotsArray;
		this.valuesArray = valuesArray;
		this.lastPivotIndex = pivotsArray.length - 1;
	}

	public CubicPaddedRawWaveTable lookupWavetableForFreq( float freq )
	{
		int indexToStartAt = lastPivotIndex / 2;
		return findWavetableFromPivot( 0, lastPivotIndex, indexToStartAt, freq );
	}
	
	private CubicPaddedRawWaveTable findWavetableFromPivot( int lowerInclusiveBound,
				int upperInclusiveBound,
				int indexToStartAt,
				float freq )
	{
		float freqAtPivot = pivotsArray[ indexToStartAt ];

		int boundsDiff = upperInclusiveBound - lowerInclusiveBound;
		if( boundsDiff == 0 )
		{
			if( freq < freqAtPivot && lowerInclusiveBound > 0 )
			{
				int retIndex = lowerInclusiveBound - 1;
				return valuesArray[ retIndex];
			}
			else
			{
				return valuesArray[ lowerInclusiveBound ];
			}
		}
		else if( boundsDiff == 1 )
		{
			if( freq < pivotsArray[ upperInclusiveBound ] )
			{
				return valuesArray[ lowerInclusiveBound ];
			}
			else
			{
				return valuesArray[ upperInclusiveBound ];
			}
		}
		else
		{
			if( freq < freqAtPivot )
			{
				int newUpperBound = indexToStartAt -1;
				int indexBetween = (newUpperBound + lowerInclusiveBound) / 2;
				return findWavetableFromPivot( lowerInclusiveBound, indexToStartAt - 1, indexBetween, freq );
			}
			else
			{
				int newLowerBound = indexToStartAt;
				int indexBetween = (upperInclusiveBound + newLowerBound) / 2;
				return findWavetableFromPivot( newLowerBound, upperInclusiveBound, indexBetween, freq );
			}
		}
	}

	public CubicPaddedRawWaveTable iterativeLookupWavetableForFreq( float freq )
	{
		int lowerInclusiveBound = 0;
		int upperInclusiveBound = lastPivotIndex;
		int curPivotIndex = lastPivotIndex / 2;
		
		while( true )
		{
//			log.debug("iterativeFindWaveTableFromPivot [" + lowerInclusiveBound + "->" + upperInclusiveBound + "] curPivotIndex(" + curPivotIndex + ") with freq(" + MathFormatter.fastFloatPrint( freq, 1, false ) + ")");
			float freqAtPivot = pivotsArray[ curPivotIndex ];
//			log.debug("FreqAtTestIndex(" + MathFormatter.fastFloatPrint( freqAtPivot, 1, false ) + ")");
			
			if( freq < freqAtPivot )
			{
//				log.debug( "Moving upper bound to below pivot index" );
				upperInclusiveBound = curPivotIndex  - 1;
			}
			else
			{
//				log.debug( "Moving lower bound to pivot index" );
				lowerInclusiveBound = curPivotIndex;
			}

			int boundsDiff = upperInclusiveBound - lowerInclusiveBound;
			
			if( boundsDiff == 0 )
			{
//				log.debug("Only one candidate now.");
				if( freq < pivotsArray[ curPivotIndex ] && lowerInclusiveBound > 0 )
				{
					int retIndex = lowerInclusiveBound - 1;
//					log.debug("Iterative lookup returning index " + retIndex );
					return valuesArray[ retIndex ];
				}
				else
				{
//					log.debug("Iterative lookup returning index " + lowerInclusiveBound );
					return valuesArray[ lowerInclusiveBound ];
				}
			}
			else if( boundsDiff == 1 )
			{
//				log.debug("Two candidates - will check freq on upper bound.");
				if( freq < pivotsArray[ upperInclusiveBound ] )
				{
//					log.debug("Iterative lookup returning index " + lowerInclusiveBound );
					return valuesArray[ lowerInclusiveBound ];
				}
				else
				{
//					log.debug("Iterative lookup returning index " + upperInclusiveBound );
					return valuesArray[ upperInclusiveBound ];
				}
			}
			
			curPivotIndex = (upperInclusiveBound + lowerInclusiveBound) / 2;
		}
	}
}
