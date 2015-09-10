package uk.co.modularaudio.mads.base.scope.ui.display;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scope.ui.ScopeColours;
import uk.co.modularaudio.mads.base.scope.ui.ScopeDisplayUiJComponent;

public class ScopeAmpMarks extends JPanel
{
	private static final long serialVersionUID = 4239433019054754730L;

//	private static Log log = LogFactory.getLog( ScopeAmpMarks.class.getName() );

	private int width;
	private int height;
	private int yOffset;
	private int vertPixelsPerMarker;
	private final int numAmpMarkers;

	public ScopeAmpMarks( final int numAmpMarkers )
	{
		this.numAmpMarkers = numAmpMarkers;
		setMinimumSize( new Dimension( ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH, ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH ) );
	}

	@Override
	public void paint( final Graphics g )
	{
		g.translate( 0, yOffset );
		g.setColor( ScopeColours.SCOPE_AXIS_DETAIL );

		for( int i = 0 ; i < numAmpMarkers ; ++i )
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

		final int magsHeight = ScopeDisplayUiJComponent.getAdjustedHeightOfDisplay( this.height, numAmpMarkers );

		yOffset = this.height - magsHeight;

		vertPixelsPerMarker = ScopeDisplayUiJComponent.getAdjustedHeightBetweenMarkers( this.height, numAmpMarkers );

	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalDistances( width, height );
	}
}
