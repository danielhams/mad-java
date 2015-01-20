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



public class MadChannelNoteEvent
{
	protected int channel = -1;
	protected int eventSampleIndex = -1;
	protected MadChannelNoteEventType eventType = null;
	protected int paramOne = 0;
	protected int paramTwo = 0;
	// For continuations, it's the midi number of the previous note we are a continuation of
	protected int paramThree = 0;

	public MadChannelNoteEvent()
	{
		reset();
	}

	public final void reset()
	{
		channel = -1;
		eventSampleIndex = -1;
		eventType = MadChannelNoteEventType.EMPTY;
		paramOne = -1;
		paramTwo = -1;
		paramThree = -1;
	}

	public void set( int channel, int eventSampleIndex, MadChannelNoteEventType eventType, int paramOne, int paramTwo, int paramThree )
	{
		this.channel = channel;
		this.eventSampleIndex = eventSampleIndex;
		this.eventType = eventType;
		this.paramOne = paramOne;
		this.paramTwo = paramTwo;
		this.paramThree = paramThree;
	}

	public int getChannel()
	{
		return channel;
	}

	public int getEventSampleIndex()
	{
		return eventSampleIndex;
	}

	public MadChannelNoteEventType getEventType()
	{
		return eventType;
	}

	public int getParamOne()
	{
		return paramOne;
	}

	public int getParamTwo()
	{
		return paramTwo;
	}

	public int getParamThree()
	{
		return paramThree;
	}

	public void setEventType( MadChannelNoteEventType eventType )
	{
		this.eventType = eventType;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(64);
		sb.append( "c:" );
		sb.append( channel );
		sb.append( " s:");
		sb.append( eventSampleIndex );
		sb.append( " t:" );
		sb.append( eventType );
		sb.append( " p1:" );
		sb.append( paramOne );
		sb.append( " p2:" );
		sb.append( paramTwo );
		sb.append( " p3:" );
		sb.append( paramThree );
		return sb.toString();
	}
}
