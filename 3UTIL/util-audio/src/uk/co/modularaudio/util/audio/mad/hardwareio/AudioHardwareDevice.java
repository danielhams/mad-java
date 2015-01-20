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

public final class AudioHardwareDevice
{
	protected String providerId = null;
	protected String id = null;
	protected String userVisibleName = null;
	protected DeviceDirection streamType = DeviceDirection.PRODUCER;
	protected int numChannels = -1;

	public AudioHardwareDevice( String providerId,
			String id,
			String userVisibleName,
			DeviceDirection streamType,
			int numChannels )
	{
		this.providerId = providerId;
		this.id = id;
		this.userVisibleName = userVisibleName;
		this.streamType = streamType;
		this.numChannels = numChannels;
	}
	
	public String getProviderId()
	{
		return providerId;
	}
	
	public String getId()
	{
		return id;
	}

	public String getUserVisibleName()
	{
		return userVisibleName;
	}

	public DeviceDirection getStreamType()
	{
		return streamType;
	}

	public int getNumChannels()
	{
		return numChannels;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder( id.length() + 2 + userVisibleName.length() );
		sb.append( id );
		sb.append( " " );
		sb.append( userVisibleName );
		return sb.toString();
	}
}
