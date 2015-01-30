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

package uk.co.modularaudio.mads.base.oscilloscope.ui;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeCaptureTriggerEnum;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class OscilloscopeTriggerComboUiJComponent extends PacComboBox<String>
		implements
		IMadUiControlInstance<OscilloscopeMadDefinition, OscilloscopeMadInstance, OscilloscopeMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final OscilloscopeMadUiInstance uiInstance;

	private final Map<String, OscilloscopeCaptureTriggerEnum> triggerNameToEnumMap = new HashMap<String, OscilloscopeCaptureTriggerEnum>();

	public OscilloscopeTriggerComboUiJComponent(
			final OscilloscopeMadDefinition definition,
			final OscilloscopeMadInstance instance,
			final OscilloscopeMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( false );

		triggerNameToEnumMap.put( "None", OscilloscopeCaptureTriggerEnum.NONE );
		triggerNameToEnumMap.put( "On Rise", OscilloscopeCaptureTriggerEnum.ON_RISE );
		triggerNameToEnumMap.put( "On Fall", OscilloscopeCaptureTriggerEnum.ON_FALL );

		final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for (final String triggerName : triggerNameToEnumMap.keySet())
		{
			cbm.addElement( triggerName );
		}
		this.setModel( cbm );

		final Font f = this.getFont();
		setFont( f );

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
			final OscilloscopeCaptureTriggerEnum ev = triggerNameToEnumMap.get( name );
			uiInstance.sendTriggerChoice( ev );
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
