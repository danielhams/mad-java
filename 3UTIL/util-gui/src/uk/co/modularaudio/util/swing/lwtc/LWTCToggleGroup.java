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

package uk.co.modularaudio.util.swing.lwtc;


public abstract class LWTCToggleGroup
{
//	private static Log log = LogFactory.getLog( LWTCToggleGroup.class.getName() );

	private final LWTCToggleButton[] toggleButtons;

	private int currentlySelectedIndex;

	private class LWTCToggleGroupToggleButton extends LWTCToggleButton
	{
		private static final long serialVersionUID = -5969130109396285751L;

		private final int myIndex;

		public LWTCToggleGroupToggleButton( final LWTCButtonColours colours,
				final String optionLabel,
				final boolean defaultValue,
				final boolean isImmediate,
				final int myIndex )
		{
			super( colours, optionLabel, isImmediate, defaultValue );
			this.myIndex = myIndex;
		}

		@Override
		public void receiveUpdateEvent(final boolean previousValue, final boolean newValue)
		{
			receiveButtonSelection( myIndex );
		}
	};

	public LWTCToggleGroup( final LWTCButtonColours colours,
			final String[] optionLabels,
			final int defaultOption,
			final boolean isImmediate )
	{
		final int numOptions = optionLabels.length;
		toggleButtons = new LWTCToggleButton[ numOptions ];
		for( int i = 0; i < numOptions ; ++i )
		{
			final boolean isActive = (i == defaultOption);
			toggleButtons[i] = new LWTCToggleGroupToggleButton( colours,
					optionLabels[i],
					isActive,
					isImmediate,
					i );
		}
		currentlySelectedIndex = defaultOption;
	}

	protected void receiveButtonSelection( final int newIndex )
	{
//		log.debug("Received button selection of " + newIndex );
		for( int i = 0 ; i < toggleButtons.length ; ++ i )
		{
			final boolean shouldBeActive = (i == newIndex );
//			log.debug("Setting button " + i + " to " + shouldBeActive );
			toggleButtons[i].setSelectedNoPropogate( shouldBeActive );
			toggleButtons[i].repaint();
		}
		if( newIndex != currentlySelectedIndex )
		{
			receiveUpdateEvent(currentlySelectedIndex, newIndex);
			currentlySelectedIndex = newIndex;
		}
	}

	public abstract void receiveUpdateEvent( int previousSelection, int newSelection );

	public LWTCToggleButton[] getToggleButtons()
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
