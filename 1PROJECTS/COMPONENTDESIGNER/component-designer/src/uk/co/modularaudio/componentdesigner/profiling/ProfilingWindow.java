package uk.co.modularaudio.componentdesigner.profiling;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

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

public class ProfilingWindow extends JFrame
{
	private static final long serialVersionUID = 5841688948040826459L;

	private static final Dimension MIN_SIZE = new Dimension(
			(TracksVisualiser.TRACK_BLOCK_WIDTH * 3) + 60,
			256 );

	private static Log log = LogFactory.getLog( ProfilingWindow.class.getName() );

	private final ProfilingPanel pp;

	private final TracksVisualiser tv;
	private final JScrollPane scrollPane;

	private final JobDataListComparator jobDataListComparator = new JobDataListComparator();

	public ProfilingWindow( final ComponentDesignerFrontController fc )
	{
		super.setTitle( "Audio Job Profiling" );

		setMinimumSize( MIN_SIZE );

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 15");
		msh.addLayoutConstraint( "gap 5");
		msh.addLayoutConstraint( "fill" );

		msh.addRowConstraint( "[][][grow]" );

		setLayout( msh.createMigLayout() );

		final JButton refreshButton = new JButton( "Refresh" );

		refreshButton.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				try
				{
					final int scrollPos = scrollPane.getVerticalScrollBar().getValue();

					final RenderingPlanProfileResults pr = fc.getProfileResults();

					processProfilingResults( pr );

					SwingUtilities.invokeLater( new Runnable()
					{

						@Override
						public void run()
						{
							scrollPane.getVerticalScrollBar().setValue( scrollPos );
						}
					} );

				}
				catch( final DatastoreException de )
				{
					log.error("Error during profiling data fetch: " + de.toString(),de );
				}
			}
		} );

		add( refreshButton, "cell 0 0" );

		pp = new ProfilingPanel();

		add( pp, "cell 0 1, growx" );

		tv = new TracksVisualiser();

		scrollPane = new JScrollPane();

		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );

		scrollPane.setViewportView( tv );

		this.add( scrollPane, "cell 0 2, spanx 2, grow");

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

		pp.setData( totalDuration,
				loopDuration,
				numRenderingThreads,
				numUniqueThreads );

		tv.setTrackData( numRenderingThreads,
				numUniqueThreads,
				totalDuration,
				shortestJob,
				jobDataList );
	}


}
