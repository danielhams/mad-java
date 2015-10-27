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

package uk.co.modularaudio.mads.base.scopen.mu;

import uk.co.modularaudio.mads.base.scopen.ui.display.ScopeWaveDisplay;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;

public class ScopeNInstanceConfiguration
{
	public static final int TRIGGER_INDEX = 0;

	private final int numScopeChannels;
	private final int numTotalChannels;

	private final String[] channelNames;
	private final MadChannelType[] channelTypes;
	private final MadChannelDirection[] channelDirections;
	private final MadChannelPosition[] channelPositions;

	public ScopeNInstanceConfiguration( final int numScopeChannels )
	 throws MadProcessingException
	{
		if( numScopeChannels > ScopeWaveDisplay.MAX_VIS_COLOURS )
		{
			throw new MadProcessingException( "Num scope channels currently unsupported" );
		}
		this.numScopeChannels = numScopeChannels;
		this.numTotalChannels = numScopeChannels + 1;

		channelNames = new String[ numTotalChannels ];
		channelTypes = new MadChannelType[ numTotalChannels ];
		channelDirections = new MadChannelDirection[ numTotalChannels ];
		channelPositions = new MadChannelPosition[ numTotalChannels ];

		// First channel, the trigger
		channelNames[0] = "Input Trigger";
		channelTypes[0] = MadChannelType.CV;
		channelDirections[0] = MadChannelDirection.CONSUMER;
		channelPositions[0] = MadChannelPosition.MONO;

		for( int i = 1 ; i <= numScopeChannels ; ++i )
		{
			channelNames[i] = "Input Signal " + i;
			channelTypes[i] = MadChannelType.CV;
			channelDirections[i] = MadChannelDirection.CONSUMER;
			channelPositions[i] = MadChannelPosition.MONO;
		}
	}

	public ScopeNInstanceConfiguration( final ScopeNInstanceConfiguration ic ) throws MadProcessingException
	{
		this( ic.numScopeChannels );
	}

	public int getNumScopeChannels()
	{
		return numScopeChannels;
	}

	public int getNumTotalChannels()
	{
		return numTotalChannels;
	}

	public String[] getChannelNames()
	{
		return channelNames;
	}

	public MadChannelType[] getChannelTypes()
	{
		return channelTypes;
	}

	public MadChannelDirection[] getChannelDirections()
	{
		return channelDirections;
	}

	public MadChannelPosition[] getChannelPositions()
	{
		return channelPositions;
	}


}
