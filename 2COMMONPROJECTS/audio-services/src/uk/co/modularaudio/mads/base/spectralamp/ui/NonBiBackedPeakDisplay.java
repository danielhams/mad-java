package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.mads.base.spectralamp.util.SpecDataListener;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.LogarithmicNaturalAmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.LogarithmicFreqScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.FastFallComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.NoAverageComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.RunningAverageComputer;

public class NonBiBackedPeakDisplay extends JPanel
implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>,
	AmpAxisChangeListener, FreqAxisChangeListener, RunningAvChangeListener,
	SpecDataListener
{
	private static final long serialVersionUID = -180425607349546323L;

	private static Log log = LogFactory.getLog( NonBiBackedPeakDisplay.class.getName() );

	private boolean previouslyShowing;
	private final SpectralAmpMadUiInstance uiInstance;

	private AmpScaleComputer ampScaleComputer = new LogarithmicNaturalAmpScaleComputer();
	private float currentMaxDb = 0.0f;
	private float currentMaxAbs = 1.0f;
	private FrequencyScaleComputer freqScaleComputer = new LogarithmicFreqScaleComputer();
	private RunningAverageComputer runAvComputer = new FastFallComputer();

	private final float[] runningBinPeaks;
	private final float[] previousBinPeaks;
	private final float[] computedBins;

	private final BasicStroke singleLineStroke;
	private final BasicStroke wideLineStroke;

	private int currentNumBins;

	// Places for our polygon indices
	private final int[] xPoints = new int[3];
	private final int[] yPoints = new int[3];

	private final int[] dxs = new int[3];
	private final int[] dys = new int[3];

	// Our colour map
	private final OpenIntObjectHashMap<Color> colorMap = new OpenIntObjectHashMap<Color>();

	public NonBiBackedPeakDisplay( final SpectralAmpMadDefinition definition,
			final SpectralAmpMadInstance instance,
			final SpectralAmpMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		runningBinPeaks = new float[ SpectralAmpMadDefinition.MAX_NUM_FFT_BINS ];
		previousBinPeaks = new float[ SpectralAmpMadDefinition.MAX_NUM_FFT_BINS ];
		computedBins = new float[ SpectralAmpMadDefinition.MAX_NUM_FFT_BINS ];

		singleLineStroke = new BasicStroke( 1 );
		wideLineStroke = new BasicStroke( 2 );

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

	@Override
	public void paintComponent( final Graphics g )
	{
		final Graphics2D g2d = (Graphics2D)g;

//		g2d.setColor( Color.GRAY );
		g2d.setColor( SpectralAmpColours.BACKGROUND_COLOR );

		final int width = getWidth();
		final int height = getHeight();
		g2d.fillRect( 0, 0, width, height );

		// Draw the axis lines
		g2d.setColor( SpectralAmpColours.SCALE_AXIS_DETAIL );
		final int widthForAmps = width - SpectralAmpMadUiDefinition.SCALES_WIDTH_OFFSET - 1;
		final int heightForAmps = height - SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET - 1;

		final float vertPixelsPerMarker = heightForAmps / ((float)SpectralAmpAmpAxisDisplay.NUM_MARKERS - 1);

		for( int i = 0 ; i < SpectralAmpAmpAxisDisplay.NUM_MARKERS - 1 ; ++i )
		{

			final int lineY = SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET + (int)(vertPixelsPerMarker * i);
			g2d.drawLine( 0, lineY, widthForAmps, lineY );
		}
		final float horizPixelsPerMarker = widthForAmps / ((float)SpectralAmpFreqAxisDisplay.NUM_MARKERS - 1);
		for( int j = 1 ; j < SpectralAmpFreqAxisDisplay.NUM_MARKERS ; ++j )
		{
			final int lineX = (int)(horizPixelsPerMarker * j);
			g2d.drawLine( lineX, SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET, lineX, height );
		}

		g2d.translate( 0, SpectralAmpMadUiDefinition.SCALES_HEIGHT_OFFSET + 1 );

		paintMags( g2d, widthForAmps, heightForAmps, computedBins, currentNumBins, true );
		if( !(runAvComputer instanceof NoAverageComputer ) )
		{
			paintMags( g2d, widthForAmps, heightForAmps, runningBinPeaks, currentNumBins, false );
		}
	}

	@Override
	public void receiveFreqScaleComputer( final FrequencyScaleComputer desiredFreqScaleComputer )
	{
		freqScaleComputer = desiredFreqScaleComputer;
		clear();
	}

	@Override
	public void receiveAmpMaxDbChange( final float newMaxDB )
	{
		currentMaxDb = newMaxDB;
		currentMaxAbs = AudioMath.dbToLevelF( currentMaxDb );
	}

	@Override
	public void receiveAmpScaleComputer( final AmpScaleComputer desiredAmpScaleComputer )
	{
		ampScaleComputer = desiredAmpScaleComputer;
	}

	@Override
	public void receiveRunAvComputer( final RunningAverageComputer desiredRunningAverageComputer )
	{
		runAvComputer = desiredRunningAverageComputer;
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
	public void setNumBins( final int numBins )
	{
		currentNumBins = numBins;
		clear();
	}

	private void paintMags( final Graphics2D g2d,
			final int magsWidth,
			final int magsHeight,
			final float[] bins,
			final int numBins,
			final boolean drawSolid )
	{
		xPoints[0] = -1;
		xPoints[1] = -1;
		xPoints[2] = -1;
		yPoints[0] = -1;
		yPoints[1] = -1;
		yPoints[2] = -1;

		int originx = 0;
		int originy = magsHeight;

		int previousBinDrawn = -1;

		if( drawSolid )
		{
			g2d.setStroke( singleLineStroke );
		}
		else
		{
			g2d.setStroke( wideLineStroke );
		}
//		log.debug("Mags height is " + magsHeight );

		for( int i = 0 ; i < magsWidth ; i++ )
		{
			final int whichBin = freqScaleComputer.displayBinToSpectraBin( numBins, magsWidth, i );

			if( whichBin != previousBinDrawn )
			{
				xPoints[0]= xPoints[1];
				yPoints[0] = yPoints[1];
				xPoints[1] = xPoints[2];
				yPoints[1] = yPoints[2];

				previousBinDrawn = whichBin;

				final float valForBin = bins[ whichBin ];
				xPoints[2] = i;
				final float ampScaledValue = ampScaleComputer.scaleIt( valForBin );

				final float normalisedBinValue = valForBin / AmpScaleComputer.APPROX_POLAR_AMP_SCALE_FACTOR;
				final float bucketMappedValue = ampScaleComputer.rawToMappedBucket( magsHeight, currentMaxAbs, normalisedBinValue );

//				log.debug("For bin " + i + " with value " + valForBin + " the asv(" + ampScaledValue + ") bmv(" + bucketMappedValue + ")");
				yPoints[2] = magsHeight - (int)bucketMappedValue;

//				yPoints[2] = windowY;

				if( xPoints[1] != -1 )
				{
					dxs[0] = originx;
					dys[0] = originy;
					dxs[1] = xPoints[1];
					dys[1] = yPoints[1];
					dxs[2] = xPoints[2];
					dys[2] = yPoints[2];

					if( dys[0] > 0 && dys[1] > 0 && dys[2] > 0 )
					{
						Color color;

						if( !drawSolid )
						{
							color = SpectralAmpColours.RUNNING_PEAK_COLOR;
						}
						else
						{
							// Calculate a colour from the log value
							color = colorFor( ampScaledValue );
						}
						g2d.setColor( color );
						if( drawSolid )
						{
							// If we decompose into line drawing, draw line, not a polygon
							g2d.fillPolygon( dxs, dys, 3 );
							dxs[0] = originx;
							dys[0] = originy;
							dxs[1] = xPoints[2];
							dys[1] = yPoints[2];
							dxs[2] = xPoints[2];
							dys[2] = magsHeight;
							g2d.fillPolygon( dxs, dys, 3 );
						}
						else
						{
							// Running peaks, just draw lines
							g2d.drawLine(xPoints[1], yPoints[1], xPoints[2], yPoints[2] );
						}
					}
					originx = xPoints[2];
					originy = magsHeight;
				}
			}
		}
	}

	private Color colorFor( final float val )
	{
		final int intVal = newColourFor( val );
		Color retVal = colorMap.get( intVal );
		if( retVal == null )
		{
			retVal = new Color( intVal );
			colorMap.put( intVal, retVal );
		}
		return retVal;
	}

	private int newColourFor( final float val )
	{
		// Is between 0 -> 1
		// Divide into two sections
		// 0 -> 0.5 up to 255 grey
		// 0.5 -> 1.0 from grey to red
		if( val < 0.0f )
		{
			return 0;
		}
		else if( val < 0.5f )
		{
			final int greyLevel = (int)( (val * 2) * 255);
			return (greyLevel << 16) |(greyLevel << 8) | (greyLevel );
		}
		else if( val <= 1.0f )
		{
			final int redAmount = (int)(((val - 0.5f) * 2) * 255);
			final int greyLevel = 255 - redAmount;
			return (255 << 16) | (greyLevel << 8) | (greyLevel);
		}
		else
		{
			return 0xff0000;
		}
	}

	@Override
	public void receiveDataRateChange( final DataRate dataRate )
	{
		// Don't care
	}
}
