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
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;

public class PatternSequenceToggleContinuation extends JPanel
{
	private static Log log = LogFactory.getLog( PatternSequenceToggleContinuation.class.getName() );
	
	private static final long serialVersionUID = -8634575823868844834L;
	
//	private PatternSequenceModel dataModel = null;
//	private int numMidiNotes = -1;
//	private int numCols = -1;
//	private Dimension cellDimensions = null;
//	private Dimension size = null;
//	private Color backgroundColour = null;
//	private Color foregroundColour = null;
//	private Color blockColour = null;
	
	private JCheckBox contCheckbox = null;
	private JLabel contLabel = null;
	
	public PatternSequenceToggleContinuation( PatternSequenceModel dataModel,
			Dimension blockDimensions,
			Color backgroundColour,
			Color gridColour,
			Color blockColour )
	{
//		this.dataModel = dataModel;
//		this.cellDimensions = blockDimensions;
		log.debug("Table cell dimensions are " + blockDimensions.toString());
//		this.numCols = dataModel.getNumSteps();
//		this.numMidiNotes = MidiUtils.getNumMidiNotes();
//		this.backgroundColour = backgroundColour;
//		this.foregroundColour = gridColour;
//		this.blockColour = blockColour;
		
		this.setBackground( backgroundColour );
		
		MigLayout migLayout = new MigLayout("insets 0, gap 0, width " + PatternSequenceKeyboard.KEYBOARD_WIDTH + ", height " +
				(PatternSequenceAmpGrid.AMP_BOX_HEIGHT - 20) );
		setLayout( migLayout );
		
		contCheckbox = new PatternSequenceContinuationCheckbox( dataModel );
		contCheckbox.setBackground( backgroundColour );
		this.add( contCheckbox, "" );
		contLabel = new JLabel( "C" );
		Font f = contLabel.getFont().deriveFont( 9.0f );
		contLabel.setFont( f );
		contLabel.setForeground( Color.WHITE );
		this.add( contLabel, "" );
	}

}
