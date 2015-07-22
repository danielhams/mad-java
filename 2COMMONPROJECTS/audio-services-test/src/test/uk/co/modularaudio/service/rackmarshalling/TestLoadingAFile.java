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

package test.uk.co.modularaudio.service.rackmarshalling;

import junit.framework.TestCase;
import test.uk.co.modularaudio.service.rackmarshalling.config.RackMarshallingTestConfig;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;

public class TestLoadingAFile extends TestCase
{
//	private static Log log = LogFactory.getLog( TestLoadingAFile.class.getName() );

	private final RackMarshallingTestConfig tc = new RackMarshallingTestConfig();

	public void testLoadAFile()
		throws Exception
	{
		final String filename = "test_save_file_output.xml";

		final RackDataModel rdm = tc.rackMarshallingService.loadBaseRackFromFile( filename );

		tc.rackService.getRackGraphInstance( rdm );
		tc.rackService.destroyRackDataModel( rdm );
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		tc.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		tc.tearDown();
		super.tearDown();
	}
}
