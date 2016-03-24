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

package uk.co.modularaudio.mads.base.soundfile_player2.ui;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCToggleButton;

public class SoundfilePlayer2PlayStopUiJComponent extends LWTCToggleButton
		implements IMadUiControlInstance<SoundfilePlayer2MadDefinition, SoundfilePlayer2MadInstance, SoundfilePlayer2MadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private final SoundfilePlayer2MadUiInstance uiInstance;

	public SoundfilePlayer2PlayStopUiJComponent( final SoundfilePlayer2MadDefinition definition,
			final SoundfilePlayer2MadInstance instance,
			final SoundfilePlayer2MadUiInstance uiInstance,
			final int controlIndex )
	{
		super( LWTCControlConstants.STD_TOGGLE_BUTTON_COLOURS, "Play/Stop", true, false );
		this.uiInstance = uiInstance;
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final boolean selected )
	{
		final SoundfilePlayer2MadInstance.PlayingState desiredState = (selected ?
				SoundfilePlayer2MadInstance.PlayingState.PLAYING :
				SoundfilePlayer2MadInstance.PlayingState.STOPPED );
		uiInstance.sendPlayingStateChange(desiredState);
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void receiveUpdateEvent( final boolean previousValue, final boolean newValue )
	{
		passChangeToInstanceData( newValue );
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
