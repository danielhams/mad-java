package test.uk.co.modularaudio.util.audio.oscillatortable;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jtransforms.fft.DoubleFFT_1D;
import org.jtransforms.fft.FloatFFT_1D;

import uk.co.modularaudio.util.audio.fft.HannFftWindow;
import uk.co.modularaudio.util.audio.fft.JTransformsConfigurator;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.lookuptable.LookupTableUtils;
import uk.co.modularaudio.util.audio.lookuptable.raw.RawLookupTable;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.oscillatortable.CubicPaddedRawWaveTable;
import uk.co.modularaudio.util.audio.oscillatortable.JunoRawWaveTableGenerator;
import uk.co.modularaudio.util.audio.oscillatortable.RawWaveTableGenerator;
import uk.co.modularaudio.util.audio.stft.StftDataFrame;
import uk.co.modularaudio.util.audio.stft.StftDataFrameDouble;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.tools.ComplexPolarConverter;
import uk.co.modularaudio.util.math.MathDefines;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestReverseFftBandLimitedWaveform
{
	private static final String OUTPUT_DIR = "tmpoutput";
	private static final String REVERSEFFTSQUAREDOUBLE_WAV = OUTPUT_DIR + File.separatorChar + "reversefftsquaredouble.wav";
	private static final String REVERSEFFTSQUARE_WAV = OUTPUT_DIR + File.separatorChar + "reversefftsquare.wav";
	private static final String WAVEGENERATOR_WAV = OUTPUT_DIR + File.separatorChar + "wavegenerator.wav";

	private static Log log = LogFactory.getLog( TestReverseFftBandLimitedWaveform.class.getName());

	private final int sampleRate = DataRate.SR_48000.getValue();

	final int fftRealLength = 32768;
//	final int fftRealLength = 256;

	final HannFftWindow fftWindow = new HannFftWindow( fftRealLength );

	final StftParameters stftParams;

	final ComplexPolarConverter cpc;

//	private final int numHarmonics = 1;
//	private final int numHarmonics = 3;
	private final int numHarmonics = 100;
//	private final int numHarmonics = 120;
//	private final int numHarmonics = 337;

	private final int numToConcatenate = 1;
//	private final int numToConcatenate = 20;
//	private final int numToConcatenate = 4096;
	private final long numExpectedFrames = numToConcatenate * fftRealLength;

	private static final double MAX_DB = -0.75;
	private static final float MAX_VALUE_FOR_NORMALISE_F = AudioMath.dbToLevelF( (float)MAX_DB );
	private static final double MAX_VALUE_FOR_NORMALISE_D = AudioMath.dbToLevel( MAX_DB );

	private final float[][] fArray = new float[1][];
	private final double[][] dArray = new double[1][];

	private final RawWaveTableGenerator waveTableGenerator = new JunoRawWaveTableGenerator();
//	private final RawWaveTableGenerator waveTableGenerator = new SawRawWaveTableGenerator();
//	private final RawWaveTableGenerator waveTableGenerator = new SineRawWaveTableGenerator();
//	private final RawWaveTableGenerator waveTableGenerator = new SquareRawWaveTableGenerator();
//	private final RawWaveTableGenerator waveTableGenerator = new Test1RawWaveTableGenerator();
//	private final RawWaveTableGenerator waveTableGenerator = new TriangleRawWaveTableGenerator();

	public TestReverseFftBandLimitedWaveform() throws Exception
	{
		stftParams = new StftParameters( DataRate.CD_QUALITY,
				1,
				fftRealLength,
				4,
				fftRealLength,
				fftWindow );
		cpc = new ComplexPolarConverter( stftParams );
	}

	public void analyseAdditiveFourierVersion() throws Exception
	{
		final CubicPaddedRawWaveTable waveTable = waveTableGenerator.reallyGenerateWaveTable( fftRealLength,
				numHarmonics );

		assert( fftRealLength == waveTable.buffer.length - CubicPaddedRawWaveTable.NUM_EXTRA_SAMPLES_IN_BUFFER );

		final int numBins = stftParams.getNumBins();
		final int fftSize = stftParams.getNumReals();
		final int fftComplexArraySize = stftParams.getComplexArraySize();
		log.debug("Source fftRealLength is " + fftRealLength );
		log.debug("The fft will have " + numBins + " bins");
		log.debug("The fft size will be " + fftSize );
		log.debug("The fft will have " + fftComplexArraySize + " float results as output");

		final FloatFFT_1D fftEngine = new FloatFFT_1D( fftSize );

		final StftDataFrame dataFrame = new StftDataFrame( 1, fftSize, fftComplexArraySize, numBins );
		Arrays.fill( dataFrame.complexFrame[0], 0.0f );
		System.arraycopy( waveTable.buffer, 1, dataFrame.complexFrame[0], 0, fftRealLength );

		fftEngine.realForward( dataFrame.complexFrame[0] );

		cpc.complexToPolar( dataFrame );

		final int numBinsToDisplay = 60;
		for( int b = 0 ; b < numBinsToDisplay ; ++b )
		{
			log.debug("Bin " + b + " of freq " + MathFormatter.fastFloatPrint((stftParams.getFreqPerBin() * b), 8, false ) +
				" amp=" + MathFormatter.slowFloatPrint( dataFrame.amps[0][b], 8, true ) +
				" phase=" + MathFormatter.slowFloatPrint( dataFrame.phases[0][b], 8, true ) );
		}

		final TestWaveFileWriter outWrite = new TestWaveFileWriter( WAVEGENERATOR_WAV, 1, sampleRate, numExpectedFrames );

		final float[] normVersion = new float[ fftRealLength ];
		Arrays.fill( normVersion, 0.0f );
		System.arraycopy( waveTable.buffer, 1, normVersion, 0, fftRealLength );

		LookupTableUtils.normaliseFloatsToMax( normVersion, 0, fftRealLength, MAX_VALUE_FOR_NORMALISE_F );
		fArray[0] = normVersion;

		for( int s = 0 ; s < numToConcatenate ; ++s )
		{
			outWrite.writeFloats( fArray, 0, fftRealLength );
		}

		outWrite.close();
	}

	public void generateTestOutput() throws Exception
	{
		final int numBins = stftParams.getNumBins();
		final int fftComplexArraySize = stftParams.getComplexArraySize();
		log.debug("Source fftRealLength is " + fftRealLength );
		log.debug("The fft will have " + numBins + " bins");
		log.debug("The fft will have " + fftComplexArraySize + " float results as output");

		final FloatFFT_1D fftEngine = new FloatFFT_1D( fftRealLength );

		final StftDataFrame dataFrame = new StftDataFrame( 1, fftRealLength, fftComplexArraySize, numBins );
		Arrays.fill( dataFrame.amps[0], 0.0f );
		Arrays.fill( dataFrame.phases[0], 0.0f );
		Arrays.fill( dataFrame.complexFrame[0], 0.0f );

		final int startBin = 1;

		final RawLookupTable harmonics = waveTableGenerator.getHarmonics( numHarmonics );
		final float phase = waveTableGenerator.getPhase() * MathDefines.TWO_PI_F;

		// What's used in the fourier generator
		for( int i = 0 ; i < numHarmonics ; ++i )
		{
			// Every other bin gets a peak
			final int harmonicIndex = i;
			final int binToFill = startBin + harmonicIndex;
			final float ampOfBin = harmonics.floatBuffer[ harmonicIndex ] * (fftRealLength / 2);

			dataFrame.amps[0][binToFill] = ampOfBin;
			dataFrame.phases[0][binToFill] = phase;
		}

		// Convert back to complex form
		cpc.polarToComplex( dataFrame );
		fftEngine.realInverse( dataFrame.complexFrame[0], true );

		final TestWaveFileWriter outWrite = new TestWaveFileWriter( REVERSEFFTSQUARE_WAV, 1, sampleRate, numExpectedFrames );

		final float[] normVersion = new float[ fftRealLength ];
		Arrays.fill( normVersion, 0.0f );
		System.arraycopy( dataFrame.complexFrame[0], 0, normVersion, 0, fftRealLength );

		LookupTableUtils.normaliseFloatsToMax( normVersion, 0, fftRealLength, MAX_VALUE_FOR_NORMALISE_F );
		fArray[0] = normVersion;

		for( int s = 0 ; s < numToConcatenate ; ++s )
		{
			outWrite.writeFloats( fArray, 0, fftRealLength );
		}

		outWrite.close();
	}

	public void generateTestOutputDouble() throws Exception
	{
		final int numBins = stftParams.getNumBins();
		final int fftComplexArraySize = stftParams.getComplexArraySize();
		log.debug("Source fftRealLength is " + fftRealLength );
		log.debug("The fft will have " + numBins + " bins");
		log.debug("The fft will have " + fftComplexArraySize + " float results as output");

		final DoubleFFT_1D fftEngine = new DoubleFFT_1D( fftRealLength );

		final StftDataFrameDouble dataFrame = new StftDataFrameDouble( 1, fftRealLength,
				fftComplexArraySize, numBins );
		Arrays.fill( dataFrame.amps[0], 0.0 );
		Arrays.fill( dataFrame.phases[0], 0.0 );
		Arrays.fill( dataFrame.complexFrame[0], 0.0 );

		final int startBin = 1;

		final RawLookupTable harmonics = waveTableGenerator.getHarmonics( numHarmonics );
		final double phase = waveTableGenerator.getPhase() * MathDefines.TWO_PI_D;

		// What's used in the fourier generator
		for( int i = 0 ; i < numHarmonics ; ++i )
		{
			// Every other bin gets a peak
			final int harmonicIndex = i;
			final int binToFill = startBin + harmonicIndex;
			final double ampOfBin = harmonics.floatBuffer[ harmonicIndex ] * (fftRealLength / 2);

			dataFrame.amps[0][binToFill] = ampOfBin;
			dataFrame.phases[0][binToFill] = phase;
		}

		// Convert back to complex form
		cpc.polarToComplexDouble( dataFrame );
		fftEngine.realInverse( dataFrame.complexFrame[0], true );

		final TestWaveFileWriter outWrite = new TestWaveFileWriter( REVERSEFFTSQUAREDOUBLE_WAV, 1, sampleRate, numExpectedFrames );

		final double[] normVersion = new double[ fftRealLength ];
		Arrays.fill( normVersion, 0.0f );
		System.arraycopy( dataFrame.complexFrame[0], 0, normVersion, 0, fftRealLength );

		LookupTableUtils.normaliseDoublesToMax( normVersion, 0, fftRealLength, MAX_VALUE_FOR_NORMALISE_D );
		dArray[0] = normVersion;

		for( int s = 0 ; s < numToConcatenate ; ++s )
		{
			outWrite.writeDoubles( dArray, 0, fftRealLength );
		}

		outWrite.close();

	}

	public static void main( final String[] args ) throws Exception
	{
		JTransformsConfigurator.setThreadingLowerBound( 256 * 1024 );

		final TestReverseFftBandLimitedWaveform t = new TestReverseFftBandLimitedWaveform();
		t.analyseAdditiveFourierVersion();
		t.generateTestOutput();
		t.generateTestOutputDouble();
	}

}
