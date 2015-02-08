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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentImageCache;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.table.layeredpane.LayeredPaneTableComponent;

public class ComponentWithSurroundBack extends AbstractGuiAudioComponent implements LayeredPaneTableComponent
{
	private static final long serialVersionUID = -2752405956760137849L;

//	private static Log log = LogFactory.getLog( GuiAudioComponentBack.class.getName() );

	private static final boolean SHOW_BOUNDING_BOX = true;

	private final ComponentBack backGuiComponent;

	public ComponentWithSurroundBack( final GuiComponentImageCache backImageCache, final RackComponent inComponent )
	{
		super( inComponent );
		// Allow stuff underneath to show through
		this.setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "inset 1" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "debug" );

		msh.addColumnConstraint( "[][grow, fill][]" );
		msh.addRowConstraint( "fill" );

		this.setLayout( msh.createMigLayout() );

		this.add( new RackEdgeDragBarSpacer( ComponentSide.BACK) );
		backGuiComponent = new ComponentBack( backImageCache, inComponent );
		this.add( backGuiComponent );
		this.add( new RackEdgeDragBarSpacer( ComponentSide.BACK) );
	}

	@Override
	public boolean isPointLocalDragRegion(final Point localPoint)
	{
		if( !rackComponent.isDraggable() )
		{
			return false;
		}
		// Make sure it doesn't fall in the HORIZON_INSET region
		final int x = localPoint.x;
		return ( x >= PaintedComponentDefines.DRAG_BAR_WIDTH && x < this.getWidth() - PaintedComponentDefines.DRAG_BAR_WIDTH );
	}

	@Override
	public void paint( final Graphics g )
	{
		// Paint a rounded rectangle that shows our outline
		if( SHOW_BOUNDING_BOX )
		{
			final Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor( PaintedComponentDefines.HIGHLIGHT_COLOR );
			final Rectangle renderedRectangle = getRenderedRectangle();
			final int x = renderedRectangle.x;
			final int y = renderedRectangle.y;
			final int width = renderedRectangle.width;
			final int height = renderedRectangle.height;
			final float arcWidth = PaintedComponentDefines.ARC;
			final float arcHeight = PaintedComponentDefines.ARC;
			final RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float( x, y, width, height, arcWidth, arcHeight );
			g2d.fill( roundedRectangle );
			g2d.setColor( PaintedComponentDefines.CONTENTS_COLOR );
			g2d.draw( roundedRectangle );
		}

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
		return new Rectangle( PaintedComponentDefines.DRAG_BAR_WIDTH , 0, this.getWidth() - (2*PaintedComponentDefines.DRAG_BAR_WIDTH), this.getHeight());
	}

	@Override
	public GuiChannelPlug getPlugFromPosition(final Point localPoint)
	{
		// Transform the point to remove the HROIZON_INSET
		final Point transformedPoint = new Point( localPoint );
		transformedPoint.translate( -PaintedComponentDefines.DRAG_BAR_WIDTH, 0 );
		return backGuiComponent.getPlugFromPosition( transformedPoint );
	}

	@Override
	public GuiChannelPlug getPlugFromMadChannelInstance( final MadChannelInstance auChannelInstance )
	{
		return backGuiComponent.getPlugFromChannelInstance( auChannelInstance );
	}

	@Override
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
