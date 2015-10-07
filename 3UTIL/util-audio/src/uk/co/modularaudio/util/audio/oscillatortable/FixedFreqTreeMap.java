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

	private final int startPivotIndex;
	private final float[] pivotsArray;
	private final CubicPaddedRawWaveTable[] valuesArray;

	public FixedFreqTreeMap( final float[] pivotsArray, final CubicPaddedRawWaveTable[] valuesArray )
	{
		this.pivotsArray = pivotsArray;
		this.valuesArray = valuesArray;
		this.startPivotIndex = (pivotsArray.length) / 2;
	}

	public CubicPaddedRawWaveTable iterativeLookupWavetableForFreq( final float freq )
	{
		int lowerInclusiveBound = 0;
		int upperExclusiveBound = pivotsArray.length;
		int curPivotIndex = startPivotIndex;

		while( true )
		{
//			log.debug("iterativeFindWaveTableFromPivot [" + lowerInclusiveBound + "->" +
//					upperExclusiveBound + ") curPivotIndex(" +
//					curPivotIndex + ") with freq(" +
//					MathFormatter.fastFloatPrint( freq, 1, false ) + ")");

			final float freqAtPivot = pivotsArray[ curPivotIndex ];
//			log.debug("FreqAtTestIndex(" + MathFormatter.fastFloatPrint( freqAtPivot, 1, false ) + ")");

			if( freq < freqAtPivot )
			{
//				log.debug( "Moving upper bound to below tested pivot index" );
				upperExclusiveBound = curPivotIndex;
			}
			else
			{
//				log.debug( "Moving lower bound to tested pivot index" );
				lowerInclusiveBound = curPivotIndex;
			}

			final int boundsDiff = upperExclusiveBound - lowerInclusiveBound;

			if( boundsDiff == 1 )
			{
//				log.debug("Only one candidate now at freq(" + pivotsArray[lowerInclusiveBound] + ")");
//				log.debug("Iterative lookup returning index " + lowerInclusiveBound );
				return valuesArray[ lowerInclusiveBound ];
			}
			else if( boundsDiff == 2 )
			{
//				log.debug("Two candidates");
				final int upperCandidateIndex = lowerInclusiveBound+1;
				if( freq < pivotsArray[ upperCandidateIndex ] )
				{
//					log.debug("Is lower candidate freq(" + pivotsArray[lowerInclusiveBound] + ")");
//					log.debug("Iterative lookup returning index " + lowerInclusiveBound );
					return valuesArray[ lowerInclusiveBound ];
				}
				else
				{
//					log.debug("Is upper candidate freq(" + pivotsArray[upperCandidateIndex] + ")");
//					log.debug("Iterative lookup returning index " + upperCandidateIndex );
					return valuesArray[ upperCandidateIndex ];
				}
			}

			curPivotIndex = (upperExclusiveBound + lowerInclusiveBound) / 2;
//			log.debug( "Moved pivot index to " + curPivotIndex );
		}
	}
}
