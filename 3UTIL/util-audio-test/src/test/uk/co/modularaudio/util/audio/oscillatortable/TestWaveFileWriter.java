package test.uk.co.modularaudio.util.audio.oscillatortable;

import java.io.IOException;

import uk.co.modularaudio.util.audio.fileio.WavFileException;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;


public class TestWaveFileWriter
{
	private final WaveFileWriter myWriter;

	public TestWaveFileWriter( final String outFilename, final int numChannels, final int sampleRate, final long numFrames )
			throws IOException
	{
		if( numChannels != 1 )
		{
			throw new IOException("Only one channel supported");
		}
		myWriter = new WaveFileWriter( outFilename, numChannels, sampleRate, (short)32 );
	}

	public void writeFloats( final float[][] source, final int offset, final int length ) throws IOException, WavFileException
	{
		myWriter.writeFrames( source[0], offset, length );
	}

	public void writeDoubles( final double[][] source, final int offset, final int length ) throws IOException, WavFileException
	{
		myWriter.writeFramesDoubles( source[0], offset, length );
	}

	public void close() throws IOException
	{
		myWriter.close();
	}
}
