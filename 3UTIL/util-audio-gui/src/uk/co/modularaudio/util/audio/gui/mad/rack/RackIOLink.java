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
	private MadChannelInstance rackChannelInstance = null;
	private RackComponent rackComponent = null;
	private MadChannelInstance rackComponentChannelInstance = null;
	
	public RackIOLink( MadChannelInstance rackChannelInstance,
			RackComponent rackComponent,
			MadChannelInstance rackComponentChannelInstance )
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

	public String toString()
	{
		StringBuilder retVal = new StringBuilder();
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
