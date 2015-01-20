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
import java.util.LinkedList;
import java.util.Vector;

/**
 * <P>FIFO structure for resource storage in the pool.</P>
 * @author dan
 * @version 1.0
 * @see uk.co.modularaudio.util.pooling.common.PoolStructure
 */
public class QueuePoolStructure extends PoolStructure
{
  @Override
  public Resource useFreeResource()
  {
	  Resource res = freeQueue.removeFirst();
	  busyQueue.add(res);
	  return(res);
  }

  @Override
  public void releaseUsedResource(Resource res)
  {
	  busyQueue.remove( res );
	  freeQueue.addLast( res );
  }

  @Override
  public void addResource(Resource res)
  {
	  freeQueue.addLast( res );
  }

  @Override
  public Resource removeResource(Resource res)
  {
	  Resource retVal = null;
	  busyQueue.remove( res );
      freeQueue.remove( res );
      return( retVal );
  }

  @Override
  public Resource removeAnyFreeResource()
  {
	  Resource res = freeQueue.getLast();
	  removeResource(res);
	  return(res);
  }

  @Override
  public Collection<Resource> getAllResources()
  {
	  Collection<Resource> col = new Vector<Resource>();
	  col.addAll(freeQueue);
	  col.addAll(busyQueue);
	  return(col);
  }

  @Override
  public Collection<Resource> getAllBusyResources()
  {
	  Collection<Resource> col = new Vector<Resource>();
	  col.addAll(busyQueue);
	  return(col);
  }

  @Override
  public int freeSize()
  {
	  return(freeQueue.size());
  }

  @Override
  public int busySize()
  {
	  return(busyQueue.size());
  }

  protected LinkedList<Resource> busyQueue = new LinkedList<Resource>();
  protected LinkedList<Resource> freeQueue = new LinkedList<Resource>();
}
