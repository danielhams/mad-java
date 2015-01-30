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

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane;

import java.awt.event.ActionEvent;

import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserUiBufferState;
import uk.co.modularaudio.mads.base.audioanalyser.ui.BufferFreezeListener;
import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserUiBufferState.PanDirection;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacButton;

public class LeftButton extends PacButton
	implements BufferFreezeListener
{
	private static final long serialVersionUID = -7907941938527570894L;

//	private static Log log = LogFactory.getLog( LeftButton.class.getName() );

	private final AudioAnalyserUiBufferState uiBufferState;

	public LeftButton( AudioAnalyserUiBufferState uiBufferState )
	{
		this.uiBufferState = uiBufferState;
		setText("<");
		
		uiBufferState.addBufferFreezeListener(this);
	}

	@Override
	public void receiveEvent(ActionEvent e)
	{
		uiBufferState.pan( PanDirection.BACK );
	}

	@Override
	public void receiveFreezeStateChange(boolean frozen)
	{
		setVisible( frozen );
	}
}
