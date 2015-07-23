package test.uk.co.modularaudio.service.samplecaching;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;

public class CacheFillListener implements BufferFillCompletionListener
{
	final CyclicBarrier cb;

	public CacheFillListener( final CyclicBarrier cb )
	{
		this.cb = cb;
	}

	@Override
	public void notifyBufferFilled(final SampleCacheClient sampleCacheClient)
	{
		try
		{
			cb.await();
		}
		catch( final InterruptedException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( final BrokenBarrierException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cb.reset();
	}
}
