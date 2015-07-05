package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Dimension;

import javax.swing.JPanel;

public class SpectralPeakEmptyPlot extends JPanel
{
	private static final long serialVersionUID = 7201682973826590002L;

//	private static Log log = LogFactory.getLog( NewEmptyPlot.class.getName() );

	public SpectralPeakEmptyPlot()
	{
		setBackground( SpectralAmpColours.BACKGROUND_COLOR );
		this.setMinimumSize( new Dimension( SpectralAmpDisplayUiJComponent.AXIS_MARKS_LENGTH, SpectralAmpDisplayUiJComponent.AXIS_MARKS_LENGTH ) );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
	}
}
