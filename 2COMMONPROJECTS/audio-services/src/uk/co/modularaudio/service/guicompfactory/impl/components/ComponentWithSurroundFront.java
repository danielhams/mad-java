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

package uk.co.modularaudio.service.guicompfactory.impl.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentImageCache;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;



public class ComponentWithSurroundFront extends AbstractGuiAudioComponent
{
	private static final int DRAG_BAR_WIDTH = 20;

	private static final long serialVersionUID = -117457865168310944L;
//	private static Log log = LogFactory.getLog( ComponentWithSurroundFront.class.getName() );

	//	private BufferedImageAllocationService bufferedImageAllocationService = null;
//	private AllocationMatch allocationMatch = new AllocationMatch();

	private final GuiComponentImageCache imageCache;

	private BufferedImage componentImage;

	private final Rectangle renderedRectangle = new Rectangle();

	private final ComponentFront guiPanelFront;

	public ComponentWithSurroundFront( final GuiComponentImageCache imageCache, final RackComponent inComponent )
	{
		super( inComponent );
		this.imageCache = imageCache;
//		this.bufferedImageAllocationService = bufferedImageAllocationService;

		// Allow stuff underneath to show through
		this.setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "inset 1" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );
//		msh.addLayoutConstraint( "debug" );

		msh.addColumnConstraint( "[][grow, fill][]" );
		msh.addRowConstraint( "fill" );
		this.setLayout( msh.createMigLayout() );

		this.add( new RackEdgeDragBarSpacer( ComponentSide.FRONT) );
		guiPanelFront = new ComponentFront( inComponent );
		this.add( guiPanelFront );
		this.add( new RackEdgeDragBarSpacer( ComponentSide.FRONT) );
	}

	@Override
	public boolean isPointLocalDragRegion(final Point localPoint)
	{
		if( !rackComponent.isDraggable() )
		{
			return false;
		}
		// Only allow dragging in the first/last 10 pixels of the component width
		final int curWidth = this.getWidth();
		final int mouseXPos = localPoint.x;
//		int mouseYPos = localPoint.y;
		if( (mouseXPos > PaintedComponentDefines.INSET && mouseXPos < PaintedComponentDefines.INSET + DRAG_BAR_WIDTH) ||
					(mouseXPos < curWidth - PaintedComponentDefines.INSET && mouseXPos > curWidth - DRAG_BAR_WIDTH ) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private BufferedImage getComponentImage()
	{
		if( componentImage == null )
		{
			componentImage = imageCache.getImageForRackComponent( rackComponent, this.getWidth(), this.getHeight(), true );
		}
		return componentImage;
	}

	@Override
	public void paint( final Graphics g )
	{
		renderedRectangle.setBounds( PaintedComponentDefines.INSET, 0,
				this.getWidth() - ( 2 * PaintedComponentDefines.INSET ),  getHeight() ) ;
		// Paint a rounded rectangle that shows our outline
		g.drawImage( getComponentImage(), 0, 0, null );
		super.paint( g );

		if( PaintedComponentDefines.DRAWING_DEBUG )
		{
			g.setColor( Color.red );
			g.drawRect( 0, 0, getWidth() - 1, getHeight() - 1);
		}
	}

	@Override
	public Rectangle getRenderedRectangle()
	{
		return renderedRectangle;
	}

	@Override
	public GuiChannelPlug getPlugFromPosition(final Point localPoint)
	{
		return null;
	}

	@Override
	public GuiChannelPlug getPlugFromMadChannelInstance( final MadChannelInstance auChannelInstance )
	{
		return null;
	}

	@Override
	public String toString()
	{
		return rackComponent.getComponentName();
	}

	@Override
	public void destroy()
	{
		guiPanelFront.destroy();
	}
}
