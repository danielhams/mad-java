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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;

public class ToggleLoggingAction extends AbstractAction
{
	private static final long serialVersionUID = 6567077333146253552L;

	private static Log log = LogFactory.getLog( ToggleLoggingAction.class.getName() );

	private final ComponentDesignerFrontController fc;

	public ToggleLoggingAction( final ComponentDesignerFrontController fcin )
	{
		this.fc = fcin;
		this.putValue(NAME, "Enable Logging");
		this.putValue(SELECTED_KEY, "EnableLogging.selected");
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		try
		{
			fc.toggleLogging();
		}
		catch (final Exception ex)
		{
			final String msg = "Exception caught performing enable logging action: " + ex.toString();
			log.error( msg, ex );
		}
	}
}
