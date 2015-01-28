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

import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;

public class MadLink
{
	private final MadChannelInstance producerChannelInstance;
	private final MadChannelInstance consumerChannelInstance;

	public MadLink( final MadChannelInstance producerChannelInstance,
			final MadChannelInstance consumerChannelInstance )
	{
		this.producerChannelInstance = producerChannelInstance;
		this.consumerChannelInstance = consumerChannelInstance;
	}

	public MadChannelInstance getProducerChannelInstance()
	{
		return producerChannelInstance;
	}

	public MadChannelInstance getConsumerChannelInstance()
	{
		return consumerChannelInstance;
	}

	@Override
	public String toString()
	{
		final StringBuilder retVal = new StringBuilder();
		retVal.append("ProducerInstance(");
		retVal.append( producerChannelInstance.instance.getInstanceName() );
		retVal.append( ", " );
		retVal.append( producerChannelInstance.definition.name);
		retVal.append( 	") -> ConsumerInstance(" );
		retVal.append( consumerChannelInstance.instance.getInstanceName());
		retVal.append( ", " );
		retVal.append( consumerChannelInstance.definition.name);
		retVal.append( ")");
		return retVal.toString();
	}

	public String toStringWithNamesInGraph( final MadGraphInstance<?,?> graph )
	{
		final StringBuilder retVal = new StringBuilder();
		retVal.append("ProducerInstance(");
		retVal.append( graph.getInstanceNameInGraph( producerChannelInstance.instance ) );
		retVal.append( ", " );
		retVal.append( producerChannelInstance.definition.name);
		retVal.append( 	") -> ConsumerInstance(" );
		retVal.append( graph.getInstanceNameInGraph( consumerChannelInstance.instance ) );
		retVal.append( ", " );
		retVal.append( consumerChannelInstance.definition.name);
		retVal.append( ")");
		return retVal.toString();
	}
}
