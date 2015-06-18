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

package uk.co.modularaudio.mads.base.sampleplayer.ui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadDefinition;
import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadInstance;
import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSampleRuntime;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacButton;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SingleSamplePlayerSelectFileUiJComponent extends PacButton
	implements IMadUiControlInstance<SingleSamplePlayerMadDefinition, SingleSamplePlayerMadInstance, SingleSamplePlayerMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	// Wait up to twenty milliseconds for the new sample to be used.
	private static final long WAIT_TO_SWAP_NANOS = 1000 * 1000 * 20;

	private static Log log = LogFactory.getLog( SingleSamplePlayerSelectFileUiJComponent.class.getName() );

	private String lastUsedFilePath;

	private final SingleSamplePlayerMadInstance instance;

	private final AdvancedComponentsFrontController advancedComponentsFrontController;

	public SingleSamplePlayerSelectFileUiJComponent(
			final SingleSamplePlayerMadDefinition definition,
			final SingleSamplePlayerMadInstance instance,
			final SingleSamplePlayerMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.instance = instance;

		this.advancedComponentsFrontController = instance.advancedComponentsFrontController;
		this.setOpaque( false );
		setFont( this.getFont().deriveFont( 9f ) );
		this.setText( "/\\" );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final String filename )
	{
		lastUsedFilePath = filename;
		try
		{
			// First start caching the new sample
			final SingleSampleRuntime sampleRuntime = new SingleSampleRuntime( advancedComponentsFrontController, filename );

			final SingleSampleRuntime previousSampleRuntime = instance.desiredSampleRuntime.get();
			// Push the new one
			instance.desiredSampleRuntime.set( sampleRuntime );

			// Wait until it's marked as used.
			final long startTime = System.nanoTime();
			long curTime;
			boolean wasUsed;
			do
			{
				curTime = System.nanoTime();
				wasUsed = sampleRuntime.isUsed();
			}
			while( wasUsed == false && curTime < (startTime + WAIT_TO_SWAP_NANOS ) );

			if( previousSampleRuntime != null )
			{
				// Now we can de-allocate the previous clients
				previousSampleRuntime.destroy();
			}
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught attempting to use new sample file in sample player: " + e.toString();
			log.error( msg, e );
		}

	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void receiveEvent( final ActionEvent e )
	{
		final JFileChooser openFileChooser = new JFileChooser();
		final String musicDir = advancedComponentsFrontController.getSoundfileMusicRoot();
		openFileChooser.setCurrentDirectory( new File( musicDir ) );
		final int retVal = openFileChooser.showOpenDialog( this );
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			// Store the directory we navigated to so next open uses it. Will be
			// specific to the control instance of course
			final File f = openFileChooser.getSelectedFile();

			if (f != null)
			{
				if( log.isDebugEnabled() )
				{
					log.debug( "Attempting to use audio file " + f.getPath() );
				}
				passChangeToInstanceData( f.getAbsolutePath() );
			}
		}
	}

	@Override
	public String getControlValue()
	{
		return (lastUsedFilePath != null ? lastUsedFilePath : "" );
	}

	@Override
	public void receiveControlValue( final String value )
	{
		if( value != null && !value.equals( "" ) )
		{
			passChangeToInstanceData( value );
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
