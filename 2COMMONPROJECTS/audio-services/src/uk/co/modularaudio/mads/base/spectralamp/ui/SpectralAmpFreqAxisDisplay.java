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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.LogarithmicFreqScaleComputer;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class SpectralAmpFreqAxisDisplay extends JPanel
	implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>,
	FreqAxisChangeListener
{
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog( SpectralAmpFreqAxisDisplay.class.getName() );

	private final static int LL_WIDTH = 8;

	private final float currentMaxValueDb = 0.0f;

	private final FontMetrics fm;

	// Default is logarithmic
	private FrequencyScaleComputer currentFreqScaleComputer = new LogarithmicFreqScaleComputer();

	public SpectralAmpFreqAxisDisplay( final SpectralAmpMadDefinition definition,
			final SpectralAmpMadInstance instance,
			final SpectralAmpMadUiInstance uiInstance,
			final int controlIndex )
	{
		setFont( LWTCControlConstants.LABEL_SMALL_FONT );

		fm = getFontMetrics( getFont() );

		uiInstance.addFreqScaleChangeListener( this );
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
		final int x = width - 1;
		final int bottomScaleY = SpectralAmpMadUiDefinition.SCALES_OFFSET;
		final int topScaleY = height - SpectralAmpMadUiDefinition.SCALES_OFFSET - 1;
		g.drawLine( x, bottomScaleY, x, topScaleY );

		// Draw three little lines we'll mark against
		final int midY = height / 2;
		final int llStartX = width - 1 - LL_WIDTH;
		final int llEndX = width - 1;
		g.drawLine( llStartX, bottomScaleY, llEndX, bottomScaleY );

		final int midBottomY = (bottomScaleY + midY) / 2;
		g.drawLine( llStartX, midBottomY, llEndX, midBottomY );

		g.drawLine( llStartX, midY, llEndX, midY );

		final int topMidY = (topScaleY + midY) / 2;

		g.drawLine( llStartX, topMidY, llEndX, topMidY );

		g.drawLine( llStartX, topScaleY, llEndX, topScaleY );

		// Draw the scale bits
		final float currentMaxAsAbs = AudioMath.dbToLevelF( currentMaxValueDb );
		final float halfwayDb = AudioMath.levelToDbF( currentMaxAsAbs / 2.0f );

		paintScaleText( g, width, currentMaxValueDb, bottomScaleY );

		paintScaleText( g, width, halfwayDb, midBottomY );

		paintScaleText( g, width, Float.NEGATIVE_INFINITY, midY );

		paintScaleText( g, width, halfwayDb, topMidY );

		paintScaleText( g, width, currentMaxValueDb, topScaleY );
	}

	private final void paintScaleText( final Graphics g,
			final int width,
			final float scaleFloat,
			final int yOffset )
	{
		final int fontHeight = fm.getAscent();
		final int fontHeightOver2 = fontHeight / 2;
		final String scaleString = ( scaleFloat == Float.NEGATIVE_INFINITY
				?
				"-Inf dB"
				:
				MathFormatter.fastFloatPrint( scaleFloat, 0, false ) + " dB"
			);
		final char[] bscs = scaleString.toCharArray();
		final int charsWidth = fm.charsWidth( bscs, 0, bscs.length );
		final int charsEndX = width - LL_WIDTH - 2;
		g.drawChars( bscs, 0, bscs.length, charsEndX - charsWidth, yOffset + fontHeightOver2 );
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
	public void receiveFreqScaleComputer( final FrequencyScaleComputer desiredFreqScaleComputer )
	{
		log.debug("Received new freq scale computer: " + desiredFreqScaleComputer.toString());
		currentFreqScaleComputer = desiredFreqScaleComputer;
		repaint();
	}
}
