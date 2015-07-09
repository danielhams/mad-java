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

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadDefinition;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class NoteOnTypeChoiceUiJComponent
implements IMadUiControlInstance<NoteToCvMadDefinition, NoteToCvMadInstance, NoteToCvMadUiInstance>
{
//	private static Log log = LogFactory.getLog( NoteOnTypeChoiceUiJComponent.class.getName() );

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public enum NoteOnType
	{
		FOLLOW_FIRST( "Follow First" ),
		FOLLOW_LAST( "Follow Last" );

		private String label;

		private NoteOnType( final String label )
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}
	};

	public final static Map<String, NoteOnType> NOTE_ON_LABEL_TO_ENUM = buildLabelToEnumMap();

	private static Map<String, NoteOnType> buildLabelToEnumMap()
	{
		final Map<String, NoteOnType> retVal = new HashMap<>();
		retVal.put( NoteOnType.FOLLOW_FIRST.getLabel(), NoteOnType.FOLLOW_FIRST );
		retVal.put( NoteOnType.FOLLOW_LAST.getLabel(), NoteOnType.FOLLOW_LAST );
		return retVal;
	}

	public final static NoteOnType DEFAULT_NOTE_ON_TYPE = NoteOnType.FOLLOW_LAST;

	public NoteOnTypeChoiceUiJComponent(
			final NoteToCvMadDefinition definition,
			final NoteToCvMadInstance instance,
			final NoteToCvMadUiInstance uiInstance,
			final int controlIndex )
	{

		model = new DefaultComboBoxModel<>();
		model.addElement( NoteOnType.FOLLOW_FIRST.getLabel() );
		model.addElement( NoteOnType.FOLLOW_LAST.getLabel() );

		model.setSelectedItem( DEFAULT_NOTE_ON_TYPE.getLabel() );

		rotaryChoice = new LWTCRotaryChoice( LWTCControlConstants.STD_ROTARY_CHOICE_COLOURS, model, false );

		model.addListDataListener( new ListDataListener()
		{

			@Override
			public void intervalRemoved( final ListDataEvent e )
			{
			}

			@Override
			public void intervalAdded( final ListDataEvent e )
			{
			}

			@Override
			public void contentsChanged( final ListDataEvent e )
			{
				final String val = (String)model.getSelectedItem();
				final NoteOnType not = NOTE_ON_LABEL_TO_ENUM.get( val );
				uiInstance.sendNoteOnType( not );
			}
		} );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public String getControlValue()
	{
		return (String)model.getSelectedItem();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		model.setSelectedItem( value );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
	{
	}

	@Override
	public Component getControl()
	{
		return rotaryChoice;
	}

	@Override
	public void destroy()
	{
	}
}
