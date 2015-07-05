package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class SpectralPeakFreqMarks extends JPanel
{
	private static final long serialVersionUID = -3809469238579090970L;

//	private static Log log = LogFactory.getLog( NewFreqAxisMarks.class.getName() );

	private int width;
	private int height;
	private int horizPixelsPerMarker;

	public SpectralPeakFreqMarks()
	{
		setMinimumSize( new Dimension( SpectralAmpDisplayUiJComponent.AXIS_MARKS_LENGTH, SpectralAmpDisplayUiJComponent.AXIS_MARKS_LENGTH ) );
	}

	@Override
	public void paint( final Graphics g )
	{
		g.setColor( SpectralAmpColours.SCALE_AXIS_DETAIL );

		for( int i = 0 ; i < SpectralAmpDisplayUiJComponent.NUM_FREQ_MARKERS ; ++i )
		{
			final int x = (horizPixelsPerMarker * i) + SpectralAmpDisplayUiJComponent.AXIS_MARKS_LENGTH;
			g.drawLine( x, 0, x, height );
		}
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.width = width - 1 - SpectralAmpDisplayUiJComponent.AXIS_MARKS_LENGTH - SpectralAmpDisplayUiJComponent.SPECTRAL_DISPLAY_RIGHT_PADDING;
		this.height = height - 1;

		horizPixelsPerMarker = SpectralAmpDisplayUiJComponent.getAdjustedWidthBetweenMarkers( this.width );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalDistances( width, height );
	}
}
