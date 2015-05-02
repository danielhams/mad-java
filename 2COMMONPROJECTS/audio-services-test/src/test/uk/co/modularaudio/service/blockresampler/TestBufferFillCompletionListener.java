package test.uk.co.modularaudio.service.blockresampler;

import java.util.concurrent.Semaphore;

import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;

public class TestBufferFillCompletionListener implements BufferFillCompletionListener
{
	private final Semaphore receivedSemaphore = new Semaphore( 1 );

	public TestBufferFillCompletionListener()
	{
	}

	@Override
	public void notifyBufferFilled( final SampleCacheClient sampleCacheClient )
	{
		receivedSemaphore.release();
	}


	void waitForSignal() throws InterruptedException
	{
		receivedSemaphore.acquire();
	}
}
