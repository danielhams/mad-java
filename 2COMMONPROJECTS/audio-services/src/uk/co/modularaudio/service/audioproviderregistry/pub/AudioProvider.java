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

package uk.co.modularaudio.service.audioproviderregistry.pub;

import java.util.List;

import uk.co.modularaudio.service.apprenderinggraph.vos.AppRenderingErrorCallback;
import uk.co.modularaudio.service.apprenderinggraph.vos.AppRenderingErrorQueue;
import uk.co.modularaudio.service.apprenderinggraph.vos.AbstractAppRenderingIO;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDeviceCriteria;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDeviceCriteria;
import uk.co.modularaudio.util.exception.DatastoreException;

public abstract class AudioProvider
{
	private String id;
	
	protected AudioProvider( String id )
	{
		this.id = id;
	}
	
	public String getId()
	{
		return id;
	}

	public abstract List<? extends MidiHardwareDevice> getAllConsumerMidiDevices( MidiHardwareDeviceCriteria criteria )
			throws DatastoreException;

	public abstract List<? extends MidiHardwareDevice> getAllProducerMidiDevices( MidiHardwareDeviceCriteria criteria )
			throws DatastoreException;
	
	public abstract List<? extends AudioHardwareDevice> getAllProducerAudioDevices( AudioHardwareDeviceCriteria criteria )
			throws DatastoreException;

	public abstract List<? extends AudioHardwareDevice> getAllConsumerAudioDevices( AudioHardwareDeviceCriteria criteria )
			throws DatastoreException;

	public abstract AbstractAppRenderingIO createAppRenderingIOForConfiguration( HardwareIOConfiguration hardwareIOConfiguration,
			AppRenderingErrorQueue errorQueue,
			AppRenderingErrorCallback errorCallback )
		throws DatastoreException;
}
