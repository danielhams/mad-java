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

package uk.co.modularaudio.mads.base.scopegen.ui.display;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scopegen.ui.ScopeGenColours;
import uk.co.modularaudio.mads.base.scopegen.ui.ScopeGenDisplayUiJComponent;

public class ScopeAmpMarks extends JPanel
{
	private static final long serialVersionUID = 4239433019054754730L;

//	private static Log log = LogFactory.getLog( ScopeAmpMarks.class.getName() );

	private int width;
	private int height;
	private int yOffset;
	private int vertPixelsPerMarker;
	private final int numAmpMarkers;

	public ScopeAmpMarks( final int numAmpMarkers )
	{
		this.numAmpMarkers = numAmpMarkers;
		setMinimumSize( new Dimension( ScopeGenDisplayUiJComponent.AXIS_MARKS_LENGTH, ScopeGenDisplayUiJComponent.AXIS_MARKS_LENGTH ) );
	}

	@Override
	public void paint( final Graphics g )
	{
		g.translate( 0, yOffset );
		g.setColor( ScopeGenColours.SCOPE_AXIS_DETAIL );

		for( int i = 0 ; i < numAmpMarkers ; ++i )
		{
			final int y = vertPixelsPerMarker * i;
			g.drawLine( 0, y, width, y );
		}

		g.translate( 0, -yOffset );
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.width = width - 1;
		this.height = height - 1;

		final int magsHeight = ScopeGenDisplayUiJComponent.getAdjustedHeightOfDisplay( this.height, numAmpMarkers );

		yOffset = this.height - magsHeight;

		vertPixelsPerMarker = ScopeGenDisplayUiJComponent.getAdjustedHeightBetweenMarkers( this.height, numAmpMarkers );

	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalDistances( width, height );
	}
}
