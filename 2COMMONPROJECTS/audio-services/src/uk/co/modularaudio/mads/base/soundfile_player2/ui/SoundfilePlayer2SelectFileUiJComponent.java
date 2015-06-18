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

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class SoundfilePlayer2SelectFileUiJComponent extends LWTCButton
	implements IMadUiControlInstance<SoundfilePlayer2MadDefinition, SoundfilePlayer2MadInstance, SoundfilePlayer2MadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private final SoundfilePlayer2MadUiInstance uiInstance;

	private String currentFilename = "";

	public SoundfilePlayer2SelectFileUiJComponent( final SoundfilePlayer2MadDefinition definition,
			final SoundfilePlayer2MadInstance instance,
			final SoundfilePlayer2MadUiInstance uiInstance,
			final int controlIndex )
	{
		super( LWTCControlConstants.STD_BUTTON_COLOURS, "/\\", false );
		this.uiInstance = uiInstance;
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final String filename )
	{
		currentFilename = filename;
		if( currentFilename != null && currentFilename.length() > 0 )
		{
			uiInstance.setFileInfo( filename );
		}
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public String getControlValue()
	{
		return currentFilename;
	}

	@Override
	public void receiveControlValue(final String value)
	{
		passChangeToInstanceData( value );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public void receiveClick()
	{
		final JFileChooser openFileChooser = new JFileChooser();
		final String musicDir = uiInstance.advancedComponentsFrontController.getSoundfileMusicRoot();
		openFileChooser.setCurrentDirectory( new File( musicDir ) );
		final int retVal = openFileChooser.showOpenDialog( this );
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			// Store the directory we navigated to so next open uses it. Will be
			// specific to the control instance of course
			final File f = openFileChooser.getSelectedFile();

			if (f != null && !f.isDirectory())
			{
				passChangeToInstanceData( f.getAbsolutePath() );
			}
		}
	}
}
