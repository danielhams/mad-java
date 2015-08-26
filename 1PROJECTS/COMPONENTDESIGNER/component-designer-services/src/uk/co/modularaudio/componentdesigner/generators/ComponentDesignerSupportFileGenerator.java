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

package uk.co.modularaudio.componentdesigner.generators;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.componentdesigner.ComponentDesigner;
import uk.co.modularaudio.service.hibsession.impl.HibernateSessionServiceImpl;
import uk.co.modularaudio.util.audio.fft.JTransformsConfigurator;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;
import uk.co.modularaudio.util.audio.oscillatortable.StandardBandLimitedWaveTables;
import uk.co.modularaudio.util.audio.oscillatortable.StandardWaveTables;

public class ComponentDesignerSupportFileGenerator
{
	private static Log log = LogFactory.getLog( ComponentDesignerSupportFileGenerator.class.getName() );

	private final ComponentDesigner cd;

	private final String outputDirectory;

	public ComponentDesignerSupportFileGenerator( final String outputDirectory )
			throws Exception
	{
		cd = new ComponentDesigner();

		this.outputDirectory = outputDirectory;
	}

	public void init() throws Exception
	{
		cd.setupApplicationContext( ComponentDesigner.CDRELEASEGENERATOR_PROPERTIES, null, null, true, true );
	}

	public void destroy() throws Exception
	{
		cd.signalPreExit();
		cd.signalPostExit();
	}

	public void generateFiles() throws Exception
	{
		generateBlw();
	}

	public void initialiseThingsNeedingComponentGraph() throws Exception
	{
	}

	public static void main( final String[] args ) throws Exception
	{
		if( args.length != 1 )
		{
			throw new IOException( "Expecting only output directory: outputDir" );
		}
		if( log.isInfoEnabled() )
		{
			log.info("Creating output in '" + args[0] + "'");
		}

		JTransformsConfigurator.setThreadsToOne();

		final LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
		final Configuration config = ctx.getConfiguration();
		final LoggerConfig loggerConfig = config.getLoggerConfig( LogManager.ROOT_LOGGER_NAME );
		loggerConfig.setLevel( Level.INFO );
		ctx.updateLoggers();

		final ComponentDesignerSupportFileGenerator sfg = new ComponentDesignerSupportFileGenerator( args[0] );
		sfg.generateFiles();
		sfg.init();
		sfg.initialiseThingsNeedingComponentGraph();
		final String[] dbFilesToMove = sfg.getDatabaseFiles();
		sfg.destroy();

		// Finally move the (now closed) database files into the output directory
		for( final String dbFileToMove : dbFilesToMove )
		{
			final File source = new File( dbFileToMove );
			final String fileName = source.getName();
			final File target = new File( args[0] + File.separatorChar + fileName );
			Files.move( source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE );
		}

	}

	private String[] getDatabaseFiles()
	{
		final GenericApplicationContext gac = cd.getApplicationContext();
		final HibernateSessionServiceImpl hssi = gac.getBean( HibernateSessionServiceImpl.class );
		final String dbFilename = hssi.getDatabaseFilename();

		final String[] dbFiles = new String[2];
		dbFiles[0] = dbFilename + ".script";
		dbFiles[1] = dbFilename + ".properties";

		return dbFiles;
	}

	private void generateBlw() throws Exception
	{
		log.info( "Check if wave tables need to be generated..." );

		final String waveTablesOutputDirectory = outputDirectory + File.separatorChar + "wavetables";

		final StandardWaveTables swt = StandardWaveTables.getInstance( waveTablesOutputDirectory );
		final StandardBandLimitedWaveTables sblwt = StandardBandLimitedWaveTables
				.getInstance( waveTablesOutputDirectory );

		for( final OscillatorWaveShape shape : OscillatorWaveShape.values() )
		{
			swt.getTableForShape( shape );
			// No band limited tables for sine - it has no harmonics
			if( shape != OscillatorWaveShape.SINE )
			{
				sblwt.getMapForShape( shape );
			}
		}
		log.info( "Completed wave tables check" );
	}
}
