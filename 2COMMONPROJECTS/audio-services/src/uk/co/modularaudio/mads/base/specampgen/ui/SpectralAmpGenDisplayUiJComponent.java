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

import java.awt.Component;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.specampgen.mu.SpectralAmpGenMadDefinition;
import uk.co.modularaudio.mads.base.specampgen.mu.SpectralAmpGenMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class SpectralAmpGenDisplayUiJComponent<D extends SpectralAmpGenMadDefinition<D, I>,
	I extends SpectralAmpGenMadInstance<D, I>,
	U extends SpectralAmpGenMadUiInstance<D, I>>
	extends JPanel
	implements IMadUiControlInstance<D, I, U>
{
	private static final long serialVersionUID = -734000347965308505L;

	private final SpectralPeakAmpLabels ampScaleLabels;
	private final SpectralPeakEmptyPlot topEmptyPlot;
	private final SpectralPeakAmpMarks ampAxisMarks;
	private final SpectralPeakGraph spectralDispaly;
	private final SpectralPeakEmptyPlot rightEmptyPlot;
	private final SpectralPeakFreqMarks freqAxisMarks;
	private final SpectralPeakFreqLabels freqScaleLabels;

	public static final int AXIS_MARKS_LENGTH = 8;
	public static final int AMP_LABELS_WIDTH = 36;
	public static final int FREQ_LABELS_HEIGHT = 16;
	public static final int SPECTRAL_DISPLAY_RIGHT_PADDING = 16;
	public static final int SPECTRAL_DISPLAY_TOP_PADDING = 8;

	public SpectralAmpGenDisplayUiJComponent( final D definition,
			final I instance,
			final U uiInstance,
			final int controlIndex,
			final int numAmpMarkers,
			final int numFreqMarkers )
	{
		setOpaque( true );
		setBackground( SpectralAmpColours.BACKGROUND_COLOR );
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		msh.addColumnConstraint(
				"[" + AMP_LABELS_WIDTH + "px]" +
				"[" + AXIS_MARKS_LENGTH + "px]" +
				"[]" +
				"[" + SPECTRAL_DISPLAY_RIGHT_PADDING + "px]" );

		msh.addRowConstraint(
				"[" + SPECTRAL_DISPLAY_TOP_PADDING + "px]" +
				"[]" +
				"[" + AXIS_MARKS_LENGTH + "px]" +
				"[" + FREQ_LABELS_HEIGHT + "px]" );

		setLayout( msh.createMigLayout() );

		ampScaleLabels = new SpectralPeakAmpLabels( uiInstance, numAmpMarkers );
		topEmptyPlot = new SpectralPeakEmptyPlot();
		ampAxisMarks = new SpectralPeakAmpMarks( numAmpMarkers );
		spectralDispaly = new SpectralPeakGraph( uiInstance, numAmpMarkers, numFreqMarkers );
		rightEmptyPlot = new SpectralPeakEmptyPlot();
		freqScaleLabels = new SpectralPeakFreqLabels( uiInstance, numFreqMarkers );
		freqAxisMarks = new SpectralPeakFreqMarks( numFreqMarkers );

		// Sizing set using row/column constraints
		this.add( ampScaleLabels, "cell 0 0, spany 3, growy" );
		this.add( topEmptyPlot, "cell 1 0, spanx 3, growx" );
		this.add( ampAxisMarks, "cell 1 1, grow" );
		this.add( spectralDispaly, "cell 2 1, grow, push" );
		this.add( rightEmptyPlot, "cell 3 1, grow" );
		this.add( freqAxisMarks, "cell 1 2, spanx 3, growx" );
		this.add( freqScaleLabels, "cell 0 3, spanx 4, growx" );
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public String getControlValue()
	{
		return "";
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
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		spectralDispaly.doDisplayProcessing( tempEventStorage, timingParameters, currentGuiTime );
	}

	@Override
	public void receiveControlValue( final String value )
	{
	}

	static public int getAdjustedWidthOfDisplay( final int actualWidth, final int numFreqMarkers )
	{
		return getAdjustedWidthBetweenMarkers( actualWidth, numFreqMarkers ) * (numFreqMarkers-1);
	}

	static public int getAdjustedWidthBetweenMarkers( final int actualWidth, final int numFreqMarkers )
	{
		return (int)Math.floor(actualWidth / (numFreqMarkers-1));
	}

	static public int getAdjustedHeightOfDisplay( final int actualHeight, final int numAmpMarkers )
	{
		return getAdjustedHeightBetweenMarkers( actualHeight, numAmpMarkers ) * (numAmpMarkers-1);
	}

	static public int getAdjustedHeightBetweenMarkers( final int actualHeight, final int numAmpMarkers )
	{
		return (int)Math.floor(actualHeight / (numAmpMarkers-1));
	}
}
