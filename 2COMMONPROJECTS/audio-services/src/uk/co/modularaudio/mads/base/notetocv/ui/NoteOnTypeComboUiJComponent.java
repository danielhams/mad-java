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

package uk.co.modularaudio.mads.base.notetocv.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.notetocv.mu.NoteOnType;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadDefinition;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class NoteOnTypeComboUiJComponent extends PacComboBox<String>
		implements
		IMadUiControlInstance<NoteToCvMadDefinition, NoteToCvMadInstance, NoteToCvMadUiInstance>
{
	private static final long serialVersionUID = 5596596996111941194L;

	private final NoteToCvMadUiInstance uiInstance;

	private static Map<NoteOnType,String> noteOnTypeToNameMap = new HashMap<NoteOnType,String>();
	private static Map<String,NoteOnType> nameToNoteOnTypeMap = new HashMap<String,NoteOnType>();

	static
	{
		noteOnTypeToNameMap.put( NoteOnType.FOLLOW_FIRST, "Follow First" );
		noteOnTypeToNameMap.put( NoteOnType.FOLLOW_LAST, "Follow Last" );

		for( final NoteOnType type : noteOnTypeToNameMap.keySet() )
		{
			nameToNoteOnTypeMap.put( noteOnTypeToNameMap.get( type ), type );
		}
	}

	public NoteOnTypeComboUiJComponent(
			final NoteToCvMadDefinition definition,
			final NoteToCvMadInstance instance,
			final NoteToCvMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( false );

		final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for (final String noteOnType : nameToNoteOnTypeMap.keySet() )
		{
			cbm.addElement( noteOnType );
		}
		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( noteOnTypeToNameMap.get( NoteOnType.FOLLOW_FIRST ) );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
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
			final String noteOnTypeName = (String) getSelectedItem();
			final NoteOnType noteOnType = nameToNoteOnTypeMap.get( noteOnTypeName );
			uiInstance.sendNoteOnType( noteOnType );
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
