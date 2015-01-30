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

package uk.co.modularaudio.util.audio.gui.madswingcontrols;

import javax.swing.ButtonGroup;

public abstract class PacToggleGroup
{
//	private static Log log = LogFactory.getLog( PacToggleGroup.class.getName() );

	private final PacToggleButton[] toggleButtons;
	private final ButtonGroup toggleButtonGroup;

	private int currentlySelectedIndex;

	private class PacToggleGroupToggleButton extends PacToggleButton
	{
		private static final long serialVersionUID = -5969130109396285751L;

		private final int myIndex;

		public PacToggleGroupToggleButton( final String optionLabel, final boolean defaultValue, final int myIndex)
		{
			super(defaultValue);
			this.setText(optionLabel);
			this.myIndex = myIndex;
		}

		@Override
		public void receiveUpdateEvent(final boolean previousValue, final boolean newValue)
		{
//			log.debug("Received toggle button update event: " + previousValue + " and " + newValue);
			if( newValue )
			{
				receiveButtonSelection( myIndex );
			}
		}
	};

	public PacToggleGroup( final String[] optionLabels, final int defaultOption )
	{
		final int numOptions = optionLabels.length;
		toggleButtonGroup = new ButtonGroup();
		toggleButtons = new PacToggleButton[ numOptions ];
		for( int i = 0; i < numOptions ; ++i )
		{
			final boolean isActive = (i == defaultOption);
			toggleButtons[i] = new PacToggleGroupToggleButton( optionLabels[i], isActive, i );
			toggleButtonGroup.add( toggleButtons[i] );
		}
		currentlySelectedIndex = defaultOption;
	}

	protected void receiveButtonSelection( final int newIndex )
	{
		if( newIndex != currentlySelectedIndex )
		{
			receiveUpdateEvent(currentlySelectedIndex, newIndex);
			currentlySelectedIndex = newIndex;
		}
	}

	public abstract void receiveUpdateEvent( int previousSelection, int newSelection );

	public PacToggleButton[] getToggleButtons()
	{
		return toggleButtons;
	}

	public void setSelectedItemIndex( final int index )
	{
		toggleButtons[ index ].setSelected( true );
	}

	public int getSelectedItemIndex()
	{
		return currentlySelectedIndex;
	}
}
