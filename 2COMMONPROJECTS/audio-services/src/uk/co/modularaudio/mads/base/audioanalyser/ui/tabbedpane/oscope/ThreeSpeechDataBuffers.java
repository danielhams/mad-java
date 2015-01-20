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

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope;

import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserUiBufferState;


public class ThreeSpeechDataBuffers extends RmsDataBuffers
{
	private final static float minSmoothedFreq = 40.0f;
	private final static float lowCof = 500.0f;
	private final static float midCof = 500.0f;
	private final static float highCof = 4500.0f;
	
	public ThreeSpeechDataBuffers( AudioAnalyserUiBufferState uiBufferState )
	{
		// Good for music
		super( uiBufferState,
			minSmoothedFreq,
			minSmoothedFreq * 2,
			minSmoothedFreq * 4,
			lowCof,
			midCof,
			highCof );

//		// For male speech
//		float minSmoothedFreq = 40.0f;
//		float lowCof = 500.0f;
//		float midCof = 500.0f;
//		float highCof = 4500.0f;

//		// For female speech
//		float minSmoothedFreq = 40.0f;
//		float lowCof = 550.0f;
//		float midCof = 550.0f;
//		float highCof = 4500.0f;
	}
}
