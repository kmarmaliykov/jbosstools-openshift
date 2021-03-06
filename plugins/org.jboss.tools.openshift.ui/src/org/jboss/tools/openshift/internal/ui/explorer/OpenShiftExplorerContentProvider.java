/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.openshift.internal.ui.explorer;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.openshift.common.core.IRefreshable;
import org.jboss.tools.openshift.common.core.connection.ConnectionsRegistry;
import org.jboss.tools.openshift.common.core.connection.IConnection;
import org.jboss.tools.openshift.core.connection.Connection;
import org.jboss.tools.openshift.internal.common.ui.explorer.BaseExplorerContentProvider;

import com.openshift.restclient.OpenShiftException;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IProject;

/**
 * Contributes OpenShift v3 specific content to the OpenShift explorer view
 * 
 * @author jeff.cantrill
 */
public class OpenShiftExplorerContentProvider extends BaseExplorerContentProvider{
	
	private static final ResourceKind [] groupings = new ResourceKind [] {
		ResourceKind.BuildConfig, 
		ResourceKind.DeploymentConfig, 
		ResourceKind.Service, 
		ResourceKind.Pod,
		ResourceKind.ReplicationController, 
		ResourceKind.Build, 
		ResourceKind.ImageStream, 
		ResourceKind.Route
	};

	/**
	 * Called to obtain the root elements of the tree viewer,
	 * which should be Connections
	 */
	@Override
	public Object[] getExplorerElements(final Object parentElement) {
		if(parentElement instanceof ConnectionsRegistry){
			ConnectionsRegistry registry = (ConnectionsRegistry) parentElement;
			return registry.getAll(Connection.class).toArray();
		} else if (parentElement instanceof Connection) {
			return ((Connection) parentElement).get(ResourceKind.Project).toArray();
		} else {
			return new Object[0];
		}
	}
	
	/**
	 * Called to obtain the children of any element in the tree viewer
	 */
	@Override
	public Object[] getChildrenFor(Object parentElement) {
		try{
			if (parentElement instanceof Connection) {
				Connection connection = (Connection) parentElement;
				return connection.get(ResourceKind.Project).toArray();
			} else if (parentElement instanceof IProject) {
				IProject project = (IProject) parentElement;
				List<ResourceGrouping> groups = new ArrayList<ResourceGrouping>(groupings.length);
				for (ResourceKind kind : groupings) {
					final ResourceGrouping grouping = new ResourceGrouping(kind, project);
					grouping.setRefreshable(new IRefreshable() {
						@Override
						public void refresh() {
							refreshViewerObject(grouping);
						}
					});
					groups.add(grouping);
				}
				return groups.toArray();
			} else if (parentElement instanceof ResourceGrouping) {
				ResourceGrouping group = (ResourceGrouping) parentElement;
				return group.getProject().getResources(group.getKind()).toArray();
			}
		} catch (OpenShiftException e) {
			addException(parentElement, e);
		}
		return new Object[0];
	}

	@Override
	public boolean hasChildren(Object element) {
		return element instanceof ConnectionsRegistry
				|| element instanceof IConnection
				|| element instanceof IProject
				|| element instanceof ResourceGrouping;
	}
}
