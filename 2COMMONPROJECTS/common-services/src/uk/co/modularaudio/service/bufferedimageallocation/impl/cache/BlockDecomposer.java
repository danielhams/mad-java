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

package uk.co.modularaudio.service.bufferedimageallocation.impl.cache;


public class BlockDecomposer
{
//	private static Log log = LogFactory.getLog( BlockDecomposer.class.getName() );
	
	public static void decomposeImage( Decomposition outDecomposition,
			int origWidth,
			int origHeight,
			int usedWidth,
			int usedHeight )
	{
		int widthLeft = origWidth - usedWidth;
		int heightLeft = origHeight - usedHeight;
		
		if( widthLeft == 0 )
		{
			outDecomposition.hasLarger = false;
			outDecomposition.smallerBlockX = 0;
			outDecomposition.smallerBlockWidth = origWidth;
			outDecomposition.smallerBlockY = usedHeight;
			outDecomposition.smallerBlockHeight = origHeight - usedHeight;
		}
		else if( heightLeft == 0 )
		{
			outDecomposition.hasLarger = false;
			outDecomposition.smallerBlockX = usedWidth;
			outDecomposition.smallerBlockWidth = origWidth - usedWidth;
			outDecomposition.smallerBlockY = 0;
			outDecomposition.smallerBlockHeight = origHeight;
		}
		else
		{
			outDecomposition.hasLarger = true;
			decomposeIntoTwo( outDecomposition,
					origWidth,
					origHeight,
					usedWidth,
					usedHeight,
					widthLeft,
					heightLeft );
		}
	}

	private static void decomposeIntoTwo( Decomposition outDecomposition,
			int origWidth,
			int origHeight,
			int usedWidth,
			int usedHeight,
			int widthRemaining,
			int heightRemaining )
	{
//		log.debug("Asked for decomposition with orig(" + origWidth + ", " + origHeight + ") usedXY(" + usedWidth + ", " + usedHeight + ")");
		// Need to break them up to make the larger of the remaining block as square as possible
		boolean widthRemainingIsSmallerThanHeight = (widthRemaining < heightRemaining );

		boolean makeBlockToRightSmaller = false;
		
		if( widthRemainingIsSmallerThanHeight )
		{
			makeBlockToRightSmaller = true;
		}
		
		if( makeBlockToRightSmaller )
		{
//			log.debug("Making block to right smaller");
			// Make the smaller block the one to the right
			outDecomposition.smallerBlockX = usedWidth;
			outDecomposition.smallerBlockWidth = origWidth - usedWidth;
			outDecomposition.smallerBlockY = 0;
			outDecomposition.smallerBlockHeight = usedHeight;
			
			outDecomposition.largerBlockX = 0;
			outDecomposition.largerBlockWidth = origWidth;
			outDecomposition.largerBlockY = usedHeight;
			outDecomposition.largerBlockHeight = origHeight - usedHeight;
		}
		else
		{
//			log.debug("Making block underneath smaller");
			// Make the smaller block the one underneath
			outDecomposition.smallerBlockX = 0;
			outDecomposition.smallerBlockWidth = usedWidth;
			outDecomposition.smallerBlockY = usedHeight;
			outDecomposition.smallerBlockHeight = origHeight - usedHeight;
			
			outDecomposition.largerBlockX = usedWidth;
			outDecomposition.largerBlockWidth = origWidth - usedWidth;
			outDecomposition.largerBlockY = 0;
			outDecomposition.largerBlockHeight = origHeight;
		}
//		log.debug("Out decomposition is now " + outDecomposition.smallBlockToString() + " - " + outDecomposition.largeBlockToString() );
	}
}
