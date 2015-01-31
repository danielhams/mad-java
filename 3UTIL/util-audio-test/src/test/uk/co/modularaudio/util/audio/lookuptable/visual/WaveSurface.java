/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package test.uk.co.modularaudio.util.audio.lookuptable.visual;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import uk.co.modularaudio.util.audio.lookuptable.listenable.ListenableWaveTable;
import uk.co.modularaudio.util.audio.lookuptable.listenable.WaveTableListener;

public class WaveSurface extends JPanel implements WaveTableListener
{
//	private static Log log = LogFactory.getLog( WaveSurface.class.getName() );

	private static final long serialVersionUID = -1270161992189209732L;

	private ListenableWaveTable waveTable = null;

	public WaveSurface()
	{
		this.setSize( new Dimension( 800, 600 ) );
	}

	@Override
	public void paint( final Graphics g )
	{
		super.paint( g );

//		Rectangle clipRect = g.getClipBounds();
//		g.clearRect( clipRect.x, clipRect.y, clipRect.width, clipRect.height );
		final Graphics2D g2d = (Graphics2D)g;
		if( waveTable != null )
		{
//			g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			// We scale it so everything will fit in the dimensions
			final int visualWidth = getWidth();
			final int visualHeight = getHeight();

			final double heightScaleFactor = visualHeight / ((double)2.05f * 2); // 0.5 needs to be the center of the screen and we make it a little smaller to fit
			final int wtLength = waveTable.length;
			final double widthScaleFactor = (double)visualWidth / (double)wtLength;

			double previousX = 0;
			double previousY = visualHeight / 2;
			for( int i = 0 ; i < wtLength ; i++ )
			{
				final double value = waveTable.getValueAt( i );
				final double x = i * widthScaleFactor;
				final double y = visualHeight / 2 + (value * heightScaleFactor );
//				log.debug("For value " + value + " drawing a line from " + previousX + ", " + previousY + " to " + x + ", " + y );
				if( i != 0 )
				{
					g2d.drawLine( (int)previousX, (int)previousY, (int)x, (int)y );
				}
				previousX = x;
				previousY = y;
			}
		}
	}

	public ListenableWaveTable getWaveTable()
	{
		return waveTable;
	}

	public void setWaveTable(final ListenableWaveTable waveTable)
	{
		if( this.waveTable != null )
		{
			this.waveTable.removeListener( this );
		}
		this.waveTable = waveTable;
		if( this.waveTable != null )
		{
			this.waveTable.addListener( this );
		}
		this.receiveTableChanged();
	}

	@Override
	public void receiveTableChanged()
	{
		// Urgh, repaint the whole thing every time.
		this.repaint();
	}
}
