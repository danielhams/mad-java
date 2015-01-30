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

package uk.co.modularaudio.util.swing.dndtable.jpanel;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState.State;

public class JPanelDndTableDecorator implements JPanelDndTableDecorationHintListener
{
	public final static boolean DO_INDIVIDUAL_REPAINTS = false;

//	private static Log log = LogFactory.getLog( SwingDndTableDecorator.class.getName() );

	private Point mousePosition = new Point();

	private final JPanelDndTable<?,?,?> table;
	private final GuiDndTableState state;

	private final JPanelDndTableDecorations decorations;

	public JPanelDndTableDecorator(final JPanelDndTable<?,?,?> table,
			final GuiDndTableState state,
			final JPanelDndTableDecorations decorations )
	{
		this.table = table;
		this.state = state;
		this.decorations = decorations;
		for( final JPanelDndTableDecorationHint hint : decorations.getHints() )
		{
			hint.addListener( this );
		}
	}

	public void paint(final Graphics g)
	{
//		Graphics2D g2d= (Graphics2D)g;

		if( decorations != null )
		{
			// Draw the hints
			final List<JPanelDndTableDecorationHint> hints = decorations.getHints();

			if( hints != null )
			{
				for( final JPanelDndTableDecorationHint hint : hints )
				{
					if( hint.isActive() )
					{
						int xOffset = 0;
						int yOffset = 0;
						if( hint.isMouseRelative() )
						{
							xOffset = mousePosition.x;
							yOffset = mousePosition.y;
						}
						g.translate(xOffset, yOffset );
						hint.paint( g );
					}
				}
			}
		}
	}

	private final Deque<Rectangle> needsRepaintDamageQueue = new ArrayDeque<Rectangle>();

	public void setMousePosition( final Point mousePosition )
	{
		this.mousePosition = mousePosition;
		for( final JPanelDndTableDecorationHint hint : decorations.getHints() )
		{
			hint.setMousePosition( mousePosition );
		}
	}

	@Override
	public void receiveNeedsRepaintSignal( final Object source, final Rectangle damageRectangle )
	{
		if( state.getCurrentState() == State.DURING_DRAG )
		{
//			log.debug("Adding rectangle " + damageRectangle + " to needs repaint queue from " + source.getClass().getSimpleName());
			needsRepaintDamageQueue.addLast( damageRectangle );
		}
		else
		{
			receiveForceRepaintSignal( source, damageRectangle );
		}
	}

	public void doRepaintIfNecessary()
	{
		Rectangle cumulativeDamangeRectangle = null;
		final int numToRemove = needsRepaintDamageQueue.size();
		for( int i = 0 ; i < numToRemove ; i++ )
		{
			final Rectangle curRectangle = needsRepaintDamageQueue.removeFirst();
			if( DO_INDIVIDUAL_REPAINTS )
			{
				table.repaint( curRectangle );
			}
			else
			{
//				log.debug("Got a rectangle from the needs repaint damage queue: " + curRectangle );
				if( curRectangle != null )
				{
					if( i == 0 )
					{
						cumulativeDamangeRectangle = curRectangle;
					}
					else
					{
						cumulativeDamangeRectangle = cumulativeDamangeRectangle.union( curRectangle );
					}
				}
			}
		}
		if( !DO_INDIVIDUAL_REPAINTS )
		{
			if( cumulativeDamangeRectangle != null )
			{
//				log.debug("Calling repaint on damage rectangle " + cumulativeDamangeRectangle );
				table.repaint( cumulativeDamangeRectangle );
			}
		}
	}

	@Override
	public void receiveForceRepaintSignal( final Object source, final Rectangle damageRectangle )
	{
//		log.debug("Forced repaint on damage rectangle " + damageRectangle );
		table.repaint( damageRectangle );
		/*
		switch( state.getCurrentState() )
		{
		case DURING_DRAG:
			// During a drag it should emit the "needs repaint" signal, not a force repaint.
			break;
		default:
			break;
		}
		*/
	}

	public void signalAnimation()
	{
		for( final JPanelDndTableDecorationHint hint : decorations.getHints() )
		{
			hint.signalAnimation();
		}

	}

}
