package uk.co.modularaudio.componentdesigner.profiling;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.profiling.tracks.TracksVisualiser;
import uk.co.modularaudio.service.apprendering.util.structure.JobDataListComparator;
import uk.co.modularaudio.service.apprendering.util.structure.ParsedJobData;
import uk.co.modularaudio.service.renderingplan.RenderingJob;
import uk.co.modularaudio.service.renderingplan.profiling.JobProfileResult;
import uk.co.modularaudio.service.renderingplan.profiling.RenderingPlanProfileResults;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ProfileVisualiser extends JPanel
{
	private static final long serialVersionUID = 8991758893514348788L;

	private static Log log = LogFactory.getLog( ProfileVisualiser.class.getName() );

	private final ComponentDesignerFrontController fc;

	private final JobDataListComparator jobDataListComparator = new JobDataListComparator();

	// Vis data
	private int numRenderingThreads;
	private long totalDuration;
	private ParsedJobData shortestJob;
	private List<ParsedJobData> profiledJobs;

	private final ProfilingLabel totalDurationDisplay;
	private final JLabel loopDurationDisplay;
	private final JLabel numRenderingThreadsDisplay;
	private final JLabel numUsedThreadsDisplay;

	private final TracksVisualiser tracks;

	public ProfileVisualiser( final ComponentDesignerFrontController fc )
	{
		this.fc = fc;

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		msh.addRowConstraint( "[][][][][grow]" );

		this.setLayout( msh.createMigLayout() );

		final Dimension minSize = new Dimension( 400, 30 * 3 );

		this.setMinimumSize( minSize );
//		this.setPreferredSize( minSize );

		this.setBackground( Color.ORANGE );


		final JLabel totalDurationLabel = new ProfilingLabel( "Total Duration:" );
		this.add( totalDurationLabel, "cell 0 0, align right");
		totalDurationDisplay = new ProfilingLabel();
		final Dimension preferredSize = totalDurationDisplay.getPreferredSize();
		final Dimension minDisplaySize = new Dimension( 150, preferredSize.height );
		totalDurationDisplay.setMinimumSize( minDisplaySize );
		this.add( totalDurationDisplay, "cell 1 0, grow 0");

		final ProfilingLabel loopDurationLabel = new ProfilingLabel( "Loop Duration:" );
		this.add( loopDurationLabel, "cell 0 1, align right");
		loopDurationDisplay = new ProfilingLabel();
		loopDurationDisplay.setMinimumSize( minDisplaySize );
		this.add( loopDurationDisplay, "cell 1 1, grow 0");

		final ProfilingLabel numRenderingThreadsLabel = new ProfilingLabel( "Number Of Rendering Threads:" );
		this.add( numRenderingThreadsLabel, "cell 0 2, align right");
		numRenderingThreadsDisplay = new ProfilingLabel();
		numRenderingThreadsDisplay.setMinimumSize( minDisplaySize );
		this.add( numRenderingThreadsDisplay, "cell 1 2, grow 0");

		final ProfilingLabel numUsedThreadsLabel = new ProfilingLabel( "Number Of Used Threads:" );
		this.add( numUsedThreadsLabel, "cell 0 3, align right");
		numUsedThreadsDisplay = new ProfilingLabel();
		numUsedThreadsDisplay.setMinimumSize( minDisplaySize );
		this.add( numUsedThreadsDisplay, "cell 1 3, grow 0");

		tracks = new TracksVisualiser();
		this.add( tracks, "cell 0 4, spanx 2, grow");
	}

	public void refresh()
	{
		try
		{
			final RenderingPlanProfileResults pr = fc.getProfileResults();

			processProfilingResults( pr );
		}
		catch( final DatastoreException de )
		{
			log.error("Error during profiling data fetch: " + de.toString(),de );
		}
//		log.debug("After refresh, the pv preferred size is " + getPreferredSize().toString());
	}

	private void processProfilingResults( final RenderingPlanProfileResults profileResults )
	{
		final int numRenderingThreads = profileResults.getNumRenderingThreads() + 1;
		final long clockCallbackStart = profileResults.getClockCallbackStart();
		final long clockCallbackPostProducer = profileResults.getClockCallbackPostProducer();
		final long clockCallbackPostRpFetch = profileResults.getClockCallbackPostRpFetch();
		final long clockCallbackPostLoop = profileResults.getClockCallbackPostLoop();
		final long producerDuration = clockCallbackPostProducer - clockCallbackStart;
		final long rpFetchDuration = clockCallbackPostRpFetch - clockCallbackPostProducer;
		final long loopDuration = clockCallbackPostLoop - clockCallbackPostRpFetch;
		final long totalDuration = clockCallbackPostLoop - clockCallbackStart;

		final HashMap<RenderingJob, JobProfileResult> jobToProfileResultMap = profileResults.getJobToProfileResultMap();

		final ArrayList<ParsedJobData> jobDataList = new ArrayList<ParsedJobData>();

		final ParsedJobData producerJob = new ParsedJobData( 0, producerDuration, 0, "SamplesIn" );
		jobDataList.add( producerJob );

		final ParsedJobData rpFetchJob = new ParsedJobData( producerDuration, rpFetchDuration, 0, "Render Plan Fetch" );
		jobDataList.add( rpFetchJob );

		ParsedJobData shortestJob = null;

		final Set<Integer> uniqueThreadSet = new HashSet<Integer>();
		for( final RenderingJob rj : jobToProfileResultMap.keySet() )
		{
			final JobProfileResult jr = jobToProfileResultMap.get( rj );
			final long jobStartTimestamp = jr.getStartTimestamp();
			final long jobEndTimestamp = jr.getEndTimestamp();
			final long jobOffsetFromStart = jobStartTimestamp - clockCallbackStart;
			final long jobLength = jobEndTimestamp - jobStartTimestamp;
			final int jobThreadNum = jr.getJobThreadExecutor();
			final String jobName = rj.toString();
			final ParsedJobData pjd = new ParsedJobData( jobOffsetFromStart, jobLength, jobThreadNum, jobName );
			jobDataList.add( pjd );

			if( shortestJob == null || pjd.getJobLength() < shortestJob.getJobLength() )
			{
				shortestJob = pjd;
			}

			uniqueThreadSet.add( jobThreadNum );
		}

		Collections.sort( jobDataList, jobDataListComparator );

		final int numUniqueThreads = uniqueThreadSet.size();

		this.numRenderingThreads = numRenderingThreads;
		this.totalDuration = totalDuration;
		this.shortestJob = shortestJob;
		this.profiledJobs = jobDataList;

		totalDurationDisplay.setText( TimestampFormatter.formatNanos( totalDuration ) );
		loopDurationDisplay.setText( TimestampFormatter.formatNanos( loopDuration ) );
		numRenderingThreadsDisplay.setText( Integer.toString(numRenderingThreads) );
		numUsedThreadsDisplay.setText( Integer.toString( numUniqueThreads ) );

		tracks.setTrackData( numRenderingThreads,
				numUniqueThreads,
				totalDuration,
				shortestJob,
				profiledJobs );

//		log.debug("After setting track data tracks preferred size is " + tracks.getPreferredSize().toString() );
//		log.debug("Our preferred size is now " + getPreferredSize().toString() );

//		tracks.repaint();

		repaint();
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		super.paintComponent( g );
	}
}
