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

package uk.co.modularaudio.mads.base.crossfader.ui;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadDefinition;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.wavetable.powertable.RawCrossfadePowerTable;
import uk.co.modularaudio.util.audio.wavetable.powertable.StandardCrossfadePowerTables;

public class CrossFaderPowerCurveUiJComponent extends PacComboBox<String>
	implements IMadUiControlInstance<CrossFaderMadDefinition, CrossFaderMadInstance, CrossFaderMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private CrossFaderMadUiInstance uiInstance = null;

	private Map<String, RawCrossfadePowerTable> powerNameToPowerTable = new HashMap<String, RawCrossfadePowerTable>();

	public CrossFaderPowerCurveUiJComponent(
			CrossFaderMadDefinition definition,
			CrossFaderMadInstance instance,
			CrossFaderMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.setOpaque( false );

		powerNameToPowerTable.put( "Additive", StandardCrossfadePowerTables.getAdditivePowerTable() );
		powerNameToPowerTable.put( "Equal Power", StandardCrossfadePowerTables.getEqualPowerTable() );

		DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for (String waveName : powerNameToPowerTable.keySet())
		{
			cbm.addElement( waveName );
		}
		this.setModel( cbm );

//		Font f = this.getFont().deriveFont( 9f );
		Font f = this.getFont();
		setFont( f );

		this.setSelectedItem( "Additive" );
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
			// Figure what they changed, and update the component instance data with
			// the new table
			String name = (String) getSelectedItem();
			RawCrossfadePowerTable tableToUse = powerNameToPowerTable.get( name );
			uiInstance.powerCurveWaveTable = tableToUse;
			uiInstance.recalculateAmps();
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
