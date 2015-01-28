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

import java.util.Collection;
import java.util.Map;

import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;

public abstract class MadDefinition<D extends MadDefinition<D,I>, I extends MadInstance<D,I>>
{
	private final String id;
	private final String name;
	private final MadClassification classification;
	private final boolean isParametrable;
	private final MadParameterDefinition[] parameterDefinitions;
	private final MadLocklessQueueBridge<I> ioQueueBridge;

	public MadDefinition( final String id,
			final String name,
			final boolean isParametrable,
			final MadClassification classification,
			final Collection<MadParameterDefinition> parameterDefinitions,
			final MadLocklessQueueBridge<I> ioQueueBridge )
	{
		this.id = id;
		this.name = name;
		this.isParametrable = isParametrable;
		this.classification = classification;
		this.parameterDefinitions = parameterDefinitions.toArray( new MadParameterDefinition[ parameterDefinitions.size()] );
		this.ioQueueBridge = ioQueueBridge;
	}

	public final String getId()
	{
		return id;
	}

	public final String getName()
	{
		return name;
	}

	public final boolean isParametrable()
	{
		return isParametrable;
	}

	public MadParameterDefinition[] getParameterDefinitions()
	{
		return parameterDefinitions;
	}

	public abstract MadChannelConfiguration getChannelConfigurationForParameters( Map<MadParameterDefinition, String> parameterValues )
		throws MadProcessingException;

	@Override
	public String toString()
	{
		return("MadDefinition(" + this.getClass().getSimpleName() + ", " + name + ")");
	}

	public final MadClassification getClassification()
	{
		return classification;
	}

	public final MadLocklessQueueBridge<I> getIoQueueBridge()
	{
		return ioQueueBridge;
	}

}
