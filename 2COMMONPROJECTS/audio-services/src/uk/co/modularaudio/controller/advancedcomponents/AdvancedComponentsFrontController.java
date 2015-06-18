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

package uk.co.modularaudio.controller.advancedcomponents;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.AnalysisFillCompletionListener;
import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.jobexecutor.JobExecutorService;
import uk.co.modularaudio.service.library.LibraryEntry;
import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactory;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

/**
 * <p>Entry point for audio components that require advanced functionality.</p>
 * <p>For components such as a sound file player that require audio data be
 * cached in a background thread.</p>
 * <p>This front controller takes care of any needed database transaction
 * related to operations.</p>
 * <p>For operations within the real time rendering thread (such
 * as reading a block of buffered audio data) it is recommended that
 * the runtime of audio components obtain references to the services needed
 * to perform the methods directly (such as the BlockResamplerService).</p>
 *
 * @author dan
 */
public interface AdvancedComponentsFrontController
{
	/**
	 * <p>For components that allow selection of sound files, this method
	 * allows those components to have the same filesystem root.</p>
	 */
	String getSoundfileMusicRoot();

	/**
	 * <p>The oscillator factory can be used to obtain the various
	 * audio oscillators that may be used for things such as direct
	 * synthesis or LFO modulation.</p>
	 */
	OscillatorFactory getOscillatorFactory();

	/**
	 * <p>For components playing back cached audio content at anything
	 * other than unity the block resampler service can be used.</p>
	 */
	BlockResamplerService getBlockResamplerService();

	/**
	 * <p>For components interested purely in the original buffered
	 * content it can be obtained via the SampleCachingService.</p>
	 */
	SampleCachingService getSampleCachingService();

	/**
	 * <p>Obtain a sample cache client for a file.</p>
	 * <p>This method takes care of the necessary database transaction
	 * for filling an internal library with metadata.</p>
	 * @see SampleCachingService#registerCacheClientForFile(String)
	 */
	SampleCacheClient registerCacheClientForFile( String path )
		throws DatastoreException, IOException, UnsupportedAudioFileException;

	/**
	 * <p>Get the service that allows execution of background jobs.</p>
	 */
	JobExecutorService getJobExecutorService();

	/**
	 * <p>Register to receive a callback once the thread that fills the sample
	 * cache has completed the initial population pass.</p>
	 * @see SampleCachingService#registerForBufferFillCompletion(SampleCacheClient, BufferFillCompletionListener)
	 */
	void registerForBufferFillCompletion( SampleCacheClient client, BufferFillCompletionListener completionListener );

	/**
	 * <p>Release a sample cache client.</p>
	 * @see SampleCachingService#unregisterCacheClientForFile(SampleCacheClient)
	 */
	void unregisterCacheClientForFile( SampleCacheClient client ) throws DatastoreException, RecordNotFoundException, IOException;

	BlockResamplingClient createResamplingClient( final String pathToFile, final BlockResamplingMethod resamplingMethod )
			throws DatastoreException, IOException, UnsupportedAudioFileException;

	BlockResamplingClient promoteSampleCacheClientToResamplingClient( SampleCacheClient sampleCacheClient,
			BlockResamplingMethod cubic );

	void destroyResamplingClient( final BlockResamplingClient resamplingClient )
			throws DatastoreException, RecordNotFoundException;

	AnalysedData registerForLibraryEntryAnalysis( LibraryEntry libraryEntry,
			AnalysisFillCompletionListener analysisListener ) throws DatastoreException;


}
