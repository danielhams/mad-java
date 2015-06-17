package uk.co.modularaudio.componentdesigner.profiling;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ProfilingPanel extends JPanel
{
	private static final long serialVersionUID = 6597946142348036824L;

//	private static Log log = LogFactory.getLog( ProfilingPanel.class.getName() );

	private final ProfilingLabel totalDurationDisplay;
	private final JLabel loopDurationDisplay;
	private final JLabel numRenderingThreadsDisplay;
	private final JLabel numUsedThreadsDisplay;

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

		final JLabel totalDurationLabel = new ProfilingLabel( "Total Duration:" );
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

		final ProfilingLabel numRenderingThreadsLabel = new ProfilingLabel( "Number Of Rendering Threads:" );
		this.add( numRenderingThreadsLabel, "cell 0 3, align right");
		numRenderingThreadsDisplay = new ProfilingLabel();
		numRenderingThreadsDisplay.setMinimumSize( minDisplaySize );
		this.add( numRenderingThreadsDisplay, "cell 1 3, grow 0");

		final ProfilingLabel numUsedThreadsLabel = new ProfilingLabel( "Number Of Used Threads:" );
		this.add( numUsedThreadsLabel, "cell 0 4, align right");
		numUsedThreadsDisplay = new ProfilingLabel();
		numUsedThreadsDisplay.setMinimumSize( minDisplaySize );
		this.add( numUsedThreadsDisplay, "cell 1 4, grow 0");
	}

	public void setData( final long totalDuration,
			final long loopDuration,
			final int numRenderingThreads,
			final int numUniqueThreads )
	{
		totalDurationDisplay.setText( TimestampFormatter.formatNanos( totalDuration ) );
		loopDurationDisplay.setText( TimestampFormatter.formatNanos( loopDuration ) );
		numRenderingThreadsDisplay.setText( Integer.toString(numRenderingThreads) );
		numUsedThreadsDisplay.setText( Integer.toString( numUniqueThreads ) );
	}
}
