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

package uk.co.modularaudio.util.audio.fileio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.format.FloatToByteConverter;

@SuppressWarnings("unused")
public class WaveFileWriter
{
	private static Log log = LogFactory.getLog( WaveFileWriter.class.getName() );

	private final String outputFilePath;

	private final FileOutputStream fos;
//	private BufferedOutputStream bos;
	private RandomAccessFile raf;

	private final int internalFloatBufferLength;
	private final byte[] internalByteBuffer;

	private final int numChannels;

	private long chunksize2;

	private final int sampleRate;
	private short bitsPerSample = 16; // NOPMD by dan on 29/01/15 16:25

	public WaveFileWriter( final String outputFilePath, final int numChannels, final int sampleRate, final short bitsPerSample  ) // NOPMD by dan on 29/01/15 16:25
			throws IOException
	{
		this( outputFilePath, numChannels, sampleRate, bitsPerSample, WaveFileDefines.FLOAT_BUFFER_LENGTH );
	}

	public WaveFileWriter( final String outputFilePath, final int numChannels, final int sampleRate, final short bitsPerSample, final int internalFloatBufferLength  ) // NOPMD by dan on 29/01/15 16:25
			throws IOException
	{
		this.internalFloatBufferLength = internalFloatBufferLength;
		this.sampleRate = sampleRate;
		this.bitsPerSample = bitsPerSample;

		internalByteBuffer = new byte[ internalFloatBufferLength * 2];

		this.outputFilePath = outputFilePath;
		fos = new FileOutputStream( outputFilePath );
//		bos = new BufferedOutputStream( fos );
		this.numChannels = numChannels;
		writeHeader();
	}

	private void writeHeader() throws IOException
	{
		final int formatChunkSize = 16;
		// int headerSize = 8;
		final short formatType = 1; // NOPMD by dan on 29/01/15 16:25
		final int byteRate = sampleRate * numChannels * ((bitsPerSample + 7)/ 8);
		final short blockAlign = (short) (numChannels * ((bitsPerSample + 7) / 8)); // NOPMD by dan on 29/01/15 16:25
		// int waveSize = 2;
		// int data = 0x61746164;
		// Will seek back to here and write after we are done.
		final int fileSize = 0;

		writeInt( WaveFileDefines.RIFF_CHUNK_ID );
		writeInt( fileSize );
		writeInt( WaveFileDefines.RIFF_TYPE_ID );
		writeInt( WaveFileDefines.FMT_CHUNK_ID );
		writeInt( formatChunkSize );
		writeShort( formatType );
		writeShort( (short) numChannels ); // NOPMD by dan on 29/01/15 16:25
		writeInt( sampleRate );
		writeInt( byteRate );
		writeShort( blockAlign );
		writeShort( bitsPerSample );
		writeInt( WaveFileDefines.DATA_CHUNK_ID );
		final int emptyChunkSize = 0;
		writeInt( emptyChunkSize );
		// Now the data....
	}

	private void writeInt( final int val ) throws IOException
	{
		final byte b1 = (byte) (val & 0xFF);
		fos.write( b1 );
		final byte b2 = (byte) (val >> 8 & 0xFF);
		fos.write( b2 );
		final byte b3 = (byte) (val >> 16 & 0xFF);
		fos.write( b3 );
		final byte b4 = (byte) (val >> 24 & 0xFF);
		fos.write( b4 );
	}

	private void writeShort( final short val ) throws IOException // NOPMD by dan on 29/01/15 16:25
	{
		// A short is 16 bits
		final byte b1 = (byte) (val & 0xFF);
		fos.write( b1 );
		final byte b2 = (byte) (val >> 8 & 0xFF);
		fos.write( b2 );
	}

	public void writeFloats( final float[] data, final int length ) throws IOException
	{
		writeFloats( data, 0, length );
	}

	public void writeFloats( final float[] data, final int readArrayOffset, final int length ) throws IOException
	{
		int numLeft = length;
		int readPosition = readArrayOffset;
		while (numLeft > 0)
		{
			final int numThisRound = (numLeft > internalFloatBufferLength ? internalFloatBufferLength : numLeft);
			FloatToByteConverter.floatToByteConversion( data, readPosition,
					numThisRound, internalByteBuffer, 0, false );
			final int numBytesThisRound = numThisRound * 2;
			fos.write( internalByteBuffer, 0, numBytesThisRound );
			chunksize2 += numBytesThisRound;
			readPosition += numThisRound;
			numLeft -= numThisRound;
		}
	}

	private void rafWriteInt( final int val ) throws IOException
	{
		final byte b1 = (byte) (val & 0xFF);
		raf.write( b1 );
		final byte b2 = (byte) (val >> 8 & 0xFF);
		raf.write( b2 );
		final byte b3 = (byte) (val >> 16 & 0xFF);
		raf.write( b3 );
		final byte b4 = (byte) (val >> 24 & 0xFF);
		raf.write( b4 );
	}

	public void close() throws IOException
	{
		fos.close();
		raf = new RandomAccessFile( new File( outputFilePath ), "rw" );
		raf.seek( WaveFileDefines.OFFSET_FOR_CHUNKSIZE );
		final int chunksizeValue = (int) chunksize2 + 36;
		rafWriteInt( chunksizeValue );
		raf.seek( WaveFileDefines.OFFSET_FOR_CHUNKSIZE2 );
		rafWriteInt( (int) chunksize2 );
		raf.close();
	}
}
