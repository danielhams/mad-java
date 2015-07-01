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

package uk.co.modularaudio.util.audio.stft.frame.processing;

import uk.co.modularaudio.util.audio.stft.StftDataFrame;
import uk.co.modularaudio.util.audio.stft.StftException;
import uk.co.modularaudio.util.audio.stft.StftFrameHistoryRing;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.frame.synthesis.StftFrameSynthesisStep;

public interface StftFrameProcessor
{
	/**
	 * Whether the WOLA processor will attempt to get the frame processor to synthesise.
	 * @return true if the results of processIncomingFrame should be synthesised
	 */
	public boolean isSynthesisingProcessor();

	/**
	 * How many frames the processor needs to see before it will begin to return generated frames.
	 * Return &lt;= 0 means the processor will produce a frame for every input frame.
	 *
	 * @return the number of frames before output - zero indicates immediate production of frames
	 */
	public int getNumFramesNeeded();

	/**
	 * The frequency processor will need to know things like the sample rate,
	 * number of overlaps and incoming step size etc
	 *
	 * @param params The parameters that will be used during subsequent processIncomingFrame calls.
	 * @throws StftException
	 */
	public void setParams( StftParameters params ) throws StftException;

	/*
	 * Perform the necessary processing using as many lookahead frames as needed
	 * The output is placed in the passed structure for synthesis
	 *
	 * @param outputFrame
	 * @return int success = 0
	 * @throws PvException
	 */
	public int processIncomingFrame( StftDataFrame outputFrame,
			StftFrameHistoryRing frameHistoryRing,
			StftFrameSynthesisStep synthStep );

	/**
	 * Does this processor perform peak detection?
	 * @return true if the frame processor traverses and fills the peak channel buffers
	 */
	public boolean isPeakProcessor();

	/**
	 * Return the frame processor to it's initial state.
	 */
	public void reset();

	public StftFrameProcessorVisualDebugger getDebuggingVisualComponent();

	// Debugging methods
	public StftDataFrame getLastDataFrame();

	public int[][] getPeakChannelBuffers();

	public int[][] getBinToPeakChannelBuffers();


}
