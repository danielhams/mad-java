package uk.co.modularaudio.mads.base.controllerhistogram.util;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.controllerhistogram.ui.ControllerHistogramMadUiInstance;
import uk.co.modularaudio.mads.base.controllerhistogram.util.Histogram.HistogramListener;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class HistogramDisplay extends JPanel implements HistogramListener
{
	private static final long serialVersionUID = 4876392781636639883L;

//	private static Log log = LogFactory.getLog( HistogramDisplay.class.getName() );

	protected final static int EVENTS_LABELS_WIDTH = 35;
	protected final static int BUCKETS_LABELS_HEIGHT = 20;
	protected final static int AXIS_MARKER_LENGTH = 5;

	// Define our histogram in terms of the final marker that appears on the graph
	public final static int HISTOGRAM_LAST_BUCKET_UPPER_MILLIS = 26;
	public final static long HISTOGRAM_LAST_BUCKET_UPPER_NANOS = HISTOGRAM_LAST_BUCKET_UPPER_MILLIS * AudioTimingUtils.MILLIS_TO_NANOS_RATIO;

//	public final static int NUM_HISTOGRAM_BUCKETS = 100;
	public final static int NUM_HISTOGRAM_BUCKETS = 130;

	protected final static int NUM_EVENT_MARKERS = 11;
	protected final static int NUM_BUCKET_MARKERS = 11;
	protected final static int MARKER_PADDING = 15;

	private final Histogram histogram = new Histogram( NUM_HISTOGRAM_BUCKETS, HISTOGRAM_LAST_BUCKET_UPPER_NANOS );

	private final HistogramGraph graph;
	private final HistogramBucketAxisLabels bucketAxisLabels;

	private boolean shouldRepaint;

	public HistogramDisplay( final ControllerHistogramMadUiInstance uiInstance )
	{
		histogram.addHistogramListener( this );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "fill" );

		msh.addColumnConstraint( "[" + EVENTS_LABELS_WIDTH + "][" +
				AXIS_MARKER_LENGTH + "][grow]" );
		msh.addRowConstraint( "[grow][" + AXIS_MARKER_LENGTH + "][" +
				BUCKETS_LABELS_HEIGHT + "]" );

		setLayout( msh.createMigLayout() );

		setBackground( Color.BLACK );
		setOpaque( true );

		final HistogramEventAxisLabels eventAxisLabels = new HistogramEventAxisLabels( histogram );
		this.add( eventAxisLabels, "cell 0 0, spany 2, growy" );

		final HistogramEventAxisMarkers eventAxisMarkers = new HistogramEventAxisMarkers( histogram );
		this.add( eventAxisMarkers, "cell 1 0, growy" );

		graph = new HistogramGraph( histogram );
		this.add( graph, "cell 2 0, grow, push" );

		final HistogramBucketAxisMarkers bucketAxisMarkers = new HistogramBucketAxisMarkers( histogram );
		this.add( bucketAxisMarkers, "cell 1 1, spanx 2, growx" );

		bucketAxisLabels = new HistogramBucketAxisLabels( histogram );
		this.add( bucketAxisLabels, "cell 0 2, spanx 3, growx" );

		uiInstance.setNoteHistogramDisplay( this );

		final HistogramDisplayMouseListener hdml = new HistogramDisplayMouseListener( graph );

		addMouseListener( hdml );
		addMouseMotionListener( hdml );
	}

	@Override
	public void paint( final Graphics g )
	{
		super.paint( g );
	}

	public void doDisplayProcessing()
	{
		if( shouldRepaint )
		{
//			log.debug("Asking for a repaint");
			graph.repaint();
			shouldRepaint = false;
		}
	}

	@Override
	public void receiveHistogramUpdate()
	{
//		log.debug("Received an update");
		shouldRepaint = true;
	}

	public void reset()
	{
//		histogram.dumpStats();
		histogram.reset();
		shouldRepaint = true;
	}

	public void addNoteDiffNano( final int noteDiffNanos )
	{
//		log.debug("Added note diff");
		histogram.addNoteDiffNanos( noteDiffNanos );
	}
}
