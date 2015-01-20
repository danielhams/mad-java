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

package uk.co.modularaudio.mads.base.rbjfilter.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.rbjfilter.mu.RBJFilterMadDefinition;
import uk.co.modularaudio.mads.base.rbjfilter.mu.RBJFilterMadInstance;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class RBJFilterTypeComboUiJComponent extends PacComboBox<String>
	implements IMadUiControlInstance<RBJFilterMadDefinition, RBJFilterMadInstance, RBJFilterMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private Map<FrequencyFilterMode, String> modeToNameMap = new HashMap<FrequencyFilterMode, String>();
	private Map<String, FrequencyFilterMode> filterNameToModeMap = new HashMap<String, FrequencyFilterMode>();
	
	private RBJFilterMadUiInstance uiInstance = null;

	public RBJFilterTypeComboUiJComponent( RBJFilterMadDefinition definition,
			RBJFilterMadInstance instance,
			RBJFilterMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( false );

		filterNameToModeMap.put( "None", FrequencyFilterMode.NONE );
		filterNameToModeMap.put( "Low Pass", FrequencyFilterMode.LP );
		filterNameToModeMap.put( "High Pass", FrequencyFilterMode.HP );
		filterNameToModeMap.put( "Band Pass", FrequencyFilterMode.BP );
//		filterNameToModeMap.put( "Band Reject", FrequencyFilterMode.BR );
		for( String name : filterNameToModeMap.keySet() )
		{
			modeToNameMap.put( filterNameToModeMap.get( name ), name );
		}

		DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();

		FrequencyFilterMode[] modeValues = FrequencyFilterMode.values();
		for( FrequencyFilterMode mode : modeValues )
		{
			String modeName = modeToNameMap.get( mode );
			cbm.addElement( modeName );
		}

		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( "Low Pass" );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
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
			String name = (String) getSelectedItem();
			FrequencyFilterMode modeToUse = filterNameToModeMap.get( name );
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
