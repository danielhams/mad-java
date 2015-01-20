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

package uk.co.modularaudio.componentdesigner.mainframe;

import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;

public class ComponentDesignerToolbar extends JToolBar
{
	private static final long serialVersionUID = -387065135870575057L;

//	private static Log log = LogFactory.getLog( ComponentDesignerToolbar.class.getName() );
	
	private JButton dumpGraphButton = null;
	private JButton dumpProfileButton = null;
	private JToggleButton enableLoggingCheckbox = null;
	private JToggleButton playStopCheckbox = null;
	
	private ComponentDesignerFrontController fc = null;
	private MainFrameActions actions = null;

	public ComponentDesignerToolbar( ComponentDesignerFrontController fc,
			MainFrameActions actions )
	{
		this.fc = fc;
		this.actions = actions;
		this.add( getDumpGraphButton() );
		this.add( getDumpProfileButton() );
		this.add( getEnableLoggingCheckbox() );
		this.add( getPlayStopToggleButton() );
		this.setFloatable( false );
	}

	public JToggleButton getEnableLoggingCheckbox()
	{
		if( enableLoggingCheckbox == null )
		{
			enableLoggingCheckbox = new EnableLoggingCheckbox( actions );
		}
		return enableLoggingCheckbox;
	}
	
	public JToggleButton getPlayStopToggleButton()
	{
		if( playStopCheckbox == null )
		{
			playStopCheckbox = new PlayStopCheckbox( fc, actions );
		}
		return playStopCheckbox;
	}
	
	public JButton getDumpGraphButton()
	{
		if( dumpGraphButton == null )
		{
			dumpGraphButton = new DumpGraphButton( actions );
		}
		return dumpGraphButton;
	}

	public JButton getDumpProfileButton()
	{
		if( dumpProfileButton == null )
		{
			dumpProfileButton = new DumpProfileButton( actions );
		}
		return dumpProfileButton;
	}

}
