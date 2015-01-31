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
import uk.co.modularaudio.util.audio.lookuptable.valuemapping.StandardValueMappingWaveTables;

public class WaveTableChoiceAttackCombo extends WaveTableComboView
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

	private final WaveTableComboModel cm;
	private final WaveTableComboController cc;

	private final static HashSet<WaveTableComboItem> STARTUP_ITEMS = new HashSet<WaveTableComboItem>();

	static
	{
		final WaveTableComboItem li = new WaveTableComboItem( WaveTableChoiceEnum.LINEAR.toString(),
				"Linear", StandardValueMappingWaveTables.getLinearAttackMappingWaveTable(), false );
		STARTUP_ITEMS.add( li );
		final WaveTableComboItem expi = new WaveTableComboItem( WaveTableChoiceEnum.EXP.toString(),
				"Exponential", StandardValueMappingWaveTables.getExpAttackMappingWaveTable(), false );
		STARTUP_ITEMS.add( expi );
		final WaveTableComboItem expfi = new WaveTableComboItem( WaveTableChoiceEnum.EXP_FREQ.toString(),
				"Exponential Frequency", StandardValueMappingWaveTables.getExpFreqAttackMappingWaveTable(), false );
		STARTUP_ITEMS.add( expfi );
		final WaveTableComboItem logi = new WaveTableComboItem( WaveTableChoiceEnum.LOG.toString(),
				"Logarythmic", StandardValueMappingWaveTables.getLogAttackMappingWaveTable(), false );
		STARTUP_ITEMS.add( logi );
		final WaveTableComboItem logfi = new WaveTableComboItem( WaveTableChoiceEnum.LOG_FREQ.toString(),
				"Logarythmic Frequency", StandardValueMappingWaveTables.getLogFreqAttackMappingWaveTable(), false );
		STARTUP_ITEMS.add( logfi );
	}

	public WaveTableChoiceAttackCombo( final WaveTableChoiceChangeReceiver changeReceiver )
	{
		super();
		cm = new WaveTableComboModel( STARTUP_ITEMS );
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
