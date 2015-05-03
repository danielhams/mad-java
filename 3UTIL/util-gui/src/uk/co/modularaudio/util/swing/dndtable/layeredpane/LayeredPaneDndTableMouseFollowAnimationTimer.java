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

package uk.co.modularaudio.util.swing.dndtable.layeredpane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class LayeredPaneDndTableMouseFollowAnimationTimer
{
	// 30 Hz update speed
//	private static final int REPAINT_MILLIS = 1000 / 1;
//	private static final int REPAINT_MILLIS = 1000 / 15;
	private static final int REPAINT_MILLIS = 1000 / 20;
//	private static final int REPAINT_MILLIS = 1000 / 30;
//	private static final int REPAINT_MILLIS = 1000 / 60;

//	private static Log log = LogFactory.getLog( LayeredPaneDndTableMouseFollowAnimationTimer.class.getName() );

	private RepaintingTimerTask timerTask;
	private Timer repainterTimer;

	private final LayeredPaneDndTableDecorator tableDecorator;

	public LayeredPaneDndTableMouseFollowAnimationTimer( final LayeredPaneDndTableDecorator tableDecorator)
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
//			log.debug("Starting repainting timer.");
		}
	}

	public void stop()
	{
		if( repainterTimer != null )
		{
//			log.debug("Stopping repainting timer.");
			repainterTimer.stop();
			repainterTimer = null;
		}
	}

	public class RepaintingTimerTask implements ActionListener
	{
//		private LayeredPaneDndTableMouseFollowAnimationTimer mouseFollowThread = null;
		private LayeredPaneDndTableDecorator tableDecorator = null;

		public RepaintingTimerTask( final LayeredPaneDndTableMouseFollowAnimationTimer mouseFollowThread,
				final LayeredPaneDndTableDecorator tableDecorator)
		{
//			this.mouseFollowThread = mouseFollowThread;
			this.tableDecorator = tableDecorator;
		}

		@Override
		public void actionPerformed(final ActionEvent e)
		{
//			log.debug("Got a ping");
			tableDecorator.signalAnimation();
		}
	}

}
