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

package test.uk.co.modularaudio.util.swing.rollpainter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import test.uk.co.modularaudio.util.swing.rollpainter.buffer.Buffer;
import test.uk.co.modularaudio.util.swing.rollpainter.buffer.BufferClearer;
import test.uk.co.modularaudio.util.swing.rollpainter.buffer.SampleFactory;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainter;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;

public class RPCanvas extends PacPanel implements ValueChangeListener
{
	private static final long serialVersionUID = -2651829740846756379L;
	
//	private static Log log = LogFactory.getLog( RPCanvas.class.getName() );
	
	private SampleFactory sampleFactory;
	private RollPainter<Buffer, BufferClearer> rollPainter;
	
//	private BufferStrategy bufferStrategy;
	
//	private final RPGestureRecogniser gestureRecogniser;
	
	public RPCanvas() throws DatastoreException
	{
		this.setMinimumSize( new Dimension( RPConstants.RP_CANVAS_WIDTH * 3, RPConstants.RP_CANVAS_HEIGHT + 40 ) );
		this.setBackground(  Color.black  );
		
		sampleFactory = new SampleFactory( this );
		rollPainter = new RollPainter<Buffer, BufferClearer>( RPConstants.RP_CANVAS_WIDTH, sampleFactory );
//		gestureRecogniser = new RPGestureRecogniser( this );
	}
	
	@Override
	public void receiveValueChange( Object source, float newValue )
	{
		// message from the speed slider
//		log.debug("Received value change: " + newValue );
		sampleFactory.setSpeedValue( (int)newValue );
	}

	public void receivedCallbackTick()
	{
		if( rollPainter.checkAndUpdate() )
		{
			// We have an update, ask for a repaint
			paintImmediately( getVisibleRect() );
		}
	}
	
//	@Override
//	public void paint( Graphics g )
//	{
//		if( rollPainter.buffer0Visible() )
//		{
//			g.drawImage( rollPainter.buffer0.image, rollPainter.buffer0XOffset + RPConstants.RP_CANVAS_WIDTH, 0, null );
////			log.debug("buffer0Visible");
//		}
//		else
//		{
//			log.debug("Not drawing buffer0");
//		}
//		if( rollPainter.buffer1Visible() )
//		{
//			g.drawImage( rollPainter.buffer1.image, rollPainter.buffer1XOffset + RPConstants.RP_CANVAS_WIDTH, 0, null );
////			log.debug("buffer1Visible");
//		}
//		else
//		{
//			log.debug("Not drawing buffer1");
//		}
//	}

	@Override
	protected void paintComponent( Graphics g )
	{
		g.setColor( Color.BLACK );
		g.fillRect( 0, 0, getWidth(), getHeight() );
		if( rollPainter.buffer0Visible() )
		{
			g.drawImage( rollPainter.buffer0.image, rollPainter.buffer0XOffset + RPConstants.RP_CANVAS_WIDTH, 20, null );
//			log.debug("buffer0Visible");
		}
		if( rollPainter.buffer1Visible() )
		{
			g.drawImage( rollPainter.buffer1.image, rollPainter.buffer1XOffset + RPConstants.RP_CANVAS_WIDTH, 20, null );
//			log.debug("buffer1Visible");
		}
		
		g.setColor( Color.GREEN );
		g.drawRect( RPConstants.RP_CANVAS_WIDTH, 0, RPConstants.RP_CANVAS_WIDTH-1, 39 );
		g.drawRect( RPConstants.RP_CANVAS_WIDTH, RPConstants.RP_CANVAS_HEIGHT, RPConstants.RP_CANVAS_WIDTH-1, 39 );
	}
	

}
