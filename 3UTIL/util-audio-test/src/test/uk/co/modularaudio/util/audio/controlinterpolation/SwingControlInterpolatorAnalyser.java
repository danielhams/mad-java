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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.list.IntArrayList;

import uk.co.modularaudio.util.audio.controlinterpolation.CDLowPass12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.CDLowPass24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.CDSpringAndDamperDouble24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.CDSpringAndDamperDouble12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.ControlValueInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.HalfHannWindowInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearLowPass12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LowPass12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LowPass24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.NoneInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDouble24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamper24Interpolator;
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
//	private static final float VALUE_CHASE_MILLIS = 9.8f;
//	private static final float VALUE_CHASE_MILLIS = 9.5f;
//	private static final float VALUE_CHASE_MILLIS = 8.33f;
	private static final float VALUE_CHASE_MILLIS = 8.2f;
//	private static final float VALUE_CHASE_MILLIS = 7.33f;
//	private static final float VALUE_CHASE_MILLIS = 5.33f;
//	private static final float VALUE_CHASE_MILLIS = 3.7f;
//	private static final float VALUE_CHASE_MILLIS = 1.0f;

	private static final int SAMPLE_RATE = DataRate.SR_48000.getValue();

	private static final int TEST_PERIOD_LENGTH = 1024;

	private static final int VALUE_CHASE_SAMPLES =
			AudioTimingUtils.getNumSamplesForMillisAtSampleRate( SAMPLE_RATE, VALUE_CHASE_MILLIS );

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
//	public static final int VIS_WIDTH = 1400;
	public static final int VIS_WIDTH = 500;
	public static final int VIS_HEIGHT = 200;
//	public static final int VIS_WIDTH = 1024;
//	public static final int VIS_HEIGHT = 75;
//	public static final int VIS_SAMPLES_PER_PIXEL=10;
	public static final int VIS_SAMPLES_PER_PIXEL=4;
//	public static final int VIS_SAMPLES_PER_PIXEL=2;
//	public static final int VIS_SAMPLES_PER_PIXEL=1;
	private static final float DIFF_FOR_7BIT_CONTROLLER = 1.0f / 128.0f;

	private enum INTERPOLATOR
	{
		NONE,
		LINEAR,
		HALFHANN,
		SPRINGANDDAMPER,
		LOWPASS,
		LOWPASS24,
		CDLOWPASS,
		CDLOWPASS24,
		SPRINGANDDAMPERDOUBLE,
		CDSPRINGANDDAMPERDOUBLE,
		CDSPRINGANDDAMPERDOUBLE24,
		LINEARLOWPASS12
	};
	private final static int NUM_INTERPOLATORS = INTERPOLATOR.values().length;

	private final static ControlValueInterpolator[] interpolators;

	static
	{
		interpolators = new ControlValueInterpolator[NUM_INTERPOLATORS];
		interpolators[INTERPOLATOR.NONE.ordinal()] = new NoneInterpolator();
		interpolators[INTERPOLATOR.LINEAR.ordinal()] = new LinearInterpolator();
		interpolators[INTERPOLATOR.HALFHANN.ordinal()] = new HalfHannWindowInterpolator();
		interpolators[INTERPOLATOR.SPRINGANDDAMPER.ordinal()] = new SpringAndDamper24Interpolator();
		interpolators[INTERPOLATOR.LOWPASS.ordinal()] = new LowPass12Interpolator();
		interpolators[INTERPOLATOR.LOWPASS24.ordinal()] = new LowPass24Interpolator();
		interpolators[INTERPOLATOR.CDLOWPASS.ordinal()] = new CDLowPass12Interpolator();
		interpolators[INTERPOLATOR.CDLOWPASS24.ordinal()] = new CDLowPass24Interpolator();
		interpolators[INTERPOLATOR.SPRINGANDDAMPERDOUBLE.ordinal()] = new SpringAndDamperDouble24Interpolator();
		interpolators[INTERPOLATOR.CDSPRINGANDDAMPERDOUBLE.ordinal()] = new CDSpringAndDamperDouble12Interpolator();
		interpolators[INTERPOLATOR.CDSPRINGANDDAMPERDOUBLE24.ordinal()] = new CDSpringAndDamperDouble24Interpolator();
		interpolators[INTERPOLATOR.LINEARLOWPASS12.ordinal()] = new LinearLowPass12Interpolator();
	}

	private final InterpolatorVisualiser[] visualisers = new InterpolatorVisualiser[NUM_INTERPOLATORS];

	public SwingControlInterpolatorAnalyser()
	{
//		setSize( 1024, 768 );
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		final ControlValueInterpolator noneInterpolator = interpolators[INTERPOLATOR.NONE.ordinal()];
		final InterpolatorVisualiser noneVisualiser = new InterpolatorVisualiser( noneInterpolator, null );
		visualisers[INTERPOLATOR.NONE.ordinal()] = noneVisualiser;

		for( int i = INTERPOLATOR.NONE.ordinal() + 1 ; i < NUM_INTERPOLATORS ; ++i )
		{
			final ControlValueInterpolator interpolator = interpolators[i];
			visualisers[i] = new InterpolatorVisualiser( interpolator,
					interpolator == noneInterpolator ? null : noneVisualiser );
		}

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "fill" );
		setLayout( msh.createMigLayout() );

		// In rows:
		//	Linear		Low Pass			CD Low Pass			CDSpringAndDamperDouble
		//	Half Hann	Low Pass 24			CD Low Pass 24		CDSpringAndDamperDouble24

		add( new JLabel("Linear"), "cell 0 0");
		add( visualisers[INTERPOLATOR.LINEAR.ordinal()], "cell 0 1");

		add( new JLabel("Half Hann"), "cell 0 2");
		add( visualisers[INTERPOLATOR.HALFHANN.ordinal()], "cell 0 3");

		add( new JLabel("Low Pass"), "cell 1 0");
		add( visualisers[INTERPOLATOR.LOWPASS.ordinal()], "cell 1 1");

		add( new JLabel("Low Pass 24"), "cell 1 2");
		add( visualisers[INTERPOLATOR.LOWPASS24.ordinal()], "cell 1 3");

		add( new JLabel("Linear Low Pass "), "cell 1 4");
		add( visualisers[INTERPOLATOR.LINEARLOWPASS12.ordinal()], "cell 1 5");

		add( new JLabel("CD Low Pass"), "cell 2 0");
		add( visualisers[INTERPOLATOR.CDLOWPASS.ordinal()], "cell 2 1");

		add( new JLabel("CD Low Pass 24"), "cell 2 2");
		add( visualisers[INTERPOLATOR.CDLOWPASS24.ordinal()], "cell 2 3");

		add( new JLabel("CDSpringAndDamperDouble"), "cell 3 0");
		add( visualisers[INTERPOLATOR.CDSPRINGANDDAMPERDOUBLE.ordinal()], "cell 3 1");

		add( new JLabel("CDSpringAndDamperDouble24"), "cell 3 2");
		add( visualisers[INTERPOLATOR.CDSPRINGANDDAMPERDOUBLE24.ordinal()], "cell 3 3");

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

		for( final ControlValueInterpolator cvi : interpolators )
		{
			cvi.resetSampleRateAndPeriod( SAMPLE_RATE, TEST_PERIOD_LENGTH, VALUE_CHASE_SAMPLES );
			cvi.hardSetValue( firstValue );
		}

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
		final IntArrayList controlValueChanges = new IntArrayList();

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

		for( final ControlValueInterpolator cvi : interpolators )
		{
			cvi.resetSampleRateAndPeriod( SAMPLE_RATE, TEST_PERIOD_LENGTH, VALUE_CHASE_SAMPLES );
			cvi.hardSetValue( samples[0] );
		}

		int prevOffset = 0;
		for( int vc = 0 ; vc < controlValueChanges.size() ; ++vc )
		{
			final int valueChangeOffset = controlValueChanges.get( vc );

			final float sampleAtChange = samples[valueChangeOffset];

			for( final ControlValueInterpolator cvi : interpolators )
			{
				cvi.notifyOfNewValue( sampleAtChange );
			}

			final int lengthToGenerate = valueChangeOffset - prevOffset;

			for( int i = 0 ; i < interpolators.length ; ++i )
			{
				interpolators[i].generateControlValues(
						processedSamples[i],
						prevOffset,
						lengthToGenerate );
			}

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
//					scia.applyToWavFile( WAV_FILE_IN,WAV_FILE_OUT );
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
