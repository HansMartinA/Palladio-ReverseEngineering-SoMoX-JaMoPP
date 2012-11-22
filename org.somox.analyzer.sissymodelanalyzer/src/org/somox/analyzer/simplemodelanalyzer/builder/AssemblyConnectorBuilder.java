package org.somox.analyzer.simplemodelanalyzer.builder;

import org.apache.log4j.Logger;
import org.somox.analyzer.AnalysisResult;
import org.somox.configuration.SoMoXConfiguration;

import de.fzi.gast.core.Root;
import eu.qimpress.samm.core.NamedEntity;
import eu.qimpress.samm.staticstructure.ComponentType;
import eu.qimpress.samm.staticstructure.CompositeStructure;
import eu.qimpress.samm.staticstructure.Connector;
import eu.qimpress.samm.staticstructure.Port;
import eu.qimpress.samm.staticstructure.StaticstructureFactory;
import eu.qimpress.samm.staticstructure.SubcomponentEndpoint;
import eu.qimpress.samm.staticstructure.SubcomponentInstance;

/**
 * Builder for Assembly Connectors.
 * This builder is complemented by {@link IAssemblyConnectorStrategy}
 * @author Klaus Krogmann
 */
public class AssemblyConnectorBuilder extends AbstractBuilder {

	private static final Logger logger = Logger.getLogger(AssemblyConnectorBuilder.class);
	
	public AssemblyConnectorBuilder(Root gastModel,
			SoMoXConfiguration somoxConfiguration, AnalysisResult analysisResult) {
		super(gastModel, somoxConfiguration, analysisResult);
	}

	/**
	 * Create a new instance of an assembly connector.
	 * @param parentComponent the outer composite component
	 * @param requiredPort the required port of an inner component
	 * @param providedPort the provided port of an inner component
	 * @param requiredComponentType
	 * @param providedComponentType
	 */
	public static Connector createAssemblyConnector(
			CompositeStructure parentComponent,
			Port requiredPort,
			Port providedPort,
			ComponentType requiredComponentType,
			ComponentType providedComponentType) {
		
		Connector newConnector = createAssemblyConnectorEntity(parentComponent,
				requiredComponentType, providedComponentType);
		 
		SubcomponentEndpoint sourceEndpoint = addSubcomponentEndpoint(
				newConnector, parentComponent, requiredComponentType);
		sourceEndpoint.setPort(requiredPort);		
		
		SubcomponentEndpoint targetEndpoint = addSubcomponentEndpoint(
				newConnector, parentComponent, providedComponentType);
		targetEndpoint.setPort(providedPort);
		
		return newConnector;
	}
	
	
	/**
	 * Create a new instance of an assembly connector.
	 * @param parentComponent the outer composite component
	 * @param requiredPort the required port of an inner component
	 * @param providedPort the provided port of an inner component
	 * @param requiredComponentInstance
	 * @param providedComponentInstance
	 */
	public static Connector createAssemblyConnector(
			CompositeStructure parentComponent,
			Port requiredPort,
			Port providedPort,
			SubcomponentInstance requiredComponentInstance,
			SubcomponentInstance providedComponentInstance) {

		Connector newConnector = createAssemblyConnectorEntity(parentComponent,
				requiredComponentInstance, providedComponentInstance);
		 
		SubcomponentEndpoint sourceEndpoint = addSubcomponentEndpoint(
				newConnector, requiredComponentInstance);
		sourceEndpoint.setPort(requiredPort);		
		
		SubcomponentEndpoint targetEndpoint = addSubcomponentEndpoint(
				newConnector, providedComponentInstance);
		targetEndpoint.setPort(providedPort);
		
		return newConnector;		
	}

	private static Connector createAssemblyConnectorEntity(
			CompositeStructure parentComponent,
			NamedEntity requiredComponentType,
			NamedEntity providedComponentType) {
		logger.debug("Creating new assembly connector from "+requiredComponentType.getName()+" to "+providedComponentType.getName());		
		Connector newConnector = StaticstructureFactory.eINSTANCE.createConnector();
		parentComponent.getConnector().add(newConnector);
		newConnector.setDocumentation("Assembly Connector from "+requiredComponentType.getName()+" to "+providedComponentType.getName());
		return newConnector;
	}	

	/**
	 * Adds a subcomponent endpoint to an existing connector.
	 * @param newConnector The connector for which to add a subcomponent endpoint.
	 * @param subcomponentInstance 
	 * @return
	 */
	private static SubcomponentEndpoint addSubcomponentEndpoint(
			Connector newConnector, SubcomponentInstance subcomponentInstance) {
		
		SubcomponentEndpoint endpoint = StaticstructureFactory.eINSTANCE.createSubcomponentEndpoint();
		endpoint.setSubcomponent(subcomponentInstance);
	
		newConnector.getEndpoints().add(endpoint);
		return endpoint;
	}
	
	/**
	 * Adds a subcomponent endpoint to an existing connector.
	 * <br>
	 * Assumption: only one subcomponent instance per component type
	 * @param newConnector The connector for which to add a subcomponent endpoint.
	 * @param parentComponent The parent component of the connector
	 * @param componentTypeOfPort The inner type of the component for which
	 * to create a subcomponent endpoint. Each type must not occur multiple
	 * times within the same composite component. 
	 * @param subcomponentInstance optional; if set to null it will be searched in the 
	 * parent composite component.
	 * @return
	 */
	private static SubcomponentEndpoint addSubcomponentEndpoint(
			Connector newConnector, CompositeStructure parentComponent,
			ComponentType componentTypeOfPort) {
		
		SubcomponentEndpoint endpoint = StaticstructureFactory.eINSTANCE.createSubcomponentEndpoint();

		endpoint.setSubcomponent(findComponentInstance(parentComponent, componentTypeOfPort));

		newConnector.getEndpoints().add(endpoint);
		return endpoint;
	}	

	public static SubcomponentInstance findComponentInstance(
			CompositeStructure parentComponent,
			ComponentType componentTypeOfPort) {
		SubcomponentInstance resultSubcomponentInstance = null;
		boolean found = false;
		for(SubcomponentInstance subcomponentInstance : parentComponent.getSubcomponents()) {
			
			if(subcomponentInstance.getRealizedBy().equals(componentTypeOfPort)) {
				if(found) {
					throw new IllegalArgumentException("Assumption on input model does not hold. " +
							"Only one instance per component type per composite component assumed!");
				}
				resultSubcomponentInstance = subcomponentInstance;
				found = true;
			}
		}
		if(!found) {
			logger.warn("No subcomponent instance found for parent " + parentComponent.getName() +
					" and child component " + componentTypeOfPort.getName());
		}
		return resultSubcomponentInstance;
		
	}

}