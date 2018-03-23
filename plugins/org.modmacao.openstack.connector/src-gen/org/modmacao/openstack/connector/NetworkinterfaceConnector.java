/**
 * Copyright (c) 2016-2017 Inria
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * - Philippe Merle <philippe.merle@inria.fr>
 * - Faiez Zalila <faiez.zalila@inria.fr>
 * - Fabian Korte <fabian.korte@cs.uni-goettingen.de>
 *
 * Generated at Fri Mar 02 15:56:56 CET 2018 from platform:/plugin/org.eclipse.cmf.occi.infrastructure/model/Infrastructure.occie by org.eclipse.cmf.occi.core.gen.connector
 */
package org.modmacao.openstack.connector;

import org.eclipse.cmf.occi.core.AttributeState;
import org.eclipse.cmf.occi.core.MixinBase;
import org.eclipse.cmf.occi.core.util.OcciHelper;
import org.eclipse.cmf.occi.infrastructure.Ipnetworkinterface;
import org.eclipse.cmf.occi.infrastructure.NetworkInterfaceStatus;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV2;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.network.IP;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.builder.PortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import openstackruntime.OpenstackruntimeFactory;
import openstackruntime.Runtimeid;


/**
 * Connector implementation for the OCCI kind:
 * - scheme: http://schemas.ogf.org/occi/infrastructure#
 * - term: networkinterface
 * - title: NetworkInterface Link
 */
public class NetworkinterfaceConnector extends org.eclipse.cmf.occi.infrastructure.impl.NetworkinterfaceImpl
{
	private OSClientV2 os = null;
	private Port port = null;
	/**
	 * Initialize the logger.
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(NetworkinterfaceConnector.class);

	// Start of user code Networkinterfaceconnector_constructor
	/**
	 * Constructs a networkinterface connector.
	 */
	NetworkinterfaceConnector()
	{
		LOGGER.debug("Constructor called on " + this);
	}
	// End of user code
	//
	// OCCI CRUD callback operations.
	//
	
	// Start of user code NetworkinterfaceocciCreate
	/**
	 * Called when this Networkinterface instance is completely created.
	 */
	@Override
	public void occiCreate()
	{
		LOGGER.debug("occiCreate() called on " + this);
		
		os = OpenStackHelper.getInstance().getOSClient();
		
		String networkID = OpenStackHelper.getInstance().getRuntimeID(this.getTarget());
		Network network = os.networking().network().get(networkID);
		String serverID = OpenStackHelper.getInstance().getRuntimeID(this.getSource());
		Server server = os.compute().servers().get(serverID);
		String fixedIP = null;
						
		String subnetID = network.getSubnets().get(0);
		
		String runtimeID = OpenStackHelper.getInstance().getRuntimeID(this);
		
		if (runtimeID != null) {
			port = os.networking().port().get(runtimeID);
			if (port == null) {
				this.setOcciNetworkinterfaceState(NetworkInterfaceStatus.ERROR);
				this.setOcciNetworkinterfaceStateMessage("Runtime id set, but unable to connect to runtime object.");
			}
			return;
		}
		
		for (MixinBase mixin: this.getParts()) {
			if (mixin instanceof Ipnetworkinterface) {
				fixedIP = ((Ipnetworkinterface) mixin).
				getOcciNetworkinterfaceAddress();				
			}
		}
		
		boolean exists = false;
		
		for (Port osport: os.networking().port().list()) {
			if (osport.getDeviceId().equals(server.getId()) 
					&& osport.getNetworkId().equals(networkID)) {
				if (fixedIP != null) {
					for (IP ip: osport.getFixedIps()) {
						if (ip.getIpAddress().equals(fixedIP)) {
							exists = true;
							port = osport;
						}
					}
				} else {
					exists = true;
					port = osport;
					break;
				}
			}
		}
		
		if (exists) {
			LOGGER.debug("Port found with matching properties, set runtimeid accordingly.");
			
			Runtimeid runtimeIDMixin = OpenstackruntimeFactory.eINSTANCE.createRuntimeid();
			runtimeIDMixin.setOpenstackRuntimeId(port.getId());
			this.getParts().add(runtimeIDMixin);
		}
		else {
			LOGGER.debug("No port found with matching properties, creating new port.");
			
			PortBuilder builder = Builders.port();
			builder.name(this.getTitle())
			.networkId(networkID);
		
			if (fixedIP != null) {
				builder.fixedIp(fixedIP, subnetID);
			}
		
			port = os.networking().port().create(builder.build());
			
			Runtimeid runtimeIDMixin = OpenstackruntimeFactory.eINSTANCE.createRuntimeid();
			runtimeIDMixin.setOpenstackRuntimeId(port.getId());
			this.getParts().add(runtimeIDMixin);
			
			os.compute().servers().interfaces().create(
				serverID,
				port.getId());
		}	
	}
	// End of user code

	// Start of user code Networkinterface_occiRetrieve_method
	/**
	 * Called when this Networkinterface instance must be retrieved.
	 */
	@Override
	public void occiRetrieve()
	{
		LOGGER.debug("occiRetrieve() called on " + this);
		os = OpenStackHelper.getInstance().getOSClient();
		
		port = getRuntimeObject();
		
		if (port == null) {
			this.setOcciNetworkinterfaceState(NetworkInterfaceStatus.ERROR);
			this.setOcciNetworkinterfaceStateMessage("Unable to retrieve runtime object.");
			return;
		}
		
		// Update IP address
		for (MixinBase mixin: this.getParts()) {
			if (mixin instanceof Ipnetworkinterface) {
				LOGGER.debug("Associated port has IP: " 
						+ port.getFixedIps().iterator().next().getIpAddress());
				for (AttributeState state: this.getAttributes()) {
					if (state.getName().equals("occi.networkinterface.address")){
						state.setValue(port.getFixedIps().iterator().next().getIpAddress());
					}
				}
				
				OcciHelper.setAttribute(mixin, "occi.networkinterface.address", 
						port.getFixedIps().iterator().next().getIpAddress());
			}
		}
	}
	// End of user code

	// Start of user code Networkinterface_occiUpdate_method
	/**
	 * Called when this Networkinterface instance is completely updated.
	 */
	@Override
	public void occiUpdate()
	{
		LOGGER.debug("occiUpdate() called on " + this);
		// TODO: Implement this callback or remove this method.
	}
	// End of user code

	// Start of user code NetworkinterfaceocciDelete_method
	/**
	 * Called when this Networkinterface instance will be deleted.
	 */
	@Override
	public void occiDelete()
	{
		LOGGER.debug("occiDelete() called on " + this);
		os = OpenStackHelper.getInstance().getOSClient();
		
		port = getRuntimeObject();
		
		if (port != null) {
			os.networking().port().delete(port.getId());
		}
		
		OpenStackHelper.getInstance().removeRuntimeID(this);
		
		this.setOcciNetworkinterfaceState(NetworkInterfaceStatus.INACTIVE);
	}
	
	private Port getRuntimeObject() {
		String runtimeid = OpenStackHelper.getInstance().getRuntimeID(this);
		if (runtimeid == null) {
			return null;
		}
		port = os.networking().port().get(runtimeid);
		return port;
	}
	
	// End of user code

	//
	// Networkinterface actions.
	//
}	
