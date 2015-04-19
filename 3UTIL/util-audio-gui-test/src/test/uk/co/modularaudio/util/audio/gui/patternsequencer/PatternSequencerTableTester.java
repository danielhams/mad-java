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

package test.uk.co.modularaudio.util.audio.gui.patternsequencer;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.patternsequencer.PatternSequenceDefines;
import uk.co.modularaudio.util.audio.gui.patternsequencer.PatternSequencer;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModelImpl;

public class PatternSequencerTableTester
{
	public static Log log = LogFactory.getLog( PatternSequencerTableTester.class.getName() );

	private final PatternSequencer patternSequencer;

	public PatternSequencerTableTester()
	{
		final Dimension tableDimensions = new Dimension( PatternSequenceDefines.DEFAULT_PATTERN_LENGTH, PatternSequenceDefines.DEFAULT_NUM_KEYS );
		final PatternSequenceModel dataModel = new PatternSequenceModelImpl( tableDimensions.width );
		final Dimension blockDimensions = new Dimension( 15, 8 );

		final float backLevel = 0.3f;
		final Color backgroundColour = new Color( backLevel, backLevel, backLevel );
		final Color gridColour = new Color( 0.5f, backLevel, backLevel );
		final Color blockColour = new Color( 0.95f, 0.2f, 0.2f );

		patternSequencer = new PatternSequencer( dataModel, blockDimensions, backgroundColour, gridColour, blockColour );
	}

	public void go()
	{
		final JFrame frame = new JFrame("Test pattern sequencer");
		final Dimension size = new Dimension(320,200);
		frame.setPreferredSize( size);
		frame.setMinimumSize( size);

		frame.add( patternSequencer );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setVisible( true );
	}

	public static void main( final String[] args )
	{
		final PatternSequencerTableTester pstt = new PatternSequencerTableTester();
		pstt.go();
	}
}
