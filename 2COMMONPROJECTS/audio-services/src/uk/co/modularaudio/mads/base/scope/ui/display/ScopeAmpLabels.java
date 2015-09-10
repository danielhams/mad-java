package uk.co.modularaudio.mads.base.scope.ui.display;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scope.ui.ScopeColours;
import uk.co.modularaudio.mads.base.scope.ui.ScopeDisplayUiJComponent;
import uk.co.modularaudio.mads.base.scope.ui.ScopeMadUiInstance;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class ScopeAmpLabels extends JPanel
{
	private static final long serialVersionUID = 6218233547049390740L;

	private final FontMetrics fm;
	private final int numAmpMarkers;

	private int realWidth;
	private int realHeight;
	private int width;
	private int height;

	private int magsHeight;
	private int yOffset;
	private int vertPixelsPerMarker;

	public ScopeAmpLabels( final ScopeMadUiInstance uiInstance,
			final int numAmpMarkers )
	{
		this.setBackground( ScopeColours.BACKGROUND_COLOR );

		this.numAmpMarkers = numAmpMarkers;

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );
		setMinimumSize( new Dimension( ScopeDisplayUiJComponent.AMP_LABELS_WIDTH, ScopeDisplayUiJComponent.AMP_LABELS_WIDTH  ) );

		fm = getFontMetrics( getFont() );
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.realWidth = width;
		this.realHeight = height;
		this.width = width - 1;
		this.height = height - 1 - ScopeDisplayUiJComponent.AMP_DISPLAY_TOP_PADDING -
				ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH;

		magsHeight = ScopeDisplayUiJComponent.getAdjustedHeightOfDisplay( this.height, numAmpMarkers );

		yOffset = ScopeDisplayUiJComponent.AMP_DISPLAY_TOP_PADDING + (this.height - magsHeight);

		vertPixelsPerMarker = ScopeDisplayUiJComponent.getAdjustedHeightBetweenMarkers( this.height, numAmpMarkers );
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

		g.translate( 0, yOffset );
		g.setColor( ScopeColours.SCOPE_AXIS_DETAIL );

		final float diffPerMarker = 2.0f / (numAmpMarkers-1);
		float curValue = -1.0f;

		for( int i = 0 ; i < numAmpMarkers ; ++i )
		{
			final int y = magsHeight - (vertPixelsPerMarker * i);

			paintScaleTextDb( g, width, curValue, y );
			curValue += diffPerMarker;
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
