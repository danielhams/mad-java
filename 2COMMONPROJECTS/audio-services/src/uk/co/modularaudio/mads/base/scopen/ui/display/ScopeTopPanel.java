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

package uk.co.modularaudio.mads.base.scopen.ui.display;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scopen.ui.ScopeNColours;
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNUiInstanceConfiguration;
import uk.co.modularaudio.util.swing.colouredtoggle.ColouredTextToggle;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.texttoggle.TextToggle;
import uk.co.modularaudio.util.swing.toggle.ToggleReceiver;

public class ScopeTopPanel extends JPanel
{
	private static final long serialVersionUID = 7011689081245984767L;

//	private static Log log = LogFactory.getLog( ScopeTopTriggerToggle.class.getName() );

	private final ColouredTextToggle triggerToggle;

	private final TextToggle monoBiPolarToggle;

	public ScopeTopPanel( final ScopeNUiInstanceConfiguration uiInstanceConfiguration,
			final ToggleReceiver triggerToggleReceiver,
			final ToggleReceiver biUniPolarToggleReceiver )
	{
		this.setOpaque( true );
		this.setBackground( ScopeNColours.BACKGROUND_COLOR );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "insets 0" );

		msh.addColumnConstraint( "[][20%!]" );

		setLayout( msh.createMigLayout() );

		triggerToggle = new ColouredTextToggle(
				"Trigger",
				"Click to toggle display of the trigger signal",
				ScopeNColours.BACKGROUND_COLOR,
				uiInstanceConfiguration.getVisColours()[0],
				true,
				triggerToggleReceiver,
				0 );

		add( triggerToggle, "align center, width 120px" );

		monoBiPolarToggle = new TextToggle(
				"Bipole",
				"Monopole",
				ScopeNColours.SCOPE_BODY,
				ScopeNColours.SCOPE_AXIS_DETAIL,
				ScopeNColours.BACKGROUND_COLOR,
				ScopeNColours.SCOPE_AXIS_DETAIL,
				true,
				true,
				biUniPolarToggleReceiver,
				-1 );

		add( monoBiPolarToggle, "align center" );
	}

	public ColouredTextToggle getTriggerToggle()
	{
		return triggerToggle;
	}

	public TextToggle getBiUniPolarToggle()
	{
		return monoBiPolarToggle;
	}
}
