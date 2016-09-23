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

package uk.co.modularaudio.mads.base.controllertocv.ui;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class ControllerToCvChannelChoiceUiJComponent
implements IMadUiControlInstance<ControllerToCvMadDefinition, ControllerToCvMadInstance, ControllerToCvMadUiInstance>
{
//	private static Log log = LogFactory.getLog( ControllerToCvChannelChoiceUiJComponent.class.getName() );

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public enum NoteChannel
	{
		ALL( "All Chans", -1 ),
		CHANNEL_0( "Chan 0", 0 ),
		CHANNEL_1( "Chan 1", 1 ),
		CHANNEL_2( "Chan 2", 2 ),
		CHANNEL_3( "Chan 3", 3 ),
		CHANNEL_4( "Chan 4", 4 ),
		CHANNEL_5( "Chan 5", 5 ),
		CHANNEL_6( "Chan 6", 6 ),
		CHANNEL_7( "Chan 7", 7 ),
		CHANNEL_8( "Chan 8", 8 ),
		CHANNEL_9( "Chan 9", 9 ),
		CHANNEL_10( "Chan 10", 10 ),
		CHANNEL_11( "Chan 11", 11 ),
		CHANNEL_12( "Chan 12", 12 ),
		CHANNEL_13( "Chan 13", 13 ),
		CHANNEL_14( "Chan 14", 14 ),
		CHANNEL_15( "Chan 15", 15 );

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

	public ControllerToCvChannelChoiceUiJComponent(
			final ControllerToCvMadDefinition definition,
			final ControllerToCvMadInstance instance,
			final ControllerToCvMadUiInstance uiInstance,
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
				uiInstance.sendSelectedChannel( nc.getChannelNum() );
			}
		} );

		uiInstance.addLearnListener( new ControllerToCvLearnListener()
		{

			@Override
			public void receiveLearntController( final int channel, final int controller )
			{
				final String channelElement = model.getElementAt( channel + 1 );
				model.setSelectedItem( channelElement );
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
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage ,
			final MadTimingParameters timingParameters ,
			final int U_currentGuiTime , int framesSinceLastTick  )
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
