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

package uk.co.modularaudio.controller.samplecaching;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;

/**
 * <p>Entry point for operations related to caching of audio data.</p>
 * <p>The sample caching controller is a vertical responsibility controller
 * that delegates and/or coordinates work as appropriate from services
 * that implement the required functionality.</p>
 *
 * @author dan
 */
public interface SampleCachingController
{
	/**
	 * <p>Obtain a sample caching client.</p>
	 * <p>The client will be added to the list of clients that a background
	 * thread will be charged with ensuring sufficient caching before
	 * and after the current frame position of the client.</p>
	 * <p>The file specified will be added to some internal library.</p>
	 * @see SampleCachingService#registerCacheClientForFile(String)
	 */
	SampleCacheClient registerCacheClientForFile( String path )
		throws DatastoreException, NoSuchHibernateSessionException, IOException, UnsupportedAudioFileException;

	/**
	 * <p>Register to receive a callback once the thread that fills the sample
	 * cache has completed the initial population pass.</p>
	 * @see SampleCachingService#registerForBufferFillCompletion(SampleCacheClient, BufferFillCompletionListener)
	 */
	void registerForBufferFillCompletion( SampleCacheClient client, BufferFillCompletionListener completionListener );

	/**
	 * <p>Unregister the supplied client.</p>
	 * @see SampleCachingService#unregisterCacheClientForFile(SampleCacheClient)
	 */
	void unregisterCacheClientForFile( SampleCacheClient client ) throws DatastoreException, RecordNotFoundException, IOException;

	/**
	 * <p>Create a sampling client that can be used with the block resampler service
	 * to output audio at variable speeds.</p>
	 * @see BlockResamplerService#createResamplingClient(String, BlockResamplingMethod)
	 */
	BlockResamplingClient createResamplingClient( final String pathToFile,
			final BlockResamplingMethod resamplingMethod )
			throws DatastoreException, IOException, UnsupportedAudioFileException;

	/**
	 * <p>Promote a regular sample caching client into one that can
	 * be used with the block resampler service.</p>
	 * @see BlockResamplerService#promoteSampleCacheClientToResamplingClient(SampleCacheClient, BlockResamplingMethod)
	 */
	BlockResamplingClient promoteSampleCacheClientToResamplingClient( SampleCacheClient sampleCacheClient,
			BlockResamplingMethod cubic );

	/**
	 * <p>Unregister and clean up a block resampler service client.</p>
	 * @see BlockResamplerService#destroyResamplingClient(BlockResamplingClient)
	 */
	void destroyResamplingClient( final BlockResamplingClient resamplingClient )
			throws DatastoreException, RecordNotFoundException;

	/**
	 * <p>Output debugging information onto the console about the use of the
	 * sample cache</p>
	 * @see SampleCachingService#dumpSampleCacheToLog()
	 */
	void dumpSampleCache();

}
