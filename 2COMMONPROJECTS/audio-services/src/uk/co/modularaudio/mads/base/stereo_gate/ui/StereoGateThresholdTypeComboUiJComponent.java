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

package uk.co.modularaudio.mads.base.stereo_gate.ui;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.stereo_gate.mu.StereoGateMadDefinition;
import uk.co.modularaudio.mads.base.stereo_gate.mu.StereoGateMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class StereoGateThresholdTypeComboUiJComponent extends PacComboBox<ThresholdTypeEnum>
		implements
		IMadUiControlInstance<StereoGateMadDefinition, StereoGateMadInstance, StereoGateMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final StereoGateMadUiInstance uiInstance;

	public StereoGateThresholdTypeComboUiJComponent(
			final StereoGateMadDefinition definition,
			final StereoGateMadInstance instance,
			final StereoGateMadUiInstance uiInstance,
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

		this.setSelectedItem( "RMS" );
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
