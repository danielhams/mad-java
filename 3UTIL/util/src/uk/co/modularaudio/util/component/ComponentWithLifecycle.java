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

package uk.co.modularaudio.util.component;

import uk.co.modularaudio.util.exception.ComponentConfigurationException;

/**
 * <p>An interface that components (controllers, services etc) should implement
 * that grants them lifecycle calls after any necessary dependency
 * injection has taken place.</p>
 *
 * <p>Components wishing for additional initialisation/destruction calls should
 * also implement {@link ComponentWithPostInitPreShutdown}.</p>
 *
 * @author dan
 *
 */
public interface ComponentWithLifecycle
{
    /**
     * An initialisation method that will be called after any necessary
     * dependency injection has occurred.
     *
     * @throws ComponentConfigurationException should the initialisation fail
     */
    public void init() throws ComponentConfigurationException;

    /**
     * A method allowing a component/service to clean up any held resources.
     */
    public void destroy();
}
