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

import java.awt.image.BufferedImage;

import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;
import uk.co.modularaudio.util.table.Span;

public abstract class MadUiDefinition
	<D extends MadDefinition<D,I>,
	I extends MadInstance<D,I>>
	implements IMadUiDefinition<D, I>
{
//	private static Log log = LogFactory.getLog( MadUiDefinition.class.getName() );

	protected final D definition;
	protected final BufferedImageAllocator bufferedImageAllocator;
	protected final ImageFactory imageFactory;

	protected BufferedImage frontBufferedImage;
	protected BufferedImage backBufferedImage;

	protected boolean isDraggable;
	protected boolean isParametrable;

	public MadUiDefinition( final BufferedImageAllocator bia,
			final ImageFactory imageFactory,
			final String imageRoot,
			final String imagePrefix,
			final D definition ) throws DatastoreException
	{
		this( bia, imageFactory, imageRoot, imagePrefix, definition, true, false );
	}

	public MadUiDefinition( final BufferedImageAllocator bia,
			final ImageFactory imageFactory,
			final String imageRoot,
			final String imagePrefix,
			final D definition,
			final boolean isDraggable,
			final boolean isParametrable ) throws DatastoreException
	{
		this.bufferedImageAllocator = bia;
		this.imageFactory = imageFactory;
		this.definition = definition;
		this.isDraggable = isDraggable;
		this.isParametrable = isParametrable;

		frontBufferedImage = imageFactory.getBufferedImage( imageRoot,
				imagePrefix + "_front.png" );

		backBufferedImage = imageFactory.getBufferedImage( imageRoot,
				imagePrefix + "_back.png");
	}

	public abstract AbstractMadUiInstance<?, ?> createNewUiInstance( I instance ) throws DatastoreException;

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

	public final BufferedImageAllocator getBufferedImageAllocator()
	{
		return bufferedImageAllocator;
	}

	public abstract Span getCellSpan();

	@Override
	public final BufferedImage getFrontBufferedImage()
	{
		return frontBufferedImage;
	}

	@Override
	public final BufferedImage getBackBufferedImage()
	{
		return backBufferedImage;
	}

}
