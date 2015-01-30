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

package uk.co.modularaudio.mads.base.sampleplayer.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadDefinition;
import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;

public class SingleSamplePlayerRootNoteComboUiJComponent extends PacComboBox<String>
		implements
		IMadUiControlInstance<SingleSamplePlayerMadDefinition, SingleSamplePlayerMadInstance, SingleSamplePlayerMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final SingleSamplePlayerMadUiInstance uiInstance;

	private final Map<String, MidiNote> noteNameToMidiNoteMap = new HashMap<String, MidiNote>();

	public SingleSamplePlayerRootNoteComboUiJComponent(
			final SingleSamplePlayerMadDefinition definition,
			final SingleSamplePlayerMadInstance instance,
			final SingleSamplePlayerMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( false );

		final List<MidiNote> allMidiNotes = MidiUtils.getOrderedMidiNotes();

		final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for( int i =0 ; i < allMidiNotes.size() ; i++ )
		{
			final MidiNote mn = allMidiNotes.get( i );
			noteNameToMidiNoteMap.put( mn.getNoteName(), mn );
			cbm.addElement( mn.getNoteName() );
		}

		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( "C2" );
		this.setSelectedItem( "C3" );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	protected void receiveIndexUpdate( final int previousIndex, final int newIndex )
	{
		if( previousIndex != newIndex )
		{
			final String name = (String) getSelectedItem();
			final MidiNote mn = noteNameToMidiNoteMap.get( name );
			uiInstance.sendRootNoteChoice( mn );
		}
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
