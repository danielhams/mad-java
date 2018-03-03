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

package uk.co.modularaudio.mads.base.djeq3.ui.crossfreqdiag;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class EQCrossoverFreqDialog extends JDialog
{
//	private static Log log = LogFactory.getLog( TextInputDialog.class.getName());

	private static final long serialVersionUID = -8249294046151891200L;

	private final EQCrossoverFreqInputPanel eqFreqInputPanel;

	public EQCrossoverFreqDialog()
	{
		this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		eqFreqInputPanel = new EQCrossoverFreqInputPanel( this );
		this.setModal( true );

		this.setMinimumSize( new Dimension( 300, 150 ) );

		this.add( eqFreqInputPanel );
		this.pack();
	}

	public void setValues( final Component parentComponent,
			final String message,
			final String title,
			final String initialValue,
			final EQCrossoverFreqDialogCallback callback )
	{
		this.setTitle( title );
		eqFreqInputPanel.setValues( message, initialValue, callback );
		this.pack();

		this.setLocationRelativeTo( parentComponent );
	}

	public String getValue()
	{
		return eqFreqInputPanel.getValue();
	}

	public void go()
	{
		setVisible( true );
		eqFreqInputPanel.doFocusSetting();
	}
}
