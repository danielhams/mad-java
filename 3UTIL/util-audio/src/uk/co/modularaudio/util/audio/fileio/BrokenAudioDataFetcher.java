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
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.spi.mpeg.sampled.convert.DecodedMpegAudioInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.sound.spi.Flac2PcmAudioInputStream;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import uk.co.modularaudio.util.audio.format.FloatToByteConverter;


public class BrokenAudioDataFetcher implements IAudioDataFetcher
{
	// 4K byte buffer size
	protected static final int BYTE_BUFFER_SIZE = 4 * 1024;

	private static Log log = LogFactory.getLog( BrokenAudioDataFetcher.class.getName() );

	private File inputFile = null;

	private int numChannels = -1;
	private long numTotalFrames = -1;
	private long numTotalFloats = -1;
	private int numBytesPerFrame = -1;
	private int numFloatsPerFrame = -1;
	private float frameRate = -1;
	private int sampleRate = -1;
	private int sampleSizeInBits = -1;
	private Encoding encoding = Encoding.PCM_SIGNED;
	private boolean bigEndian = false;

	private AudioFormat desiredAudioFormat = null;
	private AudioInputStream rawAudioInputStream = null;
	private AudioInputStream desiredAudioInputStream = null;
	private long currentReadPosition = 0;

	private DetectedFormat detectedFormat = DetectedFormat.NONE;

	public BrokenAudioDataFetcher()
	{
	}

	@Override
	public void open( final File inputFile )
			throws UnsupportedAudioFileException, IOException
	{
		log.debug("Beginning open");
		this.inputFile = inputFile;

		// Now get the total number of frames in the file in this format and setup an
		// AudioInputStream that has the appropriate format
		// We use a temporary input stream as some of the SPI code forces us to read a little
		// to be able to get enough meta data to calculate the total length.
		AudioInputStream tmpRawInputStream = null;
		AudioInputStream tmpDesiredAudioInputStream = null;
		try
		{
			tmpRawInputStream = AudioSystem.getAudioInputStream( inputFile );

			final AudioFormat rawFormat = tmpRawInputStream.getFormat();

			boolean isCompressedFormat = false;
			this.frameRate = rawFormat.getFrameRate();
			if( frameRate <= 0 )
			{
				isCompressedFormat = true;
			}
			this.sampleRate = (int)rawFormat.getSampleRate();
			this.numChannels = rawFormat.getChannels();
			this.numBytesPerFrame = rawFormat.getFrameSize();
			if( numBytesPerFrame <= 0 )
			{
				isCompressedFormat = true;
			}
			this.numFloatsPerFrame = numBytesPerFrame / 2;
			this.sampleSizeInBits = rawFormat.getSampleSizeInBits();
			if( sampleSizeInBits <= 0 )
			{
				isCompressedFormat = true;
			}
			this.encoding = rawFormat.getEncoding();
			this.bigEndian = rawFormat.isBigEndian();

			if( isCompressedFormat )
			{
				// Reset the other parts to something sensible
				// For now this means 16 bit little endian signed
				// Since for java, we're not really that fussed
				// about extreme quality.
				this.frameRate = sampleRate;
				this.numBytesPerFrame = 4;
				this.numFloatsPerFrame = numBytesPerFrame / 2;
				this.sampleSizeInBits = 16;
				this.encoding = Encoding.PCM_SIGNED;
				this.bigEndian = false;
			}

			// Ask for same for decoded format
			this.desiredAudioFormat = new AudioFormat( encoding,
					sampleRate,
					sampleSizeInBits,
					numChannels,
					numBytesPerFrame,
					frameRate,
					bigEndian );

			tmpDesiredAudioInputStream = AudioSystem.getAudioInputStream( desiredAudioFormat, tmpRawInputStream );
			final AudioFileFormat aff = AudioSystem.getAudioFileFormat( inputFile );

			this.numTotalFrames = tmpDesiredAudioInputStream.getFrameLength();
			if( numTotalFrames < 0 )
			{
				// Total length comes from various places depending on the audio format
				if (aff instanceof TAudioFileFormat) {
					// It's Ogg?
					final Map<?, ?> properties = ((TAudioFileFormat)aff).properties();
					final String key = "duration";
					final Long microseconds = (Long) properties.get(key);
					//			        if( DecodedVorbisAudioInputStream.class.isInstance( tmpDesiredAudioInputStream ) )
					//			        {
					//			        	log.debug("It's ogg - changing the endianess");
					//			        	boolean currentEndianSetting = desiredAudioFormat.isBigEndian();
					//			        	desiredAudioFormat = new AudioFormat( desiredAudioFormat.getSampleRate(), desiredAudioFormat.getSampleSizeInBits(), desiredAudioFormat.getChannels(), true, currentEndianSetting == false );
					//			        }
					// Compute the length from the time
					this.numTotalFrames = (long)( (microseconds / 1000000.0f) * frameRate );
					this.numTotalFloats = numTotalFrames * numChannels;
					if( tmpDesiredAudioInputStream instanceof DecodedMpegAudioInputStream )
					{
						detectedFormat = DetectedFormat.MP3;
					}
				}
				/**/
				else if( tmpDesiredAudioInputStream instanceof Flac2PcmAudioInputStream )
				{
					// Flac
					// Have to read some of the stream to force flac to pull in the stream info
					tmpDesiredAudioInputStream.mark( Integer.MAX_VALUE );
					final byte[] flacFetchHack = new byte[256];
					tmpDesiredAudioInputStream.read( flacFetchHack );
					final Flac2PcmAudioInputStream flacAis = (Flac2PcmAudioInputStream)tmpDesiredAudioInputStream;
					final StreamInfo si = flacAis.getStreamInfo();
					this.numTotalFrames = si.getTotalSamples();
					detectedFormat = DetectedFormat.FLAC;
				}
				/**/
				else
				{
					this.numTotalFrames = tmpRawInputStream.getFrameLength();
					//			        throw new UnsupportedAudioFileException();
				}
			}

			numTotalFloats = numTotalFrames * numFloatsPerFrame;

			// Ok, now open the real desired input stream
			openNewAudioStreamInstance( desiredAudioFormat );
		}
		finally
		{
			if( tmpDesiredAudioInputStream != null )
			{
				tmpDesiredAudioInputStream.close();
				tmpDesiredAudioInputStream = null;
			}

			if( tmpRawInputStream != null )
			{
				tmpRawInputStream.close();
				tmpRawInputStream = null;
			}
		}
		log.debug("Open completed");
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.util.audio.saf.blockpop.IAudioDataFetcher#open(javax.sound.sampled.AudioFormat, java.io.File)
	 */
	@Override
	public void open( final AudioFormat desiredAudioFormat, final File inputFile )
			throws UnsupportedAudioFileException, IOException
	{
		log.debug("Beginning open");
		this.inputFile = inputFile;
		this.desiredAudioFormat = desiredAudioFormat;

		this.frameRate = desiredAudioFormat.getFrameRate();
		this.sampleRate = (int)frameRate;
		this.numChannels = desiredAudioFormat.getChannels();
		this.numBytesPerFrame = desiredAudioFormat.getFrameSize();
		this.numFloatsPerFrame = numBytesPerFrame / 2;

		// Now get the total number of frames in the file in this format and setup an
		// AudioInputStream that has the appropriate format
		// We use a temporary input stream as some of the SPI code forces us to read a little
		// to be able to get enough meta data to calculate the total length.
		AudioInputStream tmpRawInputStream = null;
		AudioInputStream tmpDesiredAudioInputStream = null;
		try
		{
			tmpRawInputStream = AudioSystem.getAudioInputStream( inputFile );
			tmpDesiredAudioInputStream = AudioSystem.getAudioInputStream( desiredAudioFormat, tmpRawInputStream );
			final AudioFileFormat aff = AudioSystem.getAudioFileFormat( inputFile );

			this.numTotalFrames = tmpDesiredAudioInputStream.getFrameLength();
			if( numTotalFrames < 0 )
			{
				// Total length comes from various places depending on the audio format
				if (aff instanceof TAudioFileFormat) {
					// It's Ogg?
					final Map<?, ?> properties = ((TAudioFileFormat)aff).properties();
					final String key = "duration";
					final Long microseconds = (Long) properties.get(key);
					//			        if( DecodedVorbisAudioInputStream.class.isInstance( tmpDesiredAudioInputStream ) )
					//			        {
					//			        	log.debug("It's ogg - changing the endianess");
					//			        	boolean currentEndianSetting = desiredAudioFormat.isBigEndian();
					//			        	desiredAudioFormat = new AudioFormat( desiredAudioFormat.getSampleRate(), desiredAudioFormat.getSampleSizeInBits(), desiredAudioFormat.getChannels(), true, currentEndianSetting == false );
					//			        }
					// Compute the length from the time
					this.numTotalFrames = (long)( (microseconds / 1000000.0f) * frameRate );
					if( tmpDesiredAudioInputStream instanceof DecodedMpegAudioInputStream )
					{
						detectedFormat = DetectedFormat.MP3;
					}
				}
				/**/
				else if( tmpDesiredAudioInputStream instanceof Flac2PcmAudioInputStream )
				{
					// Flac
					// Have to read some of the stream to force flac to pull in the stream info
					tmpDesiredAudioInputStream.mark( Integer.MAX_VALUE );
					final byte[] flacFetchHack = new byte[256];
					tmpDesiredAudioInputStream.read( flacFetchHack );
					final Flac2PcmAudioInputStream flacAis = (Flac2PcmAudioInputStream)tmpDesiredAudioInputStream;
					final StreamInfo si = flacAis.getStreamInfo();
					this.numTotalFrames = si.getTotalSamples();
					detectedFormat = DetectedFormat.FLAC;
				}
				/**/
				else
				{
					this.numTotalFrames = tmpRawInputStream.getFrameLength();
					//			        throw new UnsupportedAudioFileException();
				}
			}

			numTotalFloats = numTotalFrames * numFloatsPerFrame;

			// Ok, now open the real desired input stream
			openNewAudioStreamInstance( desiredAudioFormat );
		}
		finally
		{
			if( tmpDesiredAudioInputStream != null )
			{
				tmpDesiredAudioInputStream.close();
				tmpDesiredAudioInputStream = null;
			}

			if( tmpRawInputStream != null )
			{
				tmpRawInputStream.close();
				tmpRawInputStream = null;
			}
		}
		log.debug("Open completed");
	}

	private void openNewAudioStreamInstance( final AudioFormat desiredAudioFormat )
			throws UnsupportedAudioFileException, IOException
	{
		this.rawAudioInputStream = AudioSystem.getAudioInputStream( inputFile );
		this.desiredAudioInputStream = AudioSystem.getAudioInputStream( desiredAudioFormat, rawAudioInputStream );
		this.currentReadPosition = 0;
	}

	private final byte[] tmpByteBuf = new byte[ BYTE_BUFFER_SIZE ];

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.util.audio.saf.blockpop.IAudioDataFetcher#read(float[], int, long, int)
	 */
	@Override
	public int read(final float[] destBuf, final int destOffset, final long startPos, final int length)
			throws IOException, ArrayIndexOutOfBoundsException, UnsupportedAudioFileException
	{
		//		log.debug("Attempting to read from(" + startPos + ") to(" + (startPos + length ) +")");
		int numBytesRead = 0;
		boolean endOfFile = false;

		if( startPos > numTotalFloats )
		{
			throw new ArrayIndexOutOfBoundsException();
		}
		else
		{
			if( currentReadPosition > startPos )
			{
				close();
				openNewAudioStreamInstance(desiredAudioFormat);
			}
			// Check if we can skip with current input stream
			final long numToSkip =  startPos - currentReadPosition;
			if( numToSkip < 0 )
			{
				// Shouldn't happen
				log.error("Oops - shouldn't get here!");
			}
			else if( numToSkip > 0 )
			{
				if( desiredAudioInputStream instanceof DecodedMpegAudioInputStream )
				{
					log.error( "Is MP3 input stream so refusing to do any skip" );
					return -1;
				}
				final long numToSkipInBytes = numToSkip * 2;
				if( log.isDebugEnabled() )
				{
					log.debug("Skipping " + numToSkipInBytes + " bytes = " + numToSkip + " floats");
				}
				long numLeftToSkip = numToSkipInBytes;
				//				long numSkipped = 0;

				long numSkippedThisTime = 1;
				while( numSkippedThisTime > 0 && numLeftToSkip > 0 )
				{
					numSkippedThisTime = desiredAudioInputStream.skip( numLeftToSkip );
					numLeftToSkip -= numSkippedThisTime;
					//					numSkipped += numSkippedThisTime;
				}
				if( numLeftToSkip < 0 || numLeftToSkip > 0)
				{
					// Over skipped
					log.debug("Over skip happened.");
				}
			}
			int numBytesToRead = length * 2;
			int curOutputPointer = 0;

			while( !endOfFile && numBytesToRead > 0 )
			{
				final int numBytesToReadThisTime = (numBytesToRead < BYTE_BUFFER_SIZE ? numBytesToRead : BYTE_BUFFER_SIZE );
				int bytesRead = desiredAudioInputStream.read( tmpByteBuf, 0, numBytesToReadThisTime );
				final int numFloatsRead = bytesRead / 2;
				if( bytesRead == -1 )
				{
					// End of file I'm guessing
					bytesRead = 0;
					endOfFile = true;
				}
				else if( bytesRead > 0 )
				{
					numBytesToRead -= bytesRead;
					final boolean isBigEndian = desiredAudioFormat.isBigEndian();
					FloatToByteConverter.byteToFloatConversion( tmpByteBuf, 0, destBuf, destOffset + curOutputPointer, numFloatsRead, isBigEndian );
					curOutputPointer += numFloatsRead;
					numBytesRead += bytesRead;
				}
			}
		}
		// Now set the current position
		final int numFloatsRead = numBytesRead / 2;
		currentReadPosition = startPos + numFloatsRead;

		if( numBytesRead == 0 && endOfFile )
		{
			// Re-open an input stream as this one is done
			close();
			openNewAudioStreamInstance( desiredAudioFormat );
			return -1;
		}
		else
		{
			return numFloatsRead;
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.util.audio.saf.blockpop.IAudioDataFetcher#close()
	 */
	@Override
	public void close()
	{
		if( desiredAudioInputStream != null )
		{
			try
			{
				desiredAudioInputStream.close();
				desiredAudioInputStream = null;
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}

		if( rawAudioInputStream != null )
		{
			try
			{
				rawAudioInputStream.close();
				rawAudioInputStream = null;
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.util.audio.saf.blockpop.IAudioDataFetcher#getNumTotalFloats()
	 */
	@Override
	public long getNumTotalFloats()
	{
		return numTotalFloats;
	}

	@Override
	public DetectedFormat getDetectedFormat()
	{
		return detectedFormat;
	}

	@Override
	public int getNumChannels()
	{
		return numChannels;
	}

	@Override
	public int getSampleRate()
	{
		return sampleRate;
	}
}
