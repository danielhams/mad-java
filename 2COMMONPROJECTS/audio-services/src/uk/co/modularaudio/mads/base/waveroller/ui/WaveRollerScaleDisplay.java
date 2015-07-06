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

package uk.co.modularaudio.mads.base.waveroller.ui;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadDefinition;
import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class WaveRollerScaleDisplay extends JPanel
	implements IMadUiControlInstance<WaveRollerMadDefinition, WaveRollerMadInstance, WaveRollerMadUiInstance>,
	ScaleLimitChangeListener
{
	private static final long serialVersionUID = 1L;

//	private static Log log = LogFactory.getLog( WaveRollerScaleDisplay.class.getName() );

	public final static int SCALE_MARGIN = 10;

	private final static int LL_WIDTH = 8;

	private final boolean isLeftDisplay;

	private float currentScaleLimitDb = 0.0f;

	private final FontMetrics fm;

	public WaveRollerScaleDisplay( final WaveRollerMadDefinition definition,
			final WaveRollerMadInstance instance,
			final WaveRollerMadUiInstance uiInstance,
			final int controlIndex )
	{
//		log.debug("Created scale display with index " + controlIndex );
		isLeftDisplay = ( controlIndex == 2 );

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );

		fm = getFontMetrics( getFont() );

		uiInstance.addScaleChangeListener( this );
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();

		// Clear
		g.setColor(  WaveRollerColours.BACKGROUND_COLOR  );
		g.fillRect( 0, 0, width, height );

		// Draw scale margin
		g.setColor( WaveRollerColours.SCALE_AXIS_DETAIL );
		final int x = ( isLeftDisplay ? width - 1 : 0 );
		final int bottomScaleY = SCALE_MARGIN;
		final int topScaleY = height - SCALE_MARGIN - 1;
		g.drawLine( x, bottomScaleY, x, topScaleY );

		// Draw three little lines we'll mark against
		final int midY = height / 2;
		final int llStartX = ( isLeftDisplay ? width - 1 - LL_WIDTH : 0 );
		final int llEndX = ( isLeftDisplay ? width - 1 : LL_WIDTH );
		g.drawLine( llStartX, bottomScaleY, llEndX, bottomScaleY );

		final int midBottomY = (bottomScaleY + midY) / 2;
		g.drawLine( llStartX, midBottomY, llEndX, midBottomY );

		g.drawLine( llStartX, midY, llEndX, midY );

		final int topMidY = (topScaleY + midY) / 2;

		g.drawLine( llStartX, topMidY, llEndX, topMidY );

		g.drawLine( llStartX, topScaleY, llEndX, topScaleY );

		// Draw the scale bits
		final float currentMaxAsAbs = AudioMath.dbToLevelF( currentScaleLimitDb );
		final float halfwayDb = AudioMath.levelToDbF( currentMaxAsAbs / 2.0f );

		paintScaleText( g, width, currentScaleLimitDb, bottomScaleY );

		paintScaleText( g, width, halfwayDb, midBottomY );

		paintScaleText( g, width, Float.NEGATIVE_INFINITY, midY );

		paintScaleText( g, width, halfwayDb, topMidY );

		paintScaleText( g, width, currentScaleLimitDb, topScaleY );
	}

	private final void paintScaleText( final Graphics g,
			final int width,
			final float scaleFloat,
			final int yOffset )
	{
		final int fontHeight = fm.getAscent();
		final int fontHeightOver2 = fontHeight / 2;
		final String scaleString = MathFormatter.fastFloatPrint( scaleFloat, 0, false ) + " dB";
		final char[] bscs = scaleString.toCharArray();
		final int charsWidth = fm.charsWidth( bscs, 0, bscs.length );
		final int charsEndX = ( isLeftDisplay ? width - LL_WIDTH - 2 : LL_WIDTH + 2 + charsWidth );
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
	public void receiveScaleLimitChange( final float newMaxDB )
	{
		currentScaleLimitDb = newMaxDB;
		repaint();
	}
}
