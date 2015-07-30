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

package uk.co.modularaudio.mads.base.soundfile_player.ui;

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.util.atomicio.FileUtilities;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class SoundfilePlayerSelectFileUiJComponent extends LWTCButton
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private static final char POS_SEP_CHAR = '|';
	private static final String INITIAL_POS_STR = POS_SEP_CHAR + "0";

	private final SoundfilePlayerMadUiInstance uiInstance;

	private String currentFilename = "";
	private String currentDirectory;

	private final AdvancedComponentsFrontController acfc;

	public SoundfilePlayerSelectFileUiJComponent( final SoundfilePlayerMadDefinition definition,
			final SoundfilePlayerMadInstance instance,
			final SoundfilePlayerMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( LWTCControlConstants.STD_BUTTON_COLOURS, "/\\", false );
		this.uiInstance = uiInstance;
		this.acfc = uiInstance.advancedComponentsFrontController;
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final String filenameAndPos )
	{
		if( currentFilename != null )
		{
			final int colonPosition = filenameAndPos.indexOf( POS_SEP_CHAR );
			final String filename = (
					colonPosition == -1 ? filenameAndPos :
					filenameAndPos.substring( 0, colonPosition )
					);
			final long position = (
					colonPosition == -1 ? 0 :
					Long.parseLong( filenameAndPos.substring( colonPosition + 1 ))
					);
			currentFilename = filename;

			if( !FileUtilities.isRelativePath( currentFilename ) )
			{
				final String userMusicDir = acfc.getSoundfileMusicRoot();
				if( currentFilename.startsWith( userMusicDir ) )
				{
					currentFilename = currentFilename.substring( userMusicDir.length() + 1 );
				}
			}
			if( currentFilename.length() > 0 )
			{
				uiInstance.setFileInfo( currentFilename, position );
			}
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
		final StringBuilder sb = new StringBuilder();
		final long songPosition = uiInstance.getCurrentSongPosition();
		sb.append( currentFilename );
		sb.append( POS_SEP_CHAR );
		sb.append( Long.toString(songPosition) );
		return sb.toString();
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

		if( currentDirectory == null )
		{
			currentDirectory = uiInstance.advancedComponentsFrontController.getSoundfileMusicRoot();
		}

		openFileChooser.setCurrentDirectory( new File( currentDirectory ) );
		final int retVal = openFileChooser.showOpenDialog( this );
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			// Store the directory we navigated to so next open uses it. Will be
			// specific to the control instance of course
			final File f = openFileChooser.getSelectedFile();

			if (f != null && !f.isDirectory())
			{
				currentDirectory = f.getParentFile().getAbsolutePath();
				// Make zero the start position
				passChangeToInstanceData( f.getAbsolutePath() + INITIAL_POS_STR );
			}
		}
	}
}
