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

package uk.co.modularaudio.mads.base.audioanalyser.ui;

import uk.co.modularaudio.mads.base.audioanalyser.ui.modeprocessors.DbWaveModeProcessor;
import uk.co.modularaudio.mads.base.audioanalyser.ui.modeprocessors.ModeProcessor;
import uk.co.modularaudio.mads.base.audioanalyser.ui.modeprocessors.RawWaveModeProcessor;
import uk.co.modularaudio.mads.base.audioanalyser.ui.modeprocessors.SpectralAmpModeProcessor;
import uk.co.modularaudio.mads.base.audioanalyser.ui.modeprocessors.SpectralRollModeProcessor;
import uk.co.modularaudio.mads.base.audioanalyser.ui.modeprocessors.ThreeMusicModeProcessor;
import uk.co.modularaudio.mads.base.audioanalyser.ui.modeprocessors.ThreeVoiceModeProcessor;


public enum AudioAnalyserDisplayMode
{
	RAW_WAVE,
	DB_WAVE,
	THREE_VOICE,
	THREE_MUSIC,
	SPECTRAL_ROLL,
	SPECTRAL_AMP,
	NUM_MODES;
	
	private static ModeProcessor[] modeProcessors;
	
	static
	{
		modeProcessors = new ModeProcessor[ NUM_MODES.ordinal() ];
		modeProcessors[ RAW_WAVE.ordinal() ] = new RawWaveModeProcessor();
		modeProcessors[ DB_WAVE.ordinal() ] = new DbWaveModeProcessor();
		modeProcessors[ THREE_VOICE.ordinal() ] = new ThreeVoiceModeProcessor();
		modeProcessors[ THREE_MUSIC.ordinal() ] = new ThreeMusicModeProcessor();
		modeProcessors[ SPECTRAL_ROLL.ordinal() ] = new SpectralRollModeProcessor();
		modeProcessors[ SPECTRAL_AMP.ordinal() ] = new SpectralAmpModeProcessor();
	}
	
	public ModeProcessor getModeProcessor()
	{
		return modeProcessors[this.ordinal()];
	}
}
