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

package uk.co.modularaudio.util.audio.pvoc.frame;

import java.util.ArrayList;

import uk.co.modularaudio.util.audio.pvoc.PvocParameters;
import uk.co.modularaudio.util.audio.pvoc.support.PvocDataFrame;
import uk.co.modularaudio.util.audio.pvoc.support.PvocFrameSynthesisStep;

public abstract class PvocFrameProcessor
{
	protected PvocParameters parameters;
	
	public PvocFrameProcessor( PvocParameters parameters )
	{
		this.parameters = parameters;
	}
	
	/**
	 * Whether the WOLA processor will attempt to get the frame processor to synthesise.
	 * @return
	 */
	public abstract boolean isSynthesisingProcessor();
	
	/**
	 * How many frames the processor needs to see before it will begin to return generated frames.
	 * Return <= 0 means the processor will produce a frame for every input frame.
	 * 
	 * @return
	 */
	public abstract int getNumFramesNeeded();

	/*
	 * Perform the necessary processing using as many lookahead frames as needed
	 * The output is placed in the passed structure for synthesis
	 * 
	 * @param outputFrame
	 * @return int success = 0
	 * @throws PvException
	 */
	public abstract int processIncomingFrame( PvocDataFrame outputFrame,
			ArrayList<PvocDataFrame> lookaheadFrames,
			PvocFrameSynthesisStep synthStep );
	
	/**
	 * Does this processor perform peak detection?
	 * @return
	 */
	public abstract boolean isPeakProcessor();

	/**
	 * Return the frame processor to it's initial state.
	 */
	public abstract void reset();
}
