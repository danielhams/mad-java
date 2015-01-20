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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SoundfilePlayerSelectFileUiJComponent extends NoDisplayPacButton
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;
	
	private final SoundfilePlayerMadUiInstance uiInstance;

	private String lastUsedDirectory = null;
	private String currentFilename = "";
	
	public SoundfilePlayerSelectFileUiJComponent( SoundfilePlayerMadDefinition definition,
			SoundfilePlayerMadInstance instance,
			SoundfilePlayerMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.lastUsedDirectory = uiInstance.getMusicRoot();
		this.setOpaque( true );
//		Font f = this.getFont().deriveFont( 9f );
		Font f = this.getFont();
		setFont( f );
		this.setText( "/\\" );
	}

	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( String filename )
	{
		currentFilename = filename;
		if( currentFilename != null && currentFilename.length() > 0 )
		{
			uiInstance.setFileInfo( filename );
		}
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
	}

	@Override
	public void receiveEvent( ActionEvent e )
	{
		JFileChooser openFileChooser = new JFileChooser();
		openFileChooser.setCurrentDirectory( new File( lastUsedDirectory ) );
		int retVal = openFileChooser.showOpenDialog( this );
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			// Store the directory we navigated to so next open uses it. Will be
			// specific to the control instance of course
			File f = openFileChooser.getSelectedFile();

			if (f != null && !f.isDirectory())
			{
				lastUsedDirectory = f.getParent();
				passChangeToInstanceData( f.getAbsolutePath() );
			}
		}
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
	public void receiveControlValue(String value)
	{
		passChangeToInstanceData( value );
	}
}
