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

package uk.co.modularaudio.mads.base.imixern.ui.lane;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.JLabel;


public class AmpSliderLabelHashtable extends Hashtable<Integer,JLabel>
{
	private static final long serialVersionUID = -317936996704989571L;

//	private static Log log = LogFactory.getLog( AmpSliderLabelHashtable.class.getName() );

	public AmpSliderLabelHashtable()
	{
	}

	@Override
	public synchronized JLabel get( final Object key )
	{
//		Integer i = (Integer)key;
//		log.debug("Attempted to fetch " + key + " which as int is " + i.intValue() );
		return super.get( key );
	}

	@Override
	public synchronized int size()
	{
//		log.debug("Size called.");
		return super.size();
	}

	@Override
	public synchronized boolean containsKey( final Object key )
	{
//		log.debug("Containskey called with " + key );
		return super.containsKey( key );
	}

	@Override
	public Set<Integer> keySet()
	{
//		log.debug("KeySet called");
		return super.keySet();
	}

	@Override
	public synchronized Enumeration<Integer> keys()
	{
//		log.debug("Keys called");
		return super.keys();
	}
}
