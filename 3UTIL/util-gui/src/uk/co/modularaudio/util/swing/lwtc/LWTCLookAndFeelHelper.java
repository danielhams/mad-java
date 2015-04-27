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

package uk.co.modularaudio.util.swing.lwtc;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class LWTCLookAndFeelHelper
{
	private static Log log = LogFactory.getLog( LWTCLookAndFeelHelper.class.getName() );
	private static LWTCLookAndFeelHelper instance = new LWTCLookAndFeelHelper();

	private final LookAndFeel laf;
	private UIDefaults uiDefaults;

	private LWTCLookAndFeelHelper()
	{
		laf = new MetalLookAndFeel();
		if( laf.isSupportedLookAndFeel() )
		{
			laf.initialize();
			uiDefaults = laf.getDefaults();
		}
		else
		{
			log.error("Unable to initialise metal look and feel for lightweight components");
		}
	}

	public static LWTCLookAndFeelHelper getInstance()
	{
		return instance;
	}

	public ComponentUI getComponentUi( final JComponent component )
	{
		return uiDefaults.getUI( component );
	}
}
