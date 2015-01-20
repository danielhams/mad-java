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

import java.util.HashSet;
import java.util.Set;

public class BlockRecomposer
{
//	private static Log log = LogFactory.getLog( BlockRecomposer.class.getName() );
	
	public static boolean canRecomposeWithFreeEntries( RawImage usedRawImage,
			Set<FreeEntry> freeEntriesForRawImage,
			Recomposition recompositionForFree,
			UsedEntry usedEntry )
	{
		return topLevelRecomposeWithFreeEntries( usedRawImage, freeEntriesForRawImage, recompositionForFree, usedEntry );
	}

	private static boolean topLevelRecomposeWithFreeEntries( RawImage usedRawImage,
			Set<FreeEntry> freeEntriesForRawImage,
			Recomposition recompositionForFree,
			UsedEntry usedEntry )
	{
		Set<FreeEntry> currentFreeEntries = new HashSet<FreeEntry>();
		
		currentFreeEntries.addAll( freeEntriesForRawImage );
		
		// Setup the structure we'll fill in / change
		recompositionForFree.x = usedEntry.getX();
		recompositionForFree.y = usedEntry.getY();
		recompositionForFree.width = usedEntry.getWidth();
		recompositionForFree.height = usedEntry.getHeight();
		recompositionForFree.freeEntriesToRemove.clear();

		return recomposeWithFreeEntries( currentFreeEntries, recompositionForFree );
	}

	private static boolean recomposeWithFreeEntries( Set<FreeEntry> currentFreeEntries,
			Recomposition recompositionForFree )
	{
		// Iterate over the current free entries looking for a match to do stuff
		boolean matchedOne = true;
		boolean matching = true;
		
		while( matching )
		{
			matching = false;
			FreeEntry testFreeEntry = null;
			
			for( FreeEntry checkFreeEntry : currentFreeEntries )
			{
				if( mergeIfPossible( currentFreeEntries, checkFreeEntry, recompositionForFree ) )
				{
					matchedOne = true;
					matching = true;
					testFreeEntry = checkFreeEntry;
					break;
				}
			}
			if( matching && matchedOne )
			{
				currentFreeEntries.remove( testFreeEntry );
				recompositionForFree.freeEntriesToRemove.add( testFreeEntry );
			}
		}
		return matchedOne;
	}

	private static boolean mergeIfPossible( Set<FreeEntry> currentFreeEntries,
			FreeEntry testFreeEntry,
			Recomposition recompositionForFree )
	{
		boolean merged = false;
		
		// Test the four edges
		// Edge are clockwise and are "0" from 12 oclock
		// For edge 0
		int entryX = testFreeEntry.getX();
		int entryWidth = testFreeEntry.getWidth();
		int entryY = testFreeEntry.getY();
		int entryHeight = testFreeEntry.getHeight();
		
		if( ( entryX == recompositionForFree.x && entryWidth == recompositionForFree.width ) &&
				( (entryY + entryHeight) == recompositionForFree.y ) )
		{
			// Only start Y and height changes
			recompositionForFree.y = entryY;
			recompositionForFree.height += entryHeight;
			merged = true;
		}
		else if( (entryX == (recompositionForFree.x + recompositionForFree.width ) ) &&
				(entryY == recompositionForFree.y && entryHeight == recompositionForFree.height ) )
		{
			// Only width changes
			recompositionForFree.width += entryWidth;
			merged = true;
		}
		else if( (entryY == (recompositionForFree.y + recompositionForFree.height ) ) &&
				(entryX == recompositionForFree.x && entryWidth == recompositionForFree.width ) )
		{
			// Only height changes
			recompositionForFree.height += entryHeight;
			merged = true;
		}
		else if( ((entryX + entryWidth) == recompositionForFree.x ) &&
				( entryY == recompositionForFree.y && entryHeight == recompositionForFree.height ) )
		{
			// X and width changes
			recompositionForFree.x = entryX;
			recompositionForFree.width += entryWidth;
			merged = true;
		}

		return merged;
	}
}
