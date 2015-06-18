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

package test.uk.co.modularaudio.service.stubs;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
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
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class FakeAdvancedComponentsFrontController
	implements ComponentWithLifecycle, AdvancedComponentsFrontController
{

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public String getSoundfileMusicRoot()
	{
		return "/tmp";
	}

	@Override
	public OscillatorFactory getOscillatorFactory()
	{
		return null;
	}

	@Override
	public SampleCacheClient registerCacheClientForFile(final String path)
			throws DatastoreException, UnsupportedAudioFileException
	{
		throw new DatastoreException("NI");
	}

	@Override
	public void unregisterCacheClientForFile(final SampleCacheClient client)
			throws DatastoreException, RecordNotFoundException, IOException
	{
		throw new DatastoreException("NI");
	}

	@Override
	public BlockResamplerService getBlockResamplerService()
	{
		return null;
	}

	@Override
	public SampleCachingService getSampleCachingService()
	{
		return null;
	}

	@Override
	public BlockResamplingClient createResamplingClient( final String pathToFile, final BlockResamplingMethod resamplingMethod )
			throws DatastoreException, UnsupportedAudioFileException
	{
		return null;
	}

	@Override
	public void destroyResamplingClient( final BlockResamplingClient resamplingClient )
			throws DatastoreException, RecordNotFoundException
	{
	}

	@Override
	public void registerForBufferFillCompletion( final SampleCacheClient client,
			final BufferFillCompletionListener completionListener )
	{
		completionListener.notifyBufferFilled( client );
	}

	@Override
	public JobExecutorService getJobExecutorService()
	{
		return null;
	}

	@Override
	public AnalysedData registerForLibraryEntryAnalysis( final LibraryEntry libraryEntry,
			final AnalysisFillCompletionListener analysisListener ) throws DatastoreException
	{
		return null;
	}

	@Override
	public BlockResamplingClient promoteSampleCacheClientToResamplingClient( final SampleCacheClient sampleCacheClient,
			final BlockResamplingMethod cubic )
	{
		// TODO Auto-generated method stub
		return null;
	}
}
