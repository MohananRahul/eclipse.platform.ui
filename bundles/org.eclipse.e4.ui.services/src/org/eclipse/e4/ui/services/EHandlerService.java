/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.services;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.services.context.IEclipseContext;

/**
 */
public interface EHandlerService {
	public IEclipseContext getContext();
	
	public void activateHandler(String commandId, Object handler);

	public void deactivateHandler(String commandId, Object handler);
	
	public Object executeHandler(ParameterizedCommand command);
	
	public boolean canExecute(ParameterizedCommand command);
}
