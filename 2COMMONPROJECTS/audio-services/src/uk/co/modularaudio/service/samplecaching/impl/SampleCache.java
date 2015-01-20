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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenLongObjectHashMap;

import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;
import uk.co.modularaudio.service.library.vos.CuePoint;
import uk.co.modularaudio.service.library.vos.LibraryEntry;
import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleAcceptor;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.impl.SampleCacheBlock.SampleCacheBlockEnum;
import uk.co.modularaudio.util.audio.floatblockpool.BlockBufferingConfiguration;
import uk.co.modularaudio.util.audio.floatblockpool.BlockNotAvailableException;
import uk.co.modularaudio.util.audio.floatblockpool.FloatBufferBlock;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;
import uk.co.modularaudio.util.tuple.TwoTuple;

public class SampleCache
{
	private static Log log = LogFactory.getLog( SampleCache.class.getName() );
	
	private BlockBufferingConfiguration blockBufferingConfiguration;
	private AudioFileIOService audioFileIOService;

	// Various maps for internal maintenance that go from
	// Client -> SampleCacheEntry						(used when determining which blocks all clients need)
	// LibraryEntry -> SampleCacheEntry			(used to lookup existing entry for a particular library entry)
	// SampleCacheEntry -> referenceCount		(used to schedule release of blocks when entries not used)
	private Integer cacheAccessMutex = new Integer(0);

	private OpenLongObjectHashMap<SampleCacheEntry> libraryEntryToSampleCacheEntryMap =
			new OpenLongObjectHashMap<SampleCacheEntry>();
	
	private SampleCachePopulatorThread cachePopulatorThread;
	
	private TemperatureBufferBlockMap temperatureBufferBlockMap;
	
	private HashSet<SampleCacheEntry> currentSampleCacheEntries = new HashSet<SampleCacheEntry>();

	private HashSet<SampleCacheBlock> hotBlocksToCoolSet = new HashSet<SampleCacheBlock>();
	
	private ArrayList<TwoTuple<BufferFillCompletionListener,SampleCacheClient>> listenersToNotifyOnNextCompletion =
			new ArrayList<TwoTuple<BufferFillCompletionListener,SampleCacheClient>>();
	
	public SampleCache( AudioFileIOService audioFileIOService,
			BlockBufferingConfiguration blockBufferingConfiguration )
	{
		this.audioFileIOService = audioFileIOService;
		this.blockBufferingConfiguration = blockBufferingConfiguration;
		temperatureBufferBlockMap = new TemperatureBufferBlockMap( blockBufferingConfiguration );
	}

	public void init( boolean runThread )
	{
		if( runThread )
		{
			cachePopulatorThread = new SampleCachePopulatorThread( this, temperatureBufferBlockMap );
			cachePopulatorThread.start();
		}
	}

	public void destroy()
	{
		synchronized (cacheAccessMutex)
		{
			if( cachePopulatorThread != null )
			{
				try
				{
					cachePopulatorThread.halt();
					// Will "wake" any sleeping thread
					cachePopulatorThread.addOneJobToDo();
					cachePopulatorThread.join();
					cachePopulatorThread = null;
				}
				catch (InterruptedException e)
				{
					String msg = "InterruptedException whilst joining cache populator: " + e.toString();
					log.error( msg, e );
				}
			}
			temperatureBufferBlockMap.destroy();
		}
	}

	public void addClient( InternalSampleCacheClient internalClient ) throws DatastoreException, RecordNotFoundException, IOException
	{
		LibraryEntry libraryEntry = internalClient.getLibraryEntry();
		synchronized( cacheAccessMutex )
		{
			SampleCacheEntry sce = libraryEntryToSampleCacheEntryMap.get( libraryEntry.getLibraryEntryId() );
			
			if( sce == null )
			{
				String location = libraryEntry.getLocation();
				StaticMetadata sm = audioFileIOService.sniffFileFormatOfFile( location );
				AudioFileHandleAtom afha = audioFileIOService.openForRead( location );
				long numFloats = libraryEntry.getTotalNumFloats();
				long numFrames = libraryEntry.getTotalNumFrames();
				assert( numFrames == sm.numFrames );
				int numBlockDivisor = (int)(numFloats / blockBufferingConfiguration.blockLengthInFloats);
				int extraSamples = (int)(numFloats % blockBufferingConfiguration.blockLengthInFloats);
				int numCacheBlocks = numBlockDivisor + (extraSamples > 0 ? 1 : 0 );
				sce = new SampleCacheEntry( libraryEntry, afha, numCacheBlocks );
				libraryEntryToSampleCacheEntryMap.put( libraryEntry.getLibraryEntryId(), sce );
			}
			
			sce.addReference( internalClient );
			
			currentSampleCacheEntries.add( sce );
			
		}
		// Wake up the cache populator immediately so we don't have to wait until it is scheduled.
		cachePopulatorThread.addOneJobToDo();
	}

	public void removeClient( InternalSampleCacheClient internalClient ) throws DatastoreException, RecordNotFoundException
	{
		LibraryEntry libraryEntry = internalClient.getLibraryEntry();
		synchronized( cacheAccessMutex )
		{
			SampleCacheEntry sce = libraryEntryToSampleCacheEntryMap.get( internalClient.getLibraryEntry().getLibraryEntryId() );
			if( sce == null )
			{
				throw new RecordNotFoundException( "No such cache entry for clients library entry");
			}
			
			sce.removeReference( internalClient );

			int curCount = sce.getReferenceCount();
			
			if( curCount == 0 )
			{
				libraryEntryToSampleCacheEntryMap.removeKey( libraryEntry.getLibraryEntryId() );
				try
				{
					audioFileIOService.closeHandle( sce.getAudioFileHandleAtom() );
				}
				catch( IOException ioe )
				{
					log.error("IOException caught closing file handle atom: " + ioe.toString(), ioe );
				}
				// Any assigned "hot" blocks should be released on the next run of
				// the population thread when it notices there are no clients for them.
				currentSampleCacheEntries.remove( sce );
			}
		}
		// Wake up the cache populator immediately so we don't have to wait until it is scheduled.
		cachePopulatorThread.addOneJobToDo();
	}

	public RealtimeMethodReturnCodeEnum readSamplesForCacheClient( InternalSampleCacheClient client,
			float[] outputSamples,
			int outputFramePos,
			long readFramePosition,
			int numFramesToRead )
	{
		RealtimeMethodReturnCodeEnum retVal = RealtimeMethodReturnCodeEnum.SUCCESS;
		LibraryEntry libraryEntry = client.getLibraryEntry();
		int clientLastReadBlockNumber = client.getLastReadBlockNumber();
		
		int libraryEntryId = libraryEntry.getLibraryEntryId();
//		log.debug("Need samples for " + libraryEntry.getLocation() + " at frame position " + readFramePosition + " of " + numFramesToRead );
		
		int leNumChannels = libraryEntry.getNumChannels();
		long leTotalNumFrames = libraryEntry.getTotalNumFrames();

		int curOutputFloatPos = outputFramePos * leNumChannels;
		int totalNumFloatsToRead = numFramesToRead * leNumChannels;
		
		long lastFramePositon = readFramePosition + numFramesToRead;

		// Handling of pre or post frame values. We don't throw an error
		// we just return zeros for unknown frames - it's the caller's
		// responsibility to supply correct frame values.
		if( readFramePosition < 0 )
		{
			long numZeroFrames = -readFramePosition;
			int numZeroFramesToFill = (int)(numZeroFrames > numFramesToRead ? numFramesToRead : numZeroFrames );
			int numZeroFloats = numZeroFramesToFill * leNumChannels;
//			log.debug("Fill in " + numZeroFrames + " frames of zeros ");
			Arrays.fill( outputSamples, curOutputFloatPos, curOutputFloatPos + numZeroFloats, 0.0f );
			curOutputFloatPos += numZeroFloats;
			totalNumFloatsToRead -= numZeroFloats;
			readFramePosition += numZeroFramesToFill;
			numFramesToRead -= numZeroFramesToFill;
			outputFramePos += numZeroFramesToFill;
//			log.debug("This leaves " + numFramesToRead + " frames to be read");
			if( numFramesToRead == 0 )
			{
				return RealtimeMethodReturnCodeEnum.SUCCESS;
			}
		}
		
        if( lastFramePositon >= leTotalNumFrames )
		{
			long numZeroFrames = lastFramePositon - leTotalNumFrames;
			int numZeroFramesToFill = (int)(numZeroFrames > numFramesToRead ? numFramesToRead : numZeroFrames );
			int numZeroFloats = numZeroFramesToFill * leNumChannels;
			int zerosFrameOffset = outputFramePos + (numFramesToRead - numZeroFramesToFill);
			int zerosFloatOffset = curOutputFloatPos + (zerosFrameOffset * leNumChannels);
			Arrays.fill( outputSamples, zerosFloatOffset, zerosFloatOffset + numZeroFloats, 0.0f );
			totalNumFloatsToRead -= numZeroFloats;
			numFramesToRead -= numZeroFramesToFill;
			if( numFramesToRead == 0 )
			{
				return RealtimeMethodReturnCodeEnum.SUCCESS;
			}
		}
		
		SampleCacheEntry sce = libraryEntryToSampleCacheEntryMap.get( libraryEntryId );
		
		long rawFloatPosition = SampleCache.frameToRawFloat( readFramePosition, leNumChannels );
		int blockNumber = (int)(rawFloatPosition / blockBufferingConfiguration.blockLengthInFloats );
		boolean clientChangedBlocks = (blockNumber != clientLastReadBlockNumber);
		client.setLastReadBlockNumber( blockNumber );
		int readFloatsOffset = (int)(rawFloatPosition % blockBufferingConfiguration.blockLengthInFloats );

		OpenLongObjectHashMap<SampleCacheBlock> blockIdToSampleCacheBlockMap = sce.getAtomicSampleCacheBlocksMap().get();
		long blockMapIndex = buildBlockMapIndex( libraryEntryId, blockNumber );
		
		SampleCacheBlock curBlock = null;
//		log.debug("Reading real samples for output at position " + outputFramePos + " from read frame position " + readFramePosition + " of " + numFramesToRead + " frames");
//		log.debug("This begins in block " + blockNumber + " at raw float position " + rawFloatPosition );
		while( totalNumFloatsToRead > 0 )
		{
			curBlock = blockIdToSampleCacheBlockMap.get( blockMapIndex );
			if( curBlock == null )
			{
				// No more data in the buffer
				// Fill in remaining samples with nothing.
				Arrays.fill( outputSamples, curOutputFloatPos, curOutputFloatPos + totalNumFloatsToRead, 0.0f );
				break;
			}

			FloatBufferBlock curBlockData = curBlock.blockData;
			
			int numFloatsInBlock = curBlockData.getNumReadableFloatsInBlock();
			int floatsReadableFromPositionInBlock = numFloatsInBlock - readFloatsOffset;
			
			int numFloatsThisRound = (totalNumFloatsToRead < floatsReadableFromPositionInBlock ? totalNumFloatsToRead : floatsReadableFromPositionInBlock);
//			log.debug("Doing read of " + numFloatsThisRound + " floats from block " + blockNumber + " readpos " + readFloatsOffset + " writing to pos " + curOutputFloatPos );
			
			float[] curBlockBuffer = curBlockData.getBuffer();

			System.arraycopy( curBlockBuffer, readFloatsOffset, outputSamples, curOutputFloatPos, numFloatsThisRound );

			rawFloatPosition += numFloatsThisRound;
			curOutputFloatPos += numFloatsThisRound;
			
			floatsReadableFromPositionInBlock -= numFloatsThisRound;
			if( floatsReadableFromPositionInBlock <= 0 )
			{
				// Go up a block
				blockNumber++;
				blockMapIndex = buildBlockMapIndex( libraryEntryId, blockNumber );
				readFloatsOffset = 0;
				clientChangedBlocks = true;
			}
			else
			{
				readFloatsOffset += numFloatsThisRound;
			}
			totalNumFloatsToRead -= numFloatsThisRound;
		}
		
		if( clientChangedBlocks )
		{
//			log.debug("Client changed blocks, will wake population thread");
			cachePopulatorThread.addOneJobToDo();
		}
		
		return retVal;
	}

	public RealtimeMethodReturnCodeEnum readSamplesInBlocksForCacheClient(
				InternalSampleCacheClient client,
				long readFramePosition,
				int numFramesToRead,
				SampleAcceptor sampleAcceptor)
	{
		RealtimeMethodReturnCodeEnum retVal = RealtimeMethodReturnCodeEnum.SUCCESS;
		LibraryEntry libraryEntry = client.getLibraryEntry();
			
		int libraryEntryId = libraryEntry.getLibraryEntryId();
//		log.debug("Reading samples for " + libraryEntry.getLocation() + " at frame position " + readFramePosition + " of " + numFramesToRead );
			
		int leNumChannels = libraryEntry.getNumChannels();
		long leTotalNumFrames = libraryEntry.getTotalNumFrames();
			
		int totalNumFloatsToRead = numFramesToRead * leNumChannels;
			
		if( readFramePosition < 0 )
		{
			long numZeroFrames = -readFramePosition;
			int numZeroFramesToFill = (int)(numZeroFrames > numFramesToRead ? numFramesToRead : numZeroFrames );

			sampleAcceptor.acceptEmptySamples(0, numZeroFramesToFill );
			
			readFramePosition += numZeroFramesToFill;
			numFramesToRead -= numZeroFramesToFill;
			if( numFramesToRead == 0 )
			{
				return RealtimeMethodReturnCodeEnum.SUCCESS;
			}
		}
		
		// Check if it's all zeros at the end
		if( readFramePosition >= leTotalNumFrames )
		{
			sampleAcceptor.acceptEmptySamples(readFramePosition, numFramesToRead);
			return RealtimeMethodReturnCodeEnum.SUCCESS;
		}
		
		long framesOver = (readFramePosition + numFramesToRead) - leTotalNumFrames;
		int numZerosAtEnd = (framesOver > 0 ? (int)framesOver : 0 );
		numFramesToRead -= numZerosAtEnd;
		
		SampleCacheEntry sce = libraryEntryToSampleCacheEntryMap.get( libraryEntryId );
		
		long rawFloatPosition = SampleCache.frameToRawFloat( readFramePosition, leNumChannels );
		
		int blockNumber = (int)(rawFloatPosition / blockBufferingConfiguration.blockLengthInFloats );
		int readFloatsOffset = (int)(rawFloatPosition % blockBufferingConfiguration.blockLengthInFloats );

		OpenLongObjectHashMap<SampleCacheBlock> blockIdToSampleCacheBlockMap = sce.getAtomicSampleCacheBlocksMap().get();
		long blockMapIndex = buildBlockMapIndex( libraryEntryId, blockNumber );
		
		SampleCacheBlock curBlock = null;
		
		while( totalNumFloatsToRead > 0 )
		{
			curBlock = blockIdToSampleCacheBlockMap.get( blockMapIndex );
			if( curBlock == null )
			{
				// No more data in the buffer
				// Fill in remaining samples with nothing.
				sampleAcceptor.acceptEmptySamples( readFramePosition, totalNumFloatsToRead / leNumChannels );
				break;
			}

			FloatBufferBlock curBlockData = curBlock.blockData;
			
			int numFloatsInBlock = curBlockData.getNumReadableFloatsInBlock();
			int floatsReadableFromPositionInBlock = numFloatsInBlock - readFloatsOffset;
			
			int numFloatsThisRound = (totalNumFloatsToRead < floatsReadableFromPositionInBlock ? totalNumFloatsToRead : floatsReadableFromPositionInBlock);
//			log.debug("Doing read of " + numFloatsThisRound + " floats from block " + blockNumber + " readpos " + readFloatsOffset );
			
			float[] curBlockBuffer = curBlockData.getBuffer();
			
			int numFramesThisRound = numFloatsThisRound / leNumChannels;
			sampleAcceptor.acceptSamples( readFramePosition, numFramesThisRound, curBlockBuffer, readFloatsOffset );

			rawFloatPosition += numFloatsThisRound;

			totalNumFloatsToRead -= numFloatsThisRound;
			readFramePosition += numFramesThisRound;

			floatsReadableFromPositionInBlock -= numFloatsThisRound;
			if( floatsReadableFromPositionInBlock <= 0 )
			{
				// Go up a block
				blockNumber++;
				blockMapIndex = buildBlockMapIndex( libraryEntryId, blockNumber );
				readFloatsOffset = 0;
//				log.debug("Going up a block - totalNumFloatsToRead is currently " + totalNumFloatsToRead );
			}
			else
			{
				readFloatsOffset += numFloatsThisRound;
			}
		}
		
		if( numZerosAtEnd > 0 )
		{
			sampleAcceptor.acceptEmptySamples( readFramePosition, numZerosAtEnd );
		}

		return retVal;
	}

	public void refreshCache() throws BlockNotAvailableException, DatastoreException, IOException
	{
		synchronized( cacheAccessMutex )
		{
			hotBlocksToCoolSet.clear();
			hotBlocksToCoolSet.addAll( temperatureBufferBlockMap.getHotBlocks() );
			
			for( SampleCacheEntry sce : currentSampleCacheEntries )
			{
				LibraryEntry le = sce.getLibraryEntry();
				int libraryEntryId = le.getLibraryEntryId();
				OpenLongObjectHashMap<SampleCacheBlock> blocksForCacheEntry = new OpenLongObjectHashMap<SampleCacheBlock>();

				buildBlockCacheBoolsForClients( sce );
				
				int numBlocksForCacheEntry = sce.getNumCacheBlocks();
				
				boolean[] blocksNeedToBeCached = sce.getRequiredCachedBlocks();
				
				for( int i = 0 ; i < numBlocksForCacheEntry ; ++i )
				{
					boolean shouldCacheBlock = blocksNeedToBeCached[ i ];
					long curBlockMapIndex = buildBlockMapIndex( libraryEntryId, i );
					SampleCacheBlock curBlock = temperatureBufferBlockMap.getBlockById( curBlockMapIndex );
					
					if( shouldCacheBlock )
					{
						if( curBlock == null )
						{
//							log.debug("Will populate entry " + le.getTitle() + " offset " + (i*blockBufferingConfiguration.blockLengthInFloats) + " - block " + curBlockMapIndex );
							SampleCacheBlock newlyPopulatedBlock = populateCacheForSampleCacheEntryBlock( sce, le, i, curBlockMapIndex );
							blocksForCacheEntry.put( curBlockMapIndex, newlyPopulatedBlock );
						}
						else
						{
							SampleCacheBlockEnum blockState = curBlock.useStatus.get();
							switch( blockState )
							{
								case HOT:
								{
									hotBlocksToCoolSet.remove( curBlock );
									break;
								}
								case WARM:
								{
									// Re-warm the cache entry
//									log.debug("Will re-warm existing block " + curBlockMapIndex );
									temperatureBufferBlockMap.reheatBlock( curBlock );
									break;
								}
								default:
								{
									break;
								}
							}
							blocksForCacheEntry.put( curBlockMapIndex, curBlock );
						}
					}
					else if( !shouldCacheBlock && curBlock != null && curBlock.useStatus.get() == SampleCacheBlockEnum.HOT )
					{
//						log.debug("Will cool hot block " + curBlockMapIndex );
						temperatureBufferBlockMap.moveBlockFromHotToWarmQueue( curBlockMapIndex );
						hotBlocksToCoolSet.remove( curBlock );
					}
				}
				// Now set this map of blocks into the sample cache client
				sce.getAtomicSampleCacheBlocksMap().set( blocksForCacheEntry );
			}
				
			// Now clean up any remaining hot blocks by moving them to "warm"
			for( SampleCacheBlock hotBlock : hotBlocksToCoolSet )
			{
//				log.debug("Will set orphaned block " + hotBlock.blockID + " to warm");

				temperatureBufferBlockMap.moveBlockFromHotToWarmQueue( hotBlock.blockID );
			}
			
			// Now notify any listeners waiting
			for( TwoTuple<BufferFillCompletionListener, SampleCacheClient> bAndS : listenersToNotifyOnNextCompletion )
			{
				bAndS.getHead().notifyBufferFilled( bAndS.getTail() );
			}
			listenersToNotifyOnNextCompletion.clear();
		} // Synchronised
	}
	
	public void dumpDetails()
	{
		synchronized( cacheAccessMutex )
		{
			int numTotalClients = 0;
			int numUniqueSamples = 0;

			for( SampleCacheEntry sce : currentSampleCacheEntries )
			{
				numUniqueSamples++;
				HashSet<InternalSampleCacheClient> clientsForCacheEntry = sce.getCurrentClientSet();
				
				for( InternalSampleCacheClient iscc : clientsForCacheEntry )
				{
					log.debug("SampleCacheEntry( " + sce.getLibraryEntry().getTitle() + ", " + iscc.getTotalNumFrames() + ", " + iscc.getCurrentFramePosition() + ")");
					numTotalClients++;
				}
			}
			log.debug("Total num clients: " + numTotalClients + " with " + numUniqueSamples + " unique sample(s)");
			temperatureBufferBlockMap.dumpDetails();
		}
	}

	private void buildBlockCacheBoolsForClients( SampleCacheEntry sce )
	{
		HashSet<InternalSampleCacheClient> sampleCacheEntryClients = sce.getCurrentClientSet();
		boolean[] requiredCachedBlocks = sce.getRequiredCachedBlocks();
		Arrays.fill( requiredCachedBlocks, false );
		// Fill in the cue points from the library entry
		LibraryEntry le = sce.getLibraryEntry();
		int numChannels = le.getNumChannels();
		long totalNumFrames = le.getTotalNumFrames();
		
		List<CuePoint> libraryEntryCuePoints = le.getCuePoints();
		for( int i = 0 ; i < libraryEntryCuePoints.size() ; i++ )
		{
			CuePoint cp = libraryEntryCuePoints.get( i );

			long framePosition = cp.getFramePosition();
			framePosition = ( framePosition < 0 ? 0 : (framePosition < totalNumFrames ? framePosition : totalNumFrames ) );
			long floatPosition = SampleCache.frameToRawFloat( framePosition, numChannels );
			setBlocksToCacheFromBlockBoundaries( le, requiredCachedBlocks, floatPosition );
		}
		
		// And do the same for each client and their positions
		for( InternalSampleCacheClient iscc : sampleCacheEntryClients )
		{
			long currentFramePosition = iscc.getCurrentFramePosition();
			currentFramePosition = (currentFramePosition < 0 ? 0 : (currentFramePosition < totalNumFrames ? currentFramePosition : totalNumFrames ) );
			
			long floatPosition = SampleCache.frameToRawFloat( currentFramePosition, numChannels );
			setBlocksToCacheFromBlockBoundaries( le, requiredCachedBlocks, floatPosition );

			long intendedFramePosition = iscc.getIntendedFramePosition();
			intendedFramePosition = (intendedFramePosition < 0 ? 0 : (intendedFramePosition < totalNumFrames ? intendedFramePosition : totalNumFrames ) );
			
			floatPosition = SampleCache.frameToRawFloat( intendedFramePosition, numChannels );
			setBlocksToCacheFromBlockBoundaries( le, requiredCachedBlocks, floatPosition );
		}
	}
	
	private SampleCacheBlock populateCacheForSampleCacheEntryBlock( SampleCacheEntry sce,
			LibraryEntry libraryEntry,
			int blockNumber,
			long blockMapIndex )
		throws BlockNotAvailableException, DatastoreException, IOException
	{
		SampleCacheBlock blockToUse = temperatureBufferBlockMap.getWarmOrFreeBlockCopyID( blockMapIndex );
		int numChannels = libraryEntry.getNumChannels();
		int blockLengthInFrames = blockBufferingConfiguration.blockLengthInFloats / numChannels;
		int destPosition = 0;
		int frameReadOffset = blockLengthInFrames * blockNumber;
		int numFramesLeftToRead = (int)(libraryEntry.getTotalNumFrames() - frameReadOffset);
		int numFrames = ( numFramesLeftToRead < blockLengthInFrames ? numFramesLeftToRead : blockLengthInFrames );
		AudioFileHandleAtom audioFileHandleAtom = sce.getAudioFileHandleAtom();
		float[] cacheBuffer = blockToUse.blockData.getBuffer();
//		log.debug( "Asking for " + numFrames + " frames at frame position " + frameReadOffset );
		audioFileIOService.readFloats( audioFileHandleAtom, cacheBuffer, destPosition, numFrames, frameReadOffset );
		blockToUse.blockData.setNumReadableFloatsInBlock( numFrames * numChannels );
		temperatureBufferBlockMap.setBlockMakeHot( blockMapIndex, blockToUse );
		return blockToUse;
	}
	
	private void setBlocksToCacheFromBlockBoundaries( LibraryEntry le, boolean[] whichBlocksCached, long floatPosition )
	{
		int numFloatsInfront = (int)(blockBufferingConfiguration.minSecsBeforePosition * le.getSampleRate() * le.getNumChannels());
		long infrontPosition = floatPosition - numFloatsInfront;
		if( infrontPosition < 0 )
		{
			infrontPosition = 0;
		}

		int numFloatsAfter = (int)(blockBufferingConfiguration.minSecsAfterPosition * le.getSampleRate() * le.getNumChannels());
		long afterPosition = floatPosition + numFloatsAfter;
		if( afterPosition > le.getTotalNumFloats() )
		{
			afterPosition = le.getTotalNumFloats();
		}

		int fromBlockNum = blockBufferingConfiguration.floatPositionToBlockNumber( infrontPosition );
		int toBlockNum = blockBufferingConfiguration.floatPositionToBlockNumber( afterPosition );
		Arrays.fill( whichBlocksCached, fromBlockNum, toBlockNum, true );
	}
	
	private final static long buildBlockMapIndex( int libraryEntryID, int blockNumber )
	{
		long combined = ((long)libraryEntryID << 32 ) | blockNumber;
        return combined;
	}

	private final static long frameToRawFloat( long frameOffset, int numChannels )
	{
		return frameOffset * numChannels;
	}

	public void registerForBufferFillCompletion( InternalSampleCacheClient client,
			BufferFillCompletionListener completionListener)
	{
		synchronized( cacheAccessMutex )
		{
			log.debug("Adding " + client.getLibraryEntry().getLocation() + " to listeners to notify list");
			listenersToNotifyOnNextCompletion.add( new TwoTuple<BufferFillCompletionListener, SampleCacheClient>( completionListener, client ) );
			log.debug("Added " + client.getLibraryEntry().getLocation() + " to listeners to notify list");
			cachePopulatorThread.addOneJobToDo();
		}		
	}
	
	public void addJobForPopulationThread()
	{
		cachePopulatorThread.addOneJobToDo();
	}
}
