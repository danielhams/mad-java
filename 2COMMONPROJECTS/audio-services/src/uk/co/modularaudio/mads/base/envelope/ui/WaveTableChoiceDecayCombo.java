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

package uk.co.modularaudio.mads.base.envelope.ui;

import java.util.HashSet;

import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboController;
import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboItem;
import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboModel;
import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboView;
import uk.co.modularaudio.util.audio.wavetable.valuemapping.StandardValueMappingWaveTables;

public class WaveTableChoiceDecayCombo extends WaveTableComboView
{
	public enum WaveTableChoiceEnum
	{
		LINEAR,
		EXP,
		EXP_FREQ,
		LOG,
		LOG_FREQ
	};
	
	private static final long serialVersionUID = 2831518979757488524L;
	
	private WaveTableComboModel cm = null;
	private WaveTableComboController cc = null;

	private static HashSet<WaveTableComboItem> startupItems = new HashSet<WaveTableComboItem>();
	
	static
	{
		WaveTableComboItem li = new WaveTableComboItem( WaveTableChoiceEnum.LINEAR.toString(),
				"Linear", StandardValueMappingWaveTables.getLinearDecayMappingWaveTable(), false );
		startupItems.add( li );
		WaveTableComboItem expi = new WaveTableComboItem( WaveTableChoiceEnum.EXP.toString(),
				"Exponential", StandardValueMappingWaveTables.getExpDecayMappingWaveTable(), false );
		startupItems.add( expi );
		WaveTableComboItem expfi = new WaveTableComboItem( WaveTableChoiceEnum.EXP_FREQ.toString(),
				"Exponential Frequency", StandardValueMappingWaveTables.getExpFreqDecayMappingWaveTable(), false );
		startupItems.add( expfi );
		WaveTableComboItem logi = new WaveTableComboItem( WaveTableChoiceEnum.LOG.toString(),
				"Logarythmic", StandardValueMappingWaveTables.getLogDecayMappingWaveTable(), false );
		startupItems.add( logi );
		WaveTableComboItem logfi = new WaveTableComboItem( WaveTableChoiceEnum.LOG_FREQ.toString(),
				"Logarythmic Frequency", StandardValueMappingWaveTables.getLogFreqDecayMappingWaveTable(), false );
		startupItems.add( logfi );
	}

	public WaveTableChoiceDecayCombo( WaveTableChoiceChangeReceiver changeReceiver )
	{
		super();
		cm = new WaveTableComboModel( startupItems );
		cc = new WaveTableControllerEmitChoice( cm, changeReceiver );
		setModelAndController( cm, cc );
	}
	
	public WaveTableComboController getWTController()
	{
		return cc;
	}
	
	public WaveTableComboModel getWTModel()
	{
		return cm;
	}

}
