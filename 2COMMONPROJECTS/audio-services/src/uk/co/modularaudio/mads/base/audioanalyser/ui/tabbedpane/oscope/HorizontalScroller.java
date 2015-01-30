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

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope;

import java.awt.Graphics;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserUiBufferState;
import uk.co.modularaudio.mads.base.audioanalyser.ui.BufferZoomAndPositionListener;
import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserUiBufferState.ZoomDirection;
import uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.AAColours;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;

public class HorizontalScroller extends PacPanel implements BufferZoomAndPositionListener
{
	private static final long serialVersionUID = 4784309468461416844L;
	
//	private static final Log log = LogFactory.getLog( HorizontalScroller.class.getName() );
	
	private final AudioAnalyserUiBufferState uiBufferState;
	
//	private float normalisedBufferWindowStart = 0.2f;
//	private float normalisedBufferWindowEnd = 0.8f;
	private float normalisedBufferWindowStart = 0.0f;
	private float normalisedBufferWindowEnd = 1.0f;

	public HorizontalScroller( final AudioAnalyserUiBufferState uiBufferState )
	{
		this.uiBufferState = uiBufferState;
		
		setOpaque(true);
		setBackground(AAColours.BACKGROUND);
		
		uiBufferState.addBufferZoomAndPositionListener( this );
		
		this.addMouseWheelListener( new MouseWheelListener()
		{
			
			@Override
			public void mouseWheelMoved( MouseWheelEvent e )
			{
//				log.debug("Received mouse wheel moved event: " + e.toString() );
				int wheelRotation = e.getWheelRotation();
				// < 0 - away from user (zoom in)
				// > 0 towards user (zoom out)
				ZoomDirection direction = (wheelRotation < 0 ? ZoomDirection.IN : ZoomDirection.OUT );
				uiBufferState.zoom( direction );
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		int width = getWidth();
		int height = getHeight();
		
		// Clear
		g.setColor( AAColours.BACKGROUND );
		g.fillRect( 0, 10, width - 1, height - 11 );
		
		// White box outline
		g.setColor( AAColours.FOREGROUND_WHITISH );
		g.drawRect( 0, 10, width - 1, height - 11);
		
		// Filled scroll position
		int boxWidth = (width - 6);
		int startXOffset = 3 + (int)(boxWidth * normalisedBufferWindowStart);
		int endXOffset = 3 + (int)(boxWidth * normalisedBufferWindowEnd);
		g.fillRect( startXOffset, 13, endXOffset - startXOffset, height - 16 );
	}

	@Override
	public void receiveZoomAndPositionUpdate()
	{
		float maxNumSamples = uiBufferState.maxSamplesToDisplay;
		int swo = uiBufferState.bufferPositions.startWindowOffset;
		normalisedBufferWindowStart = swo / maxNumSamples;
		int ewo = uiBufferState.bufferPositions.endWindowOffset;
		normalisedBufferWindowEnd = ewo / maxNumSamples;
		repaint();
	}
}
