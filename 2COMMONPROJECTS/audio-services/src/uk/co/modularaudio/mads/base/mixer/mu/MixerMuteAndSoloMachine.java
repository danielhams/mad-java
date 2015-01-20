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

package uk.co.modularaudio.mads.base.mixer.mu;

import java.util.Arrays;

import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class MixerMuteAndSoloMachine
{
//	private static Log log = LogFactory.getLog( Channel8MixerMuteAndSoloMachine.class.getName() );

	private MixerMadInstance instance = null;
	private LaneProcessor[] channelLaneProcessors = null;
	
	private int numChannels = -1;
	
	private boolean[] channelMuteValues = null;
	private boolean[] channelSoloValues = null;
	private int numChannelsInSolo = 0;

	public MixerMuteAndSoloMachine( MixerMadInstance instance,
			LaneProcessor[] channelLaneProcessors )
	{
		this.instance = instance;
		this.channelLaneProcessors = channelLaneProcessors;
		
		numChannels = channelLaneProcessors.length;
		
		channelMuteValues = new boolean[ numChannels ];
		channelSoloValues = new boolean[ numChannels ];
		Arrays.fill( channelMuteValues, false );
		Arrays.fill( channelSoloValues, false );
	}

	public void setLaneMute( ThreadSpecificTemporaryEventStorage tses, long timestamp, int laneNumber, boolean muteValue )
	{
//		log.debug("Received lane mute of " + muteValue + " for lane " + laneNumber );
		channelMuteValues[ laneNumber ] = muteValue;
		updateLaneProcessors( tses, timestamp );
	}

	public void setLaneSolo( ThreadSpecificTemporaryEventStorage tses, long timestamp, int laneNumber, boolean soloValue )
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
	
	private void updateLaneProcessors( ThreadSpecificTemporaryEventStorage tses, long timestamp )
	{
		boolean soloMode = ( numChannelsInSolo > 0 );
		if( soloMode )
		{
//			log.debug("Updating in solo mode");
			for( int i = 0 ; i < numChannels ; i++ )
			{
				boolean channelSolod = channelSoloValues[ i ];
				channelLaneProcessors[ i ].setLaneActive( channelSolod );
				
				instance.emitLaneSoloStatus( tses, timestamp, i, channelSolod );
				instance.emitLaneMuteStatus( tses, timestamp, i, channelMuteValues[ i ] );
			}
			
		}
		else
		{
//			log.debug("Updating in mute mode");
			for( int i = 0 ; i < numChannels ; i++ )
			{
				boolean channelMuted = channelMuteValues[ i ];
				channelLaneProcessors[ i ].setLaneActive( !channelMuted );
				
				instance.emitLaneMuteStatus( tses, timestamp, i, channelMuted );
				instance.emitLaneSoloStatus( tses, timestamp, i, false );
			}
		}
	}

}
