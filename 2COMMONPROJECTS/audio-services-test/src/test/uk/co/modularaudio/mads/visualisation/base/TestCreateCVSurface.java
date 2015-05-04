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

import junit.framework.TestCase;
import test.uk.co.modularaudio.mads.visualisation.base.genericsetup.GenericComponentVisualiser;
import uk.co.modularaudio.mads.base.cvsurface.mu.CvSurfaceMadDefinition;

public class TestCreateCVSurface extends TestCase
{
	private final GenericComponentVisualiser gcv;

	public TestCreateCVSurface() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		gcv = new GenericComponentVisualiser();
	}

	@Override
	protected void setUp() throws Exception
	{
		gcv.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		gcv.tearDown();
	}

	public void testAndShowComponent()
			throws Exception
	{
		gcv.testAndShowComponent( CvSurfaceMadDefinition.DEFINITION_ID );
	}
}
