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

package uk.co.modularaudio.util.audio.gui.mad.rollpainter;

public class RollPaintDefaultUpdateStructure implements RollPaintUpdate
{
	private RollPaintDirection direction = RollPaintDirection.FORWARDS;
	private RollPaintUpdateType updateType = RollPaintUpdateType.NONE;
	private int numSamplesInUpdate;

	public RollPaintDefaultUpdateStructure()
	{
	}

	@Override
	public RollPaintDirection getDirection()
	{
		return direction;
	}

	@Override
	public RollPaintUpdateType getUpdateType()
	{
		return updateType;
	}

	@Override
	public int getNumSamplesInUpdate()
	{
		return numSamplesInUpdate;
	}

	public void setDirection( final RollPaintDirection direction )
	{
		this.direction = direction;
	}

	public void setUpdateType( final RollPaintUpdateType updateType )
	{
		this.updateType = updateType;
	}

	public void setNumSamplesInUpdate( final int numSamplesInUpdate )
	{
		this.numSamplesInUpdate = numSamplesInUpdate;
	}

	public void setUpdateValues( final RollPaintUpdateType updateType,
			final RollPaintDirection direction,
			final int numSamplesInUpdate )
	{
		this.updateType = updateType;
		this.direction = direction;
		this.numSamplesInUpdate = numSamplesInUpdate;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("UpdateType(");
		sb.append( updateType.toString() );
		sb.append( ") Direction(" );
		sb.append( direction.toString() );
		sb.append( ") NumSamplesInUpdate(" );
		sb.append( numSamplesInUpdate );
		sb.append( ")" );
		return sb.toString();
	}
}
