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

package uk.co.modularaudio.mads.base.sampleplayer.mu;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;

public class SamplePlayerStateProcessor
{
	private static final int MAX_SPEED = 4;
	
	private static Log log = LogFactory.getLog( SamplePlayerStateProcessor.class.getName() );
	
	private int numPeriodEntries = 0;
	private SamplePlayerPeriod[] periodEntries = null;
	
	private float[] emptyPeriodFloats = null;
	
	private SamplePlayerPeriodType previousPeriodType = SamplePlayerPeriodType.OFF;
	
	private SamplePlayerPeriodType lastOutputSamplesPeriodType = SamplePlayerPeriodType.OFF;

	public SamplePlayerStateProcessor( SingleSamplePlayerMadInstance instance,
			AdvancedComponentsFrontController advancedComponentsFrontController,
			long sampleRate, int periodLength )
	{

		int maxNumEntries = periodLength * 2 + 1;
		periodEntries = new SamplePlayerPeriod[ maxNumEntries ];
		for( int i = 0 ; i < maxNumEntries ; i++ )
		{
			periodEntries[ i ] = new SamplePlayerPeriod();
		}
		
		emptyPeriodFloats = new float[ periodLength ];
		Arrays.fill( emptyPeriodFloats, 0.0f );
	}
	
	public void processIncomingData(
			int numFrames,
			boolean gateConnected, MadChannelBuffer inGateBuffer,
			boolean inRetriggerConnected, MadChannelBuffer inRetriggerBuffer,
			boolean freqConnected, MadChannelBuffer inFreqBuffer,
			boolean ampConnected, MadChannelBuffer inAmpBuffer )
	{

		if( !gateConnected )
		{
			// Gate isn't connected, spit out nothing.
			numPeriodEntries = 1;
			periodEntries[ 0 ].periodStartIndex = 0;
			periodEntries[ 0 ].periodEndIndex = numFrames;
			previousPeriodType = SamplePlayerPeriodType.OFF;
			periodEntries[ 0 ].periodType = previousPeriodType;
			return;
		}
		
		numPeriodEntries  = 0;
		float[] inGateFloats = inGateBuffer.floatBuffer;
		float[] inRetriggerFloats = ( inRetriggerConnected ? inRetriggerBuffer.floatBuffer : emptyPeriodFloats );
//		float[] inFrequencyFloats = ( freqConnected ? inFreqBuffer.floatBuffer : emptyPeriodFloats );
//		float[] inAmpFloats = ( ampConnected ? inAmpBuffer.floatBuffer : emptyPeriodFloats );
		
		int periodStartIndex = 0;
		SamplePlayerPeriodType periodType = previousPeriodType;
		
		for( int s = 0 ; s < numFrames ; s++ )
		{
			float gateFloat = inGateFloats[ s ];
			float retriggerFloat = inRetriggerFloats[ s ];

			switch( periodType )
			{
				case OFF:
				{
					if( gateFloat > 0.0f )
					{
						if( s != 0 )
						{
							addPeriodEntry( periodStartIndex, s, s - periodStartIndex, periodType );
						}
						
						periodStartIndex = s;
						periodType = SamplePlayerPeriodType.PLAYING;
					}
					break;
				}
				case PLAYING:
				{
					if( gateFloat <= 0.0f )
					{
						if( s != 0 )
						{
							addPeriodEntry( periodStartIndex, s, s - periodStartIndex, periodType );
						}
						
						periodStartIndex = s;
						periodType = SamplePlayerPeriodType.OFF;
					}
					else if( retriggerFloat > 0.0f )
					{
						if( s != 0 )
						{
							addPeriodEntry( periodStartIndex, s, s - periodStartIndex, periodType );
						}
						
						addPeriodEntry( s, s+1, 1, SamplePlayerPeriodType.TRIGGER );
						
						assert( s + 1 < numFrames );
						
						periodStartIndex = s + 1;
						periodType = SamplePlayerPeriodType.PLAYING;
					}
					break;
				}
				default:
				{
				}
			}
		}
		
		// Catch final period that isn't closed (when retrigger on last index)
		if( periodStartIndex != numFrames )
		{
			addPeriodEntry( periodStartIndex, numFrames, numFrames - periodStartIndex, periodType );
		}
		
		previousPeriodType = periodType;
	}

	private void addPeriodEntry( int startIndex, int endIndex, int length, SamplePlayerPeriodType periodType )
	{
		switch( periodType )
		{
			case OFF:
			{
				periodEntries[ numPeriodEntries ].periodStartIndex = startIndex;
				periodEntries[ numPeriodEntries ].periodEndIndex = endIndex;
				periodEntries[ numPeriodEntries ].periodLength = length;
				periodEntries[ numPeriodEntries ].periodType = periodType;
				numPeriodEntries++;
				break;
			}
			case TRIGGER:
			case PLAYING:
			{
				periodEntries[ numPeriodEntries ].periodStartIndex = startIndex;
				periodEntries[ numPeriodEntries ].periodEndIndex = endIndex;
				periodEntries[ numPeriodEntries ].periodLength = length;
				periodEntries[ numPeriodEntries ].periodType = periodType;
				numPeriodEntries++;
				break;
			}
		}	
	}
	
	public void outputPeriodSamples( float[] tmpBuffer,
			int outputSampleRate,
			int numFrames,
			SingleSampleRuntime sampleRuntime,
			float currentRootNoteFrequency,
			boolean freqConnected, MadChannelBuffer freqBuf,
			boolean ampConnected, MadChannelBuffer ampBuf,
			boolean audioOutLeftConnected, MadChannelBuffer audioOutLeftBuf,
			boolean audioOutRightConnected, MadChannelBuffer audioOutRightBuf )
	{
		float[] audioOutLeftFloats = audioOutLeftBuf.floatBuffer;
		float[] audioOutRightFloats = audioOutRightBuf.floatBuffer;
		
		if( sampleRuntime != null )
		{
			float[] freqFloats = ( freqConnected ? freqBuf.floatBuffer : emptyPeriodFloats );
			float[] ampFloats = ( ampConnected ? ampBuf.floatBuffer : emptyPeriodFloats );
			for( int p = 0 ; p < numPeriodEntries ; p++ )
			{
				SamplePlayerPeriod spp = periodEntries[ p ];

				doOnePeriodOfType( tmpBuffer,
						outputSampleRate,
						numFrames,
						sampleRuntime,
						currentRootNoteFrequency,
						freqConnected,
						audioOutLeftFloats,
						audioOutRightFloats,
						freqFloats,
						ampFloats,
						spp );
				
				lastOutputSamplesPeriodType = spp.periodType;
			}
		}
		else
		{
			if( audioOutLeftConnected )
			{
				Arrays.fill( audioOutLeftFloats, 0.0f );
			}
			if( audioOutRightConnected )
			{
				Arrays.fill( audioOutRightFloats, 0.0f );
			}
		}
			
	}

	private void doOnePeriodOfType( float[] tmpBuffer,
			int outputSampleRate,
			int numFrames,
			SingleSampleRuntime sampleRuntime,
			float currentRootNoteFrequency,
			boolean freqConnected,
			float[] audioOutLeftFloats,
			float[] audioOutRightFloats,
			float[] freqFloats,
			float[] ampFloats,
			SamplePlayerPeriod spp )
	{
		int periodStartIndex = spp.periodStartIndex;
		int periodEndIndex = spp.periodEndIndex;
		int length = spp.periodLength;
		SamplePlayerPeriodType periodType = spp.periodType;
		if( periodStartIndex > numFrames - 1 || periodEndIndex > numFrames )
		{
			log.error("Dodgy indexes again!");
		}
		
		if( periodType != lastOutputSamplesPeriodType )
		{
			switch( periodType )
			{
				case OFF:
				{
					sampleRuntime.receiveStateEvent( SingleSampleState.Event.NOTE_OFF );
					break;
				}
				case TRIGGER:
				{
					sampleRuntime.receiveStateEvent( SingleSampleState.Event.NOTE_RETRIGGER );
					break;
				}
				case PLAYING:
				{
					// No events necessary.
					sampleRuntime.receiveStateEvent( SingleSampleState.Event.NOTE_ON );
					break;
				}
				default:
				{
					log.error("Unhandled transition periodType: " + periodType );
					break;
				}
			}
		}
		
		float freq = ( freqConnected ? freqFloats[ periodStartIndex ] : currentRootNoteFrequency );
		float playbackSpeed = freq;
		// Normalise assuming 44k is playback at C3
		playbackSpeed = (playbackSpeed / currentRootNoteFrequency );
		if( playbackSpeed > MAX_SPEED )
		{
			log.warn( "Speed reduced from " + playbackSpeed + " to " + MAX_SPEED  + " freq " + freq + " curRn " + currentRootNoteFrequency );
			playbackSpeed = MAX_SPEED;
		}
		else
		{
//					log.debug("Playing at speed " + playbackSpeed );
		}

		sampleRuntime.outputPeriod( tmpBuffer,
				outputSampleRate,
				audioOutLeftFloats,
				audioOutRightFloats,
				periodStartIndex,
				periodEndIndex,
				length,
				playbackSpeed,
				ampFloats );
	}
}
