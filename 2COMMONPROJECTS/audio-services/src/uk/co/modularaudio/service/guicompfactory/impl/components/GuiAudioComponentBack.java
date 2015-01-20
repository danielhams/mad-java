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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.service.gui.valueobjects.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.gui.valueobjects.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentImageCache;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.swing.table.layeredpane.LayeredPaneTableComponent;

public class GuiAudioComponentBack extends AbstractGuiAudioComponent implements LayeredPaneTableComponent
{
	private static final long serialVersionUID = -2752405956760137849L;

//	private static Log log = LogFactory.getLog( GuiAudioComponentBack.class.getName() );
	
	private final static int HORIZON_INSET = 20;
	private final static float ARC = 10;
	
	private final boolean SHOW_BOUNDING_BOX = false;
	
	private GuiJPanelBack backGuiComponent = null;
	
	public GuiAudioComponentBack( GuiComponentImageCache backImageCache, RackComponent inComponent )
	{
		super( inComponent );
		// Allow stuff underneath to show through
		this.setOpaque( false );
		this.setLayout( new MigLayout( "inset 1, gap 0, fill", "[][grow, fill][]", "fill") );
		
		this.add( new RackEdgeDragBarSpacer( ComponentSide.BACK) );
		backGuiComponent = new GuiJPanelBack( backImageCache, inComponent );
		this.add( backGuiComponent );
		this.add( new RackEdgeDragBarSpacer( ComponentSide.BACK) );
	}

	@Override
	public boolean isPointLocalDragRegion(Point localPoint)
	{
		if( !rackComponent.isDraggable() )
		{
			return false;
		}
		// Make sure it doesn't fall in the HORIZON_INSET region
		int x = localPoint.x;
		return ( x >= HORIZON_INSET && x < this.getWidth() - HORIZON_INSET );
	}
	
	@Override
	public void paint( Graphics g )
	{
		// Paint a rounded rectangle that shows our outline
		if( SHOW_BOUNDING_BOX )
		{
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor( ColorDefines.HIGHLIGHT_COLOR );
			Rectangle renderedRectangle = getRenderedRectangle();
			int x = renderedRectangle.x;
			int y = renderedRectangle.y;
			int width = renderedRectangle.width;
			int height = renderedRectangle.height;
			float arcWidth = ARC;
			float arcHeight = ARC;
			RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float( x, y, width, height, arcWidth, arcHeight );
			g2d.fill( roundedRectangle );
			g2d.setColor( ColorDefines.CONTENTS_COLOR );
			g2d.draw( roundedRectangle );
		}
		
		super.paint( g );
	}

	@Override
	public Rectangle getRenderedRectangle()
	{
		return new Rectangle( HORIZON_INSET , 0, this.getWidth() - (2*HORIZON_INSET), this.getHeight());
	}

	@Override
	public GuiChannelPlug getPlugFromPosition(Point localPoint)
	{
		// Transform the point to remove the HROIZON_INSET
		Point transformedPoint = new Point( localPoint );
		transformedPoint.translate( -HORIZON_INSET, 0 );
		return backGuiComponent.getPlugFromPosition( transformedPoint );
	}

	@Override
	public GuiChannelPlug getPlugFromMadChannelInstance( MadChannelInstance auChannelInstance )
	{
		return backGuiComponent.getPlugFromChannelInstance( auChannelInstance );
	}
	
	public String toString()
	{
		return rackComponent.toString();
	}

	@Override
	public void destroy()
	{
		backGuiComponent.destroy();
	}
}
