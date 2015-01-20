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

import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RollPainterTester
{
	private static Log log = LogFactory.getLog( RollPainterTester.class.getName() );
	
	public RollPainterTester()
	{
	}
	
	public void go() throws Exception
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			
			@Override
			public void run()
			{
				try
				{
					RPFrame frame = new RPFrame();		
					frame.pack();
					frame.setVisible( true );
					frame.startCallbacks();
				}
				catch( Exception e )
				{
					log.error("Exception caught creating frame:" + e.toString(), e );
				}
			}
		} );
		
	}

	/**
	 * @param args
	 */
	public static void main( String[] args )
		throws Exception
	{
		RollPainterTester t = new RollPainterTester();
		t.go();
	}

}
