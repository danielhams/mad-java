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

package uk.co.modularaudio.mads.base.waveshaper.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.waveshaper.mu.WaveShaperMadDefinition;
import uk.co.modularaudio.mads.base.waveshaper.mu.WaveShaperMadInstance;
import uk.co.modularaudio.mads.base.waveshaper.mu.WaveShaperWaveTables;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class WaveShaperShapeComboUiJComponent extends PacComboBox<String>
		implements
		IMadUiControlInstance<WaveShaperMadDefinition, WaveShaperMadInstance, WaveShaperMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final WaveShaperMadUiInstance uiInstance;

	private final Map<String, WaveShaperWaveTables.WaveType> waveNameToTypeMap = new HashMap<String, WaveShaperWaveTables.WaveType>();

	public WaveShaperShapeComboUiJComponent(
			final WaveShaperMadDefinition definition,
			final WaveShaperMadInstance instance,
			final WaveShaperMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( false );

		waveNameToTypeMap.put( "Compressor", WaveShaperWaveTables.WaveType.COMPRESSOR );
		waveNameToTypeMap.put( "Triangle", WaveShaperWaveTables.WaveType.TRIANGLE );
		waveNameToTypeMap.put( "Square", WaveShaperWaveTables.WaveType.SQUARE );

		final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for (final String waveName : waveNameToTypeMap.keySet())
		{
			cbm.addElement( waveName );
		}
		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( "Compressor" );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	protected void receiveIndexUpdate( final int previousIndex, final int newIndex )
	{
		if( previousIndex != newIndex )
		{
			final String name = (String) getSelectedItem();
			final WaveShaperWaveTables.WaveType waveType = waveNameToTypeMap.get( name );
			uiInstance.sendWaveChoice( waveType );
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

}
