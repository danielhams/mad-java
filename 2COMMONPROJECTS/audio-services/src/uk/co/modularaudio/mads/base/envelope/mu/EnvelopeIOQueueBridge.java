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

package uk.co.modularaudio.mads.base.envelope.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class EnvelopeIOQueueBridge extends MadLocklessQueueBridge<EnvelopeMadInstance>
{
	private static Log log = LogFactory.getLog( EnvelopeIOQueueBridge.class.getName() );

	public static final int COMMAND_IN_ATTACK_FROM_ZERO = 0;
	public static final int COMMAND_IN_ATTACK_MILLIS = 1;
	public static final int COMMAND_IN_ATTACK_WAVE_CHOICE = 2;
	public static final int COMMAND_IN_DECAY_MILLIS = 3;
	public static final int COMMAND_IN_DECAY_WAVE_CHOICE = 4;
	public static final int COMMAND_IN_SUSTAIN_LEVEL = 5;
	public static final int COMMAND_IN_RELEASE_MILLIS = 6;
	public static final int COMMAND_IN_RELEASE_WAVE_CHOICE = 7;

	public EnvelopeIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final EnvelopeMadInstance instance,
			final ThreadSpecificTemporaryEventStorage tses,
			final long periodTimestamp,
			final IOQueueEvent queueEntry )
	{
//		log.debug("Received queued event : " + queueEntry.command);
		switch( queueEntry.command )
		{
			case COMMAND_IN_ATTACK_FROM_ZERO:
			{
				instance.getEnvelope().setAttackFromZero( (queueEntry.value != 0 ) );
				break;
			}
			case COMMAND_IN_ATTACK_MILLIS:
			{
				final float attackMillis = Float.intBitsToFloat( (int)queueEntry.value );
				instance.getEnvelope().setAttackMillis( attackMillis );
				break;
			}
			case COMMAND_IN_ATTACK_WAVE_CHOICE:
			{
				final EnvelopeWaveChoice attackWaveChoice = EnvelopeWaveChoice.values()[ (int)queueEntry.value ];
				instance.getEnvelope().setAttackWaveChoice( attackWaveChoice );
				break;
			}
			case COMMAND_IN_DECAY_MILLIS:
			{
				final float decayMillis = Float.intBitsToFloat( (int)queueEntry.value );
				instance.getEnvelope().setDecayMillis( decayMillis );
				break;
			}
			case COMMAND_IN_DECAY_WAVE_CHOICE:
			{
				final EnvelopeWaveChoice decayWaveChoice = EnvelopeWaveChoice.values()[ (int)queueEntry.value ];
				instance.getEnvelope().setDecayWaveChoice( decayWaveChoice );
				break;
			}
			case COMMAND_IN_SUSTAIN_LEVEL:
			{
				final float sustainLevel = Float.intBitsToFloat( (int)queueEntry.value );
				instance.getEnvelope().setSustainLevel( sustainLevel );
				break;
			}
			case COMMAND_IN_RELEASE_MILLIS:
			{
				final float releaseMillis = Float.intBitsToFloat( (int)queueEntry.value );
				instance.getEnvelope().setReleaseMillis( releaseMillis );
				break;
			}
			case COMMAND_IN_RELEASE_WAVE_CHOICE:
			{
				final EnvelopeWaveChoice releaseWaveChoice = EnvelopeWaveChoice.values()[ (int)queueEntry.value ];
				instance.getEnvelope().setReleaseWaveChoice( releaseWaveChoice );
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
