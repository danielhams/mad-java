package uk.co.modularaudio.service.renderingplan;

import uk.co.modularaudio.service.apprendering.util.AppRenderingJobQueue;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public interface RenderingJob
{
	@Override
	String toString();

	RenderingJob[] getConsJobsThatWaitForUs();

	boolean markOneProducerAsCompleteCheckIfReadyToGo();

	RealtimeMethodReturnCodeEnum goWithTimestamps( int jobThreadExecutor, ThreadSpecificTemporaryEventStorage tempQueueEntryStorage );

	void forDumpResetNumProducersStillToComplete();

	long getJobStartTimestamp();

	long getJobEndTimestamp();

	int getJobThreadExecutor();

	void addSelfToQueue( AppRenderingJobQueue renderingJobQueue );

}