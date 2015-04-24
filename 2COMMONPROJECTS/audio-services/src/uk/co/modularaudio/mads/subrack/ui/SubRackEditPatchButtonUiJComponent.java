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

import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.subrack.jpanel.PatchTabCloseListener;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCToggleButton;

public class SubRackEditPatchButtonUiJComponent extends LWTCToggleButton
	implements IMadUiControlInstance<SubRackMadDefinition, SubRackMadInstance, SubRackMadUiInstance>, PatchTabCloseListener
{
	private static final long serialVersionUID = -6066972568143292726L;

//	private static Log log = LogFactory.getLog( SubRackEditPatchButtonUiJComponent.class.getName() );

	private final SubRackMadUiInstance uiInstance;

	public SubRackEditPatchButtonUiJComponent( final SubRackMadDefinition definition,
			final SubRackMadInstance instance,
			final SubRackMadUiInstance uiInstance,
			final SubRackEditPatchButtonUiControlDefinition def )
	{
		super( LWTCControlConstants.STD_TOGGLE_BUTTON_COLOURS, "Edit", false, false );

		this.uiInstance = uiInstance;
		uiInstance.addPatchTabCloseListener( this );
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
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
	public void receiveUpdateEvent( final boolean previousValue, final boolean newValue )
	{
//		log.debug("Received update event(" + newValue + ")");
		uiInstance.makeSubRackFrameVisible( newValue );
		// Synthesise mouse exit so the button returns to an appropriate state.
		if( previousValue == false || newValue == true )
		{
			mouseExited( new MouseEvent( this, 0, 0, 0, 0, 0, 0, 0, 0, false, 0 ) );
		}
	}

	@Override
	public void receivePatchTabClose()
	{
		if( this.isSelected() )
		{
//			log.debug("Received tab close signal, will deselect edit button");
			this.setSelected( false );
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
