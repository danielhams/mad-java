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

package uk.co.modularaudio.componentdesigner.profiling;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ProfilingPanel extends JPanel
{
	private static final long serialVersionUID = 6597946142348036824L;

//	private static Log log = LogFactory.getLog( ProfilingPanel.class.getName() );

	private final ProfilingLabel totalDurationDisplay;
	private final ProfilingLabel loopDurationDisplay;
	private final ProfilingLabel jobDurationDisplay;
	private final ProfilingLabel overheadDurationDisplay;
	private final ProfilingLabel overheadPercentDisplay;
	private final ProfilingLabel numRenderingThreadsDisplay;
	private final ProfilingLabel numUsedThreadsDisplay;

	public ProfilingPanel()
	{
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		this.setLayout( msh.createMigLayout() );

		this.setOpaque( true );

		this.setBackground( Color.ORANGE );

		final ProfilingLabel totalDurationLabel = new ProfilingLabel( "Total Duration:" );
		this.add( totalDurationLabel, "cell 0 1, align right");
		totalDurationDisplay = new ProfilingLabel();
		final Dimension preferredSize = totalDurationDisplay.getPreferredSize();
		final Dimension minDisplaySize = new Dimension( 150, preferredSize.height );
		totalDurationDisplay.setMinimumSize( minDisplaySize );
		this.add( totalDurationDisplay, "cell 1 1, grow 0");

		final ProfilingLabel loopDurationLabel = new ProfilingLabel( "Loop Duration:" );
		this.add( loopDurationLabel, "cell 0 2, align right");
		loopDurationDisplay = new ProfilingLabel();
		loopDurationDisplay.setMinimumSize( minDisplaySize );
		this.add( loopDurationDisplay, "cell 1 2, grow 0");

		final ProfilingLabel jobDurationLabel = new ProfilingLabel( "DSP Job Duration:" );
		this.add( jobDurationLabel, "cell 0 3, align right");
		jobDurationDisplay = new ProfilingLabel();
		jobDurationDisplay.setMinimumSize( minDisplaySize );
		this.add( jobDurationDisplay, "cell 1 3, grow 0");

		final ProfilingLabel overheadDurationLabel = new ProfilingLabel( "Overhead Duration:" );
		this.add( overheadDurationLabel, "cell 0 4, align right");
		overheadDurationDisplay = new ProfilingLabel();
		overheadDurationDisplay.setMinimumSize( minDisplaySize );
		this.add( overheadDurationDisplay, "cell 1 4, grow 0");

		final ProfilingLabel overheadPercentageLabel = new ProfilingLabel( "Overhead Percentage:" );
		this.add( overheadPercentageLabel, "cell 0 5, align right");
		overheadPercentDisplay = new ProfilingLabel();
		overheadPercentDisplay.setMinimumSize( minDisplaySize );
		this.add( overheadPercentDisplay, "cell 1 5, grow 0");

		final ProfilingLabel numRenderingThreadsLabel = new ProfilingLabel( "Number Of Rendering Threads:" );
		this.add( numRenderingThreadsLabel, "cell 0 6, align right");
		numRenderingThreadsDisplay = new ProfilingLabel();
		numRenderingThreadsDisplay.setMinimumSize( minDisplaySize );
		this.add( numRenderingThreadsDisplay, "cell 1 6, grow 0");

		final ProfilingLabel numUsedThreadsLabel = new ProfilingLabel( "Number Of Used Threads:" );
		this.add( numUsedThreadsLabel, "cell 0 7, align right");
		numUsedThreadsDisplay = new ProfilingLabel();
		numUsedThreadsDisplay.setMinimumSize( minDisplaySize );
		this.add( numUsedThreadsDisplay, "cell 1 7, grow 0");
	}

	public void setData( final long totalDuration,
			final long loopDuration,
			final long dspJobDuration,
			final long overheadDuration,
			final int numRenderingThreads,
			final int numUniqueThreads )
	{
		totalDurationDisplay.setText( TimestampFormatter.formatNanos( totalDuration ) );
		loopDurationDisplay.setText( TimestampFormatter.formatNanos( loopDuration ) );
		jobDurationDisplay.setText( TimestampFormatter.formatNanos( dspJobDuration ) );
		overheadDurationDisplay.setText( TimestampFormatter.formatNanos( overheadDuration ) );
		final float percentage = (overheadDuration / (float)totalDuration) * 100.0f;
		overheadPercentDisplay.setText( MathFormatter.fastFloatPrint( percentage, 2, false ) );
		numRenderingThreadsDisplay.setText( Integer.toString(numRenderingThreads) );
		numUsedThreadsDisplay.setText( Integer.toString( numUniqueThreads ) );
	}
}
