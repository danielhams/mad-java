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

public final class MidiHardwareDevice
{
	protected final String providerId;
	protected final String id;
	protected final String userVisibleName;
	protected final DeviceDirection streamType;

	public MidiHardwareDevice( final String providerId,
			final String id,
			final String userVisibleString,
			final DeviceDirection streamType )
	{
		this.providerId = providerId;
		this.id = id;
		this.userVisibleName = userVisibleString;
		this.streamType = streamType;
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

	public DeviceDirection getType()
	{
		return streamType;
	}

}
