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

package test.uk.co.modularaudio.mads.visualisation.base;

import javax.swing.UnsupportedLookAndFeelException;

import test.uk.co.modularaudio.mads.visualisation.base.genericsetup.GenericComponentVisualiser;
import uk.co.modularaudio.mads.rackmasterio.mu.RackMasterIOMadDefinition;

public class TestCreateRackMasterIO
{
	private final GenericComponentVisualiser gcv;

	public TestCreateRackMasterIO() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		gcv = new GenericComponentVisualiser();
	}

	protected void go() throws Exception
	{
		gcv.setUp();
		gcv.testAndShowComponent( RackMasterIOMadDefinition.DEFINITION_ID );
		gcv.tearDown();
	}

	public static void main( final String[] args ) throws Exception
	{
		final TestCreateRackMasterIO tc = new TestCreateRackMasterIO();
		tc.go();
	}
}
