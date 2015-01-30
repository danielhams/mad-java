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

package uk.co.modularaudio.mads.base.stereo_compressor.ui;

import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class StereoCompressorThresholdTypeComboUiJComponent extends PacComboBox<ThresholdTypeEnum>
		implements
		IMadUiControlInstance<StereoCompressorMadDefinition, StereoCompressorMadInstance, StereoCompressorMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private StereoCompressorMadUiInstance uiInstance = null;

	public StereoCompressorThresholdTypeComboUiJComponent(
			StereoCompressorMadDefinition definition,
			StereoCompressorMadInstance instance,
			StereoCompressorMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;

		DefaultComboBoxModel<ThresholdTypeEnum> cbm = new DefaultComboBoxModel<ThresholdTypeEnum>();
		for( ThresholdTypeEnum e : ThresholdTypeEnum.values() )
		{
			cbm.addElement( e );
		}
		this.setModel( cbm );

//		Font f = this.getFont().deriveFont( 9f );
		Font f = this.getFont();
		setFont( f );

		this.setSelectedItem( ThresholdTypeEnum.RMS );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	protected void receiveIndexUpdate( int previousIndex, int newIndex )
	{
		if( previousIndex != newIndex )
		{
			ThresholdTypeEnum tType = (ThresholdTypeEnum) getSelectedItem();
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
