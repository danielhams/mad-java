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

package uk.co.modularaudio.util.audio.mad;

import uk.co.modularaudio.util.audio.buffer.BufferObjectCopier;

public class MadChannelNoteEventCopier implements BufferObjectCopier<MadChannelNoteEvent>
{

	@Override
	public void copyValues( final MadChannelNoteEvent in, final MadChannelNoteEvent out )
	{
		out.channel = in.channel;
		out.eventSampleIndex = in.eventSampleIndex;
		out.eventType = in.eventType;
		out.paramOne = in.paramOne;
		out.paramTwo = in.paramTwo;
		out.paramThree = in.paramThree;
	}

	@Override
	public MadChannelNoteEvent allocInstance()
	{
		return new MadChannelNoteEvent();
	}

	public void copyArray( final MadChannelNoteEvent[] inputNotes, final MadChannelNoteEvent[] outputNotes )
	{
		boolean done = false;
		for( int i = 0 ; !done && i < inputNotes.length ; i++ )
		{
			copyValues( inputNotes[ i ], outputNotes[ i ] );
			if( inputNotes[i].eventType == MadChannelNoteEventType.EMPTY )
			{
				done = true;
			}
		}
	}

}
