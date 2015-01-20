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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.service.gui.valueobjects.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.gui.valueobjects.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentImageCache;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;



public class GuiAudioComponentFront extends AbstractGuiAudioComponent
{
	private static final int DRAG_BAR_WIDTH = 20;

	private static final long serialVersionUID = -117457865168310944L;
//	private static Log log = LogFactory.getLog( GuiAudioComponentFront.class.getName() );
	
	private final static int INSET = 3;
	//	private BufferedImageAllocationService bufferedImageAllocationService = null;
//	private AllocationMatch allocationMatch = new AllocationMatch();
	
	private GuiComponentImageCache imageCache = null;
	
	private BufferedImage componentImage = null;
	
	private Rectangle renderedRectangle = new Rectangle();

	private GuiJPanelFront guiPanelFront = null;
	
	public GuiAudioComponentFront( GuiComponentImageCache imageCache, RackComponent inComponent )
	{
		super( inComponent );
		this.imageCache = imageCache;
//		this.bufferedImageAllocationService = bufferedImageAllocationService;

		// Allow stuff underneath to show through
		this.setOpaque( false );
		this.setLayout( new MigLayout( "inset 1, gap 0, fill", "[][grow, fill][]", "fill") );
		
		this.add( new RackEdgeDragBarSpacer( ComponentSide.FRONT) );
		guiPanelFront = new GuiJPanelFront( inComponent );
		this.add( guiPanelFront );
		this.add( new RackEdgeDragBarSpacer( ComponentSide.FRONT) );
	}

	@Override
	public boolean isPointLocalDragRegion(Point localPoint)
	{
		if( !rackComponent.isDraggable() )
		{
			return false;
		}
		// Only allow dragging in the first/last 10 pixels of the component width
		int curWidth = this.getWidth();
		int mouseXPos = localPoint.x;
//		int mouseYPos = localPoint.y;
		if( (mouseXPos > INSET && mouseXPos < INSET + DRAG_BAR_WIDTH) ||
					(mouseXPos < curWidth - INSET && mouseXPos > curWidth - DRAG_BAR_WIDTH ) )
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
	public void paint( Graphics g )
	{
		renderedRectangle.setBounds( INSET, 0, this.getWidth() - ( 2 * INSET ),  getHeight() ) ;
		// Paint a rounded rectangle that shows our outline
		g.drawImage( getComponentImage(), 0, 0, null );
		super.paint( g );
		
	}
	
	@Override
	public Rectangle getRenderedRectangle()
	{
		return renderedRectangle;
	}

	@Override
	public GuiChannelPlug getPlugFromPosition(Point localPoint)
	{
		return null;
	}

	@Override
	public GuiChannelPlug getPlugFromMadChannelInstance( MadChannelInstance auChannelInstance )
	{
		return null;
	}
	
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
