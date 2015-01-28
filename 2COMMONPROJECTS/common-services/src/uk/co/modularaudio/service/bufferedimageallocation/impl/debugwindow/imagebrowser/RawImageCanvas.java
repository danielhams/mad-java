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

package uk.co.modularaudio.service.bufferedimageallocation.impl.debugwindow.imagebrowser;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.FreeEntry;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.RawImage;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.UsedEntry;

public class RawImageCanvas extends JComponent
{
	private static Log log = LogFactory.getLog( RawImageCanvas.class.getName() );

	private static final long serialVersionUID = 9129489845103805746L;

	private BufferedImage curBufferedImage;

	private final ArrayList<Rectangle> freeBlockRectangles = new ArrayList<Rectangle>();
	private final ArrayList<Rectangle> usedBlockRectangles = new ArrayList<Rectangle>();

	private int width = 0;
	private int height = 0;

	private final Composite opaqueComposite = AlphaComposite.getInstance( AlphaComposite.SRC );
	private final Composite ninetyPercentAlphaComposite = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.9f );
//	private Composite fiftyPercentAlphaComposite = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.5f );
//	private Composite tenPercentAlphaComposite = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.1f );

	private static final Color DARK_BLUE = Color.blue.darker().darker();

	public RawImageCanvas()
	{
	}

	public void clearDisplayedImage()
	{
		curBufferedImage = null;
		freeBlockRectangles.clear();
		revalidate();
	}

	public void setDisplayedImage( final RawImage ri, final Set<FreeEntry> freeEntrySet, final Set<UsedEntry> usedEntrySet )
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Selected image has " + freeEntrySet.size() + " free entries");
		}
		curBufferedImage = ri.getRootBufferedImage();
		freeBlockRectangles.clear();
		for( final FreeEntry fe : freeEntrySet )
		{
			final Rectangle freeBlockRectangle = new Rectangle( fe.getX(), fe.getY(), fe.getWidth(), fe.getHeight() );
			freeBlockRectangles.add( freeBlockRectangle );
		}
		usedBlockRectangles.clear();
		for( final UsedEntry ue : usedEntrySet )
		{
			final Rectangle usedBlockRectangle = new Rectangle( ue.getX(), ue.getY(), ue.getWidth(), ue.getHeight() );
			usedBlockRectangles.add( usedBlockRectangle );
		}
		width = curBufferedImage.getWidth();
		height = curBufferedImage.getHeight();
		doSizeThingy( width, height );
		revalidate();
	}

	private void doSizeThingy( final int width, final int height )
	{
		final Dimension size = new Dimension( width + 1, height + 1);
		this.setMinimumSize( size );
		this.setSize( size );
		this.setMaximumSize( size );
		this.setPreferredSize( size );
	}

	private final Rectangle clipBounds = new Rectangle();

	@Override
	public void paint( final Graphics rawG )
	{
		final Graphics2D g = (Graphics2D)rawG;
		g.getClipBounds( clipBounds );

		if( curBufferedImage != null )
		{
			g.setComposite( opaqueComposite );
			g.setColor( Color.DARK_GRAY );
			g.fillRect( 0, 0, width, height );
			g.drawImage( curBufferedImage, 0, 0, null );

			// Draw the free blocks over the top
			// We draw 50 transparent fill then outline with full yellow
			g.setColor( DARK_BLUE );
			g.setComposite( ninetyPercentAlphaComposite );
//			g.setComposite( fiftyPercentAlphaComposite );
//			g.setComposite( tenPercentAlphaComposite );
			for( final Rectangle freeBlockRectangle : freeBlockRectangles )
			{
				g.fillRect( freeBlockRectangle.x, freeBlockRectangle.y, freeBlockRectangle.width, freeBlockRectangle.height );
			}

			g.setColor( Color.yellow );
			g.setComposite( opaqueComposite );
			for( final Rectangle freeBlockRectangle : freeBlockRectangles )
			{
				g.drawRect( freeBlockRectangle.x, freeBlockRectangle.y, freeBlockRectangle.width, freeBlockRectangle.height );
			}

			g.setComposite( opaqueComposite );
			g.setColor( Color.RED );
			for( final Rectangle usedBlockRectangle : usedBlockRectangles )
			{
				g.drawRect( usedBlockRectangle.x, usedBlockRectangle.y, usedBlockRectangle.width, usedBlockRectangle.height );
			}
		}
		else
		{
			g.setColor( Color.gray );
			g.fillRect( 0, 0, width, height );
		}
	}

}
