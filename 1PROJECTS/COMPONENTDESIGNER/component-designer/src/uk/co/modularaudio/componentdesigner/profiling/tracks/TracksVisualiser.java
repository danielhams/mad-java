package uk.co.modularaudio.componentdesigner.profiling.tracks;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import uk.co.modularaudio.componentdesigner.profiling.TimestampFormatter;
import uk.co.modularaudio.service.apprendering.util.structure.ParsedJobData;

public class TracksVisualiser extends JPanel
{
	private static final long serialVersionUID = 8118299559065004280L;

//	private static Log log = LogFactory.getLog( TracksVisualiser.class.getName() );

	private static final Color[] threadColors = new Color[] {
		Color.decode( "#ffbbbb" ),
		Color.decode( "#bbffbb" ),
		Color.decode( "#bbbbff" ),
		Color.pink,
		Color.cyan,
		Color.magenta,
		Color.yellow,
		Color.lightGray
	};

	private static final int BLOCK_WIDTH = 300;

	private final List<TrackBlock> trackBlocks = new ArrayList<TrackBlock>();

	private final Dimension requiredSize = new Dimension( 150, 30 );

	public TracksVisualiser()
	{
		setLayout( null );
	}

	public void setTrackData( final int numRenderingThreads,
			final int numUniqueThreads,
			final long totalDuration,
			final ParsedJobData shortestJob,
			final List<ParsedJobData> profiledJobs )
	{
		// Remove any existing blocks
		removeBlocks();

		// Work out shortest block so we can put labels on them
		final int requiredWidth = BLOCK_WIDTH * numRenderingThreads;

		final long shortestJobLength = shortestJob.getJobLength();
//		final int shortestJobHeight = getFontMetrics( getFont() ).getHeight();
		final int shortestJobHeight = 4;

		final float requiredHeightFloat = (totalDuration / (float)shortestJobLength) * shortestJobHeight;
		final int requiredHeight = (int)requiredHeightFloat;

		requiredSize.setSize( requiredWidth, requiredHeight );

		setPreferredSize( requiredSize );
//		setMinimumSize( requiredSize );
//		log.debug( "Set min + preferred size to " + requiredSize.toString() );

		for( final ParsedJobData pjd : profiledJobs )
		{
			final long jobOffset = pjd.getJobOffsetFromStart();
			final long jobLength = pjd.getJobLength();
			final int jobThread = pjd.getJobThreadNum();
			final String jobName = pjd.getJobName();
			final int offsetX = jobThread * BLOCK_WIDTH;
			final float offsetYFloat = (jobOffset / (float)shortestJobLength) * shortestJobHeight;
			final int offsetY = (int)offsetYFloat;
			final float blockHeightFloat = (jobLength / (float)shortestJobLength) * shortestJobHeight;
			final int blockHeight = (int)blockHeightFloat;

			final StringBuilder sb = new StringBuilder();
			sb.append( TimestampFormatter.formatNanos( jobOffset ) );
			sb.append( " - " );
			sb.append( jobName );
			sb.append( "\nLength(" );
			sb.append( TimestampFormatter.formatNanos( jobLength ) );
			sb.append( ")" );

			final StringBuilder ttb = new StringBuilder();
			ttb.append( jobName );
			ttb.append( " Length " );
			ttb.append( TimestampFormatter.formatNanos( jobLength ) );

			final TrackBlock tb = new TrackBlock( sb.toString(), threadColors[jobThread], ttb.toString() );
			this.add( tb );
			trackBlocks.add( tb );
			tb.setBounds( offsetX, offsetY, BLOCK_WIDTH, blockHeight );
		}
	}

	private void removeBlocks()
	{
		for( final TrackBlock tb : trackBlocks )
		{
			this.remove( tb );
		}
		trackBlocks.clear();
	}

}
