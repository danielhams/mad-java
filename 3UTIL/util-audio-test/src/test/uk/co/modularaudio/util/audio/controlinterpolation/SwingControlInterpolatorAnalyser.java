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

package test.uk.co.modularaudio.util.audio.controlinterpolation;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.controlinterpolation.CDLowPassInterpolator24;
import uk.co.modularaudio.util.audio.controlinterpolation.CDSpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.HalfHannWindowInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LowPassInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.NoneInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperInterpolator;
import uk.co.modularaudio.util.audio.fileio.WaveFileReader;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class SwingControlInterpolatorAnalyser extends JFrame
{
	private static final long serialVersionUID = -4175746847701555282L;
	private static Log log = LogFactory.getLog( SwingControlInterpolatorAnalyser.class.getName() );

//	private static final float VALUE_CHASE_MILLIS = 20.0f;
//	private static final float VALUE_CHASE_MILLIS = 15.0f;
//	private static final float VALUE_CHASE_MILLIS = 10.0f;
	private static final float VALUE_CHASE_MILLIS = 9.8f;
//	private static final float VALUE_CHASE_MILLIS = 8.33f;
//	private static final float VALUE_CHASE_MILLIS = 7.33f;
//	private static final float VALUE_CHASE_MILLIS = 5.33f;
//	private static final float VALUE_CHASE_MILLIS = 3.7f;
//	private static final float VALUE_CHASE_MILLIS = 1.0f;

	private static final int SAMPLE_RATE = DataRate.SR_48000.getValue();

	private static final int VALUE_CHASE_SAMPLES = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( SAMPLE_RATE, VALUE_CHASE_MILLIS );

	private final static String SRC_DIR = "control_interpolation_events";
//	private final static String SRC_FILE = "zero_to_one_events.txt";
//	private final static String SRC_FILE = "zero_to_one_multi_events.txt";
	private final static String SRC_FILE = "zero_to_one_and_back_multi_events.txt";

//	public final static String WAV_FILE_IN = "/home/dan/Temp/fadermovements_48k_1chan.wav";
//	public final static String WAV_FILE_OUT = "/home/dan/Temp/fadermovements_48k_5chan_processed.wav";

	public final static String WAV_FILE_IN = "/home/dan/Temp/fadermovementsandmixxresponse_48k_1chan.wav";
	public final static String WAV_FILE_OUT = "/home/dan/Temp/fadermovements_interpout.wav";

//	public static final int VIS_WIDTH = 100;
//	public static final int VIS_WIDTH = 200;
//	public static final int VIS_WIDTH = 1600;
	public static final int VIS_WIDTH = 1400;
	public static final int VIS_HEIGHT = 100;
//	public static final int VIS_WIDTH = 1024;
//	public static final int VIS_HEIGHT = 75;
//	public static final int VIS_SAMPLES_PER_PIXEL=10;
	public static final int VIS_SAMPLES_PER_PIXEL=4;
//	public static final int VIS_SAMPLES_PER_PIXEL=2;
//	public static final int VIS_SAMPLES_PER_PIXEL=1;
	private static final float DIFF_FOR_7BIT_CONTROLLER = 1.0f / 128.0f;

	private final NoneInterpolator noneInterpolator;
	private final LinearInterpolator lInterpolator;
	private final HalfHannWindowInterpolator hhInterpolator;
	private final SpringAndDamperInterpolator sdInterpolator;
	private final LowPassInterpolator lpInterpolator;
	private final CDLowPassInterpolator24 lpInterpolator24;
	private final SpringAndDamperDoubleInterpolator sddInterpolator;
	private final CDSpringAndDamperDoubleInterpolator cdsddInterpolator;

	private final InterpolatorVisualiser noneVisualiser;
	private final InterpolatorVisualiser lVisualiser;
	private final InterpolatorVisualiser hhVisualiser;
	private final InterpolatorVisualiser sdVisualiser;
	private final InterpolatorVisualiser lpVisualiser;
	private final InterpolatorVisualiser lpVisualiser24;
	private final InterpolatorVisualiser sddVisualiser;
	private final InterpolatorVisualiser cdsddVisualiser;

	private final InterpolatorVisualiser[] visualisers;

	public SwingControlInterpolatorAnalyser()
	{
//		setSize( 1024, 768 );
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		noneInterpolator = new NoneInterpolator();
		lInterpolator = new LinearInterpolator();
		hhInterpolator = new HalfHannWindowInterpolator();
		sdInterpolator = new SpringAndDamperInterpolator( 0.0f, 1.0f );
		lpInterpolator = new LowPassInterpolator();
		lpInterpolator24 = new CDLowPassInterpolator24();
		sddInterpolator = new SpringAndDamperDoubleInterpolator( 0.0f, 1.0f );
		cdsddInterpolator = new CDSpringAndDamperDoubleInterpolator( 0.0f, 1.0f );

		noneVisualiser = new InterpolatorVisualiser( noneInterpolator, null );
		lVisualiser = new InterpolatorVisualiser( lInterpolator, noneVisualiser );
		hhVisualiser = new InterpolatorVisualiser( hhInterpolator, noneVisualiser );
		sdVisualiser = new InterpolatorVisualiser( sdInterpolator, noneVisualiser );
		lpVisualiser = new InterpolatorVisualiser( lpInterpolator, noneVisualiser );
		lpVisualiser24 = new InterpolatorVisualiser( lpInterpolator24, noneVisualiser );
		sddVisualiser = new InterpolatorVisualiser( sddInterpolator, noneVisualiser );
		cdsddVisualiser = new InterpolatorVisualiser( cdsddInterpolator, noneVisualiser );
		visualisers = new InterpolatorVisualiser[8];
		visualisers[0] = noneVisualiser;
		visualisers[1] = lVisualiser;
		visualisers[2] = hhVisualiser;
		visualisers[3] = sdVisualiser;
		visualisers[4] = lpVisualiser;
		visualisers[5] = lpVisualiser24;
		visualisers[6] = sddVisualiser;
		visualisers[7] = cdsddVisualiser;

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "fill" );
		setLayout( msh.createMigLayout() );

		add( new JLabel("Control"), "wrap");
		add( noneVisualiser, "grow, wrap");

		add( new JLabel("Linear"), "wrap");
		add( lVisualiser, "grow,wrap");

		add( new JLabel("HalfHann"), "wrap");
		add( hhVisualiser, "grow,wrap");

		add( new JLabel("SpringAndDamper"), "wrap");
		add( sdVisualiser, "grow,wrap");

		add( new JLabel("LowPass"), "wrap");
		add( lpVisualiser, "grow,wrap");

		add( new JLabel("CDLowPass24"), "wrap");
		add( lpVisualiser24, "grow,wrap");

		add( new JLabel("SpringAndDamperDouble"), "wrap");
		add( sddVisualiser, "grow,wrap");

		add( new JLabel("CDSpringAndDamperDouble"), "wrap");
		add( cdsddVisualiser, "grow");

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

			// Map to 7bit quantities
			final float sbQuat = te.getEventValue() * 127.0f;
			final int sbQuantInt = (int)sbQuat;
			log.debug("Quat(" + sbQuantInt + ")");

			te.setEventValue( sbQuantInt / 127.0f );
		}

		// Set the initial value from the first event
		final TestEvent firstEvent = events[0];
		final float firstValue = firstEvent.getEventValue();

		// The none interpolator doesn't have a reset.

		noneInterpolator.hardSetValue( firstValue );
		lInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		lInterpolator.hardSetValue( firstValue );
		hhInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		hhInterpolator.hardSetValue( firstValue );
		sdInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		sdInterpolator.hardSetValue( firstValue );
		lpInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		lpInterpolator.hardSetValue( firstValue );
		lpInterpolator24.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		lpInterpolator24.hardSetValue( firstValue );
		sddInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		sddInterpolator.hardSetValue( firstValue );
		cdsddInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		cdsddInterpolator.hardSetValue( firstValue );

		// Pass it to all the visualisers for each interpolation
		// type - we'll use the "none" interpolator to show the orginal signal
		for( final InterpolatorVisualiser iv : visualisers )
		{
			iv.interpolateEvents( events );
		}

	}

	public void applyToWavFile( final String wavFile,
			final String outWavFile )
			throws IOException
	{
		final WaveFileReader reader = new WaveFileReader( wavFile );

		final long numSamples = reader.getNumTotalFloats();

		final int numSamplesInt = (int)numSamples;

		final float[] samples = new float[numSamplesInt];

		reader.readFrames( samples, 0, 0, numSamplesInt );

		// Work out the offsets for control value changes
		final ArrayList<Integer> controlValueChanges = new ArrayList<Integer>();

		float curSample = samples[0];

		for( int s = 1 ; s < numSamplesInt ; ++s )
		{
			final float diff = curSample - samples[s];
			final float absDiff = (diff < 0.0f ? -diff : diff );
			if( absDiff > DIFF_FOR_7BIT_CONTROLLER )
			{
				controlValueChanges.add( s );
				curSample = samples[s];
			}
		}

		final WaveFileWriter writer = new WaveFileWriter( outWavFile,
				visualisers.length,
				DataRate.SR_48000.getValue(),
				(short)16 );

		final float[][] processedSamples = new float[visualisers.length][];
		for( int i = 0 ; i < visualisers.length ; ++i )
		{
			processedSamples[i] = new float[numSamplesInt];
		}

		noneInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		noneInterpolator.hardSetValue( samples[0] );

		lInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		lInterpolator.hardSetValue( samples[0] );

		hhInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		hhInterpolator.hardSetValue( samples[0] );

		sdInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		sdInterpolator.hardSetValue( samples[0] );

		lpInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		lpInterpolator.hardSetValue( samples[0] );

		lpInterpolator24.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		lpInterpolator24.hardSetValue( samples[0] );

		sddInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		sddInterpolator.hardSetValue( samples[0] );

		cdsddInterpolator.resetSampleRateAndPeriod( SAMPLE_RATE, VALUE_CHASE_SAMPLES );
		cdsddInterpolator.hardSetValue( samples[0] );

		int prevOffset = 0;
		for( final int valueChangeOffset : controlValueChanges )
		{
			final float sampleAtChange = samples[valueChangeOffset];

			noneInterpolator.notifyOfNewValue( sampleAtChange );
			lInterpolator.notifyOfNewValue( sampleAtChange );
			hhInterpolator.notifyOfNewValue( sampleAtChange );
			sdInterpolator.notifyOfNewValue( sampleAtChange );
			lpInterpolator.notifyOfNewValue( sampleAtChange );
			lpInterpolator24.notifyOfNewValue( sampleAtChange );
			sddInterpolator.notifyOfNewValue( sampleAtChange );
			cdsddInterpolator.notifyOfNewValue( sampleAtChange );

			noneInterpolator.generateControlValues( processedSamples[0], prevOffset, valueChangeOffset - prevOffset );
			lInterpolator.generateControlValues( processedSamples[1], prevOffset, valueChangeOffset - prevOffset );
			hhInterpolator.generateControlValues( processedSamples[2], prevOffset, valueChangeOffset - prevOffset );
			sdInterpolator.generateControlValues( processedSamples[3], prevOffset, valueChangeOffset - prevOffset );
			lpInterpolator.generateControlValues( processedSamples[4], prevOffset, valueChangeOffset - prevOffset );
			lpInterpolator24.generateControlValues( processedSamples[4], prevOffset, valueChangeOffset - prevOffset );
			sddInterpolator.generateControlValues( processedSamples[5], prevOffset, valueChangeOffset - prevOffset );
			cdsddInterpolator.generateControlValues( processedSamples[5], prevOffset, valueChangeOffset - prevOffset );
			prevOffset = valueChangeOffset;
		}

		final float[] outFloats = new float[ numSamplesInt * visualisers.length ];

		int outIndex = 0;
		for( int s = 0 ; s < numSamplesInt ; ++s )
		{
			for( int v = 0 ; v < visualisers.length ; ++v )
			{
				outFloats[outIndex++] = processedSamples[v][s];
			}
		}
		writer.writeFrames( outFloats, 0, numSamplesInt );
		writer.close();
		reader.close();
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		final SwingControlInterpolatorAnalyser scia = new SwingControlInterpolatorAnalyser();
		SwingUtilities.invokeLater( new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					scia.setVisible( true );
					scia.applyToWavFile( WAV_FILE_IN,WAV_FILE_OUT );
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
