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

	private final OscilloscopeMadUiInstance uiInstance;

	private final Map<String, OscilloscopeCaptureRepetitionsEnum> repetitionsNameToEnumMap = new HashMap<String, OscilloscopeCaptureRepetitionsEnum>();

	public OscilloscopeRepetitionsComboUiJComponent(
			final OscilloscopeMadDefinition definition,
			final OscilloscopeMadInstance instance,
			final OscilloscopeMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( false );

		repetitionsNameToEnumMap.put( "Continous", OscilloscopeCaptureRepetitionsEnum.CONTINOUS );
		repetitionsNameToEnumMap.put( "Once", OscilloscopeCaptureRepetitionsEnum.ONCE );

		final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for (final String repetitionName : repetitionsNameToEnumMap.keySet())
		{
			cbm.addElement( repetitionName );
		}
		this.setModel( cbm );

		final Font f = this.getFont();
		setFont( f );

		this.setSelectedItem( "Continous" );
		this.setSelectedItem( "Once" );
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
			final OscilloscopeCaptureRepetitionsEnum rv = repetitionsNameToEnumMap.get( name );
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
