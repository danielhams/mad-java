package uk.co.modularaudio.mads.base.scope.mu;

import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class ScopeIOQueueBridge extends MadLocklessQueueBridge<ScopeMadInstance>
{

	@Override
	public void receiveQueuedEventsToInstance( final ScopeMadInstance instance, final ThreadSpecificTemporaryEventStorage tses,
			final long periodTimestamp, final IOQueueEvent queueEntry )
	{
	}

}
