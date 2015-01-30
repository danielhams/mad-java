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

package uk.co.modularaudio.service.userpreferences.mvc.controllers;

import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.map.OpenIntIntHashMap;

import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.util.mvc.intslider.impl.BasicIntegerSliderController;
import uk.co.modularaudio.util.mvc.intslider.impl.BasicIntegerSliderModel;

public class BufferSizeSliderMVCController extends BasicIntegerSliderController
{
	public final static OpenIntIntHashMap BUF_SIZE_TO_INDEX_MAP = new OpenIntIntHashMap();
	public final static OpenIntIntHashMap INDEX_TO_BUF_SIZE_MAP = new OpenIntIntHashMap();

	static
	{
		BUF_SIZE_TO_INDEX_MAP.put( 256, 1 );
		BUF_SIZE_TO_INDEX_MAP.put( 512, 2 );
		BUF_SIZE_TO_INDEX_MAP.put( 1024, 3 );
		BUF_SIZE_TO_INDEX_MAP.put( 1536, 4 );
		BUF_SIZE_TO_INDEX_MAP.put( 2048, 5 );
		BUF_SIZE_TO_INDEX_MAP.put( 2560, 6 );
		BUF_SIZE_TO_INDEX_MAP.put( 4096, 7 );
		BUF_SIZE_TO_INDEX_MAP.put( 8192, 8 );
		BUF_SIZE_TO_INDEX_MAP.put( 16384, 9 );
		BUF_SIZE_TO_INDEX_MAP.put( 32768, 10 );

		final IntArrayList intArrayList = BUF_SIZE_TO_INDEX_MAP.keys();

		for( int i = 0 ; i < intArrayList.size() ; i++ )
		{
			final int bufSize = intArrayList.get( i );
			final int modelIndex = BUF_SIZE_TO_INDEX_MAP.get( bufSize );

			INDEX_TO_BUF_SIZE_MAP.put( modelIndex, bufSize );
		}
	}

	public BufferSizeSliderMVCController(final BasicIntegerSliderModel model, final UserPreferencesMVCController userPreferencesMVCController)
	{
		super(model);
	}
}
