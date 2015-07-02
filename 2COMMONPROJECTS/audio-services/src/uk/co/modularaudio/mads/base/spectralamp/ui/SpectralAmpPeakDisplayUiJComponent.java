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
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	private static Log log = LogFactory.getLog( SpectralAmpPeakDisplayUiJComponent.class.getName() );

	private boolean previouslyShowing;
	private final SpectralAmpMadUiInstance uiInstance;
	private FrequencyScaleComputer freqScaleComputer;
	private AmpScaleComputer ampScaleComputer;
	private RunningAverageComputer runAvComputer;

	// Setup when setBounds is called.
	private int width;
	private int height;
	private int magsWidth;
	private int magsHeight;
	private final static int yOffset = SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET + 1;
	private float vertPixelsPerMarker;
	private float horizPixelsPerMarker;

	// Setup when setNumBins called
	private int currentNumBins;

	private final float[] runningBinPeaks;
	private final float[] previousBinPeaks;
	private final float[] computedBins;

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
		this.uiInstance = uiInstance;
		this.freqScaleComputer = uiInstance.getDesiredFreqScaleComputer();
		this.ampScaleComputer = uiInstance.getDesiredAmpScaleComputer();
		this.runAvComputer = uiInstance.getDesiredRunningAverageComputer();

		setOpaque( true );
		setBackground( SpectralAmpColours.BACKGROUND_COLOR );

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

	private void paintGridLines( final Graphics g )
	{
		g.setColor( SpectralAmpColours.SCALE_AXIS_DETAIL );

		// Draw the axis lines
		for( int i = 0 ; i < SpectralAmpAmpAxisDisplay.NUM_MARKERS - 1 ; ++i )
		{
			final int lineY = SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET + (int)(vertPixelsPerMarker * i);
			g.drawLine( 0, lineY, magsWidth, lineY );
		}

		for( int j = 1 ; j < SpectralAmpFreqAxisDisplay.NUM_MARKERS ; ++j )
		{
			final int lineX = (int)(horizPixelsPerMarker * j);
			g.drawLine( lineX, SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET, lineX, height );
		}
	}

	private int setupPolygons( final float[] bins,
			final int numBins )
	{
		// Start after the origin point
		int pointOffset = 1;

		int previousBinDrawn = -1;

//		log.debug("Mags height is " + magsHeight );

		final float maxFrequency = freqScaleComputer.getMaxFrequency();
		final float freqPerBin = maxFrequency / (numBins - 1);

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

	private int setupPolyline( final float[] bins,
			final int numBins )
	{
		// Start after the origin point
		int pointOffset = 0;

		int previousBinDrawn = -1;

//		log.debug("Mags height is " + magsHeight );

		final float maxFrequency = freqScaleComputer.getMaxFrequency();
		final float freqPerBin = maxFrequency / (numBins - 1);

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

	private void internalOptimisedPaint( final Graphics g )
	{
		g.setColor( SpectralAmpColours.BACKGROUND_COLOR );
		g.fillRect( 0, 0, width, height );
		g.setColor( SpectralAmpColours.SPECTRAL_BODY );

		final int numPointsInPolygon = setupPolygons( computedBins, currentNumBins );
		g.fillPolygon( polygonXPoints, polygonYPoints, numPointsInPolygon );

		g.setColor( SpectralAmpColours.RUNNING_PEAK_COLOUR );

		final int numPointsInPolyline = setupPolyline( runningBinPeaks, currentNumBins );
		g.drawPolyline( polylineXPoints, polylineYPoints, numPointsInPolyline );
		g.drawPolyline( polylineExtraXPoints, polylineExtraYPoints, numPointsInPolyline );

		paintGridLines( g );
	}

	@Override
	public void paint( final Graphics g )
	{
		if( bi == null )
		{
			bi = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage( width, height );
//			bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
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

		final boolean USE_BUFFERED_IMAGE = true;

		if( !USE_BUFFERED_IMAGE )
		{
			internalOptimisedPaint( g );
		}
		else
		{
			internalOptimisedPaint( biG2d );

			g.drawImage( bi, 0, 0, null );
		}
	}

	@Override
	public void receiveFreqScaleChange( final FrequencyScaleComputer freqScaleComputer )
	{
		this.freqScaleComputer = freqScaleComputer;
		clear();
	}

	@Override
	public void receiveAmpScaleChange( final AmpScaleComputer ampScaleComputer )
	{
		this.ampScaleComputer = ampScaleComputer;
		clear();
	}

	@Override
	public void receiveRunAvComputer( final RunningAverageComputer runAvComputer )
	{
		this.runAvComputer = runAvComputer;
		clear();
	}

	@Override
	public void processScopeData( final float[] amps )
	{
//		log.debug("Received new scope data.");
		assert( amps.length == currentNumBins );
		System.arraycopy( amps, 0, computedBins, 0, currentNumBins );

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
	public void setBounds( final Rectangle r )
	{
		super.setBounds( r );

		width = r.width;
		height = r.height;

		magsWidth = width - SpectralAmpMadUiDefinition.SCALES_WIDTH_OFFSET - 1;
		magsHeight = height - SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET - 1;

		vertPixelsPerMarker = magsHeight / ((float)SpectralAmpAmpAxisDisplay.NUM_MARKERS - 1);
		horizPixelsPerMarker = magsWidth / ((float)SpectralAmpFreqAxisDisplay.NUM_MARKERS - 1);
	}

	@Override
	public void receiveFftSizeChange( final int desiredFftSize )
	{
		log.warn("Unsure if we should be doing something here yet");
		final int numBins = (desiredFftSize / 2) + 1;
		currentNumBins = numBins;
		clear();
	}
}
