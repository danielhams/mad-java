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

package uk.co.modularaudio.mads.base.scopegen.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.scopegen.mu.ScopeGenMadDefinition;
import uk.co.modularaudio.mads.base.scopegen.mu.ScopeGenMadInstance;
import uk.co.modularaudio.mads.base.scopegen.ui.display.ScopeAmpLabels;
import uk.co.modularaudio.mads.base.scopegen.ui.display.ScopeAmpMarks;
import uk.co.modularaudio.mads.base.scopegen.ui.display.ScopeBottomSignalToggles;
import uk.co.modularaudio.mads.base.scopegen.ui.display.ScopeEmptyPlot;
import uk.co.modularaudio.mads.base.scopegen.ui.display.ScopeTimeLabels;
import uk.co.modularaudio.mads.base.scopegen.ui.display.ScopeTimeMarks;
import uk.co.modularaudio.mads.base.scopegen.ui.display.ScopeTopPanel;
import uk.co.modularaudio.mads.base.scopegen.ui.display.ScopeWaveDisplay;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.date.DateConverter;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.dialog.filesave.FileSaveDialog;
import uk.co.modularaudio.util.swing.dialog.filesave.FileSaveDialogCallback;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.toggle.ToggleReceiver;

public class ScopeGenDisplayUiJComponent<D extends ScopeGenMadDefinition<D, I>,
	I extends ScopeGenMadInstance<D, I>,
	U extends ScopeGenMadUiInstance<D, I>>
	extends JPanel
	implements IMadUiControlInstance<D, I, U>, ToggleReceiver, ScopeImageSaver
{
	private static final long serialVersionUID = 5515402437483693770L;

	private static Log log = LogFactory.getLog( ScopeGenDisplayUiJComponent.class.getName() );

	public static final int AMP_LABELS_WIDTH = 25;
	public static final int AXIS_MARKS_LENGTH = 6;
	public static final int AMP_DISPLAY_RIGHT_PADDING = 14;
	public static final int AMP_DISPLAY_TOP_PADDING = 24;
	public static final int TIME_LABELS_HEIGHT = 12;
	public static final int TIME_DISPLAY_BOTTOM_PADDING = AMP_DISPLAY_TOP_PADDING;

	private static final String IMAGE_SAVE_DATE_TIME_STR_FORMAT = "yyyy_MM_dd_HH_mm_ss";

	private final U uiInstance;

	private final ScopeAmpLabels<D, I, U> ampLabels;
	private final ScopeTopPanel topPanel;
	private final ScopeAmpMarks ampMarks;
	private final ScopeWaveDisplay<D, I, U> waveDisplay;
	private final ScopeEmptyPlot rightEmptyPlot;
	private final ScopeTimeMarks timeMarks;
	private final ScopeTimeLabels<D, I, U> timeLabels;
	private final ScopeBottomSignalToggles bottomSignalToggles;

	private final ReentrantLock paintingLock = new ReentrantLock( true );
	private final AllocationMatch localAllocationMatch = new AllocationMatch();

	public ScopeGenDisplayUiJComponent( final D definition,
			final I instance,
			final U uiInstance,
			final int controlIndex,
			final int numAmpMarks,
			final int numTimeMarks,
			final int ampNumDecimalPlaces )
	{
		this.uiInstance = uiInstance;

		setOpaque( true );
		setBackground( ScopeGenColours.BACKGROUND_COLOR );
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 5" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		msh.addColumnConstraint(
				"[" + AMP_LABELS_WIDTH + "px]" +
				"[" + AXIS_MARKS_LENGTH + "px]" +
				"[]" +
				"[" + AMP_DISPLAY_RIGHT_PADDING + "px]" );

		msh.addRowConstraint(
				"[" + AMP_DISPLAY_TOP_PADDING + "px]" +
				"[]" +
				"[" + AXIS_MARKS_LENGTH + "px]" +
				"[" + TIME_LABELS_HEIGHT + "px]" +
				"[" + TIME_DISPLAY_BOTTOM_PADDING + "px]" );

		setLayout( msh.createMigLayout() );

		ampLabels = new ScopeAmpLabels<D, I, U>( uiInstance, numAmpMarks, ampNumDecimalPlaces );
		topPanel = new ScopeTopPanel( this, this );
		ampMarks = new ScopeAmpMarks( numAmpMarks );
		waveDisplay = new ScopeWaveDisplay<D, I, U>( uiInstance, numTimeMarks, numAmpMarks );
		rightEmptyPlot = new ScopeEmptyPlot();
		timeMarks = new ScopeTimeMarks( numTimeMarks );
		timeLabels = new ScopeTimeLabels<D, I, U>( uiInstance, numTimeMarks );
		bottomSignalToggles = new ScopeBottomSignalToggles( this );

		this.add( ampLabels, "cell 0 0, spany 3, growy" );
		this.add( topPanel, "cell 1 0, spanx 3, center, growx" );
		this.add( ampMarks, "cell 1 1, grow" );
		this.add( waveDisplay, "cell 2 1, grow, push" );
		this.add( rightEmptyPlot, "cell 3 1, grow" );
		this.add( timeMarks, "cell 1 2, spanx 3, growx" );
		this.add( timeLabels, "cell 0 3, spanx 4, growx" );
		this.add( bottomSignalToggles, "cell 0 4, spanx 4, center, growx" );

		uiInstance.setScopeImageSaver( this );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public String getControlValue()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( topPanel.getBiUniPolarToggle().getControlValue() );
		sb.append( '|' );
		sb.append( topPanel.getTriggerToggle().getControlValue() );
		sb.append( '|' );
		sb.append( bottomSignalToggles.getControlValue( 0 ) );
		sb.append( '|' );
		sb.append( bottomSignalToggles.getControlValue( 1 ) );
		sb.append( '|' );
		sb.append( bottomSignalToggles.getControlValue( 2 ) );
		sb.append( '|' );
		sb.append( bottomSignalToggles.getControlValue( 3 ) );
		return sb.toString();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		final String[] vals = value.split("\\|");
		if( vals.length == 6 )
		{
			topPanel.getBiUniPolarToggle().receiveControlValue( vals[0] );
			topPanel.getTriggerToggle().receiveControlValue( vals[1] );

			bottomSignalToggles.receiveControlValues(
					vals[2],
					vals[3],
					vals[4],
					vals[5] );
		}
		else
		{
			if( log.isErrorEnabled() )
			{
				log.error("Failed to obtain number of expected init params. Expected 6 got " + vals.length );
			}
		}
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
	{
		waveDisplay.doDisplayProcessing( tempEventStorage, timingParameters, currentGuiTime );
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void destroy()
	{
	}

	public static int getAdjustedHeightOfDisplay( final int height, final int numAmpMarkers )
	{
		return getAdjustedHeightBetweenMarkers( height, numAmpMarkers ) * (numAmpMarkers-1);
	}

	static public int getAdjustedHeightBetweenMarkers( final int height, final int numAmpMarkers )
	{
		return (int)Math.floor(height / (numAmpMarkers-1));
	}

	static public int getAdjustedWidthOfDisplay( final int width, final int numFreqMarkers )
	{
		return getAdjustedWidthBetweenMarkers( width, numFreqMarkers ) * (numFreqMarkers-1);
	}

	static public int getAdjustedWidthBetweenMarkers( final int width, final int numFreqMarkers )
	{
		return (int)Math.floor(width / (numFreqMarkers-1));
	}

	@Override
	public void receiveToggle( final int toggleId, final boolean active )
	{
//		log.trace("Received toggle of " + toggleId + " to " + active );
		if( toggleId >= 0 )
		{
			waveDisplay.setSignalVisibility( toggleId, active );
		}
		else
		{
			ampLabels.setBiUniPolar( active );
			waveDisplay.setBiUniPolar( active );
		}
	}

	@Override
	public void paint( final Graphics g )
	{
		try
		{
			paintingLock.lock();
			super.paint( g );
		}
		finally
		{
			paintingLock.unlock();
		}
	}

	@Override
	public void saveImage()
	{
		log.debug("Would save an image");

		// Allocate an appropriately sized buffered image, paint into it
		// and then show a save dialog to select where to save
		// We need a mutex on the paint so that we aren't making a mess
		final BufferedImageAllocator bia = uiInstance.getUiDefinition().getBufferedImageAllocator();
		TiledBufferedImage tbi = null;
		try
		{
			final int width = getWidth();
			final int height = getHeight();
			tbi = bia.allocateBufferedImage( "ScopeWaveDisplay",
					localAllocationMatch  ,
					AllocationLifetime.SHORT,
					AllocationBufferType.TYPE_INT_RGB,
					width, height );
			final BufferedImage bi = tbi.getUnderlyingBufferedImage();
			final Graphics2D g2d = bi.createGraphics();
			paint( g2d );

			final FileSaveDialog fsd = new FileSaveDialog();

			final String dateTimeStr = DateConverter.javaDateToCustomDateTimeStr( new Date(),
					IMAGE_SAVE_DATE_TIME_STR_FORMAT );

			final String suggestedFilename = dateTimeStr + ".png";

			final FileSaveDialogCallback saveCallback = new FileSaveDialogCallback()
			{

				@Override
				public void receiveFileSaveDialogClosed( final String fileSavePath )
				{
					try
					{
						ImageIO.write( bi, "png", new File(fileSavePath) );
					}
					catch( final IOException e )
					{
						final String msg = "IOExcepiton caught saving image: " + e.toString();
						log.error( msg, e );
					}
				}
			};

			fsd.setValues( this,
					"Where would like the image saved?",
					"Save Scope Image",
					JOptionPane.QUESTION_MESSAGE,
					".",
					suggestedFilename,
					saveCallback );

			fsd.go();
		}
		catch( final DatastoreException e )
		{
			final String msg = "DatastoreExcepiton caught saving image: " + e.toString();
			log.error( msg, e );
		}
		finally
		{
			if( tbi != null )
			{
				try
				{
					bia.freeBufferedImage( tbi );
				}
				catch( final DatastoreException e )
				{
					final String msg = "Failed during release of tiled buffered image: " + e.toString();
					log.error( msg, e );
				}
			}
		}


	}
}
