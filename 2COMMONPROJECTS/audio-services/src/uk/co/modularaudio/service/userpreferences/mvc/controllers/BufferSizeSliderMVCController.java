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
	public static OpenIntIntHashMap bufferSizeToModelIndexMap = new OpenIntIntHashMap();
	public static OpenIntIntHashMap modelIndexToBufferSizeMap = new OpenIntIntHashMap();
	
	static
	{
		bufferSizeToModelIndexMap.put( 256, 1 );
		bufferSizeToModelIndexMap.put( 512, 2 );
		bufferSizeToModelIndexMap.put( 1024, 3 );
		bufferSizeToModelIndexMap.put( 1536, 4 );
		bufferSizeToModelIndexMap.put( 2048, 5 );
		bufferSizeToModelIndexMap.put( 2560, 6 );
		bufferSizeToModelIndexMap.put( 4096, 7 );
		bufferSizeToModelIndexMap.put( 8192, 8 );
		bufferSizeToModelIndexMap.put( 16384, 9 );
		bufferSizeToModelIndexMap.put( 32768, 10 );
		
		IntArrayList intArrayList = bufferSizeToModelIndexMap.keys();

		for( int i = 0 ; i < intArrayList.size() ; i++ )
		{
			int bufSize = intArrayList.get( i );
			int modelIndex = bufferSizeToModelIndexMap.get( bufSize );
			
			modelIndexToBufferSizeMap.put( modelIndex, bufSize );
		}
	}

	public BufferSizeSliderMVCController(BasicIntegerSliderModel model, UserPreferencesMVCController userPreferencesMVCController)
	{
		super(model);
	}
}
