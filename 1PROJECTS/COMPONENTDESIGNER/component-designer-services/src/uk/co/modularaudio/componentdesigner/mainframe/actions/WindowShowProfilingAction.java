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

package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.ExitSignalReceiver;
import uk.co.modularaudio.componentdesigner.profiling.ProfilingWindow;

public class WindowShowProfilingAction extends AbstractAction implements ExitSignalReceiver
{
	private static final long serialVersionUID = -5903263092723112562L;

//	private static Log log = LogFactory.getLog( WindowShowProfilingAction.class.getName() );

//	private final ComponentDesignerFrontController fc;

	private final ProfilingWindow profilingWindow;

	public WindowShowProfilingAction( final ComponentDesignerFrontController fc,
			final ProfilingWindow profilingWindow )
	{
//		this.fc = fc;
		this.profilingWindow = profilingWindow;
		this.putValue( NAME, "Show Profiling Window" );
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		if( !profilingWindow.isShowing() )
		{
			profilingWindow.setVisible( true );
		}
	}

	@Override
	public void signalPreExit()
	{
		if( profilingWindow != null && profilingWindow.isShowing() )
		{
			profilingWindow.dispose();
		}
	}

	@Override
	public void signalPostExit()
	{
	}
}
