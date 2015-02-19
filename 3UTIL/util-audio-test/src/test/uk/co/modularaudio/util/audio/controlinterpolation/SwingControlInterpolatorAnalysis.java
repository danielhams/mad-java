package test.uk.co.modularaudio.util.audio.controlinterpolation;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.controlinterpolation.HalfHannWindowInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.NoneInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SumOfRatiosInterpolator;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class SwingControlInterpolatorAnalysis extends JFrame
{
	private static final long serialVersionUID = -4175746847701555282L;
	private static Log log = LogFactory.getLog( SwingControlInterpolatorAnalysis.class.getName() );

	private static final float VALUE_CHASE_MILLIS = 10.0f;
//	private static final float VALUE_CHASE_MILLIS = 3.7f;
//	private static final float VALUE_CHASE_MILLIS = 1.0f;

	private static final int SAMPLE_RATE = DataRate.SR_48000.getValue();

	private final static String SRC_DIR = "control_interpolation_events";
//	private final static String SRC_FILE = "zero_to_one_events.txt";
//	private final static String SRC_FILE = "zero_to_one_multi_events.txt";
	private final static String SRC_FILE = "zero_to_one_and_back_multi_events.txt";

	public static final int VIS_WIDTH = 1400;
	public static final int VIS_HEIGHT = 100;

	private final NoneInterpolator noneInterpolator;
	private final SumOfRatiosInterpolator sorInterpolator;
	private final LinearInterpolator lInterpolator;
	private final HalfHannWindowInterpolator hhInterpolator;
	private final InterpolatorVisualiser noneVisualiser;
	private final InterpolatorVisualiser sorVisualiser;
	private final InterpolatorVisualiser lVisualiser;
	private final InterpolatorVisualiser hhVisualiser;

	private final InterpolatorVisualiser[] visualisers;

	public SwingControlInterpolatorAnalysis()
	{
//		setSize( 1024, 768 );
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		noneInterpolator = new NoneInterpolator();
		sorInterpolator = new SumOfRatiosInterpolator();
		lInterpolator = new LinearInterpolator();
		hhInterpolator = new HalfHannWindowInterpolator();

		noneVisualiser = new InterpolatorVisualiser( noneInterpolator, null );
		sorVisualiser = new InterpolatorVisualiser( sorInterpolator, noneVisualiser );
		lVisualiser = new InterpolatorVisualiser( lInterpolator, noneVisualiser );
		hhVisualiser = new InterpolatorVisualiser( hhInterpolator, noneVisualiser );
		visualisers = new InterpolatorVisualiser[4];
		visualisers[0] = noneVisualiser;
		visualisers[1] = sorVisualiser;
		visualisers[2] = lVisualiser;
		visualisers[3] = hhVisualiser;

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "fill" );
		setLayout( msh.createMigLayout() );

		add( new JLabel("Control"), "wrap");
		add( noneVisualiser, "grow, wrap");
		add( new JLabel("SumOfRatios"), "wrap");
		add( sorVisualiser, "grow, wrap");
		add( new JLabel("Linear"), "wrap");
		add( lVisualiser, "grow,wrap");
		add( new JLabel("HalfHann"), "wrap");
		add( hhVisualiser, "grow");

		this.pack();
	}

	public void go() throws IOException
	{
		// Load our test events from a file
		log.debug("Loading test events");
		final String fileName = SRC_DIR + "/" + SRC_FILE;
		final TestEvent[] events = EventLoader.loadEventsFromFile( fileName );
		final int numEvents = events.length;
		log.debug("Loaded " + numEvents + " events for display");
		for( int i = 0 ; i < numEvents ; ++i )
		{
			final TestEvent te = events[i];
			log.debug("Event(" + i + ") - " + te.toString() );
		}

		// Set the initial value from the first event
		final TestEvent firstEvent = events[0];
		final float firstValue = firstEvent.getEventValue();

		// The none interpolator doesn't really have a reset.
		noneInterpolator.hardSetValue( firstValue );
		sorInterpolator.reset( SAMPLE_RATE, VALUE_CHASE_MILLIS );
		sorInterpolator.hardSetValue( firstValue );
		lInterpolator.reset( SAMPLE_RATE, VALUE_CHASE_MILLIS );
		lInterpolator.hardSetValue( firstValue );
		hhInterpolator.reset( SAMPLE_RATE, VALUE_CHASE_MILLIS );
		hhInterpolator.hardSetValue( firstValue );

		// Pass it to all the visualisers for each interpolation
		// type - we'll use the "none" interpolator to show the orginal signal
		for( final InterpolatorVisualiser iv : visualisers )
		{
			iv.interpolateEvents( events );
		}

	}

	public static void main( final String[] args )
	{
		final SwingControlInterpolatorAnalysis scia = new SwingControlInterpolatorAnalysis();
		SwingUtilities.invokeLater( new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					scia.setVisible( true );
					scia.go();
				}
				catch( final Exception e )
				{
					log.error( e );
					scia.dispose();
				}
			}
		} );
	}

}
