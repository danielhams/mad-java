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

package uk.co.modularaudio.mads.base.supersawmodule.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.wavetable.valuemapping.SuperSawDetuneValueMappingWaveTable;
import uk.co.modularaudio.util.audio.wavetable.valuemapping.SuperSawOsc4AmpValueMappingWaveTable;
import uk.co.modularaudio.util.audio.wavetable.valuemapping.SuperSawSideOscAmpValueMappingWaveTable;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class SuperSawModuleMadInstance extends MadInstance<SuperSawModuleMadDefinition,SuperSawModuleMadInstance>
{
//	private static Log log = LogFactory.getLog( SampleAndHoldMadInstance.class.getName() );
	
	private static final int SUPERSAW_MAPPING_TABLE_LENGTH = 1024;
	
	private SuperSawDetuneValueMappingWaveTable detuneValueMappingTable =
			new SuperSawDetuneValueMappingWaveTable( SUPERSAW_MAPPING_TABLE_LENGTH );
	
	private SuperSawOsc4AmpValueMappingWaveTable osc4AmpMappingTable =
			new SuperSawOsc4AmpValueMappingWaveTable( SUPERSAW_MAPPING_TABLE_LENGTH );
	
	private SuperSawSideOscAmpValueMappingWaveTable sideOscAmpMappingTable =
			new SuperSawSideOscAmpValueMappingWaveTable( SUPERSAW_MAPPING_TABLE_LENGTH );
	
	private static final float O1_DETUNE_MAX = 0.11002313f;
	private static final float O2_DETUNE_MAX = 0.06288439f;
	private static final float O3_DETUNE_MAX = 0.01952356f;

	private static final float O5_DETUNE_MAX = 0.01991221f;
	private static final float O6_DETUNE_MAX = 0.06216538f;
	private static final float O7_DETUNE_MAX = 0.10745242f;
	
	public SuperSawModuleMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			SuperSawModuleMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( HardwareIOChannelSettings hardwareChannelSettings, MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
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
		
		boolean inCvConnected = channelConnectedFlags.get( SuperSawModuleMadDefinition.CONSUMER_CV_IN );
		float[] inCvFloats = channelBuffers[ SuperSawModuleMadDefinition.CONSUMER_CV_IN ].floatBuffer;
		boolean inCvFreqConnected = channelConnectedFlags.get(  SuperSawModuleMadDefinition.CONSUMER_CV_FREQ_IN );
		float[] inCvFreqFloats = channelBuffers[ SuperSawModuleMadDefinition.CONSUMER_CV_FREQ_IN ].floatBuffer;

//		boolean outCvConnected = channelConnectedFlags.get( SuperSawModuleMadDefinition.PRODUCER_CV_OUT );
		float[] outCvFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OUT ].floatBuffer;
		
		boolean inMixConnected = channelConnectedFlags.get( SuperSawModuleMadDefinition.CONSUMER_CV_MIX_IN );
		float[] inMixFloats = channelBuffers[ SuperSawModuleMadDefinition.CONSUMER_CV_MIX_IN ].floatBuffer;
		
		float[] o1FreqFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC1_FREQ ].floatBuffer;
		float[] o2FreqFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC2_FREQ ].floatBuffer;
		float[] o3FreqFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC3_FREQ ].floatBuffer;
		float[] o4FreqFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC4_FREQ ].floatBuffer;
		float[] o5FreqFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC5_FREQ ].floatBuffer;
		float[] o6FreqFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC6_FREQ ].floatBuffer;
		float[] o7FreqFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC7_FREQ ].floatBuffer;
		
		float[] o1AmpFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC1_AMP ].floatBuffer;
		float[] o2AmpFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC2_AMP ].floatBuffer;
		float[] o3AmpFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC3_AMP ].floatBuffer;
		float[] o4AmpFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC4_AMP ].floatBuffer;
		float[] o5AmpFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC5_AMP ].floatBuffer;
		float[] o6AmpFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC6_AMP ].floatBuffer;
		float[] o7AmpFloats = channelBuffers[ SuperSawModuleMadDefinition.PRODUCER_CV_OSC7_AMP ].floatBuffer;
		
		if( inCvConnected )
		{
			for( int s = 0 ; s < numFrames ; s++ )
			{
				float detuneAmount = detuneValueMappingTable.getValueAtNormalisedPosition( inCvFloats[s] );
				outCvFloats[s] = (detuneAmount < 0.0f ? 0.0f : (detuneAmount > 1.0f ? 1.0f : detuneAmount ) );
			}
			
			if( inCvFreqConnected )
			{
				for( int s = 0 ; s < numFrames ; s++ )
				{
					float inFreq = inCvFreqFloats[s];
					float curDetune = outCvFloats[s];
					o1FreqFloats[s] = (1 - (curDetune * O1_DETUNE_MAX)) * inFreq;
					o2FreqFloats[s] = (1 - (curDetune * O2_DETUNE_MAX)) * inFreq;
					o3FreqFloats[s] = (1 - (curDetune * O3_DETUNE_MAX)) * inFreq;
					o4FreqFloats[s] = inFreq;
					o5FreqFloats[s] = (1 + (curDetune * O5_DETUNE_MAX)) * inFreq;
					o6FreqFloats[s] = (1 + (curDetune * O6_DETUNE_MAX)) * inFreq;
					o7FreqFloats[s] = (1 + (curDetune * O7_DETUNE_MAX)) * inFreq;
				}
			}
			else
			{
				Arrays.fill( o1FreqFloats, 0, numFrames, 0.0f );
				Arrays.fill( o2FreqFloats, 0, numFrames, 0.0f );
				Arrays.fill( o3FreqFloats, 0, numFrames, 0.0f );
				Arrays.fill( o4FreqFloats, 0, numFrames, 0.0f );
				Arrays.fill( o5FreqFloats, 0, numFrames, 0.0f );
				Arrays.fill( o6FreqFloats, 0, numFrames, 0.0f );
				Arrays.fill( o7FreqFloats, 0, numFrames, 0.0f );
			}
		}
		else
		{
			for( int s = 0 ; s < numFrames ; s++ )
			{
				outCvFloats[ s ] = 0.0f;
			}
		}
		
		if( inMixConnected )
		{
			for( int s = 0 ; s < numFrames ; s++ )
			{
				float curMixValue = inMixFloats[s];
				float centerOscAmpMapValue = osc4AmpMappingTable.getValueAtNormalisedPosition( curMixValue );
				o4AmpFloats[s] = centerOscAmpMapValue;
				float sideOscAmpMapValue = sideOscAmpMappingTable.getValueAtNormalisedPosition( curMixValue );
				o1AmpFloats[s] = sideOscAmpMapValue;
				o2AmpFloats[s] = sideOscAmpMapValue;
				o3AmpFloats[s] = sideOscAmpMapValue;
				o5AmpFloats[s] = sideOscAmpMapValue;
				o6AmpFloats[s] = sideOscAmpMapValue;
				o7AmpFloats[s] = sideOscAmpMapValue;
			}
		}
		else
		{
			float curMixValue = 0.0f;
			o4AmpFloats[0] = osc4AmpMappingTable.getValueAtNormalisedPosition( curMixValue );
			Arrays.fill( o4AmpFloats, 1, numFrames, o4AmpFloats[0] );
			
			float sideOscAmpMapValue = sideOscAmpMappingTable.getValueAtNormalisedPosition( curMixValue );
			o1AmpFloats[0] = sideOscAmpMapValue;
			Arrays.fill( o1AmpFloats, 1, numFrames, o1AmpFloats[0] );
			
			System.arraycopy( o1AmpFloats, 0, o2AmpFloats, 0, numFrames );
			System.arraycopy( o1AmpFloats, 0, o3AmpFloats, 0, numFrames );
			System.arraycopy( o1AmpFloats, 0, o5AmpFloats, 0, numFrames );
			System.arraycopy( o1AmpFloats, 0, o6AmpFloats, 0, numFrames );
			System.arraycopy( o1AmpFloats, 0, o7AmpFloats, 0, numFrames );
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
