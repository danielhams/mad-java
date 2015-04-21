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

import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madstdctrls.MadButton;
import uk.co.modularaudio.util.audio.gui.madstdctrls.MadControlConstants;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SoundfilePlayerSelectFileUiJComponent extends MadButton
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private final SoundfilePlayerMadUiInstance uiInstance;

	private String lastUsedDirectory = null;
	private String currentFilename = "";

	public SoundfilePlayerSelectFileUiJComponent( final SoundfilePlayerMadDefinition definition,
			final SoundfilePlayerMadInstance instance,
			final SoundfilePlayerMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( MadControlConstants.STD_BUTTON_COLOURS, "/\\" );
		this.uiInstance = uiInstance;
		this.lastUsedDirectory = uiInstance.getMusicRoot();
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
		openFileChooser.setCurrentDirectory( new File( lastUsedDirectory ) );
		final int retVal = openFileChooser.showOpenDialog( this );
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			// Store the directory we navigated to so next open uses it. Will be
			// specific to the control instance of course
			final File f = openFileChooser.getSelectedFile();

			if (f != null && !f.isDirectory())
			{
				lastUsedDirectory = f.getParent();
				passChangeToInstanceData( f.getAbsolutePath() );
			}
		}
	}
}
