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

package uk.co.modularaudio.mads.base.controlprocessingtester.ui;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.controlprocessingtester.mu.CPTMadDefinition;
import uk.co.modularaudio.mads.base.controlprocessingtester.mu.CPTMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.lookuptable.powertable.RawCrossfadePowerTable;
import uk.co.modularaudio.util.audio.lookuptable.powertable.StandardCrossfadePowerTables;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class CPTPowerCurveUiJComponent extends PacComboBox<String>
	implements IMadUiControlInstance<CPTMadDefinition, CPTMadInstance, CPTMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final CPTMadUiInstance uiInstance;

	private final Map<String, RawCrossfadePowerTable> powerNameToPowerTable = new HashMap<String, RawCrossfadePowerTable>();

	public CPTPowerCurveUiJComponent(
			final CPTMadDefinition definition,
			final CPTMadInstance instance,
			final CPTMadUiInstance uiInstance,
			final int controlIndex )
	{
		super();
		this.uiInstance = uiInstance;
		this.setOpaque( false );

		powerNameToPowerTable.put( "Additive", StandardCrossfadePowerTables.getAdditivePowerTable() );
		powerNameToPowerTable.put( "Equal Power", StandardCrossfadePowerTables.getEqualPowerTable() );

		final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for (final String waveName : powerNameToPowerTable.keySet())
		{
			cbm.addElement( waveName );
		}
		this.setModel( cbm );

//		Font f = this.getFont().deriveFont( 9f );
		final Font f = this.getFont();
		setFont( f );

		this.setSelectedItem( "Additive" );
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
			// Figure what they changed, and update the component instance data with
			// the new table
			final String name = (String) getSelectedItem();
			final RawCrossfadePowerTable tableToUse = powerNameToPowerTable.get( name );
			uiInstance.setPowerCurve( tableToUse );
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
