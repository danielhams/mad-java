package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class SpectralPeakAmpMarks extends JPanel
{
	private static final long serialVersionUID = 7201682973826590002L;

//	private static Log log = LogFactory.getLog( NewAmpAxisMarks.class.getName() );

	private int width;
	private int height;
	private int yOffset;
	private int vertPixelsPerMarker;

	public SpectralPeakAmpMarks()
	{
		setMinimumSize( new Dimension( SpectralAmpDisplayUiJComponent.AXIS_MARKS_LENGTH, SpectralAmpDisplayUiJComponent.AXIS_MARKS_LENGTH ) );
	}

	@Override
	public void paint( final Graphics g )
	{
		g.translate( 0, yOffset );
		g.setColor( SpectralAmpColours.SCALE_AXIS_DETAIL );

		for( int i = 0 ; i < SpectralAmpDisplayUiJComponent.NUM_AMP_MARKERS ; ++i )
		{
			final int y = vertPixelsPerMarker * i;
			g.drawLine( 0, y, width, y );
		}

		g.translate( 0, -yOffset );
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.width = width - 1;
		this.height = height - 1;

		final int magsHeight = SpectralAmpDisplayUiJComponent.getAdjustedHeightOfDisplay( this.height );

		yOffset = this.height - magsHeight;

		vertPixelsPerMarker = SpectralAmpDisplayUiJComponent.getAdjustedHeightBetweenMarkers( this.height );

	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalDistances( width, height );
	}
}
