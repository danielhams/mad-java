package uk.co.modularaudio.mads.base.scope.ui.display;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scope.ui.ScopeColours;
import uk.co.modularaudio.mads.base.scope.ui.ScopeDisplayUiJComponent;

public class ScopeTimeMarks extends JPanel
{
	private static final long serialVersionUID = -6169422018023474030L;

//	private static Log log = LogFactory.getLog( ScopeTimeMarks.class.getName() );

	private int width;
	private int height;
	private int horizPixelsPerMarker;
	private final int numFreqMarkers;

	public ScopeTimeMarks( final int numFreqMarkers )
	{
		this.numFreqMarkers = numFreqMarkers;
		setMinimumSize( new Dimension( ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH, ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH ) );
	}

	@Override
	public void paint( final Graphics g )
	{
		g.setColor( ScopeColours.SCOPE_AXIS_DETAIL );

		for( int i = 0 ; i < numFreqMarkers ; ++i )
		{
			final int x = (horizPixelsPerMarker * i) + ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH;
			g.drawLine( x, 0, x, height );
		}
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.width = width - 1 - ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH - ScopeDisplayUiJComponent.AMP_DISPLAY_RIGHT_PADDING;
		this.height = height - 1;

		horizPixelsPerMarker = ScopeDisplayUiJComponent.getAdjustedWidthBetweenMarkers( this.width, numFreqMarkers );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalDistances( width, height );
	}
}
