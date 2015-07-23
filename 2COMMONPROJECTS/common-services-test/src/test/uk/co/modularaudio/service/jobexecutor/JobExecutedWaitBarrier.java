package test.uk.co.modularaudio.service.jobexecutor;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class JobExecutedWaitBarrier implements Runnable
{
	final CyclicBarrier cb;

	public JobExecutedWaitBarrier( final CyclicBarrier cb )
	{
		this.cb = cb;
	}

	@Override
	public void run()
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
