/**
 *
 * Copyright (C) 2015 -> 2018 - Daniel Hams, Modular Audio Limited
 *                              daniel.hams@gmail.com
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

package uk.co.modularaudio.mads.base.controllerhistogram.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class ReceivedLight extends JPanel
	implements ControllerEventReceivedListener
{
	private static final long serialVersionUID = 8276674761933079784L;

//	private static Log log = LogFactory.getLog( NoteReceivedLight.class.getName() );

	private final int lampLitFrames;

	private boolean shouldBeLit;

	private boolean isLit;
	private long lastLightTime;

	private final static Color LIT_COLOR = LWTCControlConstants.ROTARY_VIEW_INDICATOR_COLOR;
	private final static Color UNLIT_COLOR = LIT_COLOR.darker().darker().darker();

	public ReceivedLight()
	{
		setMinimumSize( new Dimension(14,14) );
		lampLitFrames = 8000;
	}

	@Override
	public void receivedNote()
	{
//		log.debug("Setting shouldBeLit to true");
		shouldBeLit = true;
	}

	public void doDisplayProcessing( final long currentGuiTime )
	{
		boolean doRepaint = false;

		if( shouldBeLit )
		{
//			log.debug("Should be lit");
			lastLightTime = currentGuiTime;
			shouldBeLit = false;
			if( !isLit )
			{
//				log.debug("Isn't currently lit");
				isLit = true;
				doRepaint = true;
			}
		}

		final long diff = currentGuiTime - lastLightTime;

		if( isLit && diff > lampLitFrames )
		{
//			log.debug("Is lit and expired.");
			isLit = false;
			doRepaint = true;
		}

		if( doRepaint )
		{
//			log.debug("Doing a repaint");
			repaint();
		}
	}

	@Override
	public void paint( final Graphics g )
	{
		if( isLit )
		{
			g.setColor( LIT_COLOR );
		}
		else
		{
			g.setColor( UNLIT_COLOR );
		}
		g.fillRect( 0, 0, getWidth(), getHeight() );
	}
}
