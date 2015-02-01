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

package uk.co.modularaudio.service.gui.impl.guirackpanel.sub;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialogCallback;

public class NewComponentNameConfirmedCallback implements TextInputDialogCallback
{
	private static Log log = LogFactory.getLog( NewComponentNameConfirmedCallback.class.getName() );

	private final RackService rackService;
	private final GuiService guiService;
	private final RackDataModel rackDataModel;
	private final Component parentComponent;
	private final MadDefinition<?,?> typeToAdd;

	public NewComponentNameConfirmedCallback( final RackService rackService,
			final GuiService guiService,
			final RackDataModel rackDataModel,
			final Component parentComponent,
			final MadDefinition<?,?> typeToAdd )
	{
		this.rackService = rackService;
		this.guiService = guiService;
		this.rackDataModel = rackDataModel;
		this.parentComponent = parentComponent;
		this.typeToAdd = typeToAdd;
	}

	@Override
	public void dialogClosedReceiveText( final String userNewName )
	{
		try
		{
			String question;
			String title;
			if( userNewName != null )
			{
				// Check to see if it's a parameterised instance
				final Map<MadParameterDefinition, String> paramValues = new HashMap<MadParameterDefinition, String>();
				boolean failedParameters = false;
				if( typeToAdd.isParametrable() )
				{
					log.debug("Need some logic for parameterisable instances!");

					final MadParameterDefinition[] parameterDefs = typeToAdd.getParameterDefinitions();

					for( int i = 0 ; !failedParameters && i < parameterDefs.length ; i++ )
					{
						final MadParameterDefinition aupd = parameterDefs[ i ];
						question = "Please enter a value for the parameter " + aupd.getUserVisibleString();
						title = "Enter creation parameter";
						final String parameterValue = (String)JOptionPane.showInputDialog( parentComponent,
								question,
								title,
								JOptionPane.QUESTION_MESSAGE,
								null,
								null,
								"" );
						if( parameterValue == null )
						{
							failedParameters = true;
						}
						else
						{
							paramValues.put( aupd, parameterValue );
						}
					}
				}

				if( !failedParameters )
				{
					rackService.createComponent( rackDataModel, typeToAdd, paramValues, userNewName );
				}
				else
				{
					log.debug("Won't attempt add as parameters failed to be completed.");
				}
			}
		}
		catch( final MAConstraintViolationException ecve )
		{
			guiService.showMessageDialog( parentComponent, "A Component with that name already exists.", "Unable to add component",
					JOptionPane.INFORMATION_MESSAGE, null );
		}
		catch( final Exception e )
		{
			final String msg = "Exception caught handling new component name confirmation: " + e.toString();
			log.error( msg, e );
		}
	}

}
