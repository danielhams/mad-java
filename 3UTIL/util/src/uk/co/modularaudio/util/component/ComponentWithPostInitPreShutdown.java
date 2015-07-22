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
 * that gives additional lifecycle calls after initialisation and before destruction.</p>
 * <p>This is mainly useful for services that have post initialisation work to perform.</p>
 *
 * @author dan
 *
 */
public interface ComponentWithPostInitPreShutdown
{
	/**
	 * Called after initialisation has completed.
	 *
	 * @throws ComponentConfigurationException allowing the component to indicate failure
	 */
	public void postInit() throws ComponentConfigurationException;

	/**
	 * Called before any shutdown/destroy methods.
	 */
	public void preShutdown();
}
