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

package uk.co.modularaudio.service.audioproviderregistry;

import java.util.List;

import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDeviceCriteria;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDeviceCriteria;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public interface AudioProviderRegistryService
{
	// Allow components to register themselves as being audio providers
	void registerAudioProvider( AudioProvider provider ) throws DatastoreException, MAConstraintViolationException;
	void unregisterAudioProvider( AudioProvider provider ) throws DatastoreException, RecordNotFoundException;
	public AudioProvider getProviderById(String firstFoundId) throws RecordNotFoundException;
	
	// Channels
	public List<AudioHardwareDevice> getAllProducerAudioDevices( AudioHardwareDeviceCriteria criteria ) throws DatastoreException;
	public List<AudioHardwareDevice> getAllConsumerAudioDevices( AudioHardwareDeviceCriteria criteria ) throws DatastoreException;
	public List<MidiHardwareDevice> getAllProducerMidiDevices( MidiHardwareDeviceCriteria criteria ) throws DatastoreException;
	public List<MidiHardwareDevice> getAllConsumerMidiDevices( MidiHardwareDeviceCriteria criteria ) throws DatastoreException;
}
