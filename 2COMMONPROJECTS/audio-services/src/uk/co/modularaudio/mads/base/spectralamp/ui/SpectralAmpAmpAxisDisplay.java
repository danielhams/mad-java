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
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class SpectralAmpAmpAxisDisplay extends JPanel
	implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>,
	AmpAxisChangeListener
{
	private static final long serialVersionUID = 1L;

//	private static Log log = LogFactory.getLog( SpectralAmpAmpAxisDisplay.class.getName() );

	private final static int AXIS_LABEL_LINE_WIDTH = 8;

	public static final int NUM_MARKERS = 5;

	private final SpectralAmpMadUiInstance uiInstance;

	private float currentMinValueDb = SpectralAmpAmpMinChoiceUiJComponent.DEFAULT_AMP_MIN.getDb();
	private float currentMaxValueDb = SpectralAmpAmpMaxChoiceUiJComponent.DEFAULT_AMP_MAX.getDb();

	private final FontMetrics fm;

	public SpectralAmpAmpAxisDisplay( final SpectralAmpMadDefinition definition,
			final SpectralAmpMadInstance instance,
			final SpectralAmpMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );

		fm = getFontMetrics( getFont() );

		uiInstance.addAmpAxisChangeListener( this );
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		final AmpScaleComputer ampScaleComputer = uiInstance.getDesiredAmpScaleComputer();

		final int width = getWidth();
		final int height = getHeight();

		// Clear
		g.setColor( SpectralAmpColours.BACKGROUND_COLOR  );
		g.fillRect( 0, 0, width, height );

		// Draw scale margin
		g.setColor( SpectralAmpColours.SCALE_AXIS_DETAIL );
		final int x = width - 1;
		final int topScaleY = SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET;
		final int bottomScaleY = height -
				SpectralAmpMadUiDefinition.FREQ_AXIS_COMPONENT_HEIGHT;
		g.drawLine( x, bottomScaleY, x, topScaleY );

		final int llStartX = width - 1 - AXIS_LABEL_LINE_WIDTH;
		final int llEndX = width - 1;

//		log.debug("Current max DB is " + currentMaxValueDb );
		final int numAxisPixelsToDivide = height - 1 -
				SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET -
				SpectralAmpMadUiDefinition.FREQ_AXIS_COMPONENT_HEIGHT;

		final float valueOfMax = AudioMath.dbToLevelF( currentMaxValueDb );
///		log.debug("Computed value with limit " + valueOfLimit );

		final float floatStepPerBlock = 1.0f / (NUM_MARKERS-1);

		for( int i = 0 ; i < NUM_MARKERS ; ++i )
		{
			final float normalisedValue = i * floatStepPerBlock;
//			log.debug("Doing normalised value at block " + i + " " + normalisedValue );
			final int regularY = (int)(normalisedValue * numAxisPixelsToDivide);
			// Pixels go downwards, of course, so reverse Y
			final int intraYOffset = (height - 1) - regularY -
					SpectralAmpMadUiDefinition.FREQ_AXIS_COMPONENT_HEIGHT;

			g.drawLine( llStartX, intraYOffset, llEndX, intraYOffset );

			// Work out what the label should be here
			final float result = ampScaleComputer.mappedBucketToRaw( numAxisPixelsToDivide + 1,
					valueOfMax,
					regularY );

			final float asDb = AudioMath.levelToDbF( result );
//			log.debug("Translating to db this is " + asDb );

			paintScaleTextDb( g, width, asDb, intraYOffset );
		}
	}

	private final void paintScaleTextDb( final Graphics g,
			final int width,
			final float scaleFloat,
			final int yOffset )
	{
		final int fontHeight = fm.getAscent();
		final int fontHeightOver2 = fontHeight / 2;
		final String scaleString = ( scaleFloat == Float.NEGATIVE_INFINITY
				?
				"-Inf"
				:
				MathFormatter.slowFloatPrint( scaleFloat, 1, false )
			);
		final char[] bscs = scaleString.toCharArray();
		final int charsWidth = fm.charsWidth( bscs, 0, bscs.length );
		final int charsEndX = width - AXIS_LABEL_LINE_WIDTH - 2;
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
	public void receiveAmpMaxDbChange( final float newMaxDB )
	{
		currentMaxValueDb = newMaxDB;
		repaint();
	}

	@Override
	public void receiveAmpMinDbChange( final float newMinDB )
	{
		currentMinValueDb = newMinDB;
		repaint();
	}

	@Override
	public void receiveAmpScaleComputer( final AmpScaleComputer desiredAmpScaleComputer )
	{
		repaint();
	}
}
