package uk.co.modularaudio.mads.base.scope.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.scope.ui.ScopeRepetitionsChoiceUiJComponent.RepetitionChoice;
import uk.co.modularaudio.mads.base.scope.ui.ScopeTriggerChoiceUiJComponent.TriggerChoice;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class ScopeIOQueueBridge extends MadLocklessQueueBridge<ScopeMadInstance>
{
	private static Log log = LogFactory.getLog( ScopeIOQueueBridge.class.getName() );

	public static final int COMMAND_IN_ACTIVE = 0;

	public static final int COMMAND_IN_CAPTURE_SAMPLES = 1;
	public static final int COMMAND_IN_TRIGGER = 2;
	public static final int COMMAND_IN_REPETITION = 3;
	public static final int COMMAND_IN_RECAPTURE = 4;

	public static final int COMMAND_OUT_DATA_START = 5;
	public static final int COMMAND_OUT_RINGBUFFER_WRITE_INDEX = 6;


	@Override
	public void receiveQueuedEventsToInstance( final ScopeMadInstance instance, final ThreadSpecificTemporaryEventStorage tses, final long periodTimestamp, final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_IN_ACTIVE:
			{
				final boolean active = ( queueEntry.value == 1 );
				instance.setActive( active );
				break;
			}
			case COMMAND_IN_CAPTURE_SAMPLES:
			{
				final int captureSamples = (int)queueEntry.value;
				instance.setCaptureSamples( captureSamples );
				break;
			}
			case COMMAND_IN_TRIGGER:
			{
				final TriggerChoice trigger = TriggerChoice.values()[ (int)queueEntry.value ];
				instance.setTriggerChoice( trigger );
				break;
			}
			case COMMAND_IN_REPETITION:
			{
				final RepetitionChoice repetition = RepetitionChoice.values()[ (int)queueEntry.value ];
				instance.setRepetitionChoice( repetition );
				break;
			}
			case COMMAND_IN_RECAPTURE:
			{
				instance.doRecapture();
				break;
			}
			default:
			{
				final String msg = "Unknown command passed on incoming queue: " + queueEntry.command;
				log.error( msg );
			}
		}
	}
}
