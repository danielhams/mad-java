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

package uk.co.modularaudio.mads.masterio.mu;

import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;

public class IOMadConfiguration
{
	private final String[] channelNames;

	private final MadChannelType[] channelTypes;

	private final MadChannelDirection[] channelDirections;

	private final MadChannelPosition[] channelPositions;

	private final int numAudioChannels;
	private final int numNoteChannels;
	private final int numTotalChannels;

	public IOMadConfiguration( final int numAudioChannels, final int numNoteChannels, final MadChannelDirection configurationDirection )
	{
		this.numAudioChannels = numAudioChannels;
		this.numNoteChannels = numNoteChannels;

		numTotalChannels = numAudioChannels + numNoteChannels;

		channelNames = new String[ numTotalChannels ];
		channelTypes = new MadChannelType[ numTotalChannels ];
		channelDirections = new MadChannelDirection[ numTotalChannels ];
		channelPositions = new MadChannelPosition[ numTotalChannels ];

		int oi = 0;
		final String configDirStr = (configurationDirection == MadChannelDirection.PRODUCER ? "Output" : "Input" );
		for( int c = 0 ; c < numAudioChannels ; c++ )
		{
			channelNames[ oi ] =  configDirStr + " Channel " + (c + 1);
			channelTypes[ oi ] = MadChannelType.AUDIO;
			channelDirections[ oi ] = configurationDirection;
			channelPositions[ oi ] = MadChannelPosition.MONO;
			oi++;
		}
		for( int n = 0 ; n < numNoteChannels ; n++ )
		{
			channelNames[ oi ] = configDirStr + " Note Channel " + (n + 1);
			channelTypes[ oi ] = MadChannelType.NOTE;
			channelDirections[ oi ] = configurationDirection;
			channelPositions[ oi ] = MadChannelPosition.MONO;
			oi++;
		}
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

	public int getAudioChannelIndex( final int audioChannelNum )
	{
		return audioChannelNum;
	}

	public int getNoteChannelIndex( final int noteChannelNum )
	{
		return numAudioChannels + noteChannelNum;
	}

	public int getNumAudioChannels()
	{
		return numAudioChannels;
	}

	public int getNumNoteChannels()
	{
		return numNoteChannels;
	}

	public int getNumTotalChannels()
	{
		return numTotalChannels;
	}

}
