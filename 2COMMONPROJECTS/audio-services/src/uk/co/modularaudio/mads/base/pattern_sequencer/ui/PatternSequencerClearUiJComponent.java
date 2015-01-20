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

package uk.co.modularaudio.mads.base.pattern_sequencer.ui;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerMadDefinition;
import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacButton;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class PatternSequencerClearUiJComponent extends PacButton
	implements IMadUiControlInstance<PatternSequencerMadDefinition, PatternSequencerMadInstance, PatternSequencerMadUiInstance>
{
	private static final long serialVersionUID = 6005212426192029708L;

	private PatternSequenceModel dataModel = null;;

	public PatternSequencerClearUiJComponent(
			PatternSequencerMadDefinition definition,
			PatternSequencerMadInstance instance,
			PatternSequencerMadUiInstance uiInstance,
			int controlIndex )
	{
		super();

		this.setOpaque( false );
		setFont( this.getFont().deriveFont( 9f ) );
		this.setText( "Clear" );
		
		dataModel = instance.getPatternDataModel();
	}

	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void receiveEvent( ActionEvent e )
	{
		dataModel.clear();
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
