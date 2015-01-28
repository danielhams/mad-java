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

package uk.co.modularaudio.util.audio.gui.patternsequencer;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;


public class PatternSequenceScrollingGrid extends JScrollPane
{
	private static final long serialVersionUID = 7217501052393673261L;

	private final JPanel compositeKeyboardAndGrid;
	private final PatternSequenceKeyboard keyboard;
	private final PatternSequenceNoteGrid grid;

	public PatternSequenceScrollingGrid( final PatternSequenceModel dataModel,
			final Dimension blockDimensions,
			final Color backgroundColour,
			final Color gridColour,
			final Color blockColour )
	{
		this.setOpaque( true );
		this.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		this.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		this.getVerticalScrollBar().setUnitIncrement( 16 );
		this.getHorizontalScrollBar().setUnitIncrement( 16 );
		this.setAutoscrolls( true );
		this.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );

		compositeKeyboardAndGrid = new JPanel();
		final MigLayout compLayout = new MigLayout("insets 0, gap 0");
		compositeKeyboardAndGrid.setLayout( compLayout );

		keyboard = new PatternSequenceKeyboard( dataModel, blockDimensions, backgroundColour, gridColour, blockColour );
		compositeKeyboardAndGrid.add(  keyboard, "" );

		grid = new PatternSequenceNoteGrid( dataModel, blockDimensions, backgroundColour, gridColour, blockColour );
		compositeKeyboardAndGrid.add( grid, "" );
		this.getViewport().add( compositeKeyboardAndGrid );
	}

}
