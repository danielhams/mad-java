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

package uk.co.modularaudio.util.audio.pvoc;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.pvoc.frame.PvocFrameProcessor;
import uk.co.modularaudio.util.audio.pvoc.frame.creation.PvocFrameCreator;
import uk.co.modularaudio.util.audio.pvoc.frame.synthesis.PvocFrameSynthesiser;
import uk.co.modularaudio.util.audio.pvoc.support.PvocDataFrame;
import uk.co.modularaudio.util.audio.pvoc.support.PvocFrameSynthesisStep;

public class PhaseVocoder
{
	private static Log log = LogFactory.getLog( PhaseVocoder.class.getName() );
	
	private int numChannels;
	private int numReals;
	private int fftComplexArraySize;
	private int numBins;
	
	// Main algorithmic loop
	private PvocFrameCreator frameCreator;
	private PvocFrameProcessor frameProcessor;
	private PvocFrameSynthesiser frameSynthesiser;
	
	private int numFramesLookahead;
	
	// Our forward looking cache
	private ArrayList<PvocDataFrame> forwardLookingFrames;

	// Somewhere the frame processor can store it's output
	// that we will pass to the synthesiser
	private PvocDataFrame processedFrame;
	
	// How far we will move on synthesis. Needed by our frame processors
	// to be able to correctly calculate phase
	private PvocFrameSynthesisStep synthStep;
	
	public PhaseVocoder( PvocParameters params, PvocFrameProcessor frameProcessor )
	{
		this.numChannels = params.getNumChannels();
		this.numReals = params.getNumReals();
		this.fftComplexArraySize = params.getFftComplexArraySize();
		this.numBins = params.getNumBins();
		
		frameCreator = new PvocFrameCreator( params );
		
		this.frameProcessor = frameProcessor;
		
		frameSynthesiser = new PvocFrameSynthesiser( params );
		
		synthStep = new PvocFrameSynthesisStep( params );

		// Need to let the frame processor have the parameters before we call this - it might be
		// dependant on the numOverlaps (particularly in the case of transient processing)
		numFramesLookahead = frameProcessor.getNumFramesNeeded();
		forwardLookingFrames = new ArrayList<PvocDataFrame>( numFramesLookahead );
		
		reset();
	}
	
	public int doNextStep( float[][] inputStep, double speed, double pitch, UnsafeFloatRingBuffer[] outputRingBuffers )
	{
		if( inputStep.length != numChannels )
		{
			String msg = "Number of input channels in step does not match configured number of channels.";
			log.error( msg );
			return -1;
		}

		PvocDataFrame latestFrame = forwardLookingFrames.remove( forwardLookingFrames.size() - 1 );

		// Build the new pvframe from this step
		int creationRc = frameCreator.makeFrameFromNextStep( inputStep, latestFrame );
		if( creationRc < 0 )
		{
			String msg = "Frame creation failed.";
			log.error( msg );
			return -1;
		}

		// Add to the front of the latest frames
		forwardLookingFrames.add( 0, latestFrame  );

		synthStep.calculate( speed, pitch );

		int processingRc = frameProcessor.processIncomingFrame( processedFrame, forwardLookingFrames, synthStep );
		if( processingRc < 0 )
		{
			String msg = "Frame processing failed.";
			log.error( msg );
			return -1;
		}
		
		if( frameProcessor.isSynthesisingProcessor() )
		{
			// If we have enough frames in the look ahead structure we can go ahead
			// and synthesise the result from the frame processor
			int numFramesBuffered = forwardLookingFrames.size();
			if( numFramesBuffered >= numFramesLookahead )
			{
				// Now pass the place we want the output to the synthesiser
				int synthesisRc = frameSynthesiser.synthesiseFrame( processedFrame, speed, pitch, outputRingBuffers, synthStep );
				if( synthesisRc < 0 )
				{
					String msg = "Frame synthesis failed.";
					log.error( msg );
					return -1;
				}
				
			}	
		}
		return 0;
	}
	
	public void reset()
	{
		frameCreator.reset();
		frameProcessor.reset();
		frameSynthesiser.reset();
		
		forwardLookingFrames.clear();
		// Now fill up with empty frames
		for( int i = 0 ; i < numFramesLookahead ; i++ )
		{
			forwardLookingFrames.add( new PvocDataFrame( numChannels, numReals, fftComplexArraySize, numBins ) );
		}
		
		processedFrame = new PvocDataFrame( numChannels, numReals, fftComplexArraySize, numBins );
	}

}
