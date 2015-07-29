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

package uk.co.modularaudio.mads.base.specampgen.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class SpectralPeakFreqLabels extends JPanel implements FreqAxisChangeListener
{
	private static final long serialVersionUID = 8208419176860684686L;

//	private static Log log = LogFactory.getLog( SpectralPeakFreqLabels.class.getName() );

	private FrequencyScaleComputer freqScaleComputer;

	private final FontMetrics fm;

	private int realWidth;
	private int realHeight;
	private int width;
	private int magsWidth;
	private int xOffset;
	private int horizPixelsPerMarker;
	private final int numFreqMarkers;

	public SpectralPeakFreqLabels( final SpectralAmpGenMadUiInstance<?,?> uiInstance, final int numFreqMarkers )
	{
		this.setBackground( SpectralAmpColours.BACKGROUND_COLOR );

		this.freqScaleComputer = uiInstance.getDesiredFreqScaleComputer();
		this.numFreqMarkers = numFreqMarkers;

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );
		setMinimumSize( new Dimension( SpectralAmpGenDisplayUiJComponent.FREQ_LABELS_HEIGHT, SpectralAmpGenDisplayUiJComponent.FREQ_LABELS_HEIGHT ) );

		fm = getFontMetrics( getFont() );

		uiInstance.addFreqAxisChangeListener( this );
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.realWidth = width;
		this.realHeight = height;
		this.width = width - SpectralAmpGenDisplayUiJComponent.SPECTRAL_DISPLAY_RIGHT_PADDING -
				SpectralAmpGenDisplayUiJComponent.AXIS_MARKS_LENGTH -
				SpectralAmpGenDisplayUiJComponent.AMP_LABELS_WIDTH;

		magsWidth = SpectralAmpGenDisplayUiJComponent.getAdjustedWidthOfDisplay( this.width, numFreqMarkers );

		xOffset = SpectralAmpGenDisplayUiJComponent.AMP_LABELS_WIDTH +
				SpectralAmpGenDisplayUiJComponent.AXIS_MARKS_LENGTH;

		horizPixelsPerMarker = SpectralAmpGenDisplayUiJComponent.getAdjustedWidthBetweenMarkers( this.width, numFreqMarkers );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalDistances( width, height );
	}

	@Override
	public void receiveFreqScaleChange( final FrequencyScaleComputer freqScaleComputer )
	{
		this.freqScaleComputer = freqScaleComputer;
		repaint();
	}

	@Override
	public void receiveFftSizeChange( final int desiredFftSize )
	{
		// Do nothing, we don't change based on fft size changes.
	}

	@Override
	public void paint( final Graphics g )
	{
		g.setFont( LWTCControlConstants.LABEL_SMALL_FONT );

		g.setColor( SpectralAmpColours.BACKGROUND_COLOR );
		g.fillRect( 0, 0, realWidth, realHeight );

		g.translate( xOffset, 0 );
		g.setColor( SpectralAmpColours.SCALE_AXIS_DETAIL );

		for( int i = 0 ; i < numFreqMarkers ; ++i )
		{
			final int x = horizPixelsPerMarker * i;

			final float result = freqScaleComputer.mappedBucketToRawMinMax( magsWidth + 1, x );
			paintScaleText( g, result, x );
		}

		g.translate( -xOffset, 0 );
	}

	private final void paintScaleText( final Graphics g,
			final float displayFloat,
			final int xOffset )
	{
		final int fontHeight = fm.getAscent();
		final String displayString = MathFormatter.fastFloatPrint( displayFloat, 0, false );

		final char[] bscs = displayString.toCharArray();
		final int charsWidth = fm.charsWidth( bscs, 0, bscs.length );

		g.drawChars( bscs, 0, bscs.length, xOffset - (charsWidth / 2), fontHeight );
	}
}
