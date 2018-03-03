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

package test.uk.co.modularaudio.mads.visualisation.base.eqcrossfreqdiag;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.djeq3.ui.crossfreqdiag.EQCrossoverFreqDialog;
import uk.co.modularaudio.mads.base.djeq3.ui.crossfreqdiag.EQCrossoverFreqDialogCallback;

public class TestUseDialogs
{
	private static Log LOG = LogFactory.getLog( TestUseDialogs.class.getName() );

	public void testUseCrossoverDialog() throws Exception
	{
		final EQCrossoverFreqDialog tid = new EQCrossoverFreqDialog();

		final EQCrossoverFreqDialogCallback testCallback = new EQCrossoverFreqDialogCallback()
		{
			@Override
			public void dialogClosedReceiveValues( final String valueOrNull )
			{
				LOG.debug("JUnit test received callback with: " + valueOrNull );
				tid.dispose();
			}
		};
		final String initialValues = "2000.0:3500.0";
		tid.setValues( null, "Some message", "Some title", initialValues, testCallback );

		tid.go();
	}

	public static void main( final String[] args ) throws Exception
	{
		final String gtkLookAndFeelClassName = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
		boolean foundGtkLaf = false;

		final LookAndFeelInfo lafis[] = UIManager.getInstalledLookAndFeels();

		for( final LookAndFeelInfo lafi : lafis )
		{
			final String lc = lafi.getClassName();
			if( lc.equals( gtkLookAndFeelClassName ) )
			{
				foundGtkLaf = true;
				break;
			}
		}

		if( foundGtkLaf )
		{
			LOG.debug("Found available GTK laf. Will set active");
			UIManager.setLookAndFeel( gtkLookAndFeelClassName );
		}
		UIManager.put( "Slider.paintValue",  Boolean.FALSE );
		final TestUseDialogs tud = new TestUseDialogs();
		tud.testUseCrossoverDialog();
	}
}

