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

package test.uk.co.modularaudio.util.audio.mad.units.singlechanvolume;

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

public class SingleChannelVolumeMadDefinition extends MadDefinition<SingleChannelVolumeMadDefinition,
	SingleChannelVolumeMadInstance>
	implements MadChannelDefinitionIndexedCreator
{

	// Indexes into the channels
	public final static int CONSUMER = 0;
	public final static int PRODUCER = 1;
	public final static int NUM_CHANNELS = 2;
	
	private MadChannelConfiguration defaultChannelConfiguration = null;

	public SingleChannelVolumeMadDefinition( MadClassification classification )
	{
		// constructor is super(name, isParametrable, parameterDefinitions);
		super( "single_channel_volume", "Single Channel Volume", false, classification, new ArrayList<MadParameterDefinition>(), new SingleChannelVolumeMadQueueBridge());
		
		MadChannelDefinition[] defaultChannelDefsArray = MadChannelDefinitionBuilder.buildDefaultChannelDefinitions( this );
		defaultChannelConfiguration = new MadChannelConfiguration( defaultChannelDefsArray );
	}

	@Override
	public MadChannelConfiguration getChannelConfigurationForParameters( Map<MadParameterDefinition,String> paramValues )
	{
		return defaultChannelConfiguration;
	}
	
	// METHODS FOR MadChannelDefinitionIndexedCreator

	@Override
	public int getNumChannelDefinitions()
	{
		return NUM_CHANNELS;
	}

	public MadChannelDefinition buildChannelDefinitionForIndex( int index )
	{
		MadChannelDefinition retVal = null;
		String name = null;
		MadChannelType channelType = null;
		MadChannelDirection channelDirection = null;
		MadChannelPosition channelPosition = null;
		switch( index )
		{
			case CONSUMER:
				name = "Input";
				channelType = MadChannelType.AUDIO;
				channelDirection = MadChannelDirection.CONSUMER;
				channelPosition = MadChannelPosition.MONO;
				break;
			case PRODUCER:
				name = "Output";
				channelType = MadChannelType.AUDIO;
				channelDirection = MadChannelDirection.PRODUCER;
				channelPosition = MadChannelPosition.MONO;
				break;
			default:
				break;
		}
		retVal = new MadChannelDefinition( name, channelType, channelDirection, channelPosition );
		return retVal;
	}
}
