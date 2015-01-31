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

package uk.co.modularaudio.util.hibernate.generator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.hibernate.component.ComponentWithHibernatePersistence;
import uk.co.modularaudio.util.hibernate.component.HibernatePersistedBeanDefinition;
import uk.co.modularaudio.util.hibernate.component.TableNamePrefixer;
import uk.co.modularaudio.util.io.IOUtils;
import uk.co.modularaudio.util.spring.SpringComponentHelper;

public class GeneratorHelper
{
	/**
	 * Generate the DDL for one component implementing the IsHibernatePersistedComponent interface
	 * @param component The persisted component we wish to obtain the DDL for
	 * @param dialectName Which hibernate dialect should be used
	 * @param destinationDirectory The output directory
	 * @param outputFileName The output filename
	 * @throws IOException On errors reading the related hbm files for a component or writing the output DDL file
	 */
	public void generateDDL( final ComponentWithHibernatePersistence component, final String dialectName, final String destinationDirectory,
			final String outputFileName ) throws IOException
	{
		final Configuration configuration = new Configuration();
		final List<HibernatePersistedBeanDefinition> listHBM = component.listHibernatePersistedBeanDefinitions();
		for (final HibernatePersistedBeanDefinition hbmDefinition : listHBM)
		{
			final String originalHbmResourceName = hbmDefinition.getHbmResourceName();

			final InputStream hbmInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( originalHbmResourceName );

			final String originalHbmString = IOUtils.basicReadInputStreamUTF8( hbmInputStream );

			final String newHbmString = TableNamePrefixer.prefixTableNames( originalHbmString, hbmDefinition.getPersistedBeanTablePrefix());

			configuration.addXML( newHbmString );

		}
		configureAndGenerate( dialectName, destinationDirectory, outputFileName, configuration );
	}

	/**
	 * Method to configure necessary hibernate properties and generate DDL for a supplied configuration
	 * @param dialectName Which hibernate dialect should be used
	 * @param destinationDirectory The output directory
	 * @param outputFileName The output filename
	 * @param configuration The hibernate configuration setup with the appropriate schema objects
	 */
	private void configureAndGenerate(final String dialectName, final String destinationDirectory, final String outputFileName,
			final Configuration configuration)
	{
		final Properties dialect = new Properties();
		dialect.setProperty("hibernate.dialect", dialectName);
		configuration.addProperties(dialect);

		final SchemaExport se = new SchemaExport(configuration);
		se.setOutputFile(destinationDirectory + outputFileName);
		se.setDelimiter(";\n");
		se.create(true, false);
	}

	/**
	 * Generate an appropriate DDL by loading a Spring beans.xml and searching for components implementing the IsHibernatePersistedComponent interface
	 * @param beansFilename The Spring beans.xml file to read
	 * @param configurationFilename The configuration filename necessary
	 * @param dialectName The hibernate dialect that should be used
	 * @param destinationDirectory The output directory
	 * @param outputFileName The output filename
	 * @throws DatastoreException A general failure during processing (such as starting up the application context)
	 * @throws IOException On errors setting up the components, reading the HBM configurations or writing the output DDL file
	 */
	public void generateFromBeansDDL(final String beansFilename, final String configurationFilename, final String dialectName, final String destinationDirectory,
			final String outputFileName)
		throws DatastoreException, IOException
	{
		final Configuration configuration = new Configuration();
		final SpringComponentHelper sch = new SpringComponentHelper();
		final GenericApplicationContext appContext = sch.makeAppContext( beansFilename, configurationFilename);

		final Map<String,ComponentWithHibernatePersistence> components = appContext.getBeansOfType(ComponentWithHibernatePersistence.class);

		for (final Iterator<ComponentWithHibernatePersistence> iter = components.values().iterator(); iter.hasNext();)
		{
			final ComponentWithHibernatePersistence component = iter.next();
			final List<HibernatePersistedBeanDefinition> hbmDefinitions = component.listHibernatePersistedBeanDefinitions();

			for( final HibernatePersistedBeanDefinition beanDefinition : hbmDefinitions )
			{
				final String originalHbmResourceName = beanDefinition.getHbmResourceName();

				final InputStream hbmInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( originalHbmResourceName );

				final String originalHbmString = IOUtils.basicReadInputStreamUTF8( hbmInputStream );

				final String newHbmString = TableNamePrefixer.prefixTableNames( originalHbmString, beanDefinition.getPersistedBeanTablePrefix());

				configuration.addXML( newHbmString );
			}
		}
		configureAndGenerate(dialectName, destinationDirectory, outputFileName, configuration);
		appContext.close();
	}
}
