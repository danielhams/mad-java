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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class JPanelDndTableMouseFollowAnimationTimer
{
	// 30 Hz update speed
	private static final int REPAINT_MILLIS = 1000 / 20;

//	private static Log log = LogFactory.getLog( AutoScrollingMouseListener.class.getName() );
	
	private RepaintingTimerTask timerTask = null;
	private Timer repainterTimer = null;
	
	private JPanelDndTableDecorator tableDecorator = null;
	
	public JPanelDndTableMouseFollowAnimationTimer(JPanelDndTableDecorator tableDecorator)
	{
		this.tableDecorator = tableDecorator;
	}

	public void start()
	{
		if( repainterTimer == null )
		{
			timerTask = new RepaintingTimerTask( this, tableDecorator);
			repainterTimer = new Timer( REPAINT_MILLIS, timerTask );
			repainterTimer.start();
//			log.debug("Starting repaining timer.");
		}
	}
	
	public void stop()
	{
		if( repainterTimer != null )
		{
//			log.debug("Stopping repaining timer.");
			repainterTimer.stop();
			repainterTimer = null;
			// Do a final call to purge the repaint queue
			tableDecorator.doRepaintIfNecessary();
		}
	}

	public class RepaintingTimerTask implements ActionListener
	{
//		private SwingDndTableMouseFollowAnimationTimer mouseFollowThread = null;
		private JPanelDndTableDecorator tableDecorator = null;
	
		public RepaintingTimerTask( JPanelDndTableMouseFollowAnimationTimer mouseFollowThread,
				JPanelDndTableDecorator tableDecorator)
		{
//			this.mouseFollowThread = mouseFollowThread;
			this.tableDecorator = tableDecorator;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			tableDecorator.signalAnimation();
			tableDecorator.doRepaintIfNecessary();
		}
	}

}
