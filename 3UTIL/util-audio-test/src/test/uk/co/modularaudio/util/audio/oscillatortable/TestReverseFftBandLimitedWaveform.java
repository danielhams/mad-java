package test.uk.co.modularaudio.util.audio.oscillatortable;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jtransforms.fft.FloatFFT_1D;

import uk.co.modularaudio.util.audio.fft.HannFftWindow;
import uk.co.modularaudio.util.audio.fft.JTransformsConfigurator;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.lookuptable.LookupTableUtils;
import uk.co.modularaudio.util.audio.lookuptable.raw.RawLookupTable;
import uk.co.modularaudio.util.audio.oscillatortable.CubicPaddedRawWaveTable;
import uk.co.modularaudio.util.audio.oscillatortable.SquareRawWaveTableGenerator;
import uk.co.modularaudio.util.audio.stft.StftDataFrame;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.tools.ComplexPolarConverter;
import uk.co.modularaudio.util.math.MathDefines;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestReverseFftBandLimitedWaveform
{
	private static Log log = LogFactory.getLog( TestReverseFftBandLimitedWaveform.class.getName());

	final int fftRealLength = 256;

	final HannFftWindow fftWindow = new HannFftWindow( fftRealLength );

	final StftParameters stftParams;

	final ComplexPolarConverter cpc;

	private final int numHarmonics = 30;

	private final int numToConcatenate = 4096;

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

	public void doIt() throws Exception
	{
		final SquareRawWaveTableGenerator waveTableGenerator = new SquareRawWaveTableGenerator();
//		final SineRawWaveTableGenerator waveTableGenerator = new SineRawWaveTableGenerator();
		final CubicPaddedRawWaveTable waveTable = waveTableGenerator.reallyGenerateWaveTable( fftRealLength,
				numHarmonics );

		final int waveTableLength = waveTable.buffer.length - CubicPaddedRawWaveTable.NUM_EXTRA_SAMPLES_IN_BUFFER;

		final int numBins = stftParams.getNumBins();
		final int fftSize = stftParams.getNumReals();
		final int fftComplexArraySize = stftParams.getComplexArraySize();
		log.debug("Source wavetable length is " + waveTableLength );
		log.debug("The fft will have " + numBins + " bins");
		log.debug("The fft size will be " + fftSize );
		log.debug("The fft will have " + fftComplexArraySize + " float results as output");

		final FloatFFT_1D fftEngine = new FloatFFT_1D( fftSize );

		final StftDataFrame dataFrame = new StftDataFrame( 1, fftSize, fftComplexArraySize, numBins );
		Arrays.fill( dataFrame.complexFrame[0], 0.0f );
		System.arraycopy( waveTable.buffer, 1, dataFrame.complexFrame[0], 0, waveTableLength );

		fftEngine.realForward( dataFrame.complexFrame[0] );

		cpc.complexToPolar( dataFrame );

		final int numBinsToDisplay = 60;
		for( int b = 0 ; b < numBinsToDisplay ; ++b )
		{
			log.debug("Bin " + b + " of freq " + MathFormatter.fastFloatPrint((stftParams.getFreqPerBin() * b), 8, false ) +
				" amp=" + MathFormatter.slowFloatPrint( dataFrame.amps[0][b], 8, true ) +
				" phase=" + MathFormatter.slowFloatPrint( dataFrame.phases[0][b], 8, true ) );
		}

		final WaveFileWriter outWrite = new WaveFileWriter( "wavegenerator.wav", 1, DataRate.CD_QUALITY.getValue(), (short)16);

		final float[] normVersion = new float[ waveTableLength ];
		Arrays.fill( normVersion, 0.0f );
		System.arraycopy( waveTable.buffer, 1, normVersion, 0, waveTableLength );
		LookupTableUtils.normaliseFloats( normVersion, 0, waveTableLength );

		for( int s = 0 ; s < numToConcatenate ; ++s )
		{
			outWrite.writeFloats( normVersion, waveTableLength );
		}

		outWrite.close();
	}

	public void generateTestOutput() throws Exception
	{
		final int waveTableLength = stftParams.getNumReals();

		final int numBins = stftParams.getNumBins();
		final int fftComplexArraySize = stftParams.getComplexArraySize();
		log.debug("Source wavetable length is " + waveTableLength );
		log.debug("The fft will have " + numBins + " bins");
		log.debug("The fft will have " + fftComplexArraySize + " float results as output");

		final FloatFFT_1D fftEngine = new FloatFFT_1D( waveTableLength );

		final StftDataFrame dataFrame = new StftDataFrame( 1, waveTableLength, fftComplexArraySize, numBins );
		Arrays.fill( dataFrame.amps[0], 0.0f );
		Arrays.fill( dataFrame.phases[0], 0.0f );
		Arrays.fill( dataFrame.complexFrame[0], 0.0f );

		final int startBin = 1;

		final RawLookupTable harmonics = new RawLookupTable( numHarmonics, true );
		for( int i = 0 ; i < numHarmonics ; i+=2 )
		{
			harmonics.floatBuffer[i] = 1.0f / (i + 1);
		}

		// What's used in the fourier generator
		final float phaseOfBins = MathDefines.TWO_PI_F * -0.25f;
		for( int i = 0 ; i < numHarmonics ; ++i )
		{
			// Every other bin gets a peak
			final int harmonicIndex = i;
			final int binToFill = startBin + harmonicIndex;
			final float ampOfBin = harmonics.floatBuffer[ harmonicIndex ] * 35362.0f;

			dataFrame.amps[0][binToFill] = ampOfBin;
			dataFrame.phases[0][binToFill] = phaseOfBins;
		}

		// Convert back to complex form
		cpc.polarToComplex( dataFrame );
		fftEngine.realInverse( dataFrame.complexFrame[0], true );

		final WaveFileWriter outWrite = new WaveFileWriter( "reversefftsquare.wav", 1, DataRate.CD_QUALITY.getValue(), (short)16);

		final float[] normVersion = new float[ waveTableLength ];
		Arrays.fill( normVersion, 0.0f );
		System.arraycopy( dataFrame.complexFrame[0], 0, normVersion, 0, waveTableLength );
		LookupTableUtils.normaliseFloats( normVersion, 0, waveTableLength );

		for( int s = 0 ; s < numToConcatenate ; ++s )
		{
			outWrite.writeFloats( normVersion, waveTableLength );
		}

		outWrite.close();
	}

	public static void main( final String[] args ) throws Exception
	{
		JTransformsConfigurator.setThreadingLowerBound( 256 * 1024 );

		final TestReverseFftBandLimitedWaveform t = new TestReverseFftBandLimitedWaveform();
		t.doIt();
		t.generateTestOutput();
	}

}
