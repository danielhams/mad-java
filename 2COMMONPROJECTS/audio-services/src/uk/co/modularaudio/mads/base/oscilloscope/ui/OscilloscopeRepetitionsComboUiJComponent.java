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

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeCaptureRepetitionsEnum;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class OscilloscopeRepetitionsComboUiJComponent extends PacComboBox<String>
		implements
		IMadUiControlInstance<OscilloscopeMadDefinition, OscilloscopeMadInstance, OscilloscopeMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private OscilloscopeMadUiInstance uiInstance = null;

	private Map<String, OscilloscopeCaptureRepetitionsEnum> repetitionsNameToEnumMap = new HashMap<String, OscilloscopeCaptureRepetitionsEnum>();

	public OscilloscopeRepetitionsComboUiJComponent(
			OscilloscopeMadDefinition definition,
			OscilloscopeMadInstance instance,
			OscilloscopeMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( false );

		repetitionsNameToEnumMap.put( "Continous", OscilloscopeCaptureRepetitionsEnum.CONTINOUS );
		repetitionsNameToEnumMap.put( "Once", OscilloscopeCaptureRepetitionsEnum.ONCE );

		DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for (String repetitionName : repetitionsNameToEnumMap.keySet())
		{
			cbm.addElement( repetitionName );
		}
		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( "Continous" );
		this.setSelectedItem( "Once" );
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
			OscilloscopeCaptureRepetitionsEnum rv = repetitionsNameToEnumMap.get( name );
			uiInstance.sendRepetitionChoice( rv );
			
			if( rv == OscilloscopeCaptureRepetitionsEnum.CONTINOUS )
			{
				uiInstance.doRecapture();
			}
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
