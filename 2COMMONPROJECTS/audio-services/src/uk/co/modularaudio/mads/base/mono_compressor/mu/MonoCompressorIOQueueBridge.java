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

package uk.co.modularaudio.mads.base.mono_compressor.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.stereo_gate.ui.ThresholdTypeEnum;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class MonoCompressorIOQueueBridge extends MadLocklessQueueBridge<MonoCompressorMadInstance>
{
	private static Log log = LogFactory.getLog( MonoCompressorIOQueueBridge.class.getName() );

	public static final int COMMAND_IN_THRESHOLD = 0;
	public static final int COMMAND_IN_THRESHOLD_TYPE = 1;
	public static final int COMMAND_IN_ATTACK_MILLIS = 2;
	public static final int COMMAND_IN_RELEASE_MILLIS = 3;
	public static final int COMMAND_IN_RATIO = 4;
	public static final int COMMAND_IN_MAKEUP_GAIN = 5;
	public static final int COMMAND_IN_ACTIVE = 6;
	public static final int COMMAND_IN_LOOKAHEAD = 7;

	public static final int COMMAND_OUT_SIGNAL_IN_METER = 8;
	public static final int COMMAND_OUT_SIGNAL_OUT_METER = 9;
	public static final int COMMAND_OUT_ENV_VALUE = 10;
	public static final int COMMAND_OUT_ATTENUATION = 11;

	public MonoCompressorIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final MonoCompressorMadInstance instance,
			final ThreadSpecificTemporaryEventStorage tses,
			final long periodTimestamp,
			final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_IN_THRESHOLD:
			{
				final float valueAsFloat = Float.intBitsToFloat( (int)queueEntry.value );
				instance.desiredThresholdDb = valueAsFloat;
//				log.debug("Set desired threshold dB to " + instance.desiredThresholdDb );
				break;
			}
			case COMMAND_IN_THRESHOLD_TYPE:
			{
				final int valueAsInt = (int)queueEntry.value;
				instance.desiredThresholdType = ThresholdTypeEnum.values()[ valueAsInt ];
//				log.debug("Set thresholdtype to " + instance.desiredThresholdType );
				break;
			}
			case COMMAND_IN_ATTACK_MILLIS:
			{
				final int valueAsInt = (int)queueEntry.value;
				final float valAsFloat = Float.intBitsToFloat( valueAsInt );
				instance.desiredAttack = valAsFloat;
				instance.attackSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( instance.sampleRate,
						valAsFloat );
//				log.debug("Set attack millis to " + valAsFloat + " which is " + instance.attackSamples + " samples");
				break;
			}
			case COMMAND_IN_RELEASE_MILLIS:
			{
				final int valueAsInt = (int)queueEntry.value;
				final float valAsFloat = Float.intBitsToFloat( valueAsInt );
				instance.desiredRelease = valAsFloat;
				instance.releaseSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( instance.sampleRate,
						valAsFloat );
//				log.debug("Set release millis to " + valAsFloat + " which is " + instance.releaseSamples + " samples");
				break;
			}
			case COMMAND_IN_RATIO:
			{
				final int valueAsInt = (int)queueEntry.value;
				float valAsFloat = Float.intBitsToFloat( valueAsInt );
				valAsFloat = (valAsFloat == 0.0f ? 1.0f : valAsFloat );
				instance.desiredCompRatio = 1.0f / valAsFloat;
//				log.debug("Set ratio to " + valAsFloat + " which is " + instance.desiredCompRatio + " as a multiplier");
				break;
			}
			case COMMAND_IN_MAKEUP_GAIN:
			{
				final int valueAsInt = (int)queueEntry.value;
				float valAsFloat = Float.intBitsToFloat( valueAsInt );
				valAsFloat = (valAsFloat == 0.0f ? 1.0f : valAsFloat );
				instance.desiredMakeupGain = (float)AudioMath.dbToLevel( valAsFloat );
//				log.debug("Set makeup gain to " + valAsFloat + " which is " + instance.desiredMakeupGain + " as a multiplier");
				break;
			}
			case COMMAND_IN_ACTIVE:
			{
				instance.active = (queueEntry.value == 1);
				break;
			}
			case COMMAND_IN_LOOKAHEAD:
			{
				final boolean bValue = (queueEntry.value == 1 );
				instance.desiredLookahead = bValue;
				break;
			}
			default:
			{
				final String msg ="Unknown command to instance: " + queueEntry.command;
				log.error( msg );
				break;
			}
		}

	}
}
