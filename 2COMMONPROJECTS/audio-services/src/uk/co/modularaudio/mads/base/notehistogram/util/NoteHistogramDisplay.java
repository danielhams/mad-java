package uk.co.modularaudio.mads.base.notehistogram.util;

import java.awt.Color;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.notehistogram.ui.NoteHistogramMadUiInstance;
import uk.co.modularaudio.mads.base.notehistogram.util.NoteHistogram.HistogramListener;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class NoteHistogramDisplay extends JPanel implements HistogramListener
{
	private static final long serialVersionUID = 4876392781636639883L;

//	private static Log log = LogFactory.getLog( NoteHistogramDisplay.class.getName() );

	protected final static int EVENTS_LABELS_WIDTH = 35;
	protected final static int BINS_LABELS_HEIGHT = 20;
	protected final static int AXIS_MARKER_LENGTH = 5;

	public final static int NUM_HISTOGRAM_BUCKETS = 100;
	public final static int NUM_FRAMES_PER_BUCKET = 10;

	protected final static int PIXELS_PER_BIN = 3;

	protected final static int NUM_EVENT_MARKERS = 11;
	protected final static int NUM_BIN_MARKERS = 11;
	protected final static int MARKER_PADDING = 15;

	private final NoteHistogram histogram = new NoteHistogram( NUM_HISTOGRAM_BUCKETS, NUM_FRAMES_PER_BUCKET );

	private final HistogramGraph graph;
	private final HistogramBinAxisLabels binAxisLabels;

	private boolean shouldRepaint;

	public NoteHistogramDisplay( final NoteHistogramMadUiInstance uiInstance )
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
				BINS_LABELS_HEIGHT + "]" );

		setLayout( msh.createMigLayout() );

		setBackground( Color.BLACK );
		setOpaque( true );

		final HistogramEventAxisLabels eventAxisLabels = new HistogramEventAxisLabels( histogram );
		this.add( eventAxisLabels, "cell 0 0, spany 2, growy" );

		final HistogramEventAxisMarkers eventAxisMarkers = new HistogramEventAxisMarkers( histogram );
		this.add( eventAxisMarkers, "cell 1 0, growy" );

		graph = new HistogramGraph( histogram );
		this.add( graph, "cell 2 0, grow, push" );

		final HistogramBinAxisMarkers binAxisMarkers = new HistogramBinAxisMarkers( histogram );
		this.add( binAxisMarkers, "cell 1 1, spanx 2, growx" );

		binAxisLabels = new HistogramBinAxisLabels( histogram );
		this.add( binAxisLabels, "cell 0 2, spanx 3, growx" );

		uiInstance.setNoteHistogramDisplay( this );
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

	public void addNoteDiff( final int noteDiffFrames )
	{
//		log.debug("Added note diff");
		histogram.addNoteDiff( noteDiffFrames );
	}

	public void setSampleRate( final int sampleRate )
	{
		binAxisLabels.setSampleRate( sampleRate );
	}
}
