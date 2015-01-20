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
 * @author dan
 *
 */
public class FixedSizePool extends Pool
{
    public FixedSizePool(
        int numResources,
        Factory factory)
    {
        synchronized (poolSemaphore)
        {
            // Use a stack structure to store our resources. This makes recently
            // used resources the ones that are used next.
            poolStructure = new StackPoolStructure();

            this.numResources = numResources;
            this.factory = factory;
        }
    }

    @Override
	public void init() throws FactoryProductionException
    {
        synchronized (poolSemaphore)
        {
            // Set the factory up.
            factory.init();

			// Create the required number of resources
			for(int i = 0 ; i < numResources ; i++)
			{
				Resource res = factory.createResource();
				this.addResource( res );
			}

        }
    }

	private int numResources = 0;

}
