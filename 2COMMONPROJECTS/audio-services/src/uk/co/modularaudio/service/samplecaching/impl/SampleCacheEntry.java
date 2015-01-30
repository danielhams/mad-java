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

package uk.co.modularaudio.service.samplecaching.impl;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.mahout.math.map.OpenLongObjectHashMap;

import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.library.LibraryEntry;

public class SampleCacheEntry
{
//	private static Log log = LogFactory.getLog( SampleCacheEntry.class.getName() );

	private final LibraryEntry libraryEntry;
	private final AudioFileHandleAtom audioFileHandleAtom;

	private final int numCacheBlocks;
	private final boolean[] requiredCachedBlocks;

	private final HashSet<InternalSampleCacheClient> currentClientSet = new HashSet<InternalSampleCacheClient>();

	private final AtomicReference<OpenLongObjectHashMap<SampleCacheBlock>> atomicSampleCacheBlocksMap =
			new AtomicReference<OpenLongObjectHashMap<SampleCacheBlock>>( new OpenLongObjectHashMap<SampleCacheBlock>() );

	public SampleCacheEntry( final LibraryEntry libraryEntry,
			final AudioFileHandleAtom afha,
			final int numCacheBlocks )
	{
		this.libraryEntry = libraryEntry;
		this.audioFileHandleAtom = afha;
		this.numCacheBlocks = numCacheBlocks;
		this.requiredCachedBlocks = new boolean[ numCacheBlocks ];
	}

	public LibraryEntry getLibraryEntry()
	{
		return libraryEntry;
	}

	public AudioFileHandleAtom getAudioFileHandleAtom()
	{
		return audioFileHandleAtom;
	}

	public int getNumCacheBlocks()
	{
		return numCacheBlocks;
	}

	public boolean[] getRequiredCachedBlocks()
	{
		return requiredCachedBlocks;
	}

	public int getReferenceCount()
	{
		return currentClientSet.size();
	}

	public void addReference( final InternalSampleCacheClient client )
	{
		currentClientSet.add( client );
	}

	public void removeReference( final InternalSampleCacheClient client )
	{
		currentClientSet.remove( client );
	}

	public HashSet<InternalSampleCacheClient> getCurrentClientSet()
	{
		return currentClientSet;
	}

	public AtomicReference<OpenLongObjectHashMap<SampleCacheBlock>> getAtomicSampleCacheBlocksMap()
	{
		return atomicSampleCacheBlocksMap;
	}
}
