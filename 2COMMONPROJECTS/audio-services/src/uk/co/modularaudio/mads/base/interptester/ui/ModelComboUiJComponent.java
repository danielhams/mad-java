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

package uk.co.modularaudio.mads.base.interptester.ui;

import java.awt.Font;

import javax.swing.DefaultComboBoxModel;

import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;

public class ModelComboUiJComponent extends PacComboBox<String>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final InterpTesterMadUiInstance uiInstance;

	public ModelComboUiJComponent( final InterpTesterMadUiInstance uiInstance )
	{
		super();
		this.uiInstance = uiInstance;
		this.setOpaque( false );

		final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		cbm.addElement( "DJ Cross Fader" );
		cbm.addElement( "DJ EQ Gain" );
		cbm.addElement( "DJ Deck Fader" );
		cbm.addElement( "Mastering Mixer Fader" );
		cbm.addElement( "Speed" );
		cbm.addElement( "Frequency" );
		cbm.addElement( "Left Right" );
		cbm.addElement( "Compression Threshold" );
		cbm.addElement( "Compression Ratio" );
		cbm.addElement( "Output Gain" );
		cbm.addElement( "Time (1->5000 ms)" );

		this.setModel( cbm );

//		Font f = this.getFont().deriveFont( 9f );
		final Font f = this.getFont();
		setFont( f );

		this.setSelectedItem( "Cross Fader" );
	}

	@Override
	protected void receiveIndexUpdate( final int previousIndex, final int newIndex )
	{
		if( previousIndex != newIndex )
		{
			// Figure what they changed, and update the component instance data with
			// the new table
			uiInstance.setValueModelIndex( getSelectedIndex() );
		}
	}
}
