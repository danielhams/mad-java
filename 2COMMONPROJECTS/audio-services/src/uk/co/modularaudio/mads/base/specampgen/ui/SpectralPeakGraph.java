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

package uk.co.modularaudio.mads.base.specampgen.ui;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Arrays;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.specampgen.util.SpecDataListener;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.NoAverageComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.PeakDetectComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.RunningAverageComputer;
import uk.co.modularaudio.util.audio.stft.StftParameters;

public class SpectralPeakGraph extends JPanel
	implements AmpAxisChangeListener, FreqAxisChangeListener, RunningAvChangeListener,
		SampleRateListener, SpecDataListener
{
	private static final long serialVersionUID = 3612260008902851339L;

//	private static Log log = LogFactory.getLog( SpectralPeakGraph.class.getName() );

	private boolean previouslyShowing;
	private final SpectralAmpGenMadUiInstance<?,?> uiInstance;
	private FrequencyScaleComputer freqScaleComputer;
	private AmpScaleComputer ampScaleComputer;
	private RunningAverageComputer runAvComputer;

	private int sampleRate = DataRate.CD_QUALITY.getValue();

	// Setup when setBounds is called.
	private int width;
	private int height;
	private int magsWidth;
	private int magsHeight;
	private int yOffset;
	private int vertPixelsPerMarker;
	private int horizPixelsPerMarker;

	private final int numAmpMarkers;
	private final int numFreqMarkers;

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

	private float stftExpectedAmpMax = 512.0f;

	// Start after the origin point
	private int bodyPointOffset = 1;
	private int runAvPointOffset = 0;

	public SpectralPeakGraph( final SpectralAmpGenMadUiInstance<?,?> uiInstance,
			final int numAmpMarkers,
			final int numFreqMarkers )
	{
		this.uiInstance = uiInstance;
		this.freqScaleComputer = uiInstance.getDesiredFreqScaleComputer();
		this.ampScaleComputer = uiInstance.getDesiredAmpScaleComputer();
		this.runAvComputer = uiInstance.getDesiredRunningAverageComputer();
		this.numAmpMarkers = numAmpMarkers;
		this.numFreqMarkers = numFreqMarkers;

		setOpaque( true );
		setBackground( SpectralAmpColours.BACKGROUND_COLOR );

		runningBinPeaks = new float[ SpectralAmpGenMadUiDefinition.MAX_NUM_FFT_BINS ];
		previousBinPeaks = new float[ SpectralAmpGenMadUiDefinition.MAX_NUM_FFT_BINS ];
		computedBins = new float[ SpectralAmpGenMadUiDefinition.MAX_NUM_FFT_BINS ];

		uiInstance.addAmpAxisChangeListener( this );
		uiInstance.addFreqAxisChangeListener( this );
		uiInstance.addRunAvChangeListener( this );
		uiInstance.addSampleRateListener( this );

		uiInstance.setSpecDataListener( this );
	}

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

	private void paintGridLines( final Graphics g )
	{
		g.setColor( SpectralAmpColours.SCALE_AXIS_DETAIL );

		// Draw the axis lines
		for( int i = 0 ; i < numAmpMarkers ; ++i )
		{
			final int lineY = (vertPixelsPerMarker * i);
			g.drawLine( 0, lineY, magsWidth - 1, lineY );
		}

		for( int j = 0 ; j < numFreqMarkers ; ++j )
		{
			final int lineX = horizPixelsPerMarker * j;
			g.drawLine( lineX, 0, lineX, magsHeight );
		}
	}

	private void populateBodyAndRunAvArrays()
	{
		bodyPointOffset = 1;
		runAvPointOffset = 0;
		int previousBinDrawn = -1;

		for( int i = 0 ; i < magsWidth ; i++ )
		{
			final int whichBin = pixelToBinLookupTable[i];
//			log.trace( "For pixel " + i + " will pull values from bin: " + whichBin );

			if( whichBin != previousBinDrawn )
			{
				// Computing the spectral body amplitude
				// and running average in screen space
				int bucketMappedBodyValue;
				final int bucketMappedRunAvValue;

				if( previousBinDrawn == -1 || whichBin == previousBinDrawn + 1 )
				{
					final float bodyValForBin = computedBins[ whichBin ];
					final float normalisedBodyBinValue = bodyValForBin / stftExpectedAmpMax;

					bucketMappedBodyValue = ampScaleComputer.normalisedRawToMappedBucket( normalisedBodyBinValue );


					final float runAvValForBin = runningBinPeaks[ whichBin ];
					final float normalisedRunAvBinValue = runAvValForBin / stftExpectedAmpMax;
					bucketMappedRunAvValue = ampScaleComputer.normalisedRawToMappedBucket( normalisedRunAvBinValue );
				}
				else
				{
					float maxNormalisedValue = 0.0f;
					float maxNormalisedRunAvValue = 0.0f;
					for( int sb = previousBinDrawn + 1 ; sb <= whichBin ; ++sb )
					{
						final float bodyValForBin = computedBins[ sb ];
						final float normalisedBodyBinValue = bodyValForBin / stftExpectedAmpMax;
						if( normalisedBodyBinValue > maxNormalisedValue )
						{
							maxNormalisedValue = normalisedBodyBinValue;
						}
						final float runAvValForBin = runningBinPeaks[ sb ];
						final float normalisedRunAvBinValue = runAvValForBin / stftExpectedAmpMax;
						if( normalisedRunAvBinValue > maxNormalisedRunAvValue )
						{
							maxNormalisedRunAvValue = normalisedRunAvBinValue;
						}
					}
					bucketMappedBodyValue = ampScaleComputer.normalisedRawToMappedBucket( maxNormalisedValue );
					bucketMappedRunAvValue = ampScaleComputer.normalisedRawToMappedBucket( maxNormalisedRunAvValue );
				}

				polygonXPoints[ bodyPointOffset ] = i;
				polygonYPoints[ bodyPointOffset ] = magsHeight - bucketMappedBodyValue;
				bodyPointOffset++;

				polylineXPoints[ runAvPointOffset ] = i;
				polylineYPoints[ runAvPointOffset ] = magsHeight - bucketMappedRunAvValue;
				polylineExtraXPoints[ runAvPointOffset ] = i;
				int extraYPoint = polylineYPoints[ runAvPointOffset ] - 1;
				extraYPoint = extraYPoint < 0 ? 0 : extraYPoint;
				polylineExtraYPoints[ runAvPointOffset ] = extraYPoint;
				runAvPointOffset++;

				previousBinDrawn = whichBin;
			}
		}

		// Final pixel is a pain because it isn't necessarily a new bin
		// but we need a value for it
		final int finalPixel = magsWidth;
		final int whichBin = pixelToBinLookupTable[ finalPixel ];

//		log.debug("Final pixel " + finalPixel + " using bin " + whichBin );

		final float bodyValForBin = computedBins[ whichBin ];
		final float normalisedBodyBinValue = bodyValForBin / stftExpectedAmpMax;
		final int bucketMappedBodyValue = ampScaleComputer.normalisedRawToMappedBucket( normalisedBodyBinValue );

		polygonXPoints[ bodyPointOffset ] = finalPixel;
		polygonYPoints[ bodyPointOffset ] = magsHeight - bucketMappedBodyValue;
		bodyPointOffset++;

		final float runAvValForBin = runningBinPeaks[ whichBin ];
		final float normalisedRunAvBinValue = runAvValForBin / stftExpectedAmpMax;
		final int bucketMappedRunAvValue = ampScaleComputer.normalisedRawToMappedBucket( normalisedRunAvBinValue );
		polylineXPoints[ runAvPointOffset ] = finalPixel;
		polylineYPoints[ runAvPointOffset ] = magsHeight - bucketMappedRunAvValue;
		polylineExtraXPoints[ runAvPointOffset ] = finalPixel;
		int extraYPoint = polylineYPoints[ runAvPointOffset ] - 1;
		extraYPoint = (extraYPoint < 0 ? 0 : extraYPoint );
		polylineExtraYPoints[ runAvPointOffset ] = extraYPoint;

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
	}

	private void populateBodyAndPeakDetectArrays()
	{
		bodyPointOffset = 1;
		runAvPointOffset = 0;
		int previousBinDrawn = -1;

		for( int i = 0 ; i < magsWidth ; i++ )
		{
			final int whichBin = pixelToBinLookupTable[i];
//			log.trace( "For pixel " + i + " will pull values from bin: " + whichBin );

			if( whichBin != previousBinDrawn )
			{
				// Computing the spectral body amplitude
				// and running average in screen space
				int bucketMappedBodyValue;
				final int bucketMappedRunAvValue;

				if( previousBinDrawn == -1 || whichBin == previousBinDrawn + 1 )
				{
					final float bodyValForBin = computedBins[ whichBin ];
					final float normalisedBodyBinValue = bodyValForBin / stftExpectedAmpMax;

					bucketMappedBodyValue = ampScaleComputer.normalisedRawToMappedBucket( normalisedBodyBinValue );


					final float runAvValForBin = runningBinPeaks[ whichBin ];
					final float normalisedRunAvBinValue = runAvValForBin / stftExpectedAmpMax;
					bucketMappedRunAvValue = ampScaleComputer.normalisedRawToMappedBucket( normalisedRunAvBinValue );
				}
				else
				{
					// Skipping over multiple bin values - work out the max over the bin range
					float maxNormalisedValue = 0.0f;
					float maxNormalisedRunAvValue = 0.0f;
					for( int sb = previousBinDrawn + 1 ; sb <= whichBin ; ++sb )
					{
						final float bodyValForBin = computedBins[ sb ];
						final float normalisedBodyBinValue = bodyValForBin / stftExpectedAmpMax;
						if( normalisedBodyBinValue > maxNormalisedValue )
						{
							maxNormalisedValue = normalisedBodyBinValue;
						}
						final float runAvValForBin = runningBinPeaks[ sb ];
						final float normalisedRunAvBinValue = runAvValForBin / stftExpectedAmpMax;
						if( normalisedRunAvBinValue > maxNormalisedRunAvValue )
						{
							maxNormalisedRunAvValue = normalisedRunAvBinValue;
						}
					}
					bucketMappedBodyValue = ampScaleComputer.normalisedRawToMappedBucket( maxNormalisedValue );
					bucketMappedRunAvValue = ampScaleComputer.normalisedRawToMappedBucket( maxNormalisedRunAvValue );
				}

				polygonXPoints[ bodyPointOffset ] = i;
				polygonYPoints[ bodyPointOffset ] = magsHeight - bucketMappedBodyValue;
				bodyPointOffset++;


				polylineXPoints[ runAvPointOffset ] = i;
				polylineYPoints[ runAvPointOffset ] = magsHeight;
				runAvPointOffset++;
				polylineXPoints[ runAvPointOffset ] = i;
				polylineYPoints[ runAvPointOffset ] = magsHeight - bucketMappedRunAvValue;
				runAvPointOffset++;
				polylineXPoints[ runAvPointOffset ] = i;
				polylineYPoints[ runAvPointOffset ] = magsHeight;
				runAvPointOffset++;
//				polylineXPoints[ runAvPointOffset ] = i;
//				polylineYPoints[ runAvPointOffset ] = magsHeight - bucketMappedRunAvValue;
//				polylineExtraXPoints[ runAvPointOffset ] = i;
//				int extraYPoint = polylineYPoints[ runAvPointOffset ] - 1;
//				extraYPoint = extraYPoint < 0 ? 0 : extraYPoint;
//				polylineExtraYPoints[ runAvPointOffset ] = extraYPoint;
//				runAvPointOffset++;

				previousBinDrawn = whichBin;
			}
		}

		// Final pixel is a pain because it isn't necessarily a new bin
		// but we need a value for it
		final int finalPixel = magsWidth;
		final int whichBin = pixelToBinLookupTable[ finalPixel ];

//		log.debug("Final pixel " + finalPixel + " using bin " + whichBin );

		final float bodyValForBin = computedBins[ whichBin ];
		final float normalisedBodyBinValue = bodyValForBin / stftExpectedAmpMax;
		final int bucketMappedBodyValue = ampScaleComputer.normalisedRawToMappedBucket( normalisedBodyBinValue );

		polygonXPoints[ bodyPointOffset ] = finalPixel;
		polygonYPoints[ bodyPointOffset ] = magsHeight - bucketMappedBodyValue;
		bodyPointOffset++;

		final float runAvValForBin = runningBinPeaks[ whichBin ];
		final float normalisedRunAvBinValue = runAvValForBin / stftExpectedAmpMax;
		final int bucketMappedRunAvValue = ampScaleComputer.normalisedRawToMappedBucket( normalisedRunAvBinValue );
		polylineXPoints[ runAvPointOffset ] = finalPixel;
		polylineYPoints[ runAvPointOffset ] = magsHeight - bucketMappedRunAvValue;
		runAvPointOffset++;

		// Close off the polygon
		polygonXPoints[ bodyPointOffset ] = magsWidth;
		polygonYPoints[ bodyPointOffset ] = height;
		bodyPointOffset++;
	}

	private void drawBodyAndRunAv( final Graphics g )
	{

		if( runAvComputer instanceof PeakDetectComputer )
		{
			populateBodyAndPeakDetectArrays();
			g.setColor( SpectralAmpColours.SPECTRAL_BODY.darker().darker() );
			g.fillPolygon( polygonXPoints, polygonYPoints, bodyPointOffset );
		}
		else
		{
			populateBodyAndRunAvArrays();
			g.fillPolygon( polygonXPoints, polygonYPoints, bodyPointOffset );
		}

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

			// Regular peak continuous line uses double points
			// to get a thinker line, whilst the peak display uses
			// three
			// (newX, 0), (newX, newY), (newX, 0)
			// We'll use double and copy over the extra points
			// once we know how many there are
			polylineXPoints = new int[ maxPolylinePoints * 3 ];
			polylineYPoints = new int[ maxPolylinePoints * 3 ];
			// Space for the double points to be temporarily written
			polylineExtraXPoints = new int[ maxPolylinePoints ];
			polylineExtraYPoints = new int[ maxPolylinePoints ];
		}

		g.setColor( SpectralAmpColours.BACKGROUND_COLOR );
		g.fillRect( 0, 0, width, height );

		g.setColor( SpectralAmpColours.SPECTRAL_BODY );

		g.translate( 0, yOffset );

		drawBodyAndRunAv( g );

		paintGridLines( g );

		g.translate( 0, -yOffset );
	}

	@Override
	public void receiveFreqScaleChange( final FrequencyScaleComputer freqScaleComputer )
	{
		this.freqScaleComputer = freqScaleComputer;
		recomputePixelToBinLookup();
		repaint();
	}

	@Override
	public void receiveAmpScaleChange( final AmpScaleComputer ampScaleComputer )
	{
		this.ampScaleComputer = ampScaleComputer;
		repaint();
	}

	@Override
	public void receiveRunAvComputer( final RunningAverageComputer runAvComputer )
	{
		this.runAvComputer = runAvComputer;
		clear();
		repaint();
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

	private void setupInternalBuffersFromSize( final int width, final int height )
	{
		this.width = width - 1;
		this.height = height - 1;

//		magsWidth = width - SpectralAmpMadUiDefinition.SCALES_WIDTH_OFFSET - 1;
//		magsHeight = height - SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET - 1;

		magsWidth = SpectralAmpGenDisplayUiJComponent.getAdjustedWidthOfDisplay( this.width, numFreqMarkers );
		magsHeight = SpectralAmpGenDisplayUiJComponent.getAdjustedHeightOfDisplay( this.height, numAmpMarkers );

		yOffset = this.height - magsHeight;

		horizPixelsPerMarker = SpectralAmpGenDisplayUiJComponent.getAdjustedWidthBetweenMarkers( this.width, numFreqMarkers );
		vertPixelsPerMarker = SpectralAmpGenDisplayUiJComponent.getAdjustedHeightBetweenMarkers( this.height, numAmpMarkers );

		// We make the lookup table one larger than the width so we can overplot on width
		// so the final pixel(s) are drawn correctly
		pixelToBinLookupTable = new int[ magsWidth + 1 ];

		recomputePixelToBinLookup();

		// Inform the uiInstance of our size
		uiInstance.setDisplayPeaksHeight( magsHeight + 1 );
	}

	@Override
	public void receiveFftParams( final StftParameters params )
	{
		currentNumBins = params.getNumBins();
		stftExpectedAmpMax = params.getExpectedAmpMax();
		recomputePixelToBinLookup();
		clear();
		repaint();
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

			for( int i = 0 ; i < magsWidth + 1 ; i++ )
			{
				final float pixelRawFreq = freqScaleComputer.mappedBucketToRawMinMax( magsWidth, i );
				float adjustedBinFloat = (pixelRawFreq - binStartFreqOffset) / freqPerBin;
				adjustedBinFloat = (adjustedBinFloat < 0.0f ? 0.0f : adjustedBinFloat );
				int whichBin = Math.round( adjustedBinFloat );

//				log.debug("Pixel " + i + " has raw freq " + MathFormatter.slowFloatPrint( pixelRawFreq, 3, false ) + " which we adjust to " +
//						MathFormatter.slowFloatPrint( adjustedBinFloat, 3, false ) + " which maps to " + whichBin );

				// In the case we're generating a full spectrum the extra X pixel means we'll go over
				// the available number of bins, so clamp to that max value
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

	@Override
	public void setBounds( final Rectangle r )
	{
		super.setBounds( r );
		setupInternalBuffersFromSize( r.width, r.height );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalBuffersFromSize( width, height );
	}
}
