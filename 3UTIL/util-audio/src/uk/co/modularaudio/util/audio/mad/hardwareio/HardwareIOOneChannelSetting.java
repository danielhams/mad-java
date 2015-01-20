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

import uk.co.modularaudio.util.audio.format.DataRate;


public class HardwareIOOneChannelSetting
{
	private DataRate dataRate;
	private int channelBufferLength;
	
	public HardwareIOOneChannelSetting( DataRate dataRate,
			int channelBufferLength )
	{
		this.dataRate = dataRate;
		this.channelBufferLength = channelBufferLength;
	}

	public DataRate getDataRate()
	{
		return dataRate;
	}

	public int getChannelBufferLength()
	{
		return channelBufferLength;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "DataRate: " );
		sb.append( dataRate.toString() );
		sb.append( " ChannelBufferLength: ");
		sb.append( channelBufferLength );
		return sb.toString();
	}
}
