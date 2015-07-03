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
import java.awt.Rectangle;
import java.util.Arrays;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.mads.base.spectralamp.util.SpecDataListener;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.NoAverageComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.RunningAverageComputer;

public class SpectralAmpPeakDisplayUiJComponent extends JPanel
implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>,
	AmpAxisChangeListener, FreqAxisChangeListener, RunningAvChangeListener,
	SpecDataListener, SampleRateListener
{
	private static final long serialVersionUID = -180425607349546323L;

//	private static Log log = LogFactory.getLog( SpectralAmpPeakDisplayUiJComponent.class.getName() );

	private boolean previouslyShowing;
	private final SpectralAmpMadUiInstance uiInstance;
	private FrequencyScaleComputer freqScaleComputer;
	private AmpScaleComputer ampScaleComputer;
	private RunningAverageComputer runAvComputer;

	private int sampleRate = DataRate.CD_QUALITY.getValue();

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

	// A precomputed list of X pixels to which spectral bin
	// they map to
	private int[] pixelToBinLookupTable;

	// Where we store our polygon X/Y coords for
	// the spectral body (a polygon)
	private int[] polygonXPoints;
	private int[] polygonYPoints;

	// And the running average (a polyline)
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
		uiInstance.addSampleRateListener( this );

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
			g.drawLine( 0, lineY, magsWidth - 1, lineY );
		}

		for( int j = 1 ; j < SpectralAmpFreqAxisDisplay.NUM_MARKERS ; ++j )
		{
			final int lineX = (int)(horizPixelsPerMarker * j);
			g.drawLine( lineX, SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET, lineX, height - 1 );
		}
	}

	private void drawBodyAndRunAv( final Graphics g )
	{
		// Start after the origin point
		int bodyPointOffset = 1;
		int runAvPointOffset = 0;

		int previousBinDrawn = -1;

		for( int i = 0 ; i < magsWidth - 1; i++ )
		{
			final int whichBin = pixelToBinLookupTable[i];

			if( whichBin != previousBinDrawn )
			{
				// Computing the spectral body amplitude
				// and running average in screen space
				final int bucketMappedBodyValue;
				final int bucketMappedRunAvValue;

				if( previousBinDrawn == -1 || whichBin == previousBinDrawn + 1 )
				{
					final float bodyValForBin = computedBins[ whichBin ];
					final float normalisedBodyBinValue = bodyValForBin / AmpScaleComputer.APPROX_POLAR_AMP_SCALE_FACTOR;
					bucketMappedBodyValue = ampScaleComputer.rawToMappedBucketMinMax( magsHeight, normalisedBodyBinValue );

					final float runAvValForBin = runningBinPeaks[ whichBin ];
					final float normalisedRunAvBinValue = runAvValForBin / AmpScaleComputer.APPROX_POLAR_AMP_SCALE_FACTOR;
					bucketMappedRunAvValue = ampScaleComputer.rawToMappedBucketMinMax( magsHeight, normalisedRunAvBinValue );
				}
				else
				{
					float maxNormalisedValue = 0.0f;
					float maxNormalisedRunAvValue = 0.0f;
					for( int sb = previousBinDrawn + 1 ; sb <= whichBin ; ++sb )
					{
						final float bodyValForBin = computedBins[ sb ];
						final float normalisedBodyBinValue = bodyValForBin / AmpScaleComputer.APPROX_POLAR_AMP_SCALE_FACTOR;
						if( normalisedBodyBinValue > maxNormalisedValue )
						{
							maxNormalisedValue = normalisedBodyBinValue;
						}
						final float runAvValForBin = runningBinPeaks[ sb ];
						final float normalisedRunAvBinValue = runAvValForBin / AmpScaleComputer.APPROX_POLAR_AMP_SCALE_FACTOR;
						if( normalisedRunAvBinValue > maxNormalisedRunAvValue )
						{
							maxNormalisedRunAvValue = normalisedRunAvBinValue;
						}
					}
					bucketMappedBodyValue = ampScaleComputer.rawToMappedBucketMinMax( magsHeight, maxNormalisedValue );
					bucketMappedRunAvValue = ampScaleComputer.rawToMappedBucketMinMax( magsHeight, maxNormalisedRunAvValue );
				}

				polygonXPoints[ bodyPointOffset ] = i;
				polygonYPoints[ bodyPointOffset ] = magsHeight - bucketMappedBodyValue + yOffset;
				bodyPointOffset++;

				polylineXPoints[ runAvPointOffset ] = i;
				polylineYPoints[ runAvPointOffset ] = magsHeight - bucketMappedRunAvValue + yOffset;
				polylineExtraXPoints[ runAvPointOffset ] = i;
				polylineExtraYPoints[ runAvPointOffset ] = polylineYPoints[ runAvPointOffset ] - 1;
				runAvPointOffset++;

				previousBinDrawn = whichBin;
			}
		}

		// Final pixel is a pain because it isn't necessarily a new bin
		// but we need a value for it
		final int finalPixel = magsWidth - 1;
		final int whichBin = pixelToBinLookupTable[ finalPixel ];

		final float bodyValForBin = computedBins[ whichBin ];
		final float normalisedBodyBinValue = bodyValForBin / AmpScaleComputer.APPROX_POLAR_AMP_SCALE_FACTOR;
		final int bucketMappedBodyValue = ampScaleComputer.rawToMappedBucketMinMax( magsHeight, normalisedBodyBinValue );

		polygonXPoints[ bodyPointOffset ] = finalPixel;
		polygonYPoints[ bodyPointOffset ] = magsHeight - bucketMappedBodyValue + yOffset;
		bodyPointOffset++;

		final float runAvValForBin = runningBinPeaks[ whichBin ];
		final float normalisedRunAvBinValue = runAvValForBin / AmpScaleComputer.APPROX_POLAR_AMP_SCALE_FACTOR;
		final int bucketMappedRunAvValue = ampScaleComputer.rawToMappedBucketMinMax( magsHeight, normalisedRunAvBinValue );
		polylineXPoints[ runAvPointOffset ] = finalPixel;
		polylineYPoints[ runAvPointOffset ] = magsHeight - bucketMappedRunAvValue + yOffset;
		polylineExtraXPoints[ runAvPointOffset ] = finalPixel;
		polylineExtraYPoints[ runAvPointOffset ] = polylineYPoints[ runAvPointOffset ] - 1;
		runAvPointOffset++;

		// Close off the polygon
		polygonXPoints[ bodyPointOffset ] = magsWidth;
		polygonYPoints[ bodyPointOffset ] = height;
		bodyPointOffset++;

		// And loop back the extra points in the polyline
		final int numInOneLine = runAvPointOffset;
		for( int i = 0 ; i < numInOneLine ; ++i )
		{
			polylineXPoints[ runAvPointOffset ] = polylineExtraXPoints[ numInOneLine - i - 1 ];
			polylineYPoints[ runAvPointOffset ] = polylineExtraYPoints[ numInOneLine - i - 1 ];
			runAvPointOffset++;
		}

		g.fillPolygon( polygonXPoints, polygonYPoints, bodyPointOffset );

		if( !(runAvComputer instanceof NoAverageComputer) )
		{
			g.setColor( SpectralAmpColours.RUNNING_PEAK_COLOUR );
			g.drawPolyline( polylineXPoints, polylineYPoints, runAvPointOffset );
		}
	}

	@Override
	public void paint( final Graphics g )
	{
		if( polygonXPoints == null )
		{
			final int maxPointsInPolygon = (width + 2 );

			polygonXPoints = new int[ maxPointsInPolygon ];
			polygonYPoints = new int[ maxPointsInPolygon ];
			polygonXPoints[ 0 ] = 0;
			polygonYPoints[ 0 ] = height;

			final int maxPolylinePoints = width;
			// We'll use double and copy over the extra points
			// once we know how many there are
			polylineXPoints = new int[ maxPolylinePoints * 2 ];
			polylineYPoints = new int[ maxPolylinePoints * 2 ];
			polylineExtraXPoints = new int[ maxPolylinePoints ];
			polylineExtraYPoints = new int[ maxPolylinePoints ];
		}

		g.setColor( SpectralAmpColours.BACKGROUND_COLOR );
		g.fillRect( 0, 0, width, height );
		g.setColor( SpectralAmpColours.SPECTRAL_BODY );

		drawBodyAndRunAv( g );

		paintGridLines( g );
	}

	@Override
	public void receiveFreqScaleChange( final FrequencyScaleComputer freqScaleComputer )
	{
		this.freqScaleComputer = freqScaleComputer;
		recomputePixelToBinLookup();
	}

	@Override
	public void receiveAmpScaleChange( final AmpScaleComputer ampScaleComputer )
	{
		this.ampScaleComputer = ampScaleComputer;
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

		pixelToBinLookupTable = new int[ magsWidth ];

		recomputePixelToBinLookup();
	}

	@Override
	public void receiveFftSizeChange( final int desiredFftSize )
	{
		final int numBins = (desiredFftSize / 2) + 1;
		currentNumBins = numBins;
		recomputePixelToBinLookup();
		clear();
	}

	private void recomputePixelToBinLookup()
	{
		if( pixelToBinLookupTable != null && freqScaleComputer != null )
		{
			final float nyquistFrequency = sampleRate / 2.0f;
			final float freqPerBin = nyquistFrequency / (currentNumBins - 1);
			// We adjust the frequency by half the bin width so
			// that we don't jump the gun and use a bin too early.
			final float binStartFreqOffset = freqPerBin / 2;

			for( int i = 0 ; i < magsWidth ; i++ )
			{
				final float pixelRawFreq = freqScaleComputer.mappedBucketToRawMinMax( magsWidth, i );
				float adjustedBinFloat = (pixelRawFreq - binStartFreqOffset) / freqPerBin;
				adjustedBinFloat = (adjustedBinFloat < 0.0f ? 0.0f : adjustedBinFloat );
				int whichBin = Math.round( adjustedBinFloat );
//				log.debug("Pixel " + i + " has raw freq " + MathFormatter.slowFloatPrint( pixelRawFreq, 3, false ) + " which we adjust to " +
//						MathFormatter.slowFloatPrint( adjustedBinFloat, 3, false ) + " which maps to " + whichBin );
				// We might occasionally get asked to generate this lookup table before
				// we're notified of the sample rate change. Make sure we're not
				// filling with rubbish.
				if( whichBin > currentNumBins - 1 )
				{
					whichBin = currentNumBins - 1;
				}
				pixelToBinLookupTable[ i ] = whichBin;
			}
		}
	}

	@Override
	public void receiveSampleRateChange( final int sampleRate )
	{
		this.sampleRate = sampleRate;
		recomputePixelToBinLookup();
	}
}
