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

package uk.co.modularaudio.mads.base.moogfilter.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadDefinition;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadInstance;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class MoogFilterTypeComboUiJComponent extends PacComboBox<String>
	implements IMadUiControlInstance<MoogFilterMadDefinition, MoogFilterMadInstance, MoogFilterMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final static Map<String, FrequencyFilterMode> NAME_TO_MODE_MAP = createNameToModeMap();

	private final static Map<String, FrequencyFilterMode> createNameToModeMap()
	{
		final Map<String, FrequencyFilterMode> mm = new HashMap<String, FrequencyFilterMode>();
		mm.put( "None", FrequencyFilterMode.NONE );
		mm.put( "Low Pass", FrequencyFilterMode.LP );
		return Collections.unmodifiableMap( mm );
	}

	private final static List<String> MODES = Arrays.asList(new String[] {
			"None",
			"Low Pass" } );

	private final MoogFilterMadUiInstance uiInstance;

	public MoogFilterTypeComboUiJComponent( final MoogFilterMadDefinition definition,
			final MoogFilterMadInstance instance,
			final MoogFilterMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( false );

		final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();

		for( final String modeName : MODES )
		{
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
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
	}

	@Override
	protected void receiveIndexUpdate( final int previousIndex, final int newIndex )
	{
		if( previousIndex != newIndex )
		{
			final String name = (String) getSelectedItem();
			final FrequencyFilterMode modeToUse = NAME_TO_MODE_MAP.get( name );
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
