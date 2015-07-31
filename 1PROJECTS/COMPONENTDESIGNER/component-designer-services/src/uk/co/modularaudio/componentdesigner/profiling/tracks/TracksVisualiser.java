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

	private static final Color[] THREAD_COLOURS = new Color[] {
		Color.decode( "#ffbbbb" ),
		Color.decode( "#bbffbb" ),
		Color.decode( "#bbbbff" ),
		Color.pink,
		Color.cyan,
		Color.magenta,
		Color.yellow,
		Color.lightGray
	};

	public static final int TRACK_BLOCK_WIDTH = 300;

	// A microsecond per pixel
	private static final long NUM_NANOS_PER_PIXEL = 1000;
	private static final int MIN_BLOCK_HEIGHT = 4;

	private final List<TrackBlock> trackBlocks = new ArrayList<TrackBlock>();

	private final Dimension requiredSize = new Dimension( TRACK_BLOCK_WIDTH, 30 );

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
		final int requiredWidth = TRACK_BLOCK_WIDTH * numRenderingThreads;

		final float requiredHeightFloat = (totalDuration / (float)NUM_NANOS_PER_PIXEL);
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
			final int offsetX = jobThread * TRACK_BLOCK_WIDTH;
			final float offsetYFloat = (jobOffset / (float)NUM_NANOS_PER_PIXEL);
			final int offsetY = (int)offsetYFloat;
			final float blockHeightFloat = (jobLength / (float)NUM_NANOS_PER_PIXEL);
			int blockHeight = (int)blockHeightFloat;
			blockHeight = (blockHeight < MIN_BLOCK_HEIGHT ? MIN_BLOCK_HEIGHT : blockHeight );

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

			final TrackBlock tb = new TrackBlock( sb.toString(), THREAD_COLOURS[jobThread], ttb.toString() );
			this.add( tb );
			trackBlocks.add( tb );
			tb.setBounds( offsetX, offsetY, TRACK_BLOCK_WIDTH, blockHeight );
		}

		repaint();
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
