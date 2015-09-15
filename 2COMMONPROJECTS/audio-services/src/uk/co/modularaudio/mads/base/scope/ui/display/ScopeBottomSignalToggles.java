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

package uk.co.modularaudio.mads.base.scope.ui.display;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scope.ui.ScopeColours;
import uk.co.modularaudio.util.swing.colouredtoggle.ColouredTextToggle;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.toggle.ToggleReceiver;

public class ScopeBottomSignalToggles extends JPanel
{
	private static final long serialVersionUID = 7694531221972166980L;

//	private static Log log = LogFactory.getLog( ScopeBottomSignalToggles.class.getName() );

	private final ColouredTextToggle signal0Toggle;
	private final ColouredTextToggle signal1Toggle;
	private final ColouredTextToggle signal2Toggle;
	private final ColouredTextToggle signal3Toggle;

	public ScopeBottomSignalToggles( final ToggleReceiver toggleReceiver )
	{
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "gap 10" );
		msh.addLayoutConstraint( "insets 0" );

		msh.addColumnConstraint( "[25%][25%][25%][25%]" );

		setLayout( msh.createMigLayout() );

		setBackground( ScopeColours.BACKGROUND_COLOR );
		setOpaque( true );

		signal0Toggle = new ColouredTextToggle(
				"Signal 1",
				"Click to toggle display of signal 1",
				ScopeColours.BACKGROUND_COLOR,
				ScopeWaveDisplay.VIS_COLOURS[1],
				true,
				toggleReceiver,
				1 );
		signal1Toggle = new ColouredTextToggle(
				"Signal 2",
				"Click to toggle display of signal 2",
				ScopeColours.BACKGROUND_COLOR,
				ScopeWaveDisplay.VIS_COLOURS[2],
				true,
				toggleReceiver,
				2 );
		signal2Toggle = new ColouredTextToggle(
				"Signal 3",
				"Click to toggle display of signal 3",
				ScopeColours.BACKGROUND_COLOR,
				ScopeWaveDisplay.VIS_COLOURS[3],
				true,
				toggleReceiver,
				3 );
		signal3Toggle = new ColouredTextToggle(
				"Signal 4",
				"Click to toggle display of signal 4",
				ScopeColours.BACKGROUND_COLOR,
				ScopeWaveDisplay.VIS_COLOURS[4],
				true,
				toggleReceiver,
				4 );

		this.add( signal0Toggle, "grow, shrink 0" );
		this.add( signal1Toggle, "grow, shrink 0" );
		this.add( signal2Toggle, "grow, shrink 0" );
		this.add( signal3Toggle, "grow, shrink 0" );
	}

	public String getControlValue( final int i )
	{
		switch( i )
		{
			case 0:
			{
				return signal0Toggle.getControlValue();
			}
			case 1:
			{
				return signal1Toggle.getControlValue();
			}
			case 2:
			{
				return signal2Toggle.getControlValue();
			}
			case 3:
			{
				return signal3Toggle.getControlValue();
			}
		}
		return "";
	}

	public void receiveControlValues( final String val0, final String val1, final String val2, final String val3 )
	{
		signal0Toggle.receiveControlValue( val0 );
		signal1Toggle.receiveControlValue( val1 );
		signal2Toggle.receiveControlValue( val2 );
		signal3Toggle.receiveControlValue( val3 );
	}

}
