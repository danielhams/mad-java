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

package uk.co.modularaudio.service.rackmarshalling.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.mads.subrack.ui.SubRackMadUiInstance;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack_0_0_1.ObjectFactory;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack_0_0_1.RackComponentParameterValueXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack_0_0_1.RackComponentXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack_0_0_1.RackControlXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack_0_0_1.RackIOLinkXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack_0_0_1.RackLinkXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack_0_0_1.RackPositionXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack_0_0_1.RackXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack_0_0_1.SubRackXmlType;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.ContentsAlreadyAddedException;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;
import uk.co.modularaudio.util.table.TablePosition;

public class RackMarshallingServiceImpl implements ComponentWithLifecycle, RackMarshallingService
{
	private static Log log = LogFactory.getLog( RackMarshallingServiceImpl.class.getName() );

	private RackService rackService = null;
	private MadComponentService componentService = null;

	private ObjectFactory objectFactory = null;
	private JAXBContext jbContext = null;
	private Unmarshaller unmarshaller = null;
	private Marshaller marshaller = null;

	@Override
	public RackDataModel loadRackFromFile(String filename) throws DatastoreException, IOException
	{
		log.debug("Load rack from file (" + filename + ")");
		RackDataModel retVal = null;
		try
		{
			@SuppressWarnings("unchecked")
			JAXBElement<RackXmlType> jbRackElement = (JAXBElement<RackXmlType>)unmarshaller.unmarshal( new File( filename ) );
			RackXmlType jbRackXml = jbRackElement.getValue();
			retVal = loadRackFromStructure( filename, jbRackXml );
		}
		catch (Exception e)
		{
			String msg = "Exception caught loading rack from file: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
		retVal.setDirty( false );
		return retVal;
	}

	private RackDataModel loadRackFromStructure( String filename, RackXmlType jbRackXml )
		throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException, IOException, ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException
	{
		log.debug("Load rack from structure (" + filename + ")");
		RackDataModel retVal = null;

		String rackName = jbRackXml.getName();
		int numCols = jbRackXml.getCols();
		int numRows = jbRackXml.getRows();

		// TODO: Move the size check of the rack master io to the post init
		// so it's only done once.
		// Work out how big the IO component is
		RackDataModel blah = rackService.createNewRackDataModel( "tmp", "", 8, 10, true );
		RackComponent tmpmirc = blah.getContentsAtPosition( 0, 0 );

		int masterIOYOffset = tmpmirc.getCellSpan().y;

		retVal = rackService.createNewRackDataModel( rackName, filename, numCols, numRows + masterIOYOffset, true );

		RackComponent mirc = retVal.getContentsAtPosition( 0, 0 );

		// Now loop over all the components creating them
		List<RackComponentXmlType> jbRackComponentXmls = jbRackXml.getRackComponent();
		for( RackComponentXmlType jbRackComponentXml : jbRackComponentXmls )
		{
			String definitionId = jbRackComponentXml.getDefinitionId();
			String componentName = jbRackComponentXml.getName();
			MadDefinition<?,?> madDefinition = componentService.findDefinitionById( definitionId );
			RackPositionXmlType rackPositionXml = jbRackComponentXml.getRackPosition();
			int col = rackPositionXml.getColumn();
			int row = rackPositionXml.getRow() + masterIOYOffset;

			Map<MadParameterDefinition, String> parameterValues = new HashMap<MadParameterDefinition, String>();
			List<RackComponentParameterValueXmlType> jbParamValues = jbRackComponentXml.getRackComponentParameterValue();
			for( RackComponentParameterValueXmlType jbParamValue : jbParamValues )
			{
				lookupAndAddParameterValue( madDefinition, parameterValues, jbParamValue );
			}

			RackComponent rackComponent = rackService.createComponentAtPosition( retVal, madDefinition, parameterValues, componentName, col, row );
			MadInstance<?,?> madInstance = rackComponent.getInstance();

			// If it's a subgraph, check to see if we load the library path, or we "load" from the incoming data.
			if( jbRackComponentXml instanceof SubRackXmlType )
			{
				SubRackMadInstance subRackInstance = (SubRackMadInstance)madInstance;
				SubRackMadUiInstance subRackUiInstance = (SubRackMadUiInstance)rackComponent.getUiInstance();

				SubRackXmlType jbSubRackType = (SubRackXmlType)jbRackComponentXml;
				if( jbSubRackType.isLocalSubRack() )
				{
					// Create the rack structure from what's below in the file
					RackDataModel subRackDataModel = loadRackFromStructure( subRackInstance.getInstanceName(), jbSubRackType.getRack() );

					subRackUiInstance.setSubRackDataModel( subRackDataModel, true );
				}
				else
				{
					// Load the rack structure from the library path
					String libraryPath = jbSubRackType.getLibraryPath();
					RackDataModel subRackDataModel = loadRackFromFile( libraryPath );

					subRackUiInstance.setSubRackDataModel( subRackDataModel, true );
				}
			}

			List<RackControlXmlType> jbRackControlXmls = jbRackComponentXml.getRackControl();
//			RackComponent uiComponent = slowLookupByName( retVal, componentName );
			MadUiControlInstance<?,?,?>[] uiControlInstances = rackComponent.getUiControlInstances();
			for( RackControlXmlType jbControlType : jbRackControlXmls )
			{
				String name = jbControlType.getName();
				String value = jbControlType.getValue();
				// Find the control
				MadUiControlInstance<?,?,?> uiControlInstance = lookupUiControlInstanceByName( uiControlInstances, name );
				if( uiControlInstance != null )
				{
					uiControlInstance.receiveControlValue( value );
				}
				else
				{
					log.warn("File contains control value for " + name + " but I couldn't find a control with that name");
				}
			}
		}

		// Now the Graph IO links
		List<RackIOLinkXmlType> rackIOLinkXmls = jbRackXml.getRackIOLink();
		for( RackIOLinkXmlType rackIOLinkXml : rackIOLinkXmls )
		{
			String rackChannelName = rackIOLinkXml.getRackChannelName();
			String componentName = rackIOLinkXml.getRackComponentName();
			String componentChannelName = rackIOLinkXml.getRackComponentChannelInstanceName();
			MadChannelInstance rackChannelInstance = mirc.getInstance().getChannelInstanceByName( rackChannelName );
			RackComponent rackComponent = slowLookupByName( retVal,  componentName );
			MadChannelInstance rackComponentChannelInstance = rackComponent.getInstance().getChannelInstanceByName( componentChannelName );
			rackService.addRackIOLink( retVal, rackChannelInstance, rackComponent, rackComponentChannelInstance );
		}

		// And the links
		List<RackLinkXmlType> rackLinkXmls = jbRackXml.getRackLink();
		for( RackLinkXmlType rackLinkXml : rackLinkXmls )
		{
			String producerName = rackLinkXml.getProducerRackComponentName();
			String producerChannelName = rackLinkXml.getProducerChannelName();
			String consumerName = rackLinkXml.getConsumerRackComponentName();
			String consumerChannelName = rackLinkXml.getConsumerChannelName();
			RackComponent producerRackComponent = slowLookupByName( retVal, producerName );
			MadChannelInstance producerChannelInstance = producerRackComponent.getInstance().getChannelInstanceByName( producerChannelName );
			RackComponent consumerRackComponent = slowLookupByName( retVal, consumerName );
			MadChannelInstance consumerChannelInstance = consumerRackComponent.getInstance().getChannelInstanceByName( consumerChannelName );
			rackService.addRackLink( retVal, producerRackComponent, producerChannelInstance, consumerRackComponent, consumerChannelInstance );
		}
		return retVal;
	}

	private void lookupAndAddParameterValue( MadDefinition<?,?> definition,
			Map<MadParameterDefinition, String> parameterValues,
			RackComponentParameterValueXmlType jbParamValue )
		throws RecordNotFoundException
	{
		MadParameterDefinition[] paramDefs = definition.getParameterDefinitions();
		String paramId = jbParamValue.getParameterName();
		boolean foundIt = false;
		for( int i = 0; !foundIt && i < paramDefs.length ; i++ )
		{
			MadParameterDefinition aupd = paramDefs[ i ];
			if( aupd.getKey().equals( paramId ) )
			{
				foundIt = true;
				parameterValues.put( aupd, jbParamValue.getValue() );
			}
		}
		if( !foundIt )
		{
			String msg = "Unable to find parameter with id " + paramId + " for definition " + definition.getId();
			throw new RecordNotFoundException( msg );
		}
	}

	private MadUiControlInstance<?,?,?> lookupUiControlInstanceByName( MadUiControlInstance<?,?,?>[] uiControlInstances,
			String name)
		throws RecordNotFoundException
	{
		MadUiControlInstance<?,?,?> retVal = null;
		boolean foundIt = false;
		for( int i = 0 ; !foundIt && i < uiControlInstances.length ; i++ )
		{
			MadUiControlInstance<?,?,?> uci = uiControlInstances[ i ];
			if( uci.getUiControlDefinition().getControlName().equals( name ) )
			{
				foundIt = true;
				retVal = uci;
			}
		}

		return retVal;
	}

	private RackComponent slowLookupByName( RackDataModel rackDataModel, String componentName ) throws RecordNotFoundException
	{
		RackComponent retVal = null;
		boolean foundIt = false;
		List<RackComponent> entries = rackDataModel.getEntriesAsList();
		for( int i = 0 ; !foundIt && i < entries.size() ; i++ )
		{
			RackComponent test = entries.get( i );
			if( test.getComponentName().equals( componentName ) )
			{
				foundIt = true;
				retVal = test;
			}
		}
		if( !foundIt )
		{
			throw new RecordNotFoundException();
		}
		return retVal;
	}

	@Override
	public void saveRackToFile(RackDataModel dataModel, String filename) throws DatastoreException, IOException
	{
		dataModel.setPath( filename );

		RackXmlType jbRackXmlType = rackDataModelToXmlTypes( dataModel );

		JAXBElement<RackXmlType> jbRack = objectFactory.createRack( jbRackXmlType );

		try
		{
			// Now write it out
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			marshaller.marshal( jbRack, new FileOutputStream( filename ) );
		}
		catch (Exception e)
		{
			String msg = "Exception caught marshalling rack into XML: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

	private RackXmlType rackDataModelToXmlTypes( RackDataModel rackDataModel )
	{
		RackComponent rackMasterIOComponent = rackDataModel.getContentsAtPosition( 0, 0 );
		int yOffset = rackMasterIOComponent.getCellSpan().y;

		RackXmlType jbRackXmlType = objectFactory.createRackXmlType();
		jbRackXmlType.setName( rackDataModel.getName() );
		jbRackXmlType.setCols( rackDataModel.getNumCols() );
		jbRackXmlType.setRows( rackDataModel.getNumRows() - yOffset );

		List<RackComponentXmlType> jbRootRackComponents = jbRackXmlType.getRackComponent();
		List<RackIOLinkXmlType> jbRootRackIOLinks = jbRackXmlType.getRackIOLink();
		List<RackLinkXmlType> jbRootRackLinks = jbRackXmlType.getRackLink();

		Set<RackIOLink> rackIOLinks = rackDataModel.getRackIOLinks();
		ArrayList<RackIOLink> sortedRackIOLinks = new ArrayList<RackIOLink>( rackIOLinks );
		Collections.sort( sortedRackIOLinks, new Comparator<RackIOLink>(){

			@Override
			public int compare( RackIOLink o1, RackIOLink o2 )
			{
				if( o1.getRackChannelInstance() == o2.getRackChannelInstance() )
				{
					if( o1.getRackComponent() == o2.getRackComponent() )
					{
						return o1.getRackChannelInstance().definition.name.compareTo( o2.getRackChannelInstance().definition.name );
					}
					else
					{
						return o1.getRackComponent().getComponentName().compareTo( o2.getRackComponent().getComponentName() );
					}
				}
				else
				{
					return o1.getRackChannelInstance().definition.name.compareTo( o2.getRackChannelInstance().definition.name );
				}
			}
		}
		);

		List<RackComponent> rackComponents = rackDataModel.getEntriesAsList();
		ArrayList<RackComponent> sortedRackComponents = new ArrayList<RackComponent>( rackComponents );
		Collections.sort( sortedRackComponents, new Comparator<RackComponent>(){

			@Override
			public int compare( RackComponent o1, RackComponent o2 )
			{
				return o1.getComponentName().compareTo( o2.getComponentName() );
			}
		}
		);

		Set<RackLink> rackLinks = rackDataModel.getLinks();
		ArrayList<RackLink> sortedRackLinks = new ArrayList<RackLink>( rackLinks );
		Collections.sort( sortedRackLinks, new Comparator<RackLink>(){

			@Override
			public int compare( RackLink o1, RackLink o2 )
			{
				RackComponent o1producerRackComponent = o1.getProducerRackComponent();
				RackComponent o2producerRackComponent = o2.getProducerRackComponent();
				MadChannelInstance o1producerChannelInstance = o1.getProducerChannelInstance();
				MadChannelInstance o2producerChannelInstance = o2.getProducerChannelInstance();
				if( o1producerRackComponent == o2producerRackComponent )
				{
					if( o1producerChannelInstance == o2producerChannelInstance )
					{
						RackComponent o1consumerRackComponent = o1.getConsumerRackComponent();
						RackComponent o2consumerRackComponent = o2.getConsumerRackComponent();
						if( o1consumerRackComponent == o2consumerRackComponent )
						{
							MadChannelInstance o1consumerChannelInstance = o1.getConsumerChannelInstance();
							MadChannelInstance o2consumerChannelInstance = o2.getConsumerChannelInstance();
							return o1consumerChannelInstance.definition.name.compareTo( o2consumerChannelInstance.definition.name );
						}
						else
						{
							return o1consumerRackComponent.getComponentName().compareTo( o2consumerRackComponent.getComponentName() );
						}
					}
					else
					{
						return o1producerChannelInstance.definition.name.compareTo( o2producerChannelInstance.definition.name );
					}
				}
				else
				{
					return o1producerRackComponent.getComponentName().compareTo( o2producerRackComponent.getComponentName() );
				}
			}
		}
		);

		for( RackIOLink rackIOLink : sortedRackIOLinks )
		{
			MadChannelInstance rackChannelInstance = rackIOLink.getRackChannelInstance();
			RackComponent rackComponent = rackIOLink.getRackComponent();
			MadChannelInstance rackComponentChannelInstance = rackIOLink.getRackComponentChannelInstance();

			RackIOLinkXmlType jbRackIOLinkType = objectFactory.createRackIOLinkXmlType();
			jbRackIOLinkType.setRackChannelName( rackChannelInstance.definition.name );
			jbRackIOLinkType.setRackComponentName( rackComponent.getComponentName() );
			jbRackIOLinkType.setRackComponentChannelInstanceName( rackComponentChannelInstance.definition.name );

			jbRootRackIOLinks.add( jbRackIOLinkType );
		}

		// Create the components
		for( RackComponent component : sortedRackComponents )
		{
			if( component == rackMasterIOComponent )
			{
				continue;
			}

			MadInstance<?,?> instance = component.getInstance();
			if( instance instanceof SubRackMadInstance )
			{
				SubRackXmlType jbSubRackType = objectFactory.createSubRackXmlType();

				RackPositionXmlType jbRackPositionType = objectFactory.createRackPositionXmlType();
				TablePosition contentsOrigin = rackDataModel.getContentsOriginReturnNull(component);
				jbRackPositionType.setColumn( contentsOrigin.x );
				jbRackPositionType.setRow( contentsOrigin.y - yOffset );

				jbSubRackType.setRackPosition( jbRackPositionType );

				SubRackMadInstance sraui = (SubRackMadInstance)instance;

				jbSubRackType.setName( sraui.getInstanceName() );
				MadDefinition<?,?> aud = instance.getDefinition();
				jbSubRackType.setDefinitionId( aud.getId() );

				String srp = sraui.getSubRackDataModel().getPath();
				if( srp != null && !srp.equals("") && !sraui.isDirty() )
				{
					// Is a preset, just persist the id
					jbSubRackType.setLocalSubRack( false );
					jbSubRackType.setLibraryPath( srp );
				}
				else
				{
					// Not a preset, or we are dirty - persist the subgraph data directly underneath
					jbSubRackType.setLibraryPath( "" );
					jbSubRackType.setLocalSubRack( true );

					RackXmlType localSubRack = rackDataModelToXmlTypes( sraui.getSubRackDataModel() );
					jbSubRackType.setRack( localSubRack );
				}
				jbRootRackComponents.add( jbSubRackType );
			}
			else
			{
				RackComponentXmlType jbRackComponentType = objectFactory.createRackComponentXmlType();

				Map<MadParameterDefinition, String> parameterValueMap = instance.getCreationParameterValues();
				ArrayList<MadParameterDefinition> paramsByName = new ArrayList<MadParameterDefinition>();
				paramsByName.addAll( parameterValueMap.keySet() );
				Collections.sort( paramsByName, new Comparator<MadParameterDefinition>() {

					@Override
					public int compare( MadParameterDefinition o1, MadParameterDefinition o2 )
					{
						return o1.getKey().compareTo( o2.getKey() );
					}
				} );

				for( MadParameterDefinition paramDef : paramsByName )
				{
					String paramValue = parameterValueMap.get( paramDef );
					RackComponentParameterValueXmlType jbRackParamType = objectFactory.createRackComponentParameterValueXmlType();
					jbRackParamType.setParameterName( paramDef.getKey() );
					jbRackParamType.setValue( paramValue );
					jbRackComponentType.getRackComponentParameterValue().add( jbRackParamType );
				}

				jbRackComponentType.setName( component.getComponentName() );
				jbRackComponentType.setDefinitionId( component.getInstance().getDefinition().getId() );

				RackPositionXmlType jbRackPositionType = objectFactory.createRackPositionXmlType();
				TablePosition contentsOrigin = rackDataModel.getContentsOriginReturnNull(component);
				jbRackPositionType.setColumn( contentsOrigin.x );
				jbRackPositionType.setRow( contentsOrigin.y - yOffset );

				jbRackComponentType.setRackPosition( jbRackPositionType );

				// Grab the controls and persist their value too
				MadUiControlInstance<?,?,?>[] uiInstances = component.getUiControlInstances();
				for( MadUiControlInstance<?,?,?> cui : uiInstances )
				{
//					log.debug("Found a UI instance to persist: " + cui.getClass().getName() );
					MadUiControlDefinition<?,?,?> cud = cui.getUiControlDefinition();
					String controlName = cud.getControlName();
					String controlValue = cui.getControlValue();

					RackControlXmlType jbRackControlType = objectFactory.createRackControlXmlType();

					jbRackControlType.setName( controlName );
					jbRackControlType.setValue( controlValue );
					jbRackComponentType.getRackControl().add( jbRackControlType );
				}
				jbRootRackComponents.add( jbRackComponentType );
			}
		}

		// Same for the links
		for( RackLink link : sortedRackLinks )
		{
			RackLinkXmlType jbRackLinkType = objectFactory.createRackLinkXmlType();

			MadInstance<?,?> consumerInstance = link.getConsumerChannelInstance().instance;
			MadChannelInstance consumerChannelInstance = link.getConsumerChannelInstance();
			MadChannelDefinition consumerChannelDefinition = consumerChannelInstance.definition;

			jbRackLinkType.setConsumerRackComponentName( consumerInstance.getInstanceName() );
			jbRackLinkType.setConsumerChannelName( consumerChannelDefinition.name );

			MadInstance<?,?> producerInstance = link.getProducerChannelInstance().instance;
			MadChannelInstance producerChannelInstance = link.getProducerChannelInstance();
			MadChannelDefinition producerChannelDefinition = producerChannelInstance.definition;

			jbRackLinkType.setProducerRackComponentName( producerInstance.getInstanceName() );
			jbRackLinkType.setProducerChannelName( producerChannelDefinition.name );

			jbRootRackLinks.add( jbRackLinkType );
		}

		return jbRackXmlType;
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		try
		{
			objectFactory = new ObjectFactory();
			jbContext = JAXBContext.newInstance(  objectFactory.getClass().getPackage().getName() );
			unmarshaller = jbContext.createUnmarshaller();
			marshaller =jbContext.createMarshaller();
		}
		catch (Exception e)
		{
			String msg = "Exception caught initialising jaxb infrastructure.";
			log.error( msg, e );
			throw new ComponentConfigurationException( msg, e  );
		}
	}

	public RackService getRackService()
	{
		return rackService;
	}

	public void setRackService(RackService rackService)
	{
		this.rackService = rackService;
	}

	public MadComponentService getComponentService()
	{
		return componentService;
	}

	public void setComponentService(MadComponentService componentService)
	{
		this.componentService = componentService;
	}

}
