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
import uk.co.modularaudio.util.swing.toggle.ToggleReceiver;

public class ScopeBottomSignalToggles extends JPanel
{
	private static final long serialVersionUID = 7694531221972166980L;

//	private static Log log = LogFactory.getLog( ScopeBottomSignalToggles.class.getName() );

	private final ColouredTextToggle signalToggles[];

	public ScopeBottomSignalToggles( final ScopeNUiInstanceConfiguration uiInstanceConfiguration,
			final ToggleReceiver toggleReceiver )
	{
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "gap 10" );
		msh.addLayoutConstraint( "insets 0" );

		msh.addColumnConstraint( "[25%][25%][25%][25%]" );

		setLayout( msh.createMigLayout() );

		setBackground( ScopeNColours.BACKGROUND_COLOR );
		setOpaque( true );

		final int numScopeChannels = uiInstanceConfiguration.getNumScopeChannels();
		signalToggles = new ColouredTextToggle[numScopeChannels];

		for( int i = 0 ; i < numScopeChannels ; ++i )
		{
			final int displayNum = i+1;
			signalToggles[i] = new ColouredTextToggle(
					"Signal " + displayNum,
					"Click to toggle display of signal " + i,
					ScopeNColours.BACKGROUND_COLOR,
					uiInstanceConfiguration.getVisColours()[displayNum],
					true,
					toggleReceiver,
					displayNum );

			this.add( signalToggles[i], "grow, shrink 0" );
		}
	}

	public String getControlValue( final int i )
	{
		return signalToggles[i].getControlValue();
	}

	public void receiveControlValue( final int i, final String val )
	{
		signalToggles[i].receiveControlValue( val );
	}

}
