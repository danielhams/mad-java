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

package uk.co.modularaudio.util.audio.gui.mad;

import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public abstract class MadUiDefinition
	<D extends MadDefinition<D,I>,
	I extends MadInstance<D,I>>
	implements IMadUiDefinition<D, I>
{
//	private static Log log = LogFactory.getLog( MadUiDefinition.class.getName() );

	protected final D definition;
	protected final BufferedImageAllocator bufferedImageAllocator;

	protected String imagePrefix;

	protected boolean isDraggable;
	protected boolean isParametrable;

	public MadUiDefinition( final BufferedImageAllocator bia,
			final String imagePrefix,
			final D definition ) throws DatastoreException
	{
		this( bia, imagePrefix, definition, true, false );
	}

	public MadUiDefinition( final BufferedImageAllocator bia,
			final String imagePrefix,
			final D definition,
			final boolean isDraggable,
			final boolean isParametrable ) throws DatastoreException
	{
		this.definition = definition;
		this.isDraggable = isDraggable;
		this.isParametrable = isParametrable;
		this.imagePrefix = imagePrefix;

		this.bufferedImageAllocator = bia;
	}

	public abstract AbstractMadUiInstance<D,I> createNewUiInstance( I instance ) throws DatastoreException;

	public final D getDefinition()
	{
		return definition;
	}

	public final boolean isDraggable()
	{
		return isDraggable;
	}

	public final boolean isParametrable()
	{
		return isParametrable;
	}

	public final String getImagePrefix()
	{
		return imagePrefix;
	}

	public abstract Span getCellSpan();

	public final BufferedImageAllocator getBufferedImageAllocator()
	{
		return bufferedImageAllocator;
	}

	@SuppressWarnings("unchecked")
	public AbstractMadUiInstance<D, I> createNewUiInstanceUT( final MadInstance<?,?> componentInstance )
		throws DatastoreException
	{
		return createNewUiInstance( (I)componentInstance);
	}

	@Override
	public String getUiImagePrefix()
	{
		return imagePrefix;
	}
}
