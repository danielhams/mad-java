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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadDefinition;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class NoteChannelComboUiJComponent extends PacComboBox<String>
		implements
		IMadUiControlInstance<NoteToCvMadDefinition, NoteToCvMadInstance, NoteToCvMadUiInstance>
{
	private static final long serialVersionUID = 5596596996111941194L;

	private final NoteToCvMadUiInstance uiInstance;

	private static OpenIntObjectHashMap<String> channelNumToDisplayStringMap = new OpenIntObjectHashMap<String>();
	private static OpenObjectIntHashMap<String> displayStringToChannelNumMap = new OpenObjectIntHashMap<String>();
	private final static int MAXIMUM_CHANNEL_NUMBER = 15;
	private final static int ALL_CHANNELS_INDEX = -1;

	static
	{
		channelNumToDisplayStringMap.put( ALL_CHANNELS_INDEX, "All Channels");
		displayStringToChannelNumMap.put( "All Channels", ALL_CHANNELS_INDEX );
		for( int i = 0 ; i <= MAXIMUM_CHANNEL_NUMBER ; i++ )
		{
			final String displayString = "Channel " + i;
			channelNumToDisplayStringMap.put( i, displayString );
			displayStringToChannelNumMap.put( displayString, i );
		}
	}

	public NoteChannelComboUiJComponent( final NoteToCvMadDefinition definition,
			final NoteToCvMadInstance instance,
			final NoteToCvMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( false );

		final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for( int i = -1 ; i <= MAXIMUM_CHANNEL_NUMBER ; i++ )
		{
			final String displayString = channelNumToDisplayStringMap.get( i );
			cbm.addElement( displayString );
		}
		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( channelNumToDisplayStringMap.get( ALL_CHANNELS_INDEX ) );
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
			final String displayString = (String)getSelectedItem();
			final int channelNum = displayStringToChannelNumMap.get( displayString );
			uiInstance.sendChannelNum( channelNum );
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
