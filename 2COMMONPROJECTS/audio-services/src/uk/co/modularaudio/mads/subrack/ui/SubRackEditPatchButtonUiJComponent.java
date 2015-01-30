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

package uk.co.modularaudio.mads.subrack.ui;

import java.awt.Font;
import java.awt.Insets;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.subrack.jpanel.PatchTabCloseListener;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacToggleButton;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SubRackEditPatchButtonUiJComponent extends PacToggleButton
	implements IMadUiControlInstance<SubRackMadDefinition, SubRackMadInstance, SubRackMadUiInstance>, PatchTabCloseListener
{
	private static final long serialVersionUID = -6066972568143292726L;
	
	private SubRackMadUiInstance uiInstance = null;

	public SubRackEditPatchButtonUiJComponent( SubRackMadDefinition definition,
			SubRackMadInstance instance,
			SubRackMadUiInstance uiInstance,
			SubRackEditPatchButtonUiControlDefinition def )
	{
		super( false );
		setOpaque( false );

		this.uiInstance = uiInstance;
		uiInstance.addPatchTabCloseListener( this );

//		Font f = getFont().deriveFont( 9.0f );
		Font f = getFont();
		setFont( f );
		setMargin( new Insets( 0, 0, 0, 0 ) );
		setText( "Edit" );
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
	}

	@Override
	public JComponent getControl()
	{
		return getControl();
	}

	@Override
	public void receiveUpdateEvent( boolean previousValue, boolean newValue )
	{
//		log.debug("Received update event(" + newValue + ")");
		if( previousValue != newValue )
		{
			uiInstance.makeSubRackFrameVisible( newValue );
		}
		
	}

	@Override
	public void receivePatchTabClose()
	{
		if( this.isSelected() )
		{
//			log.debug("Received tab close signal, will deselect edit button");
			previousValue = false;
			this.setSelected( false );
			updateColours();
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
