package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class SpectralPeakAmpLabels extends JPanel implements AmpAxisChangeListener
{
	private static final long serialVersionUID = -4554672067965895575L;

//	private static Log log = LogFactory.getLog( NewAmpScaleLabels.class.getName() );

	private AmpScaleComputer ampScaleComputer;

	private final FontMetrics fm;

	private int realWidth;
	private int realHeight;
	private int width;
	private int height;
	private int magsHeight;
	private int yOffset;
	private int vertPixelsPerMarker;

	public SpectralPeakAmpLabels( final SpectralAmpMadUiInstance uiInstance )
	{
		this.setBackground( SpectralAmpColours.BACKGROUND_COLOR );

		this.ampScaleComputer = uiInstance.getDesiredAmpScaleComputer();

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );
		setMinimumSize( new Dimension( SpectralAmpDisplayUiJComponent.AMP_LABELS_WIDTH, SpectralAmpDisplayUiJComponent.AMP_LABELS_WIDTH  ) );

		fm = getFontMetrics( getFont() );

		uiInstance.addAmpAxisChangeListener( this );
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.realWidth = width;
		this.realHeight = height;
		this.width = width - 1;
		this.height = height - 1 - SpectralAmpDisplayUiJComponent.SPECTRAL_DISPLAY_TOP_PADDING -
				SpectralAmpDisplayUiJComponent.AXIS_MARKS_LENGTH;

		magsHeight = SpectralAmpDisplayUiJComponent.getAdjustedHeightOfDisplay( this.height );

		yOffset = SpectralAmpDisplayUiJComponent.SPECTRAL_DISPLAY_TOP_PADDING + (this.height - magsHeight);

		vertPixelsPerMarker = SpectralAmpDisplayUiJComponent.getAdjustedHeightBetweenMarkers( this.height );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalDistances( width, height );
	}

	@Override
	public void receiveAmpScaleChange( final AmpScaleComputer ampScaleComputer )
	{
		this.ampScaleComputer = ampScaleComputer;
		repaint();
	}

	@Override
	public void paint( final Graphics g )
	{
		g.setFont( LWTCControlConstants.LABEL_SMALL_FONT );

		g.setColor( SpectralAmpColours.BACKGROUND_COLOR );
		g.fillRect( 0, 0, realWidth, realHeight );

		g.translate( 0, yOffset );
		g.setColor( SpectralAmpColours.SCALE_AXIS_DETAIL );

		for( int i = 0 ; i < SpectralAmpDisplayUiJComponent.NUM_AMP_MARKERS ; ++i )
		{
			final int y = vertPixelsPerMarker * i;

			final float result = ampScaleComputer.mappedBucketToRawMinMax( magsHeight + 1, magsHeight - y );
			final float asDb = AudioMath.levelToDbF( result );

			paintScaleTextDb( g, width, asDb, y );
		}

		g.translate( 0, -yOffset );
	}

	private final void paintScaleTextDb( final Graphics g,
			final int width,
			final float scaleFloat,
			final int yOffset )
	{
		final int fontHeight = fm.getAscent();
		final int fontHeightOver2 = fontHeight / 2;
		final String scaleString = MathFormatter.fastFloatPrint( scaleFloat, 1, false );
		final char[] bscs = scaleString.toCharArray();
		final int charsWidth = fm.charsWidth( bscs, 0, bscs.length );
		final int charsEndX = width - 2;
		g.drawChars( bscs, 0, bscs.length, charsEndX - charsWidth, yOffset + fontHeightOver2 );
	}
}
