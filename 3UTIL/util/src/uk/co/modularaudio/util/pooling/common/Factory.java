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

/**
 * <P>Used to create the resources.</P>
 * <P>Used by the ExpiringResourcePool to create new and/or replacment resources for the pool.</P>
 * @author dan
 * @version 1.0
 * @see uk.co.modularaudio.util.pooling.common.PoolSizingThread
 * @see uk.co.modularaudio.util.pooling.common.Resource
 * @see uk.co.modularaudio.util.pooling.common.ExpiringDynamicStackPool*/
public interface Factory
{
  Resource createResource() throws FactoryProductionException;

  void init() throws FactoryProductionException;

  /** @link dependency
   * @label throws*/
  /*#FactoryProductionException lnkFactoryProductionError;*/
}
