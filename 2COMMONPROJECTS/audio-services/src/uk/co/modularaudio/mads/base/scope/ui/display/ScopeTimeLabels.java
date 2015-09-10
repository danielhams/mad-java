package uk.co.modularaudio.mads.base.scope.ui.display;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scope.ui.CaptureLengthListener;
import uk.co.modularaudio.mads.base.scope.ui.ScopeColours;
import uk.co.modularaudio.mads.base.scope.ui.ScopeDisplayUiJComponent;
import uk.co.modularaudio.mads.base.scope.ui.ScopeMadUiInstance;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LogarithmicTimeMillis1To1000SliderModel;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class ScopeTimeLabels extends JPanel implements CaptureLengthListener
{
	private static final long serialVersionUID = -129516269411473973L;

	private static final int LABEL_Y_OFFSET = 10;

	private final FontMetrics fm;
	private final int numTimeMarkers;

	private int realWidth;
	private int realHeight;
	private int width;

	private int horizPixelsPerMarker;

	private float captureTimeMillis = LogarithmicTimeMillis1To1000SliderModel.DEFAULT_MILLIS;

	public ScopeTimeLabels( final ScopeMadUiInstance uiInstance,
			final int numTimeMarkers )
	{
		this.setBackground( ScopeColours.BACKGROUND_COLOR );

		this.numTimeMarkers = numTimeMarkers;

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );
		setMinimumSize( new Dimension( ScopeDisplayUiJComponent.TIME_LABELS_HEIGHT, ScopeDisplayUiJComponent.TIME_LABELS_HEIGHT ) );

		fm = getFontMetrics( LWTCControlConstants.LABEL_SMALL_FONT );

		uiInstance.addCaptureLengthListener( this );
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.realWidth = width;
		this.realHeight = height;
		this.width = width - ScopeDisplayUiJComponent.AMP_DISPLAY_RIGHT_PADDING -
				ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH -
				ScopeDisplayUiJComponent.AMP_LABELS_WIDTH;

		horizPixelsPerMarker = ScopeDisplayUiJComponent.getAdjustedWidthBetweenMarkers( this.width, numTimeMarkers );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalDistances( width, height );
	}

	@Override
	public void paint( final Graphics g )
	{
		g.setFont( LWTCControlConstants.LABEL_SMALL_FONT );

		g.setColor( ScopeColours.BACKGROUND_COLOR );
		g.fillRect( 0, 0, realWidth, realHeight );

		g.setColor( ScopeColours.SCOPE_AXIS_DETAIL );

		final float diffPerMarker = captureTimeMillis / (numTimeMarkers-1);
		float curValue = 0.0f;

		for( int i = 0 ; i < numTimeMarkers ; ++i )
		{
			final int x = (horizPixelsPerMarker * i) + ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH;

			paintScaleText( g, LABEL_Y_OFFSET, curValue, ScopeDisplayUiJComponent.AMP_LABELS_WIDTH + x );
			curValue += diffPerMarker;
		}
	}

	private final void paintScaleText( final Graphics g,
			final int height,
			final float scaleFloat,
			final int xOffset )
	{
		final String scaleString = MathFormatter.fastFloatPrint( scaleFloat, 1, false );
		final char[] bscs = scaleString.toCharArray();
		final int charsWidth = fm.charsWidth( bscs, 0, bscs.length );
		final int charsWidthOver2 = charsWidth / 2;

		g.drawChars( bscs, 0, bscs.length, xOffset - charsWidthOver2, height );
	}

	@Override
	public void receiveCaptureLengthMillis( final float captureMillis )
	{
		this.captureTimeMillis = captureMillis;
		repaint();
	}

	@Override
	public void receiveCaptureLengthSamples( final int captureSamples )
	{
		// Don't care
	}
}
