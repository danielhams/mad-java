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

package uk.co.modularaudio.controller.apprendering;

import uk.co.modularaudio.service.apprendering.util.AppRenderingSession;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorCallback;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

/**
 * <p>The application rendering controller provides
 * functionality related to audio input/output.</p>
 *
 * @author dan
 */
public interface AppRenderingController
{
	/**
	 * <p>Create an AppRenderingSession with the supplied hardware configuration
	 * for input and output audio and MIDI data.</p>
	 * <p>The session is not started/active.</p>
	 * @param hardwareIOConfiguration the required audio and MIDI devices
	 * @param errorCallback a receiver for errors that occur while the session is active
	 * @return the newly created session in a stopped state
	 * @throws DatastoreException on unrecoverable error
	 * @throws RecordNotFoundException if passed devices are no longer available
	 */
	AppRenderingSession createAppRenderingSessionForConfiguration( HardwareIOConfiguration hardwareIOConfiguration,
			AppRenderingErrorCallback errorCallback )
		throws DatastoreException, RecordNotFoundException;
}
