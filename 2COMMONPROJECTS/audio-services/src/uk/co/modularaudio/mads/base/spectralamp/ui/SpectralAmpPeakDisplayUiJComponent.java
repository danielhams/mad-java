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

package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.mads.base.spectralamp.util.SpecDataListener;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.RunningAverageComputer;

public class SpectralAmpPeakDisplayUiJComponent extends JPanel
implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>,
	AmpAxisChangeListener, FreqAxisChangeListener, RunningAvChangeListener,
	SpecDataListener
{
	private static final long serialVersionUID = -180425607349546323L;

//	private static Log log = LogFactory.getLog( NonBiBackedPeakDisplay.class.getName() );

	private boolean previouslyShowing;
	private final SpectralAmpMadUiInstance uiInstance;

	private final float[] runningBinPeaks;
	private final float[] previousBinPeaks;
	private final float[] computedBins;

	private int currentNumBins;

	private BufferedImage bi;
	private Graphics2D biG2d;

	private int[] polygonXPoints;
	private int[] polygonYPoints;

	private int[] polylineXPoints;
	private int[] polylineYPoints;
	// A second poly line one Y below the other to avoid using a wider stroke
	private int[] polylineExtraXPoints;
	private int[] polylineExtraYPoints;

	public SpectralAmpPeakDisplayUiJComponent( final SpectralAmpMadDefinition definition,
			final SpectralAmpMadInstance instance,
			final SpectralAmpMadUiInstance uiInstance,
			final int controlIndex )
	{
		setOpaque( true );
		setBackground( SpectralAmpColours.BACKGROUND_COLOR );
		this.uiInstance = uiInstance;

		runningBinPeaks = new float[ SpectralAmpMadDefinition.MAX_NUM_FFT_BINS ];
		previousBinPeaks = new float[ SpectralAmpMadDefinition.MAX_NUM_FFT_BINS ];
		computedBins = new float[ SpectralAmpMadDefinition.MAX_NUM_FFT_BINS ];

		uiInstance.addAmpAxisChangeListener( this );
		uiInstance.addFreqAxisChangeListener( this );
		uiInstance.addRunAvChangeListener( this );

		uiInstance.setSpecDataListener( this );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public String getControlValue()
	{
		return "";
	}

	@Override
	public void receiveControlValue( final String value )
	{
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		final boolean showing = isShowing();

		if( previouslyShowing != showing )
		{
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
		}
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

	private void paintGridLines( final Graphics g, final int width, final int height )
	{
		final int widthForAmps = width - SpectralAmpMadUiDefinition.SCALES_WIDTH_OFFSET - 1;
		final int heightForAmps = height - SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET - 1;
		// Draw the axis lines
		g.setColor( SpectralAmpColours.SCALE_AXIS_DETAIL );

		final float vertPixelsPerMarker = heightForAmps / ((float)SpectralAmpAmpAxisDisplay.NUM_MARKERS - 1);

		for( int i = 0 ; i < SpectralAmpAmpAxisDisplay.NUM_MARKERS - 1 ; ++i )
		{

			final int lineY = SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET + (int)(vertPixelsPerMarker * i);
			g.drawLine( 0, lineY, widthForAmps, lineY );
		}
		final float horizPixelsPerMarker = widthForAmps / ((float)SpectralAmpFreqAxisDisplay.NUM_MARKERS - 1);
		for( int j = 1 ; j < SpectralAmpFreqAxisDisplay.NUM_MARKERS ; ++j )
		{
			final int lineX = (int)(horizPixelsPerMarker * j);
			g.drawLine( lineX, SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET, lineX, height );
		}
	}

	private int setupPolygons( final int width,
			final int height,
			final float[] bins,
			final int numBins )
	{
		final int magsWidth = width - SpectralAmpMadUiDefinition.SCALES_WIDTH_OFFSET - 1;
		final int magsHeight = height - SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET - 1;

		final int yOffset = SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET + 1;

		// Start after the origin point
		int pointOffset = 1;

		int previousBinDrawn = -1;

//		log.debug("Mags height is " + magsHeight );

		final FrequencyScaleComputer freqScaleComputer = uiInstance.getDesiredFreqScaleComputer();
		final float maxFrequency = freqScaleComputer.getMaxFrequency();
		final float freqPerBin = maxFrequency / (numBins - 1);

		final AmpScaleComputer ampScaleComputer = uiInstance.getDesiredAmpScaleComputer();

		for( int i = 0 ; i < magsWidth ; i++ )
		{
			final float pixelRawFreq = freqScaleComputer.mappedBucketToRawMinMax( magsWidth, i );
			final int whichBin = Math.round( pixelRawFreq / freqPerBin );

			if( whichBin != previousBinDrawn )
			{
				previousBinDrawn = whichBin;

				final float valForBin = bins[ whichBin ];

				final float normalisedBinValue = valForBin / AmpScaleComputer.APPROX_POLAR_AMP_SCALE_FACTOR;
				final int bucketMappedValue = ampScaleComputer.rawToMappedBucketMinMax( magsHeight, normalisedBinValue );

//				log.debug("For bin " + i + " with value " + valForBin + " the nbv(" + normalisedBinValue + ") bmv(" + bucketMappedValue + ")");

				polygonXPoints[ pointOffset ] = i;
				polygonYPoints[ pointOffset ] = magsHeight - bucketMappedValue + yOffset;
				pointOffset++;
			}
		}

		polygonXPoints[ pointOffset ] = magsWidth - 1;
		polygonYPoints[ pointOffset ] = height;
		pointOffset++;

		return pointOffset;
	}

	private int setupPolyline( final int width,
			final int height,
			final float[] bins,
			final int numBins )
	{
		final int magsWidth = width - SpectralAmpMadUiDefinition.SCALES_WIDTH_OFFSET - 1;
		final int magsHeight = height - SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET - 1;

		final int yOffset = SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET + 1;

		// Start after the origin point
		int pointOffset = 0;

		int previousBinDrawn = -1;

//		log.debug("Mags height is " + magsHeight );

		final FrequencyScaleComputer freqScaleComputer = uiInstance.getDesiredFreqScaleComputer();
		final float maxFrequency = freqScaleComputer.getMaxFrequency();
		final float freqPerBin = maxFrequency / (numBins - 1);

		final AmpScaleComputer ampScaleComputer = uiInstance.getDesiredAmpScaleComputer();

		for( int i = 0 ; i < magsWidth ; i++ )
		{
			final float pixelRawFreq = freqScaleComputer.mappedBucketToRawMinMax( magsWidth, i );
			final int whichBin = Math.round( pixelRawFreq / freqPerBin );

			if( whichBin != previousBinDrawn )
			{
				previousBinDrawn = whichBin;

				final float valForBin = bins[ whichBin ];

				final float normalisedBinValue = valForBin / AmpScaleComputer.APPROX_POLAR_AMP_SCALE_FACTOR;
				final int bucketMappedValue = ampScaleComputer.rawToMappedBucketMinMax( magsHeight, normalisedBinValue );

//				log.debug("For bin " + i + " with value " + valForBin + " the nbv(" + normalisedBinValue + ") bmv(" + bucketMappedValue + ")");

				polylineXPoints[ pointOffset ] = i;
				polylineYPoints[ pointOffset ] = magsHeight - bucketMappedValue + yOffset;
				polylineExtraXPoints[ pointOffset ] = i;
				polylineExtraYPoints[ pointOffset ] = polylineYPoints[ pointOffset ] - 1;
				pointOffset++;
			}
		}

		return pointOffset;
	}

	private void internalOptimisedPaint( final Graphics g, final int width, final int height )
	{
		final int numPointsInPolygon = setupPolygons( width, height, computedBins, currentNumBins );

		g.setColor( SpectralAmpColours.BACKGROUND_COLOR );
		g.fillRect( 0, 0, width, height );
		g.setColor( SpectralAmpColours.SPECTRAL_BODY );

		g.fillPolygon( polygonXPoints, polygonYPoints, numPointsInPolygon );

		final int numPointsInPolyline = setupPolyline( width, height, runningBinPeaks, currentNumBins );

		g.setColor( SpectralAmpColours.RUNNING_PEAK_COLOUR );

		g.drawPolyline( polylineXPoints, polylineYPoints, numPointsInPolyline );
		g.drawPolyline( polylineExtraXPoints, polylineExtraYPoints, numPointsInPolyline );

		paintGridLines( g, width, height );
	}


	@Override
	public void paintComponent( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();

		if( bi == null )
		{
			bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
			biG2d = bi.createGraphics();

			final int maxPointsInPolygon = (width + 2 );

			polygonXPoints = new int[ maxPointsInPolygon ];
			polygonYPoints = new int[ maxPointsInPolygon ];
			polygonXPoints[ 0 ] = 0;
			polygonYPoints[ 0 ] = height;

			final int maxPolylinePoints = width;

			polylineXPoints = new int[ maxPolylinePoints ];
			polylineYPoints = new int[ maxPolylinePoints ];
			polylineExtraXPoints = new int[ maxPolylinePoints ];
			polylineExtraYPoints = new int[ maxPolylinePoints ];
		}

		final boolean USE_BUFFERED_IMAGE = false;

		if( !USE_BUFFERED_IMAGE )
		{
			internalOptimisedPaint( g, width, height );
		}
		else
		{
			internalOptimisedPaint( biG2d, width, height );

			g.drawImage( bi, 0, 0, null );
		}
	}

	@Override
	public void receiveFreqScaleChange()
	{
		clear();
	}

	@Override
	public void receiveAmpScaleChange()
	{
	}

	@Override
	public void receiveRunAvComputer( final RunningAverageComputer desiredRunningAverageComputer )
	{
	}

	@Override
	public void processScopeData( final float[] amps )
	{
//		log.debug("Received new scope data.");
		assert( amps.length == currentNumBins );
		System.arraycopy( amps, 0, computedBins, 0, currentNumBins );

		final RunningAverageComputer runAvComputer = uiInstance.getDesiredRunningAverageComputer();

		runAvComputer.computeNewRunningAverages( currentNumBins, computedBins, runningBinPeaks );

		repaint();
	}

	private void clear()
	{
		Arrays.fill( runningBinPeaks, 0.0f );
		Arrays.fill( previousBinPeaks, 0.0f );
		Arrays.fill( computedBins, 0.0f );
	}

	@Override
	public void setNumBins( final int numBins )
	{
		currentNumBins = numBins;
		clear();
	}

}
