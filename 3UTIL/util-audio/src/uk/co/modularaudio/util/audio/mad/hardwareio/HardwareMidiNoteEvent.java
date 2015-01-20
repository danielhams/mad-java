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

package uk.co.modularaudio.util.audio.mad.hardwareio;

import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventType;



public class HardwareMidiNoteEvent
{
	public int channel = -1;
	public long eventFrameTime = -1;
	public MadChannelNoteEventType eventType = null;
	public int paramOne = -1;
	public int paramTwo = -1;
	// For continuations, it's the midi number of the previous note we are a continuation of
	public int paramThree = -1;
	
	public HardwareMidiNoteEvent()
	{
		reset();
	}

	public void reset()
	{
		channel = -1;
		eventFrameTime = -1;
		eventType = MadChannelNoteEventType.EMPTY;
		paramOne = -1;
		paramTwo = -1;
		paramThree = -1;
	}

	public void set( int channel, long eventFrameTime, MadChannelNoteEventType eventType, int paramOne, int paramTwo, int paramThree )
	{
		this.channel = channel;
		this.eventFrameTime = eventFrameTime;
		this.eventType = eventType;
		this.paramOne = paramOne;
		this.paramTwo = paramTwo;
		this.paramThree = paramThree;
	}
}
