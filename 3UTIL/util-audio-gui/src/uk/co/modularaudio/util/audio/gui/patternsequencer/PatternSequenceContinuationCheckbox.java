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

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;

public class PatternSequenceContinuationCheckbox extends JCheckBox implements ChangeListener
{
	private static final long serialVersionUID = 3373759560524146016L;

	private final PatternSequenceModel dataModel;

	public PatternSequenceContinuationCheckbox( final PatternSequenceModel dataModel )
	{
		this.setOpaque( false );
		this.dataModel = dataModel;
		this.addChangeListener( this );
	}

	@Override
	public void stateChanged( final ChangeEvent e )
	{
		final boolean isSelected = isSelected();
		dataModel.setContinuationState( isSelected );
	}

}
