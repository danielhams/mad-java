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

package uk.co.modularaudio.mads.base.cvsurface.ui.controller;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.cvsurface.mu.CvSurfaceMadDefinition;
import uk.co.modularaudio.mads.base.cvsurface.mu.CvSurfaceMadInstance;
import uk.co.modularaudio.mads.base.cvsurface.ui.CvSurfaceMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class CvSurfaceControllerUiJComponent extends JPanel
	implements IMadUiControlInstance<CvSurfaceMadDefinition, CvSurfaceMadInstance, CvSurfaceMadUiInstance>
{
	private static final long serialVersionUID = -4122830597685469293L;

	private static final Color BACKGROUND_COLOR = Color.black;

	private static final Color LINE_COLOR = Color.red;
	private static final Color UNSELECTED_COLOR = Color.red.darker().darker();

	private Point previousSelectionPoint = new Point(0,0);
	private final CvSurfaceControllerMouseListener mouseListener;

	private BufferedImage bi;
	private Graphics2D g2d;

	public CvSurfaceControllerUiJComponent( final CvSurfaceMadDefinition definition,
			final CvSurfaceMadInstance instance,
			final CvSurfaceMadUiInstance uiInstance,
			final int controlIndex )
	{
		// Add a mouse motion  listener
		mouseListener = new CvSurfaceControllerMouseListener( uiInstance, this );
		this.addMouseListener( mouseListener );
		this.addMouseMotionListener( mouseListener );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		final Rectangle bounds = getBounds();
		final Point currentSelectionPoint = mouseListener.getCurrentSelectionPoint();

		boolean updateNeeded = false;
		boolean pointSelected = false;
		if( previousSelectionPoint != null && currentSelectionPoint == null )
		{
			// Nothing currently pointed to
			updateNeeded = true;
			pointSelected = false;
		}
		else if( previousSelectionPoint == null && currentSelectionPoint != null )
		{
			// Selection just started...
			updateNeeded = true;
			pointSelected = true;

		}
		else if( previousSelectionPoint == null && currentSelectionPoint == null )
		{
			// Do nothing
		}
		else if( !previousSelectionPoint.equals( currentSelectionPoint ) )
		{
			// selection point changed
			updateNeeded = true;
			pointSelected = true;
		}

		if( updateNeeded )
		{
			g2d.setColor( BACKGROUND_COLOR );
			g2d.fillRect( 0, 0, bounds.width, bounds.height );
			if( pointSelected )
			{
				// Draw two lines on the x and y passing through the point
				g2d.setColor( LINE_COLOR );
				g2d.drawLine( 0,  currentSelectionPoint.y, bounds.width, currentSelectionPoint.y );
				g2d.drawLine( currentSelectionPoint.x, 0, currentSelectionPoint.x, bounds.height );
			}
			else
			{
				final int middlex = bounds.width / 2;
				final int middley = bounds.height / 2;
				// Draw line in the middle
				g2d.setColor( UNSELECTED_COLOR );
				g2d.drawLine( 0,  middley, bounds.width, middley );
				g2d.drawLine( middlex, 0, middlex, bounds.height );
			}
			previousSelectionPoint = currentSelectionPoint;
			this.repaint();
		}
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void setBounds( final Rectangle r )
	{
		super.setBounds( r );
		bi = new BufferedImage( r.width, r.height, BufferedImage.TYPE_INT_RGB );
		g2d = bi.createGraphics();
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		if( bi != null )
		{
			g.drawImage( bi,  0, 0, null );
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public String getControlValue()
	{
		return "";
	}

	@Override
	public void receiveControlValue( final String value )
	{
	}
}
