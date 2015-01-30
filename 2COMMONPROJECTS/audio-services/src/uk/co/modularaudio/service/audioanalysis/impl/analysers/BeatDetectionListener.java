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

package uk.co.modularaudio.service.audioanalysis.impl.analysers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.beatdetection.BeatDetectionRT;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.beatdetection.BeatDetector;
import uk.co.modularaudio.service.hashedstorage.HashedRef;
import uk.co.modularaudio.util.audio.format.DataRate;

public class BeatDetectionListener implements AnalysisListener
{
	private static Log log = LogFactory.getLog( BeatDetectionListener.class.getName() );

	private BeatDetectionRT rt;
	private BeatDetector detector;
//	private int numChannels = -1;
	private final int DEFAULT_WIN_LEN = 1024;

	private int numStored;
	private float[] btIn;
	private float[] btOut;

//	private long currentPosition = 0;

	public BeatDetectionListener()
	{
	}

	@Override
	public void start(final DataRate dataRate, final int numChannels, final long totalFloatsLength)
	{
//		this.numChannels = numChannels;
		// Currently beat detection is mono and uses just left channel
		rt = new BeatDetectionRT( numChannels, DEFAULT_WIN_LEN );
		btIn = new float[ DEFAULT_WIN_LEN ];
		btOut = new float[ DEFAULT_WIN_LEN ];
		detector = new BeatDetector();

		// Reset any state we might need
//		currentPosition = 0;
		numStored = 0;
	}

	@Override
	public void receiveData(final float[] data, final int dataLength )
	{
		int numRead = dataLength;

		// Beat detector requires that winlen floats are passed in one pop
		// So we check to see how many we have in the internal array and fill it up until full, then call the beat detector
		while( numRead > 0 )
		{
			if( numStored < DEFAULT_WIN_LEN && numRead > 0)
			{
//				log.debug("Filling data");
				final int numToFill = DEFAULT_WIN_LEN - numStored;
				final int numToStore = numRead / 2 > numToFill ? numToFill : numRead / 2;
//				System.arraycopy( data, dataLength - numRead, btIn, numStored, numToStore );
				// Only push in every other sample - quick hack to get around multi-channel stuff
				for( int i = 0 ; i < numToStore ; i++ )
				{
					final int dataIndex = dataLength - numRead + (i * 2);
					final int outputIndex = numStored + i;
					btIn[ outputIndex] = data[ dataIndex ];
				}
				numStored += numToStore;
				numRead -= numToStore * 2;
			}

			if( numStored == DEFAULT_WIN_LEN )
			{
//				log.debug("Pushing data");
				// Push to BT
				// It's one channel of data
				detector.detect( rt, btIn, btOut );
				numStored = 0;
				storeBpmAndConfidence();
			}
		}

	}

	@Override
	public void end()
	{
		storeBpmAndConfidence();

		debugBpmResults();
	}

	private void debugBpmResults()
	{
		while( bpmResults.size() > 50 )
		{
			bpmResults.remove( bpmResults.size() - 1 );
		}

		Collections.sort( bpmResults );
		for( final BpmResult bpmResult : bpmResults )
		{
			if( log.isDebugEnabled() )
			{
				log.debug("Bpm Result: " + bpmResult.toString());
			}
		}
	}

	private final List<BpmResult> bpmResults = new ArrayList<BpmResult>();

	private void storeBpmAndConfidence()
	{
		final float detectedBpm = rt.getBpm();
		if( detectedBpm != 0.0 )
		{
			final float origConfidence = rt.getConfidence();
			// Check to see if we can add the confidence into an existing confidence entry
			final boolean didCum = false;
			for( final BpmResult result : bpmResults )
			{
				if( result.bpm == detectedBpm )
				{
					if( log.isDebugEnabled())
					{
						log.debug("Cumulative bpm confidence at " + detectedBpm);
					}
					result.confidence += origConfidence;
				}
			}

			if( !didCum )
			{
				final BpmResult newResult = new BpmResult();
				newResult.bpm = detectedBpm;
				newResult.confidence = origConfidence;

				bpmResults.add( newResult );
			}
			Collections.sort( bpmResults );
		}
	}

	@Override
	public void updateAnalysedData( final AnalysedData analysedData, final HashedRef hashedRef )
	{
		if( bpmResults.size() > 0 )
		{
			// use the first one
			final BpmResult highestMatch = bpmResults.get( 0 );
			analysedData.setDetectedBpm( (float) highestMatch.bpm );
			final long[] detectedBeatPositions = new long[0];
			analysedData.setDetectedBeatPositions( detectedBeatPositions );
		}

	}

	private class BpmResult implements Comparable<BpmResult>
	{
		double confidence = 0.0;
		double bpm = 0.0;
		@Override
		public int compareTo(final BpmResult o)
		{
			final double diff = o.confidence - confidence;
			if( diff < 0.0 )
			{
				return -1;
			}
			else if( diff > 0.0 )
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}

		@Override
		public String toString()
		{
			return "Bpm (" + bpm + ") Confidence (" + confidence + ")";
		}
	}
}
