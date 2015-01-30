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

package uk.co.modularaudio.mads.base.ms20filter.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.ms20filter.mu.Ms20FilterMadDefinition;
import uk.co.modularaudio.mads.base.ms20filter.mu.Ms20FilterMadInstance;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class Ms20FilterTypeComboUiJComponent extends PacComboBox<String>
	implements IMadUiControlInstance<Ms20FilterMadDefinition, Ms20FilterMadInstance, Ms20FilterMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final Map<FrequencyFilterMode, String> modeToNameMap = new HashMap<FrequencyFilterMode, String>();
	private final Map<String, FrequencyFilterMode> filterNameToModeMap = new HashMap<String, FrequencyFilterMode>();

	private final Ms20FilterMadUiInstance uiInstance;

	public Ms20FilterTypeComboUiJComponent( final Ms20FilterMadDefinition definition,
			final Ms20FilterMadInstance instance,
			final Ms20FilterMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( false );

		filterNameToModeMap.put( "None", FrequencyFilterMode.NONE );
		filterNameToModeMap.put( "Low Pass", FrequencyFilterMode.LP );
		filterNameToModeMap.put( "High Pass", FrequencyFilterMode.HP );
		filterNameToModeMap.put( "Band Pass", FrequencyFilterMode.BP );
		filterNameToModeMap.put( "Band Reject", FrequencyFilterMode.BR );
		for( final String name : filterNameToModeMap.keySet() )
		{
			modeToNameMap.put( filterNameToModeMap.get( name ), name );
		}

		final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		final FrequencyFilterMode[] modeValues = FrequencyFilterMode.values();
		for( final FrequencyFilterMode mode : modeValues )
		{
			final String modeName = modeToNameMap.get( mode );
			if( modeName != null )
			{
				cbm.addElement( modeName );
			}
		}

		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( "None" );
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
			final FrequencyFilterMode modeToUse = filterNameToModeMap.get( name );
			uiInstance.sendFilterModeChange( modeToUse );
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
