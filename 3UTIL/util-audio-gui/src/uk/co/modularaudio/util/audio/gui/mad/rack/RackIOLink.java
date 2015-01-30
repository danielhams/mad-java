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

package uk.co.modularaudio.util.audio.gui.mad.rack;

import uk.co.modularaudio.util.audio.mad.MadChannelInstance;

public class RackIOLink
{
	private final MadChannelInstance rackChannelInstance;
	private final RackComponent rackComponent;
	private final MadChannelInstance rackComponentChannelInstance;

	public RackIOLink( final MadChannelInstance rackChannelInstance,
			final RackComponent rackComponent,
			final MadChannelInstance rackComponentChannelInstance )
	{
		this.rackChannelInstance = rackChannelInstance;
		this.rackComponent = rackComponent;
		this.rackComponentChannelInstance = rackComponentChannelInstance;
	}

	public MadChannelInstance getRackChannelInstance()
	{
		return rackChannelInstance;
	}

	public RackComponent getRackComponent()
	{
		return rackComponent;
	}

	public MadChannelInstance getRackComponentChannelInstance()
	{
		return rackComponentChannelInstance;
	}

	@Override
	public String toString()
	{
		final StringBuilder retVal = new StringBuilder();
		retVal.append("Rack channel(");
		retVal.append( rackChannelInstance.toString() );
		retVal.append( ") -> RackComponent(" );
		retVal.append( rackComponent.getComponentName() );
		retVal.append( ", " );
		retVal.append( rackComponentChannelInstance.toString() );
		retVal.append( ")");
		return retVal.toString();
	}
}
