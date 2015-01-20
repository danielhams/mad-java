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

package test.uk.co.modularaudio.util.audio.mad.units.stereotee;

import java.util.ArrayList;
import java.util.Map;

import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.helper.MadChannelDefinitionBuilder;
import uk.co.modularaudio.util.audio.mad.helper.MadChannelDefinitionIndexedCreator;

public class StereoTeeMadDefinition extends MadDefinition<StereoTeeMadDefinition,StereoTeeMadInstance>
	implements MadChannelDefinitionIndexedCreator
{
	public final static String DEFINITION_ID = "stereo_tee";

	// Indexes into the channels
	public final static int CONSUMER_LEFT = 0;
	public final static int CONSUMER_RIGHT = 1;
	public final static int PRODUCER_ONE_LEFT = 2;
	public final static int PRODUCER_ONE_RIGHT = 3;
	public final static int PRODUCER_TWO_LEFT = 4;
	public final static int PRODUCER_TWO_RIGHT = 5;
	public final static int NUM_CHANNELS = 6;

	private MadChannelConfiguration defaultChannelConfiguration = null;

	public StereoTeeMadDefinition( MadClassification classification )
	{
		// constructor is super(name, isParametrable, parameterDefinitions);
		super( DEFINITION_ID, "Stereo Tee", false, classification, new ArrayList<MadParameterDefinition>(), new StereoTeeMadQueueBridge());

		MadChannelDefinition[] defaultChannelDefsArray = MadChannelDefinitionBuilder.buildDefaultChannelDefinitions( this );
		defaultChannelConfiguration = new MadChannelConfiguration( defaultChannelDefsArray );
	}

	@Override
	public MadChannelConfiguration getChannelConfigurationForParameters(Map<MadParameterDefinition, String> paramValues)
	{
		return defaultChannelConfiguration;
	}

	// METHODS FOR MadChannelDefinitionIndexedCreator

	@Override
	public int getNumChannelDefinitions()
	{
		return NUM_CHANNELS;
	}

	@Override
	public MadChannelDefinition buildChannelDefinitionForIndex( int index )
	{
		MadChannelDefinition retVal = null;
		String name = null;
		MadChannelType channelType = null;
		MadChannelDirection channelDirection = null;
		MadChannelPosition channelPosition = null;
		switch( index )
		{
			case CONSUMER_LEFT:
				  name = "Left Input";
				  channelType = MadChannelType.AUDIO;
				  channelDirection = MadChannelDirection.CONSUMER;
				  channelPosition = MadChannelPosition.STEREO_LEFT;
				break;
			case CONSUMER_RIGHT:
				  name = "Right Input ";
				  channelType = MadChannelType.AUDIO;
				  channelDirection = MadChannelDirection.CONSUMER;
				  channelPosition = MadChannelPosition.STEREO_RIGHT;
				break;
			case PRODUCER_ONE_LEFT:
				name = "Output One Left";
				channelType = MadChannelType.AUDIO;
				channelDirection = MadChannelDirection.PRODUCER;
				channelPosition = MadChannelPosition.STEREO_LEFT;
				break;
			case PRODUCER_ONE_RIGHT:
				name = "Output One Right";
				channelType = MadChannelType.AUDIO;
				channelDirection = MadChannelDirection.PRODUCER;
				channelPosition = MadChannelPosition.STEREO_RIGHT;
				break;
			case PRODUCER_TWO_LEFT:
				name = "Output Two Left";
				channelType = MadChannelType.AUDIO;
				channelDirection = MadChannelDirection.PRODUCER;
				channelPosition = MadChannelPosition.STEREO_LEFT;
				break;
			case PRODUCER_TWO_RIGHT:
				name = "Output Two Right";
				channelType = MadChannelType.AUDIO;
				channelDirection = MadChannelDirection.PRODUCER;
				channelPosition = MadChannelPosition.STEREO_RIGHT;
				break;
			default:
				break;
		}
		retVal = new MadChannelDefinition( name, channelType, channelDirection, channelPosition );
		return retVal;
	}
}
