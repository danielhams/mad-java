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

package uk.co.modularaudio.mads.base.audiocvgen.mu;

import java.util.Map;

import uk.co.modularaudio.util.audio.dsp.Limiter;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class AudioToCvGenMadInstance<D extends AudioToCvGenMadDefinition<D, I>, I extends AudioToCvGenMadInstance<D,I>> extends MadInstance<D,I>
{
//	private static Log log = LogFactory.getLog( AudioToCvGenMadInstance.class.getName() );

	private final AudioToCvGenInstanceConfiguration instanceConfiguration;

	// Used for CV to audio conversions
	private final Limiter limiterRt = new Limiter( 0.99, 5 );

	private boolean active;

	public AudioToCvGenMadInstance( final String instanceName,
			final D definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		instanceConfiguration = definition.getMixerInstanceConfiguration();
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers ,
			final int frameOffset,
			final int numFrames  )
	{
		final MadChannelType consChanType = instanceConfiguration.getConsumerChannelType();
		final int numConvertedChannels = instanceConfiguration.getNumConversionChannels();

		for( int c = 0 ; c < numConvertedChannels ; ++c )
		{
			final int consChanIndex = instanceConfiguration.getConsumerChannelIndex( c );
			final int prodChanIndex = instanceConfiguration.getProducerChannelIndex( c );

			if( channelConnectedFlags.get( consChanIndex ) &&
					channelConnectedFlags.get( prodChanIndex ) )
			{
				final float[] consFloats = channelBuffers[consChanIndex].floatBuffer;
				final float[] prodFloats = channelBuffers[prodChanIndex].floatBuffer;

				switch( consChanType )
				{
					case AUDIO:
					{
						System.arraycopy( consFloats, frameOffset, prodFloats, frameOffset, numFrames );
						break;
					}
					case CV:
					default:
					{
						// Converting to audio, need to min/max it
						final int lastIndex = frameOffset + numFrames;
						for( int s = frameOffset ; s < lastIndex ; ++s )
						{
							final float srcFloat = consFloats[s];
							prodFloats[s] =
									(srcFloat < -1.0f ? -1.0f :
										(srcFloat > 1.0f ? 1.0f :
											srcFloat ) );
						}
						break;
					}
				}
			}
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	@Override
	public void stop() throws MadProcessingException
	{
		// Nothing for now.
	}

	public void setActive( final boolean active )
	{
	}
}
