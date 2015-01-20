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
import java.util.ArrayList;
import java.util.List;

public abstract class JPanelDndTableDecorationHint
{
//	private static Log log = LogFactory.getLog( SwingDndTableDecorationHint.class.getName() );
	
	private boolean active = false;
	
	public boolean isActive()
	{
		return active;
	}
	
	public abstract Rectangle getCurrentDamageRectangle();
	
	public void setActive( boolean activeBool)
	{
		Rectangle currentDamageRectangle = getCurrentDamageRectangle();
		if( active != activeBool )
		{
			active = activeBool;
			if( isMouseRelative() )
			{
//				log.debug("The hint instance " + this.getClass().getSimpleName() + " changed active status - will emit a needs repaint damage event.");
				this.emitNeedsRepaintEvent( this, currentDamageRectangle );
			}
			else
			{
//				log.debug("The hint instance " + this.getClass().getSimpleName() + " changed active status - will emit a force repaint damage event.");
				this.emitForceRepaintEvent( this, currentDamageRectangle );
			}
		}
	}
	
	public abstract boolean isMouseRelative();

	public abstract void paint( Graphics g );
	
	public List<JPanelDndTableDecorationHintListener> listeners = new ArrayList<JPanelDndTableDecorationHintListener>();
	
	public void addListener( JPanelDndTableDecorationHintListener listener )
	{
		this.listeners.add( listener );
	}
	
	public void removeListener( JPanelDndTableDecorationHintListener listener )
	{
		this.listeners.remove( listener );
	}
	
	public void emitNeedsRepaintEvent( Object source, Rectangle damageRectangle )
	{
		for( JPanelDndTableDecorationHintListener l : listeners )
		{
			l.receiveNeedsRepaintSignal( source, damageRectangle );
		}
	}
	
	public void emitForceRepaintEvent( Object source, Rectangle damageRectangle )
	{
		for( JPanelDndTableDecorationHintListener l : listeners )
		{
			l.receiveForceRepaintSignal( source, damageRectangle );
		}
	}

	public abstract void setMousePosition(Point mousePosition);

	public void signalAnimation()
	{
		// Do nothing by default. Most hints won't be animated
	}
	
}
