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

package uk.co.modularaudio.util.audio.stft;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.stft.frame.creation.StftFrameCreator;
import uk.co.modularaudio.util.audio.stft.frame.processing.StftFrameProcessor;
import uk.co.modularaudio.util.audio.stft.frame.synthesis.StftFrameSynthesisStep;
import uk.co.modularaudio.util.audio.stft.frame.synthesis.StftFrameSynthesiser;

public class WolaProcessor
{
	private static Log log = LogFactory.getLog( WolaProcessor.class.getName() );

	private final int numChannels;
	private final int analysisStepSize;
	private final int numReals;
	private final int complexArraySize;
	private final int numBins;

	// Main algorithmic loop
	private final StftFrameCreator frameCreator;
	private final StftFrameProcessor frameProcessor;
	private final StftFrameSynthesiser frameSynthesiser;

	private final int numFramesLookahead;

	// Our forward looking cache
	private final StftFrameHistoryRing frameHistoryRing;

	// Somewhere the frame processor can store it's output
	// that we will pass to the synthesiser
	private StftDataFrame processedFrame;

	// How far we will move on synthesis. Needed by our frame processors
	// to be able to correctly calculate phase
	private final StftFrameSynthesisStep synthStep = new StftFrameSynthesisStep();

	public WolaProcessor( final StftParameters params, final StftFrameProcessor frameProcessor )
			throws StftException
	{
		this.numChannels = params.getNumChannels();
		this.analysisStepSize = params.getStepSize();
		this.numReals = params.getNumReals();
		this.complexArraySize = params.getComplexArraySize();
		this.numBins = params.getNumBins();

		frameCreator = new StftFrameCreator( params );

		this.frameProcessor = frameProcessor;
		frameProcessor.setParams( params );

		frameSynthesiser = new StftFrameSynthesiser( params );

		// Need to let the frame processor have the parameters before we call this - it might be
		// dependant on the numOverlaps (particularly in the case of transient processing)
		numFramesLookahead = frameProcessor.getNumFramesNeeded();

		frameHistoryRing = new StftFrameHistoryRing( numFramesLookahead );

		reset();
	}

	public int doNextStep( final float[][] inputStep, final double speed, final double pitch, final UnsafeFloatRingBuffer[] outputRingBuffers )
	{
		if( inputStep.length != numChannels )
		{
			final String msg = "Number of input channels in step does not match configured number of channels.";
			log.error( msg );
			return -1;
		}

		StftDataFrame latestFrame = null;
		// See if we need to re-use an existing frame
		int numFramesBuffered = frameHistoryRing.getNumReadable();
		if( numFramesBuffered == numFramesLookahead )
		{
			// Recycle a frame
			latestFrame = frameHistoryRing.readOneOut();
		}

		// Build the new pvframe from this step. If "latestFrame" is null, the creator will allocate
		latestFrame = frameCreator.makeFrameFromNextStep( inputStep, latestFrame );

		// Add to the end of the latest frames
		frameHistoryRing.writeOne( latestFrame );

		synthStep.calculate( speed, pitch, analysisStepSize );

		frameProcessor.processIncomingFrame( processedFrame, frameHistoryRing, synthStep );

		if( frameProcessor.isSynthesisingProcessor() )
		{
			// If we have enough frames in the look ahead structure we can go ahead
			// and synthesise the result from the frame processor
			numFramesBuffered = frameHistoryRing.getNumReadable();
			if( numFramesBuffered >= numFramesLookahead )
			{
				// Now pass this processed frame to the synthesiser
				frameSynthesiser.synthesiseFrame( processedFrame, speed, pitch, outputRingBuffers, synthStep );
			}
		}
		return 0;
	}

	public StftFrameSynthesisStep getLastFrameSynthesisStep()
	{
		return synthStep;
	}

	public int getLastOutputStepSize()
	{
		return frameSynthesiser.getLastOutputStepSize();
	}

	public StftFrameCreator getFrameCreator()
	{
		return frameCreator;
	}

	public StftFrameSynthesiser getFrameSynthesiser()
	{
		return frameSynthesiser;
	}

	public StftFrameProcessor getFrameProcessor()
	{
		return frameProcessor;
	}

	public final void reset()
	{
		frameCreator.reset();
		frameProcessor.reset();
		frameSynthesiser.reset();

		frameHistoryRing.clear();

		// Now fill up with empty frames
		for( int i = 0 ; i < numFramesLookahead ; i++ )
		{
			frameHistoryRing.writeOne( new StftDataFrame( numChannels, numReals, complexArraySize, numBins ) );
		}

		processedFrame = new StftDataFrame( numChannels, numReals, complexArraySize, numBins );
	}

}
