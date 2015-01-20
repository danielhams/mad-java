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

package uk.co.modularaudio.util.pooling.common;

import java.util.Collection;
import java.util.Stack;
import java.util.Vector;

/**
 * <P>
 * LIFO structure for resource storage in the pool.
 * </P>
 *
 * @author dan
 * @version 1.0
 * @see uk.co.modularaudio.util.pooling.common.PoolStructure
 */
public class StackPoolStructure extends PoolStructure
{
	@Override
	public Resource useFreeResource()
	{
		Resource res = freeStack.pop();
		busyStack.push( res );
		return (res);
	}

	@Override
	public void releaseUsedResource( Resource res )
	{
		busyStack.remove( res );
		freeStack.push( res );
	}

	@Override
	public void addResource( Resource res )
	{
		freeStack.push( res );
	}

	@Override
	public Resource removeResource( Resource res )
	{
		busyStack.remove( res );
		freeStack.remove( res );
		return (res);
	}

	@Override
	public Resource removeAnyFreeResource()
	{
		Resource res = freeStack.firstElement();
		removeResource( res );
		return (res);
	}

	@Override
	public Collection<Resource> getAllResources()
	{
		Collection<Resource> col = new Vector<Resource>();
		col.addAll( freeStack );
		col.addAll( busyStack );
		return (col);
	}

	@Override
	public Collection<Resource> getAllBusyResources()
	{
		Collection<Resource> col = new Vector<Resource>();
		col.addAll( busyStack );
		return (col);
	}

	@Override
	public int freeSize()
	{
		return (freeStack.size());
	}

	@Override
	public int busySize()
	{
		return (busyStack.size());
	}

	protected Stack<Resource> busyStack = new Stack<Resource>();

	protected Stack<Resource> freeStack = new Stack<Resource>();
}
