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

package uk.co.modularaudio.util.audio.mad.helper;

import java.util.ArrayList;
import java.util.Map;

import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;

public abstract class AbstractNonConfigurableMadDefinition<MD extends MadDefinition<MD, MI>, MI extends MadInstance<MD,MI>>
	extends MadDefinition<MD,MI>
	implements MadChannelDefinitionIndexedCreator
{

	protected final MadChannelConfiguration defaultChannelConfiguration;

	private final int numChannels;
	private final String[] channelNames;
	private final MadChannelType[] channelTypes;
	private final MadChannelPosition[] channelPositions;
	private final MadChannelDirection[] channelDirections;

	public AbstractNonConfigurableMadDefinition( final String definitionId,
			final String userVisibleName,
			final MadClassification classification,
			final MadLocklessQueueBridge<MI> ioQueueBridge,
			final int numChannels,
			final String[] channelNames,
			final MadChannelType[] channelTypes,
			final MadChannelDirection[] channelDirections,
			final MadChannelPosition[] channelPositions )
	{
		super( definitionId,  userVisibleName, false, classification, new ArrayList<MadParameterDefinition>(), ioQueueBridge );

		this.numChannels = numChannels;
		this.channelNames = channelNames;
		this.channelTypes = channelTypes;
		this.channelPositions = channelPositions;
		this.channelDirections = channelDirections;

		final MadChannelDefinition[] defaultChannelDefsArray = MadChannelDefinitionBuilder.buildDefaultChannelDefinitions( this );
		defaultChannelConfiguration = new MadChannelConfiguration( defaultChannelDefsArray );
	}

	@Override
	public MadChannelConfiguration getChannelConfigurationForParameters( final Map<MadParameterDefinition, String> parameterValues )
	{
		return defaultChannelConfiguration;
	}

	@Override
	public int getNumChannelDefinitions()
	{
		return numChannels;
	}

	@Override
	public MadChannelDefinition buildChannelDefinitionForIndex( final int c )
	{
		final MadChannelDefinition retVal = new MadChannelDefinition( channelNames[ c ],
				channelTypes[ c ],
				channelDirections[ c ],
				channelPositions[ c ] );
		return retVal;
	}
}
