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

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeRuntime.EnvelopeSegment;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class EnvelopeMadInstance extends MadInstance<EnvelopeMadDefinition, EnvelopeMadInstance>
{
//	private static Log log = LogFactory.getLog( EnvelopeMadInstance.class.getName() );
	
	protected int sampleRate = -1;
	
	private Envelope envelope = new Envelope();
	
	private EnvelopeRuntime envelopeRuntime = new EnvelopeRuntime();
	
	public EnvelopeMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			EnvelopeMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( HardwareIOChannelSettings hardwareChannelSettings, MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
			envelope.setSampleRate( (int)sampleRate );
		}
		catch (Exception e)
		{
			throw new MadProcessingException( e );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			MadTimingParameters timingParameters,
			long periodStartFrameTime,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers, int numFrames )
	{
		boolean inGateConnected = channelConnectedFlags.get( EnvelopeMadDefinition.CONSUMER_GATE );
		MadChannelBuffer inGateBuffer = channelBuffers[ EnvelopeMadDefinition.CONSUMER_GATE ];
		boolean inRetriggerConnected = channelConnectedFlags.get( EnvelopeMadDefinition.CONSUMER_RETRIGGER );
		MadChannelBuffer inRetriggerBuffer = channelBuffers[ EnvelopeMadDefinition.CONSUMER_RETRIGGER ];
		
		boolean outGateConnected = channelConnectedFlags.get( EnvelopeMadDefinition.PRODUCER_EGATE );
		MadChannelBuffer outGateBuffer = channelBuffers[ EnvelopeMadDefinition.PRODUCER_EGATE ];
		float[] outGateFloats = outGateBuffer.floatBuffer;
		boolean outAmpConnected = channelConnectedFlags.get( EnvelopeMadDefinition.PRODUCER_EAMP );
		MadChannelBuffer outAmpBuffer = channelBuffers[ EnvelopeMadDefinition.PRODUCER_EAMP ];
		float[] outAmpFloats = outAmpBuffer.floatBuffer;

		int currentPeriodStartIndex = 0;
		int ste = 0;
		
		if( inGateConnected && inRetriggerConnected )
		{
			float[] inGateFloats = inGateBuffer.floatBuffer;
			float[] inRetriggerFloats = inRetriggerBuffer.floatBuffer;
			do
			{
				EnvelopeSegment currentSegment = envelopeRuntime.getCurrentSegment();
//				log.debug("At sample index " + ste + " current period start index is " + currentPeriodStartIndex + " segment type is " + currentSegment.toString() );
				
				switch( currentSegment )
				{
					case ATTACK:
					case DECAY:
					case SUSTAIN:
					{
						// Looking for a release or a trigger
						int s = ste;
						for( ; s < numFrames ; s++ )
						{
							if( inRetriggerFloats[s] > 0.0f )
							{
								// Output current envelope up to exclusive s
								if( s > 0 )
								{
									envelopeRuntime.outputEnvelope( envelope,
											outGateFloats,
											outAmpFloats,
											currentPeriodStartIndex,
											s,
											s - currentPeriodStartIndex );
								}
								// Trigger again the envelope
								envelopeRuntime.trigger( envelope );
								// Carry on looking from s+1
								currentPeriodStartIndex = s;
								s++;
								ste = s;
								// Keep going around the loop - it's a retrigger anyway
							}
							else if( inGateFloats[s] == 0.0f )
							{
								// Key off
								if( s > 0 )
								{
									envelopeRuntime.outputEnvelope( envelope,
											outGateFloats,
											outAmpFloats,
											currentPeriodStartIndex,
											s,
											s - currentPeriodStartIndex );
								}
								envelopeRuntime.release( envelope );
								currentPeriodStartIndex = s;
								s++;
								ste = s;
								// Drop to top level loop
								break;
							}
						}
						
						// If we hit the end of the period, just output the envelope up to this point
						if( currentPeriodStartIndex != s && s >= numFrames )
						{
							envelopeRuntime.outputEnvelope( envelope,
									outGateFloats,
									outAmpFloats,
									currentPeriodStartIndex,
									numFrames,
									numFrames - currentPeriodStartIndex );
							currentPeriodStartIndex = numFrames;
						}
						break;
					}
					case RELEASE:
					case OFF:
					{
						// Looking for a gate / trigger
						int s = ste;
						for( ; s < numFrames ; s++ )
						{
							if( inRetriggerFloats[s] > 0.0f )
							{
								if( s > 0 )
								{
									// Output current envelope up to s
									envelopeRuntime.outputEnvelope( envelope,
											outGateFloats,
											outAmpFloats,
											currentPeriodStartIndex,
											s,
											s - currentPeriodStartIndex );
								}
								// Trigger again the envelope
								envelopeRuntime.trigger( envelope );
								// Move envelope start to new position
								currentPeriodStartIndex = s;
								s++;
								ste = s;
							}
						}
						
						if( currentPeriodStartIndex != s && s == numFrames )
						{
							envelopeRuntime.outputEnvelope( envelope,
									outGateFloats,
									outAmpFloats,
									currentPeriodStartIndex,
									numFrames,
									numFrames - currentPeriodStartIndex );
							currentPeriodStartIndex = numFrames;
						}
						break;
					}
				}
			}
			while( currentPeriodStartIndex < numFrames );
		}
		else
		{
			// Neither connected, reset the envelope variables
			// and spit out nothing.
			envelopeRuntime.reset();
			
			if( outGateConnected )
			{
				Arrays.fill( outGateBuffer.floatBuffer, 0.0f );
			}
			if( outAmpConnected )
			{
				Arrays.fill( outAmpBuffer.floatBuffer, 0.0f );
			}
		}
		
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
	
	public Envelope getEnvelope()
	{
		return envelope;
	}
}
