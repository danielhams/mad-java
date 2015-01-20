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

package uk.co.modularaudio.service.samplecaching;

import javax.sound.sampled.UnsupportedAudioFileException;

import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public interface SampleCachingService
{
	public SampleCacheClient registerCacheClientForFile( String path ) throws NoSuchHibernateSessionException, DatastoreException, UnsupportedAudioFileException;
	public void unregisterCacheClientForFile( SampleCacheClient client ) throws DatastoreException, RecordNotFoundException;
	
	public RealtimeMethodReturnCodeEnum readSamplesForCacheClient( SampleCacheClient client, float[] outputSamples, int outputFramePos, int numFrames );
	
	public RealtimeMethodReturnCodeEnum readSamplesInBlocksForCacheClient( SampleCacheClient client,
			long framePosition,
			int numFrames,
			SampleAcceptor sampleAcceptor );
			
	public void registerForBufferFillCompletion( SampleCacheClient client, BufferFillCompletionListener completionListener );

	public void dumpSampleCacheToLog();

	public String getSampleFileTitleForCacheClient( SampleCacheClient sampleCacheClient );

	public void addJobToCachePopulationThread();
}
