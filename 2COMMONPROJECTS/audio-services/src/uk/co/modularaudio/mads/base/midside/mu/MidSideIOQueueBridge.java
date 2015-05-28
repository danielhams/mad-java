package uk.co.modularaudio.mads.base.midside.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class MidSideIOQueueBridge extends MadLocklessQueueBridge<MidSideMadInstance>
{
	private static Log log = LogFactory.getLog( MidSideIOQueueBridge.class.getName() );

	public static final int COMMAND_IN_MS_TYPE = 0;

	@Override
	public void receiveQueuedEventsToInstance( final MidSideMadInstance instance,
			final ThreadSpecificTemporaryEventStorage tses,
			final long periodTimestamp,
			final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case MidSideIOQueueBridge.COMMAND_IN_MS_TYPE:
			{
				final boolean isLrToMs = (queueEntry.value == 1);
				instance.setMidSideType( isLrToMs );
				break;
			}
			default:
			{
				final String msg = "Unknown command: " + queueEntry.command;
				log.error( msg );
				break;
			}
		}
	}

}
