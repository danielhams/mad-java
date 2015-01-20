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

package uk.co.modularaudio.mads.base.audiocvconverter.mu;

import java.util.Map;

import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;

public class AudioCvConverterMadInstanceConfiguration
{
	private int numChannels = -1;
	private MadChannelType inChannelType = null;
	private MadChannelType outChannelType = null;
	private int totalNumChannels = -1;
	
	private MadChannelConfiguration channelConfiguration = null;
	private MadChannelDefinition[] channelDefinitions = null;
	
	public AudioCvConverterMadInstanceConfiguration( Map<MadParameterDefinition, String> parameterValues ) throws MadProcessingException
	{
		String numChannelsStr = parameterValues.get( AudioCvConverterMadDefinition.NUM_CHANNELS_PARAMETER );
		boolean parsed = true;
		numChannels = -1;
		if( numChannelsStr != null)
		{
			numChannels = Integer.parseInt(  numChannelsStr );
			parsed = parsed & true;
		}
		else
		{
			parsed = false;
		}
		String inChannelTypeStr = parameterValues.get( AudioCvConverterMadDefinition.IN_CHANNEL_TYPE_PARAMETER );
		inChannelType = null;
		if( inChannelTypeStr != null )
		{
			inChannelType = MadChannelType.valueOf( inChannelTypeStr );
			switch( inChannelType )
			{
				case AUDIO:
				{
					outChannelType = MadChannelType.CV;
					break;
				}
				case CV:
				{
					outChannelType = MadChannelType.AUDIO;
					break;
				}
				default:
				{
					parsed = false;
					break;
				}
			}

			parsed = parsed && true;
		}
		else
		{
			parsed = false;
		}

		if( numChannels < 1 && numChannels > 8 )
		{
			parsed = false;
		}

		if( !parsed )
		{
//			String msg = "Audio CV Converter requires the number of channels and input channel type parameters to be set for instance creation.";
//			throw new MadProcessingException(  msg  );
			numChannels = 1;
			inChannelType = MadChannelType.AUDIO;
			outChannelType = MadChannelType.CV;
		}
		
		totalNumChannels = numChannels * 2;
		channelDefinitions = new MadChannelDefinition[ totalNumChannels ];

		int curChannelCounter = 0;
		for( int ic = 0 ; ic < numChannels ; ic++ )
		{
			channelDefinitions[ curChannelCounter ] = new MadChannelDefinition( "Input Channel " + (ic + 1),
					inChannelType,
					MadChannelDirection.CONSUMER,
					MadChannelPosition.MONO );
			curChannelCounter++;
		}
		for( int oc = 0 ; oc < numChannels ; oc++ )
		{
			channelDefinitions[ curChannelCounter ] = new MadChannelDefinition( "Output Channel " + (oc + 1),
					outChannelType,
					MadChannelDirection.PRODUCER,
					MadChannelPosition.MONO );
			curChannelCounter++;
		}

		// Build the channel configuration
		channelConfiguration = new MadChannelConfiguration( channelDefinitions );
	}

	public MadChannelConfiguration getChannelConfiguration()
	{
		return channelConfiguration;
	}

	public int getNumInputChannels()
	{
		return numChannels;
	}

	public int getNumOutputChannels()
	{
		return numChannels;
	}

	public MadChannelType getInChannelType()
	{
		return inChannelType;
	}

	public MadChannelType getOutChannelType()
	{
		return outChannelType;
	}

	public int getNumTotalChannels()
	{
		return totalNumChannels;
	}

	public int getIndexForInputChannel( int inoutChannelNum )
	{
		return inoutChannelNum;
	}
	
	public int getIndexForOutputChannel( int outputChannelIndex )
	{
		return outputChannelIndex + numChannels;
	}
}
