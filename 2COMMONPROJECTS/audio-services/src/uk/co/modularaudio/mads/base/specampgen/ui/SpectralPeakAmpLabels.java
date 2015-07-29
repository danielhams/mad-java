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

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class SpectralPeakAmpLabels extends JPanel implements AmpAxisChangeListener
{
	private static final long serialVersionUID = -4554672067965895575L;

//	private static Log log = LogFactory.getLog( SpectralPeakAmpLabels.class.getName() );

	private AmpScaleComputer ampScaleComputer;

	private final FontMetrics fm;

	private int realWidth;
	private int realHeight;
	private int width;
	private int height;
	private int magsHeight;
	private int yOffset;
	private int vertPixelsPerMarker;
	private final int numAmpMarkers;

	public SpectralPeakAmpLabels( final SpectralAmpGenMadUiInstance<?,?> uiInstance,
			final int numAmpMarkers )
	{
		this.setBackground( SpectralAmpColours.BACKGROUND_COLOR );

		this.ampScaleComputer = uiInstance.getDesiredAmpScaleComputer();
		this.numAmpMarkers = numAmpMarkers;

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );
		setMinimumSize( new Dimension( SpectralAmpGenDisplayUiJComponent.AMP_LABELS_WIDTH, SpectralAmpGenDisplayUiJComponent.AMP_LABELS_WIDTH  ) );

		fm = getFontMetrics( getFont() );

		uiInstance.addAmpAxisChangeListener( this );
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.realWidth = width;
		this.realHeight = height;
		this.width = width - 1;
		this.height = height - 1 - SpectralAmpGenDisplayUiJComponent.SPECTRAL_DISPLAY_TOP_PADDING -
				SpectralAmpGenDisplayUiJComponent.AXIS_MARKS_LENGTH;

		magsHeight = SpectralAmpGenDisplayUiJComponent.getAdjustedHeightOfDisplay( this.height, numAmpMarkers );

		yOffset = SpectralAmpGenDisplayUiJComponent.SPECTRAL_DISPLAY_TOP_PADDING + (this.height - magsHeight);

		vertPixelsPerMarker = SpectralAmpGenDisplayUiJComponent.getAdjustedHeightBetweenMarkers( this.height, numAmpMarkers );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalDistances( width, height );
	}

	@Override
	public void receiveAmpScaleChange( final AmpScaleComputer ampScaleComputer )
	{
		this.ampScaleComputer = ampScaleComputer;
		repaint();
	}

	@Override
	public void paint( final Graphics g )
	{
		g.setFont( LWTCControlConstants.LABEL_SMALL_FONT );

		g.setColor( SpectralAmpColours.BACKGROUND_COLOR );
		g.fillRect( 0, 0, realWidth, realHeight );

		g.translate( 0, yOffset );
		g.setColor( SpectralAmpColours.SCALE_AXIS_DETAIL );

		for( int i = 0 ; i < numAmpMarkers ; ++i )
		{
			final int y = vertPixelsPerMarker * i;

			final float result = ampScaleComputer.mappedBucketToRawMinMax( magsHeight + 1, magsHeight - y );
			final float asDb = AudioMath.levelToDbF( result );

			paintScaleTextDb( g, width, asDb, y );
		}

		g.translate( 0, -yOffset );
	}

	private final void paintScaleTextDb( final Graphics g,
			final int width,
			final float scaleFloat,
			final int yOffset )
	{
		final int fontHeight = fm.getAscent();
		final int fontHeightOver2 = fontHeight / 2;
		final String scaleString = MathFormatter.fastFloatPrint( scaleFloat, 1, false );
		final char[] bscs = scaleString.toCharArray();
		final int charsWidth = fm.charsWidth( bscs, 0, bscs.length );
		final int charsEndX = width - 2;
		g.drawChars( bscs, 0, bscs.length, charsEndX - charsWidth, yOffset + fontHeightOver2 );
	}
}
