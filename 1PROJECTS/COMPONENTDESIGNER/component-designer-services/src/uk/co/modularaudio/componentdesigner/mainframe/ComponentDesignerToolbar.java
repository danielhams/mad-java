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

	private final JButton dumpGraphButton;
	private final JButton dumpProfileButton;
	private final JButton dumpSampleCacheButton;
	private final JToggleButton enableLoggingCheckbox;
	private final JToggleButton playStopCheckbox;

	public ComponentDesignerToolbar( final ComponentDesignerFrontController fc,
			final MainFrameActions actions )
	{
		dumpGraphButton = new DumpGraphButton( actions );
		dumpProfileButton = new DumpProfileButton( actions );
		dumpSampleCacheButton = new DumpSampleCacheButton( actions );
		enableLoggingCheckbox = new EnableLoggingCheckbox( actions );
		playStopCheckbox = new PlayStopCheckbox( fc, actions );
		this.add( dumpGraphButton );
		this.add( dumpProfileButton );
		this.add( dumpSampleCacheButton );
		this.add( enableLoggingCheckbox );
		this.add( playStopCheckbox );
		this.setFloatable( false );
	}

	public JToggleButton getPlayStopToggleButton()
	{
		return playStopCheckbox;
	}
}
