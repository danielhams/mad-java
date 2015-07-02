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

package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class SpectralAmpFreqAxisDisplay extends JPanel
	implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>,
	FreqAxisChangeListener
{
	private static final long serialVersionUID = 1L;

//	private static Log log = LogFactory.getLog( SpectralAmpFreqAxisDisplay.class.getName() );

	private final static int AXIS_LABEL_LINE_HEIGHT = 8;

	public static final int NUM_MARKERS = 9;

	private final FontMetrics fm;

	private FrequencyScaleComputer freqScaleComputer;

	public SpectralAmpFreqAxisDisplay( final SpectralAmpMadDefinition definition,
			final SpectralAmpMadInstance instance,
			final SpectralAmpMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.freqScaleComputer = uiInstance.getDesiredFreqScaleComputer();

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );

		fm = getFontMetrics( getFont() );

		uiInstance.addFreqAxisChangeListener( this );
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();

		// Clear
		g.setColor( SpectralAmpColours.BACKGROUND_COLOR  );
		g.fillRect( 0, 0, width, height );

		// Draw scale margin
		g.setColor( SpectralAmpColours.SCALE_AXIS_DETAIL );
		final int y = 0;
		final int leftScaleX = 0;
		final int rightScaleX = width - SpectralAmpMadUiDefinition.SCALES_WIDTH_OFFSET - 1;
		g.drawLine( leftScaleX, y, rightScaleX, y );

		final int llStartY = 0;
		final int llEndY = AXIS_LABEL_LINE_HEIGHT;

		final int numAxisPixelsToDivide = width - 1 -
				SpectralAmpMadUiDefinition.SCALES_WIDTH_OFFSET;

		final float floatStepPerBlock = 1.0f / (NUM_MARKERS - 1);

		for( int i = 0 ; i < NUM_MARKERS ; ++i )
		{
			final float normalisedValue = i * floatStepPerBlock;

			final int regularX = (int)(normalisedValue * numAxisPixelsToDivide);

			g.drawLine( regularX, llStartY, regularX, llEndY );

			final float freq = freqScaleComputer.mappedBucketToRawMinMax( numAxisPixelsToDivide + 1,
					regularX );

			paintScaleText( g, freq, regularX );
		}

	}

	private final void paintScaleText( final Graphics g,
			final float displayFloat,
			final int xOffset )
	{
		final int fontHeight = fm.getAscent();
		final String displayString = MathFormatter.slowFloatPrint( displayFloat, 0, false );

		final char[] bscs = displayString.toCharArray();
		final int charsWidth = fm.charsWidth( bscs, 0, bscs.length );

		g.drawChars( bscs, 0, bscs.length, xOffset - (charsWidth / 2), AXIS_LABEL_LINE_HEIGHT + fontHeight );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
	{
	}

	@Override
	public Component getControl()
	{
		return this;
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

	@Override
	public void receiveFreqScaleChange( final FrequencyScaleComputer freqScaleComputer )
	{
		this.freqScaleComputer = freqScaleComputer;
		repaint();
	}

	@Override
	public void receiveFftSizeChange( final int desiredFftSize )
	{
		// Do nothing. We don't change scale if the fft size changes
	}
}
