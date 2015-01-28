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

public class MadClassification
{
	public enum ReleaseState
	{
		RELEASED,
		BETA,
		ALPHA
	};
	private final MadClassificationGroup group;
	private final String id;
	private final String name;
	private final String description;
	private final ReleaseState state;

	public MadClassification( final MadClassificationGroup group,
			final String id,
			final String name,
			final String description,
			final ReleaseState state )
	{
		this.group = group;
		this.id = id;
		this.name = name;
		this.description = description;
		this.state = state;
	}

	public MadClassificationGroup getGroup()
	{
		return group;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public ReleaseState getState()
	{
		return state;
	}
}
