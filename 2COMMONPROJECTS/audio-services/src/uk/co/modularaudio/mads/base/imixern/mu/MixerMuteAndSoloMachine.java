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

package uk.co.modularaudio.mads.base.imixern.mu;

import java.util.Arrays;

import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class MixerMuteAndSoloMachine<D extends MixerNMadDefinition<D, I>, I extends MixerNMadInstance<D, I>>
{
//	private static Log log = LogFactory.getLog( MixerMuteAndSoloMachine.class.getName() );

	private final LaneProcessor[] laneProcessors;

	private final int numLanes;

	private final boolean[] channelMuteValues;
	private final boolean[] channelSoloValues;
	private int numChannelsInSolo;

	public MixerMuteAndSoloMachine( final LaneProcessor[] laneProcessors )
	{
		this.laneProcessors = laneProcessors;

		numLanes = laneProcessors.length;

		channelMuteValues = new boolean[ numLanes ];
		channelSoloValues = new boolean[ numLanes ];
		Arrays.fill( channelMuteValues, false );
		Arrays.fill( channelSoloValues, false );
	}

	public void setLaneMute( final ThreadSpecificTemporaryEventStorage tses,
			final long timestamp,
			final int laneNumber,
			final boolean muteValue )
	{
//		log.debug("Received lane mute of " + muteValue + " for lane " + laneNumber );
		channelMuteValues[ laneNumber ] = muteValue;
		updateLaneProcessors( tses, timestamp );
	}

	public void setLaneSolo( final ThreadSpecificTemporaryEventStorage tses,
			final long timestamp,
			final int laneNumber,
			final boolean soloValue )
	{
//		log.debug("Received lane solo of " + soloValue + " for lane " + laneNumber );
		channelSoloValues[ laneNumber ] = soloValue;
		if( soloValue )
		{
			numChannelsInSolo++;
		}
		else
		{
			numChannelsInSolo--;
		}
		updateLaneProcessors( tses, timestamp );
	}

	private void updateLaneProcessors( final ThreadSpecificTemporaryEventStorage tses, final long timestamp )
	{
//		log.debug("in updateLaneProcessors there are " + numChannelsInSolo + " channels in solo");
		final boolean soloMode = ( numChannelsInSolo > 0 );
		if( soloMode )
		{
//			log.debug("Updating in solo mode");
			for( int i = 0 ; i < numLanes ; i++ )
			{
				final boolean channelSolod = channelSoloValues[ i ];
				laneProcessors[i].setLaneActive( channelSolod );
			}
		}
		else
		{
//			log.debug("Updating in mute mode");
			for( int i = 0 ; i < numLanes ; i++ )
			{
				final boolean channelMuted = channelMuteValues[ i ];
				laneProcessors[i].setLaneActive( !channelMuted );
			}
		}
	}

}
