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

package uk.co.modularaudio.mads.base.scopen.ui.display;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scopen.mu.ScopeNMadDefinition;
import uk.co.modularaudio.mads.base.scopen.mu.ScopeNMadInstance;
import uk.co.modularaudio.mads.base.scopen.ui.DisplayPoles;
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNColours;
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNDisplayUiJComponent;
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNMadUiInstance;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class ScopeAmpLabels<D extends ScopeNMadDefinition<D, I>,
	I extends ScopeNMadInstance<D, I>,
	U extends ScopeNMadUiInstance<D, I>>
	extends JPanel
{
	private static final long serialVersionUID = 6218233547049390740L;

	private final FontMetrics fm;
	private final int numAmpMarkers;
	private final int numDecimalPlaces;

	private int realWidth;
	private int realHeight;
	private int width;
	private int height;

	private int magsHeight;
	private int yOffset;
	private int vertPixelsPerMarker;

	private DisplayPoles displayPoles = ScopeNDisplayUiJComponent.DEFAULT_DISPLAY_POLES;

	public ScopeAmpLabels( final U uiInstance,
			final int numAmpMarkers,
			final int numDecimalPlaces )
	{
		this.setBackground( ScopeNColours.BACKGROUND_COLOR );

		this.numAmpMarkers = numAmpMarkers;
		this.numDecimalPlaces = numDecimalPlaces;

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );
		setMinimumSize( new Dimension( ScopeNDisplayUiJComponent.AMP_LABELS_WIDTH, ScopeNDisplayUiJComponent.AMP_LABELS_WIDTH  ) );

		fm = getFontMetrics( getFont() );
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.realWidth = width;
		this.realHeight = height;
		this.width = width - 1;
		this.height = height - 1 - ScopeNDisplayUiJComponent.AMP_DISPLAY_TOP_PADDING -
				ScopeNDisplayUiJComponent.AXIS_MARKS_LENGTH;

		magsHeight = ScopeNDisplayUiJComponent.getAdjustedHeightOfDisplay( this.height, numAmpMarkers );

		yOffset = ScopeNDisplayUiJComponent.AMP_DISPLAY_TOP_PADDING + (this.height - magsHeight);

		vertPixelsPerMarker = ScopeNDisplayUiJComponent.getAdjustedHeightBetweenMarkers( this.height, numAmpMarkers );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalDistances( width, height );
	}

	@Override
	public void paint( final Graphics g )
	{
		final Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		g2d.setFont( LWTCControlConstants.LABEL_SMALL_FONT );

		g2d.setColor( ScopeNColours.BACKGROUND_COLOR );
		g2d.fillRect( 0, 0, realWidth, realHeight );

		g2d.translate( 0, yOffset );
		g2d.setColor( ScopeNColours.SCOPE_AXIS_DETAIL );

		final float diffPerMarker = (displayPoles == DisplayPoles.BIPOLE ?
				2.0f / (numAmpMarkers-1)
				:
				1.0f / (numAmpMarkers-1) );
		float curValue = (displayPoles == DisplayPoles.BIPOLE ?
				-1.0f
				:
				0.0f );

		for( int i = 0 ; i < numAmpMarkers ; ++i )
		{
			final int y = magsHeight - (vertPixelsPerMarker * i);

			paintScaleText( g2d, width, curValue, y );
			curValue += diffPerMarker;
		}

		g2d.translate( 0, -yOffset );
	}

	private final void paintScaleText( final Graphics2D g2d,
			final int width,
			final float scaleFloat,
			final int yOffset )
	{
		final int fontHeight = fm.getAscent();
		final int fontHeightOver2 = fontHeight / 2;
		final String scaleString = MathFormatter.fastFloatPrint( scaleFloat, numDecimalPlaces, false );
		final char[] bscs = scaleString.toCharArray();
		final int charsWidth = fm.charsWidth( bscs, 0, bscs.length );
		final int charsEndX = width - 2;
		g2d.drawChars( bscs, 0, bscs.length, charsEndX - charsWidth, yOffset + fontHeightOver2 );
	}

	public void setDisplayPoles( final DisplayPoles displayPoles )
	{
		this.displayPoles = displayPoles;
		repaint();
	}
}
