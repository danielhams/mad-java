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
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.io.FloatToByteConverter;

@SuppressWarnings("unused")
public class WaveFileReader
{
	private static Log log = LogFactory.getLog( WaveFileReader.class.getName() );
	
	private final String inputFilePath;
	private RandomAccessFile raf;
	
	private int internalFloatBufferLength = 0;
	private byte[] internalByteBuffer = null;

	private int numChannels = 0;
	private long numTotalFrames = 0;
	private long numTotalFloats = 0;
	
	private int fileSize = -1;
	
	private int formatChunkSize = 0;
	private short formatType = 0;
	private int byteRate = 0;
	private short blockAlign = 0;
	private int dataChunkSize = 0;
	private long dataChunkOffset = 0;
	private int sampleRate = 0;
	private short bitsPerSample = 0;
	
	public WaveFileReader( String inputFilePath )
			throws IOException
	{
		this( inputFilePath, WaveFileDefines.FLOAT_BUFFER_LENGTH );
	}
	
	public WaveFileReader( String inputFilePath, int internalFloatBufferLength )
			throws IOException
	{
		this.internalFloatBufferLength = internalFloatBufferLength;
		this.internalByteBuffer = new byte[ internalFloatBufferLength * 2 ];
		this.inputFilePath = inputFilePath;
		raf = new RandomAccessFile( new File( inputFilePath ), "r" );
		readHeader();
	}
	
	public void close()
	{
		if( raf != null )
		{
			try
			{
				raf.close();
			}
			catch (IOException ioe)
			{
				String msg = "IOException caught closing raf input stream: " + ioe.toString();
				log.error( msg, ioe );
			}
			raf = null;
		}
	}

	private void readHeader()
		throws IOException
	{
		int riffChunkId = readInt();
		if( riffChunkId != WaveFileDefines.RIFF_CHUNK_ID )
		{
			throw new IOException("File is not a WAV file (chunk ID mismatch)");
		}
		fileSize = readInt();
		int riffTypeId = readInt();
		if( riffTypeId != WaveFileDefines.RIFF_TYPE_ID )
		{
			throw new IOException("File is not a WAV file (RIFF TYPE ID mismatch)");
		}
		int fmtChunkId = readInt();
		if( fmtChunkId != WaveFileDefines.FMT_CHUNK_ID )
		{
			throw new IOException("File is not a WAV file (FMT CHUNK ID mismatch)");
		}
		formatChunkSize = readInt();
		formatType = readShort();
		numChannels = (int)readShort();
		sampleRate = readInt();
		byteRate = readInt();
		blockAlign = readShort();
		bitsPerSample = readShort();
		int nextChunkId = readInt();
		int amountSkipped = 0;
		while( nextChunkId != WaveFileDefines.DATA_CHUNK_ID )
		{
			char c0 = (char)( (nextChunkId & 0xff ) );
			char c1 = (char)( (nextChunkId & 0xff00 ) >> 8 );
			char c2 = (char)( (nextChunkId & 0xff0000 ) >> 16 );
			char c3 = (char)( (nextChunkId & 0xff000000 ) >> 24 );
//			StringBuilder sb = new StringBuilder();
//			sb.append( c0 );
//			sb.append( c1 );
//			sb.append( c2 );
//			sb.append( c3 );
//			log.debug("Found chunk to skip: " + sb.toString() );
			log.debug("Found chunk to skip: '" + c0 + "' '" + c1 + "' '" + c2 + "' '" + c3 + "'" );
			int sizeToSkip = readInt();
			if( sizeToSkip < 0 )
			{
				throw new IOException("Didn't find DATA CHUNK ID during chunk parsing.");
			}
			raf.skipBytes( sizeToSkip );
			amountSkipped += sizeToSkip + 4;
			nextChunkId = readInt();
		}
		
		int dataChunkId = nextChunkId;
		if( dataChunkId != WaveFileDefines.DATA_CHUNK_ID )
		{
			throw new IOException("File is not a WAV file (DATA CHUNK ID mismatch)");
		}
		dataChunkSize = readInt();
		dataChunkOffset = raf.getFilePointer();
		numTotalFloats = ( dataChunkSize / (bitsPerSample / 8));
		numTotalFrames = numTotalFloats / numChannels;
	}

	public int getNumChannels()
	{
		return numChannels;
	}

	public long getNumTotalFloats()
	{
		return numTotalFloats;
	}

	public long getNumTotalFrames()
	{
		return numTotalFrames;
	}

	public void read( float[] result, int resultStartIndex, long waveReadPosition, int numFloatsToRead )
		throws IOException
	{
		long seekPosition = dataChunkOffset + (waveReadPosition * 2);
		if( seekPosition != raf.getFilePointer() )
		{
			raf.seek( seekPosition );
		}
		
		int curOutputPos = resultStartIndex;
		int numFloatsLeft = numFloatsToRead;
		
		while( numFloatsLeft > 0 )
		{
			int numFloatsThisRound = (numFloatsLeft < internalFloatBufferLength ? numFloatsLeft : internalFloatBufferLength );
			int numBytesThisRound = numFloatsThisRound * 2;
			raf.read( internalByteBuffer, 0, numBytesThisRound );
			FloatToByteConverter.byteToFloatConversion( internalByteBuffer, 0, result, curOutputPos, numFloatsThisRound, false );
			
			curOutputPos += numFloatsThisRound;
			numFloatsLeft -= numFloatsThisRound;
		}
	}
	
	public void readFrames( float[] result, int outFrameStartIndex, long frameReadPosition, int numFramesToRead )
		throws IOException
	{
		long seekPosition = dataChunkOffset + (frameReadPosition * numChannels * 2 );
		if( seekPosition != raf.getFilePointer() )
		{
			raf.seek( seekPosition );
		}
		
		int curOutputPos = outFrameStartIndex * numChannels;
		int numFloatsLeft = numFramesToRead * numChannels;
		
		while( numFloatsLeft > 0 )
		{
			int numFloatsThisRound = (numFloatsLeft < internalFloatBufferLength ? numFloatsLeft : internalFloatBufferLength );
			int numBytesThisRound = numFloatsThisRound * 2;
			raf.read( internalByteBuffer, 0, numBytesThisRound );
			FloatToByteConverter.byteToFloatConversion( internalByteBuffer, 0, result, curOutputPos, numFloatsThisRound, false );
			
			curOutputPos += numFloatsThisRound;
			numFloatsLeft -= numFloatsThisRound;
		}
	}

	private int readInt() throws IOException
	{
		int b1 = raf.read();
		int b2 = raf.read();
		int b3 = raf.read();
		int b4 = raf.read();
		int retVal = (b1) | ((b2) << 8) | ((b3) << 16) | ((b4) << 24);
		return retVal;
	}
	
	private short readShort() throws IOException
	{
		int b1 = raf.read();
		int b2 = raf.read();
		int retVal  = (b1) | ((b2) << 8);
		return (short)retVal;
	}
}
