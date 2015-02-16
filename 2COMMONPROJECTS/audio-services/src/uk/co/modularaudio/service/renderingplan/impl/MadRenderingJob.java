package uk.co.modularaudio.service.renderingplan.impl;

import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingSource;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public interface MadRenderingJob
{
	String getInstanceName();

	MadInstance<?, ?> getMadInstance();

	MadChannelBuffer[] getChannelBuffers();

	RealtimeMethodReturnCodeEnum go( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			MadTimingSource timingSource );

	MadChannelConnectedFlags getChannelConnectedFlags();

}