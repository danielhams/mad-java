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

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSampleState.Event;
import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class SingleSampleRuntime
{
	private static Log log = LogFactory.getLog( SingleSampleRuntime.class.getName() );

//	private final AdvancedComponentsFrontController advancedComponentsFrontController;
	private final BlockResamplerService blockResamplerService;

	private float curValueRatio = 0.0f;
//	private float newValueRatio = 0.0f;

	private final BlockResamplingClient playingSample;
	private SingleSampleState playingSampleState = SingleSampleState.STOPPED;
	private float playingSampleSpeed = 0.0f;
	private float playingSampleLastAmp = 0.0f;

	private final BlockResamplingClient fadeOutSample;
	private float fadeOutSampleSpeed = 0.0f;
	private float fadeOutSampleLastAmp = 0.0f;
	private int curFadeOutFrameCount = 0;

	private int playingStartOffset = 0;
	private int numFadeOutFrames = 0;

	private final AtomicBoolean used = new AtomicBoolean(false);

	public SingleSampleRuntime( final AdvancedComponentsFrontController advancedComponentsFrontController,
			final String filename )
			throws DatastoreException, IOException, UnsupportedAudioFileException
	{
//		this.advancedComponentsFrontController = advancedComponentsFrontController;
		this.blockResamplerService = advancedComponentsFrontController.getBlockResamplerService();

//		SampleCacheClient playingSampleCacheClient = advancedComponentsFrontController.registerCacheClientForFile( filename );
//		playingSample = new BlockResamplerSampleClient( playingSampleCacheClient, BlockResamplingMethod.LINEAR, 0, 0.0f );
		playingSample = advancedComponentsFrontController.createResamplingClient( filename, BlockResamplingMethod.LINEAR );

//		SampleCacheClient fadeOutSampleCacheClient = advancedComponentsFrontController.registerCacheClientForFile( filename );
//		fadeOutSample = new BlockResamplerSampleClient( fadeOutSampleCacheClient, BlockResamplingMethod.LINEAR, 0, 0.0f );
		fadeOutSample = advancedComponentsFrontController.createResamplingClient( filename, BlockResamplingMethod.LINEAR );
	}

	public void setRuntimeData( final int playingStartOffset, final int numFramesFadeOut, final float curValueRatio, final float newValueRatio )
	{
		this.playingStartOffset = playingStartOffset;
		this.numFadeOutFrames = numFramesFadeOut;
//		this.numFadeOutFrames = 1;
		this.curValueRatio = curValueRatio;
//		this.newValueRatio = newValueRatio;

//		log.debug("Curvalueratio is " + MathFormatter.floatPrint( curValueRatio, 3 ) );
	}

	public boolean isUsed()
	{
		return used.get();
	}

	public void setUsed( final boolean newUsed )
	{
		used.set( newUsed );
	}

	public void destroy()
			throws DatastoreException, RecordNotFoundException, IOException
	{
//		advancedComponentsFrontController.unregisterCacheClientForFile( playingSample.getSampleCacheClient() );
//		advancedComponentsFrontController.unregisterCacheClientForFile( fadeOutSample.getSampleCacheClient() );
		blockResamplerService.destroyResamplingClient(playingSample);
		blockResamplerService.destroyResamplingClient(fadeOutSample);
	}

	private void movePlayingToFadeOut()
	{
		final long framePosition = playingSample.getFramePosition();
		fadeOutSample.setFramePosition( framePosition );
		fadeOutSample.setFpOffset( playingSample.getFpOffset() );
//		fadeOutSample.getSampleCacheClient().setCurrentFramePosition( framePosition );
		fadeOutSampleSpeed = playingSampleSpeed;
		fadeOutSampleLastAmp = playingSampleLastAmp;

		curFadeOutFrameCount = 0;
	}

	public void receiveStateEvent( final Event stateEvent )
	{
//		log.debug("Received state event: " + stateEvent );
		switch( stateEvent )
		{
			case NOTE_ON:
			{
				handleNoteOnEvent();
				break;
			}
			case NOTE_OFF:
			{
				handleNoteOffEvent();
				break;
			}
			case NOTE_RETRIGGER:
			{
				handleNoteRetrigger();
				break;
			}
			case HARD_FINISH:
			{
				handleHardFinish();
				break;
			}
			case SOFT_FINISH:
			{
				handleSoftFinish();
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unhandled state event: " + stateEvent );
				}
			}
		}
	}

	private void handleSoftFinish()
	{
		switch( playingSampleState )
		{
			case SOFT_FADE:
			{
				// Change to stopped.
//				log.debug("End of soft fade. Transition back to stopped.");
				playingSampleState = SingleSampleState.STOPPED;
				// We move the sample back to the start so the cached hot block of samples
				// can be released.
				break;
			}
			case SOFT_AND_HARD_FADE:
			{
				// Change to hard fade
				playingSampleState = SingleSampleState.HARD_FADE;
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unhandled event soft finish in state " + playingSampleState );
				}
			}
		}
		playingSampleMoveToStart();
	}

	private void handleHardFinish()
	{
		switch( playingSampleState )
		{
			case PLAYING_HARD_FADE:
			{
				playingSampleState = SingleSampleState.PLAYING;
				break;
			}
			case SOFT_AND_HARD_FADE:
			{
				playingSampleState = SingleSampleState.SOFT_FADE;
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unhandled event hard finish in state " + playingSampleState );
				}
			}
		}
		hardFadeOutSampleMoveToStart();
	}

	private void handleNoteRetrigger()
	{
		switch( playingSampleState )
		{
			case PLAYING:
			{
				// Move the playing details over to the fade out details
				movePlayingToFadeOut();
//				log.debug("Transition to playing hard fade from playing.");
				playingSampleState = SingleSampleState.PLAYING_HARD_FADE;
				playingSampleMoveToStart();
				break;
			}
			case PLAYING_HARD_FADE:
			{
				// We transition to the same state.
				movePlayingToFadeOut();
				playingSampleMoveToStart();
//				log.debug("Re-transition to playing hard fade from playing.");
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unhandled event note retrigger in state " + playingSampleState );
				}
			}
		}
	}

	private void handleNoteOffEvent()
	{
		switch( playingSampleState )
		{
			case PLAYING:
			{
				playingSampleState = SingleSampleState.SOFT_FADE;
				// Set fade to start
				curFadeOutFrameCount = 0;
//				log.debug("Note off causing transition to soft fade");
				break;
			}
			case PLAYING_HARD_FADE:
			{
				playingSampleState = SingleSampleState.SOFT_AND_HARD_FADE;
//				log.debug("Note off causing transition from playing hard fade to soft and hard fade");
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unhandled event note off in state " + playingSampleState );
				}
			}
		}
	}

	private void handleNoteOnEvent()
	{
		switch( playingSampleState )
		{
			case STOPPED:
			{
				// Reset position to start.
				playingSampleMoveToStart();
				playingSampleState = SingleSampleState.PLAYING;
//				log.debug("Moving to playing from stopped");
				break;
			}
			case SOFT_FADE:
			{
				movePlayingToFadeOut();
				playingSampleMoveToStart();
				playingSampleState = SingleSampleState.PLAYING_HARD_FADE;
//				log.debug("Moving to playing hard fade from soft fade");
				break;
			}
			case PLAYING_HARD_FADE:
			{
//				// We transition to same state, but move existing to fade
//				movePlayingToFadeOut();
//				playingSampleMoveToStart();
//				log.debug("Skipping hard fade to itself");
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unhandled note on event for state: " + playingSampleState );
				}
			}
		}
	}

	private void playingSampleMoveToStart()
	{
		playingSample.setFramePosition( playingStartOffset );
		playingSample.setFpOffset( 0.0f );
//		playingSample.getSampleCacheClient().setCurrentFramePosition( playingStartOffset );
	}

	private void hardFadeOutSampleMoveToStart()
	{
		fadeOutSample.setFramePosition( playingStartOffset );
		fadeOutSample.setFpOffset( 0.0f );
//		fadeOutSample.getSampleCacheClient().setCurrentFramePosition( playingStartOffset );
	}

	public void outputPeriod( final float[] tmpBuffer,
			final int outputSampleRate,
			final float[] audioOutLeftFloats,
			final float[] audioOutRightFloats,
			final int periodStartIndex,
			final int periodEndIndex,
			final int length,
			final float playbackSpeed,
			final float[] ampFloats )
	{
		if( periodStartIndex + length > audioOutLeftFloats.length )
		{
			log.error( "Here it is " );
		}
//		if( playingSampleState != SingleSampleState.STOPPED )
//		{
//			log.debug("Currently outputting in state: " + playingSampleState.toString() );
//		}
		switch( playingSampleState )
		{
			case STOPPED:
			{
				Arrays.fill( audioOutLeftFloats, periodStartIndex, periodEndIndex, 0.0f );
				Arrays.fill( audioOutRightFloats, periodStartIndex, periodEndIndex, 0.0f );
				break;
			}
			case PLAYING:
			{
				blockResamplerService.fetchAndResample(
						playingSample,
						outputSampleRate,
						playbackSpeed,
						audioOutLeftFloats, periodStartIndex,
						audioOutRightFloats, periodStartIndex,
						length,
						tmpBuffer,
						0 );

				for( int i = 0 ; i < length ; ++i )
				{
					final float amp = ampFloats[periodStartIndex+i];
					audioOutLeftFloats[periodStartIndex+i] *= amp;
					audioOutRightFloats[periodStartIndex+i] *= amp;
				}

				playingSampleSpeed = playbackSpeed;
				playingSampleLastAmp = ampFloats[ periodEndIndex - 1 ];
				break;
			}
			case SOFT_FADE:
			{
				blockResamplerService.fetchAndResample(
						playingSample,
						outputSampleRate,
						playbackSpeed,
						audioOutLeftFloats, periodStartIndex,
						audioOutRightFloats, periodStartIndex,
						length,
						tmpBuffer,
						0 );

				for( int i = 0 ; i < length ; ++i )
				{
					final float amp = ampFloats[periodStartIndex+i];
					audioOutLeftFloats[periodStartIndex+i] *= amp;
					audioOutRightFloats[periodStartIndex+i] *= amp;
				}

				playingSampleSpeed = playbackSpeed;
				playingSampleLastAmp = ampFloats[ periodEndIndex - 1 ];
				if( playingSampleLastAmp == 0.0f )
				{
					receiveStateEvent( Event.SOFT_FINISH );
				}
				break;
			}
			case PLAYING_HARD_FADE:
			case SOFT_AND_HARD_FADE:
			{
				final int numFramesLeftToOutput = (numFadeOutFrames - curFadeOutFrameCount);
//				log.debug("Still " + numFramesLeftToOutput + " frames of fade out left");
				final int numFramesToOutput = (numFramesLeftToOutput < length ? numFramesLeftToOutput : length );

				if( numFramesLeftToOutput > 0 )
				{
					blockResamplerService.fetchAndResample(
							fadeOutSample,
							outputSampleRate,
							fadeOutSampleSpeed,
							audioOutLeftFloats, periodStartIndex,
							audioOutRightFloats, periodStartIndex,
							numFramesToOutput,
							tmpBuffer,
							0 );

					// Apply hard fade out amps
					for( int s = 0 ; s < numFramesToOutput ; s++ )
					{
						audioOutLeftFloats[ periodStartIndex + s ] *= fadeOutSampleLastAmp;
						audioOutRightFloats[ periodStartIndex + s ] *= fadeOutSampleLastAmp;
						fadeOutSampleLastAmp = (fadeOutSampleLastAmp * curValueRatio );
//						log.debug("Faded out using fosla(" + MathFormatter.floatPrint( fadeOutSampleLastAmp, 3 ) + ")");
					}

					curFadeOutFrameCount += numFramesToOutput;

					// And zero any output that's left
					for( int z = periodStartIndex + numFramesToOutput ; z < periodEndIndex ; z++ )
					{
						audioOutLeftFloats[ z ] = 0.0f;
						audioOutRightFloats[ z ] = 0.0f;
					}
				}
				else
				{
//					log.debug("Marking hard fade out as stopped.");
					receiveStateEvent( Event.HARD_FINISH );
					curFadeOutFrameCount = numFadeOutFrames;
					// And zero the output we will add to
					for( int z = periodStartIndex ; z < periodEndIndex ; z++ )
					{
						audioOutLeftFloats[ z ] = 0.0f;
						audioOutRightFloats[ z ] = 0.0f;
					}
				}

				// Pull the normal playing samples to the side
				final int tmpLeftOutputIndex = 0;
				final int tmpRightOutputIndex = tmpLeftOutputIndex + length;
				final int tmpBufferOtherIndex = tmpRightOutputIndex + length;

				blockResamplerService.fetchAndResample(
					playingSample,
					outputSampleRate,
					playbackSpeed,
					audioOutLeftFloats, tmpLeftOutputIndex,
					audioOutRightFloats, tmpRightOutputIndex,
					length,
					tmpBuffer,
					tmpBufferOtherIndex );

				// Add in over the top
				for( int i = 0 ; i < length ; ++ i )
				{
					audioOutLeftFloats[periodStartIndex+i] += tmpBuffer[tmpLeftOutputIndex+i];
					audioOutRightFloats[periodStartIndex+i] += tmpBuffer[tmpRightOutputIndex+i];
				}

				playingSampleSpeed = playbackSpeed;
				playingSampleLastAmp = ampFloats[ periodEndIndex - 1 ];
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unknown state in output period: " + playingSampleState );
				}
			}
		}
	}

}
