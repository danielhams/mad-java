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

package uk.co.modularaudio.mads.base.mono_compressor.ui;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.mono_compressor.mu.MonoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.mono_compressor.mu.MonoCompressorMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class MonoCompressorThresholdTypeComboUiJComponent extends PacComboBox<ThresholdTypeEnum>
		implements
		IMadUiControlInstance<MonoCompressorMadDefinition, MonoCompressorMadInstance, MonoCompressorMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final MonoCompressorMadUiInstance uiInstance;

	public MonoCompressorThresholdTypeComboUiJComponent(
			final MonoCompressorMadDefinition definition,
			final MonoCompressorMadInstance instance,
			final MonoCompressorMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		final DefaultComboBoxModel<ThresholdTypeEnum> cbm = new DefaultComboBoxModel<ThresholdTypeEnum>();
		for( final ThresholdTypeEnum e : ThresholdTypeEnum.values() )
		{
			cbm.addElement( e );
		}
		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( ThresholdTypeEnum.RMS );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
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
			final ThresholdTypeEnum tType = (ThresholdTypeEnum) getSelectedItem();
			uiInstance.updateThresholdType( tType.ordinal() );
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
