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
import javax.swing.border.LineBorder;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;

public class PatternSequencer extends JPanel
{
	private static final long serialVersionUID = -3560105491686396192L;

//	private static Log log = LogFactory.getLog( PatternSequencer.class.getName() );

	private final PatternSequenceScrollingGrid scrollingNoteGrid;
	private final PatternSequenceToggleContinuation toggleContinuation;
	private final PatternSequenceAmpGrid ampGrid;

	public PatternSequencer( final PatternSequenceModel dataModel,
			final Dimension blockDimensions,
			final Color backgroundColour,
			final Color gridColour,
			final Color blockColour )
	{
		this.setOpaque( true );

		setBackground( backgroundColour );
		final MigLayout layout = new MigLayout("insets 0, gap 0");
		setLayout( layout );
		final LineBorder theBorder = new LineBorder( backgroundColour, 2 );
		setBorder( theBorder );

		scrollingNoteGrid = new PatternSequenceScrollingGrid( dataModel, blockDimensions, backgroundColour, gridColour, blockColour );
		this.add( scrollingNoteGrid, "grow, wrap, span 2");
		toggleContinuation = new PatternSequenceToggleContinuation( dataModel, blockDimensions, backgroundColour, gridColour, blockColour );
		this.add( toggleContinuation, "" );
		ampGrid = new PatternSequenceAmpGrid( dataModel, blockDimensions, backgroundColour, gridColour, blockColour );
		this.add( ampGrid, "growx");
	}

}
