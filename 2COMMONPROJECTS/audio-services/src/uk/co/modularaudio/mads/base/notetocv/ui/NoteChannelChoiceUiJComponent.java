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

public class NoteChannelChoiceUiJComponent
implements IMadUiControlInstance<NoteToCvMadDefinition, NoteToCvMadInstance, NoteToCvMadUiInstance>
{
//	private static Log log = LogFactory.getLog( NoteOnTypeChoiceUiJComponent.class.getName() );

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public enum NoteChannel
	{
		ALL( "All Channels", -1 ),
		CHANNEL_0( "Channel 0", 0 ),
		CHANNEL_1( "Channel 1", 1 ),
		CHANNEL_2( "Channel 2", 2 ),
		CHANNEL_3( "Channel 3", 3 ),
		CHANNEL_4( "Channel 4", 4 ),
		CHANNEL_5( "Channel 5", 5 ),
		CHANNEL_6( "Channel 6", 6 ),
		CHANNEL_7( "Channel 7", 7 ),
		CHANNEL_8( "Channel 8", 8 ),
		CHANNEL_9( "Channel 9", 9 ),
		CHANNEL_10( "Channel 10", 10 ),
		CHANNEL_11( "Channel 11", 11 ),
		CHANNEL_12( "Channel 12", 12 ),
		CHANNEL_13( "Channel 13", 13 ),
		CHANNEL_14( "Channel 14", 14 ),
		CHANNEL_15( "Channel 15", 15 );

		private String label;
		private int channelNum;

		private NoteChannel( final String label, final int channelNum )
		{
			this.label = label;
			this.channelNum = channelNum;
		}

		public String getLabel()
		{
			return label;
		}

		public int getChannelNum()
		{
			return channelNum;
		}
	};

	public final static Map<String, NoteChannel> NOTE_CHANNEL_LABEL_TO_ENUM = buildLabelToEnumMap();

	private static Map<String, NoteChannel> buildLabelToEnumMap()
	{
		final Map<String, NoteChannel> retVal = new HashMap<>();
		for( final NoteChannel nc : NoteChannel.values() )
		{
			retVal.put( nc.getLabel(), nc );
		}
		return retVal;
	}

	public final static NoteChannel DEFAULT_CHANNEL = NoteChannel.ALL;

	public NoteChannelChoiceUiJComponent(
			final NoteToCvMadDefinition definition,
			final NoteToCvMadInstance instance,
			final NoteToCvMadUiInstance uiInstance,
			final int controlIndex )
	{

		model = new DefaultComboBoxModel<>();
		for( final NoteChannel nc : NoteChannel.values() )
		{
			model.addElement( nc.getLabel() );
		}

		model.setSelectedItem( DEFAULT_CHANNEL.getLabel() );

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
				final NoteChannel nc = NOTE_CHANNEL_LABEL_TO_ENUM.get( val );
				uiInstance.sendNoteChannel( nc.getChannelNum() );
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
