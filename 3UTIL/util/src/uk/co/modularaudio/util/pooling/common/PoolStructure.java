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

/**
 * <P>Interface that a structure must support in order to be used as the strategy
 * for resource storage in the pool.</P>
 * @author dan
 * @see uk.co.modularaudio.util.pooling.common.StackPoolStructure
 * @see uk.co.modularaudio.util.pooling.common.QueuePoolStructure
 */
public abstract class PoolStructure
{
  public abstract Resource useFreeResource();

  public abstract void releaseUsedResource(Resource res);

  public abstract void addResource(Resource res);

  public abstract Resource removeResource(Resource res);

  public abstract Resource removeAnyFreeResource();

  public abstract Collection<Resource> getAllResources();

  public abstract Collection<Resource> getAllBusyResources();

  public abstract int freeSize();

  public abstract int busySize();

  public int size()
  {
    return( freeSize() + busySize() );
  }

  @Override
  public String toString()
  {
	  StringBuilder retBuffer = new StringBuilder();
	  retBuffer.append("Free(" + freeSize() + ")");
	  retBuffer.append("Busy(" + busySize() + ")");
	  retBuffer.append("Total(" + size() + ")");
	  return(retBuffer.toString());
  }

  /** @link dependency
   * @label contains instances in different structures (stack/queue etc)*/
  /*#Resource lnkResource;*/

  /** @link dependency
   * @label throws*/
  /*#ResourceNotAvailableException lnkResourceNotAvailableException;*/
}
