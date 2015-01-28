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
import uk.co.modularaudio.util.audio.mad.MadLink;

public class RackLink
{
	private final RackComponent producerRackComponent;
	private final RackComponent consumerRackComponent;
	private final MadLink link;

	public RackLink( final RackComponent producerRackComponent,
			final MadChannelInstance producerChannelInstance,
			final RackComponent consumerRackComponent,
			final MadChannelInstance consumerChannelInstance,
			final MadLink link )
	{
		this.producerRackComponent = producerRackComponent;
		this.consumerRackComponent = consumerRackComponent;
		this.link = link;
	}

	public RackComponent getProducerRackComponent()
	{
		return producerRackComponent;
	}

	public RackComponent getConsumerRackComponent()
	{
		return consumerRackComponent;
	}

	public MadLink getLink()
	{
		return link;
	}

	public MadChannelInstance getProducerChannelInstance()
	{
		return link.getProducerChannelInstance();
	}

	public MadChannelInstance getConsumerChannelInstance()
	{
		return link.getConsumerChannelInstance();
	}

	@Override
	public String toString()
	{
		final StringBuilder retVal = new StringBuilder();
		retVal.append("ProducerComponent(");
		retVal.append( producerRackComponent.getComponentName() );
		retVal.append( ", " );
		retVal.append( link.getProducerChannelInstance().toString() );
		retVal.append( ") -> ConsumerComponent(" );
		retVal.append( consumerRackComponent.getComponentName() );
		retVal.append( ", " );
		retVal.append( link.getConsumerChannelInstance().toString() );
		retVal.append( ")");
		return retVal.toString();
	}
}
