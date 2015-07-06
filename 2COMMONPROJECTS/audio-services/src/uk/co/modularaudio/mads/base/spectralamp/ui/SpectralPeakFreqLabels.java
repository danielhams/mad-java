package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class SpectralPeakFreqLabels extends JPanel implements FreqAxisChangeListener
{
	private static final long serialVersionUID = 8208419176860684686L;

//	private static Log log = LogFactory.getLog( NewFreqScaleLabels.class.getName() );

	private FrequencyScaleComputer freqScaleComputer;

	private final FontMetrics fm;

	private int realWidth;
	private int realHeight;
	private int width;
	private int magsWidth;
	private int xOffset;
	private int horizPixelsPerMarker;

	public SpectralPeakFreqLabels( final SpectralAmpMadUiInstance uiInstance )
	{
		this.setBackground( SpectralAmpColours.BACKGROUND_COLOR );

		this.freqScaleComputer = uiInstance.getDesiredFreqScaleComputer();

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );
		setMinimumSize( new Dimension( SpectralAmpDisplayUiJComponent.FREQ_LABELS_HEIGHT, SpectralAmpDisplayUiJComponent.FREQ_LABELS_HEIGHT ) );

		fm = getFontMetrics( getFont() );

		uiInstance.addFreqAxisChangeListener( this );
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.realWidth = width;
		this.realHeight = height;
		this.width = width - SpectralAmpDisplayUiJComponent.SPECTRAL_DISPLAY_RIGHT_PADDING -
				SpectralAmpDisplayUiJComponent.AXIS_MARKS_LENGTH -
				SpectralAmpDisplayUiJComponent.AMP_LABELS_WIDTH;

		magsWidth = SpectralAmpDisplayUiJComponent.getAdjustedWidthOfDisplay( this.width );

		xOffset = SpectralAmpDisplayUiJComponent.AMP_LABELS_WIDTH +
				SpectralAmpDisplayUiJComponent.AXIS_MARKS_LENGTH;

		horizPixelsPerMarker = SpectralAmpDisplayUiJComponent.getAdjustedWidthBetweenMarkers( this.width );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalDistances( width, height );
	}

	@Override
	public void receiveFreqScaleChange( final FrequencyScaleComputer freqScaleComputer )
	{
		this.freqScaleComputer = freqScaleComputer;
		repaint();
	}

	@Override
	public void receiveFftSizeChange( final int desiredFftSize )
	{
		// Do nothing, we don't change based on fft size changes.
	}

	@Override
	public void paint( final Graphics g )
	{
		g.setFont( LWTCControlConstants.LABEL_SMALL_FONT );

		g.setColor( SpectralAmpColours.BACKGROUND_COLOR );
		g.fillRect( 0, 0, realWidth, realHeight );

		g.translate( xOffset, 0 );
		g.setColor( SpectralAmpColours.SCALE_AXIS_DETAIL );

		for( int i = 0 ; i < SpectralAmpDisplayUiJComponent.NUM_FREQ_MARKERS ; ++i )
		{
			final int x = horizPixelsPerMarker * i;

			final float result = freqScaleComputer.mappedBucketToRawMinMax( magsWidth + 1, x );
			paintScaleText( g, result, x );
		}

		g.translate( -xOffset, 0 );
	}

	private final void paintScaleText( final Graphics g,
			final float displayFloat,
			final int xOffset )
	{
		final int fontHeight = fm.getAscent();
		final String displayString = MathFormatter.fastFloatPrint( displayFloat, 0, false );

		final char[] bscs = displayString.toCharArray();
		final int charsWidth = fm.charsWidth( bscs, 0, bscs.length );

		g.drawChars( bscs, 0, bscs.length, xOffset - (charsWidth / 2), fontHeight );
	}
}
