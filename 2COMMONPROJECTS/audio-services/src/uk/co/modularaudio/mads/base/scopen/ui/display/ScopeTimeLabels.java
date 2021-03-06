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
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNCaptureLengthListener;
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNColours;
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNDisplayUiJComponent;
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNMadUiInstance;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LogarithmicTimeMillis1To1000SliderModel;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class ScopeTimeLabels<D extends ScopeNMadDefinition<D, I>,
	I extends ScopeNMadInstance<D, I>,
	U extends ScopeNMadUiInstance<D, I>>
	extends JPanel
	implements ScopeNCaptureLengthListener
{
	private static final long serialVersionUID = -129516269411473973L;

	private static final int LABEL_Y_OFFSET = 10;

	private final FontMetrics fm;
	private final int numTimeMarkers;

	private int realWidth;
	private int realHeight;
	private int width;

	private int horizPixelsPerMarker;

	private float captureTimeMillis = LogarithmicTimeMillis1To1000SliderModel.DEFAULT_MILLIS;

	public ScopeTimeLabels( final U uiInstance,
			final int numTimeMarkers )
	{
		this.setBackground( ScopeNColours.BACKGROUND_COLOR );

		this.numTimeMarkers = numTimeMarkers;

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );
		setMinimumSize( new Dimension( ScopeNDisplayUiJComponent.TIME_LABELS_HEIGHT, ScopeNDisplayUiJComponent.TIME_LABELS_HEIGHT ) );

		fm = getFontMetrics( LWTCControlConstants.LABEL_SMALL_FONT );

		uiInstance.addCaptureLengthListener( this );
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.realWidth = width;
		this.realHeight = height;
		this.width = width - ScopeNDisplayUiJComponent.AMP_DISPLAY_RIGHT_PADDING -
				ScopeNDisplayUiJComponent.AXIS_MARKS_LENGTH -
				ScopeNDisplayUiJComponent.AMP_LABELS_WIDTH;

		horizPixelsPerMarker = ScopeNDisplayUiJComponent.getAdjustedWidthBetweenMarkers( this.width, numTimeMarkers );
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

		g2d.setColor( ScopeNColours.SCOPE_AXIS_DETAIL );

		final float diffPerMarker = captureTimeMillis / (numTimeMarkers-1);
		float curValue = 0.0f;

		for( int i = 0 ; i < numTimeMarkers ; ++i )
		{
			final int x = (horizPixelsPerMarker * i) + ScopeNDisplayUiJComponent.AXIS_MARKS_LENGTH;

			paintScaleText( g2d, LABEL_Y_OFFSET, curValue, ScopeNDisplayUiJComponent.AMP_LABELS_WIDTH + x );
			curValue += diffPerMarker;
		}
	}

	private final void paintScaleText( final Graphics2D g2d,
			final int height,
			final float scaleFloat,
			final int xOffset )
	{
		final String scaleString = MathFormatter.fastFloatPrint( scaleFloat, 1, false );
		final char[] bscs = scaleString.toCharArray();
		final int charsWidth = fm.charsWidth( bscs, 0, bscs.length );
		final int charsWidthOver2 = charsWidth / 2;

		g2d.drawChars( bscs, 0, bscs.length, xOffset - charsWidthOver2, height );
	}

	@Override
	public void receiveCaptureLengthMillis( final float captureMillis )
	{
		this.captureTimeMillis = captureMillis;
		repaint();
	}

	@Override
	public void receiveCaptureLengthSamples( final int captureSamples )
	{
		// Don't care
	}
}
