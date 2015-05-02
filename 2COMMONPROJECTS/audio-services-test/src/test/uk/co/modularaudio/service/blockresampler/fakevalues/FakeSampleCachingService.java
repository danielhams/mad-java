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

package test.uk.co.modularaudio.service.blockresampler.fakevalues;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleAcceptor;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class FakeSampleCachingService implements ComponentWithLifecycle, SampleCachingService
{
	private static Log log = LogFactory.getLog( FakeSampleCachingService.class.getName() );

	private final FakeSampleCacheClient theClient = new FakeSampleCacheClient();

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public SampleCacheClient registerCacheClientForFile(final String path)
			throws NoSuchHibernateSessionException, DatastoreException,
			UnsupportedAudioFileException
	{
		return theClient;
	}

	@Override
	public void unregisterCacheClientForFile(final SampleCacheClient client)
			throws DatastoreException, RecordNotFoundException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum readSamplesForCacheClient(
			final SampleCacheClient client,
			final float[] outputSamples,
			final int outputFloatPos,
			final int numFrames)
	{
		final FakeSampleCacheClient scc = (FakeSampleCacheClient)client;
		final long curPos = scc.getCurrentFramePosition();
		log.debug("Faking frames from " + curPos + " of length " + numFrames );

		for( int i = 0 ; i < numFrames ; ++i )
		{
			final int leftOutputIndex = outputFloatPos + (i * 2);
			final int rightOutputIndex = leftOutputIndex + 1;
			float value = (curPos + i);
			if( value < 0 )
			{
				value = 0;
			}
			else if( value >= scc.getTotalNumFrames() )
			{
				value = 0;
			}

			outputSamples[ leftOutputIndex ] = value;
			outputSamples[ rightOutputIndex ] = value;
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	@Override
	public void dumpSampleCacheToLog()
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum readSamplesInBlocksForCacheClient(
			final SampleCacheClient client, final long framePosition, final int numFrames,
			final SampleAcceptor sampleAcceptor)
	{
		return RealtimeMethodReturnCodeEnum.FAIL_FATAL;
	}

	@Override
	public String getSampleFileTitleForCacheClient( final SampleCacheClient sampleCacheClient )
	{
		return "BANANAS FOR YOU MY DEAR";
	}

	@Override
	public void registerForBufferFillCompletion(final SampleCacheClient client,
			final BufferFillCompletionListener completionListener)
	{
		// Just call back immediately
		completionListener.notifyBufferFilled(client);
	}

	@Override
	public void addJobToCachePopulationThread()
	{
	}

}
