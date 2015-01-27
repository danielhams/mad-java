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
	private final static float MIN_SMOOTHED_FREQ = 40.0f;
	private final static float LOW_COF = 500.0f;
	private final static float MID_COF = 500.0f;
	private final static float HIGH_COF = 4500.0f;
	
	public ThreeSpeechDataBuffers( AudioAnalyserUiBufferState uiBufferState )
	{
		// Good for music
		super( uiBufferState,
			MIN_SMOOTHED_FREQ,
			MIN_SMOOTHED_FREQ * 2,
			MIN_SMOOTHED_FREQ * 4,
			LOW_COF,
			MID_COF,
			HIGH_COF );

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
