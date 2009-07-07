/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import junit.framework.TestCase;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.internal.services.ContextCommandService;
import org.eclipse.e4.ui.internal.services.HandlerContextFunction;
import org.eclipse.e4.ui.services.ECommandService;
import org.eclipse.e4.ui.services.EHandlerService;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.tests.Activator;
import org.eclipse.e4.workbench.ui.internal.UISchedulerStrategy;

/**
 *
 */
public class HandlerTest extends TestCase {
	private static final String HELP_COMMAND_ID = "org.eclipse.ui.commands.help";
	private static final String HELP_COMMAND1_ID = HELP_COMMAND_ID + "1";

	public static class TestHandler {
		boolean ran = false;
		boolean canRun;
		String rc;

		public TestHandler(boolean c, String ret) {
			canRun = c;
			rc = ret;
		}

		public boolean canExecute() {
			return canRun;
		}

		public Object execute() {
			ran = true;
			return rc;
		}
	}

	public void testOneCommand() throws Exception {
		IEclipseContext appContext = createGlobalContext();

		defineCommands(appContext);
		final ParameterizedCommand helpCommand = getCommand(appContext,
				HELP_COMMAND_ID);
		final ParameterizedCommand help1Command = getCommand(appContext,
				HELP_COMMAND1_ID);

		TestHandler handler = new TestHandler(true, HELP_COMMAND_ID);
		EHandlerService service = (EHandlerService) appContext
				.get(EHandlerService.class.getName());
		service.activateHandler(HELP_COMMAND_ID, handler);

		Command command = (Command) appContext.get(HELP_COMMAND_ID);
		assertEquals(HELP_COMMAND_ID, command.getId());
		assertEquals(HELP_COMMAND_ID, service.executeHandler(helpCommand));
		assertTrue(handler.ran);
		assertNull(service.executeHandler(help1Command));
	}

	public void testTwoCommands() throws Exception {
		IEclipseContext appContext = createGlobalContext();

		defineCommands(appContext);
		final ParameterizedCommand helpCommand = getCommand(appContext,
				HELP_COMMAND_ID);
		final ParameterizedCommand help1Command = getCommand(appContext,
				HELP_COMMAND1_ID);

		EHandlerService service = (EHandlerService) appContext
				.get(EHandlerService.class.getName());
		TestHandler handler = new TestHandler(true, HELP_COMMAND_ID);
		service.activateHandler(HELP_COMMAND_ID, handler);
		TestHandler handler1 = new TestHandler(false, HELP_COMMAND1_ID);
		service.activateHandler(HELP_COMMAND1_ID, handler1);
		assertEquals(HELP_COMMAND_ID, service.executeHandler(helpCommand));
		assertNull(service.executeHandler(help1Command));
		assertFalse(handler1.ran);
		handler1.canRun = true;
		assertEquals(HELP_COMMAND1_ID, service.executeHandler(help1Command));
		assertTrue(handler1.ran);
	}

	public void testTwoHandlers() throws Exception {
		IEclipseContext appContext = createGlobalContext();

		defineCommands(appContext);

		ParameterizedCommand helpCommand = getCommand(appContext,
				HELP_COMMAND_ID);

		EHandlerService service = (EHandlerService) appContext
				.get(EHandlerService.class.getName());
		TestHandler handler = new TestHandler(true, HELP_COMMAND_ID);
		service.activateHandler(HELP_COMMAND_ID, handler);

		IEclipseContext window = createContext(appContext, "windowContext");
		appContext.set(IServiceConstants.ACTIVE_CHILD, window);
		EHandlerService windowService = (EHandlerService) window
				.get(EHandlerService.class.getName());
		String windowRC = HELP_COMMAND_ID + ".window";
		TestHandler windowHandler = new TestHandler(false, windowRC);
		windowService.activateHandler(HELP_COMMAND_ID, windowHandler);
		assertNull(service.executeHandler(helpCommand));
		assertFalse(windowHandler.ran);
		assertFalse(handler.ran);

		windowHandler.canRun = true;
		assertEquals(windowRC, service.executeHandler(helpCommand));
		assertTrue(windowHandler.ran);
		assertFalse(handler.ran);
	}

	/**
	 * @param appContext
	 * @param commandId
	 * @return
	 */
	private ParameterizedCommand getCommand(IEclipseContext appContext,
			String commandId) {
		ECommandService cs = (ECommandService) appContext
				.get(ECommandService.class.getName());
		final Command cmd = cs.getCommand(commandId);
		return new ParameterizedCommand(cmd, null);
	}

	public void testCanExecute() throws Exception {
		IEclipseContext appContext = createGlobalContext();

		defineCommands(appContext);
		final ParameterizedCommand helpCommand = getCommand(appContext,
				HELP_COMMAND_ID);

		EHandlerService service = (EHandlerService) appContext
				.get(EHandlerService.class.getName());
		TestHandler handler = new TestHandler(true, HELP_COMMAND_ID);
		service.activateHandler(HELP_COMMAND_ID, handler);

		IEclipseContext window = createContext(appContext, "windowContext");
		appContext.set(IServiceConstants.ACTIVE_CHILD, window);
		EHandlerService windowService = (EHandlerService) window
				.get(EHandlerService.class.getName());
		String windowRC = HELP_COMMAND_ID + ".window";
		TestHandler windowHandler = new TestHandler(false, windowRC);
		windowService.activateHandler(HELP_COMMAND_ID, windowHandler);

		assertFalse(windowService.canExecute(helpCommand));
		windowHandler.canRun = true;
		assertTrue(windowService.canExecute(helpCommand));
		windowHandler.canRun = false;
		assertFalse(windowService.canExecute(helpCommand));
		windowService.deactivateHandler(HELP_COMMAND_ID, windowHandler);
		assertTrue(windowService.canExecute(helpCommand));
	}

	public void testThreeContexts() throws Exception {
		IEclipseContext appContext = createGlobalContext();

		defineCommands(appContext);
		final ParameterizedCommand helpCommand = getCommand(appContext,
				HELP_COMMAND_ID);

		EHandlerService service = (EHandlerService) appContext
				.get(EHandlerService.class.getName());
		TestHandler handler = new TestHandler(true, HELP_COMMAND_ID);
		service.activateHandler(HELP_COMMAND_ID, handler);

		IEclipseContext window = createContext(appContext, "windowContext");
		appContext.set(IServiceConstants.ACTIVE_CHILD, window);
		EHandlerService windowService = (EHandlerService) window
				.get(EHandlerService.class.getName());
		String windowRC = HELP_COMMAND_ID + ".window";
		TestHandler windowHandler = new TestHandler(true, windowRC);
		windowService.activateHandler(HELP_COMMAND_ID, windowHandler);
		assertEquals(windowRC, service.executeHandler(helpCommand));

		IEclipseContext dialog = createContext(appContext, "dialogContext");
		appContext.set(IServiceConstants.ACTIVE_CHILD, dialog);
		assertEquals(HELP_COMMAND_ID, service.executeHandler(helpCommand));

		appContext.set(IServiceConstants.ACTIVE_CHILD, window);
		assertEquals(windowRC, service.executeHandler(helpCommand));
	}

	public void testDifferentExecutionContexts() throws Exception {
		IEclipseContext appContext = createGlobalContext();

		defineCommands(appContext);
		final ParameterizedCommand helpCommand = getCommand(appContext,
				HELP_COMMAND_ID);

		EHandlerService service = (EHandlerService) appContext
				.get(EHandlerService.class.getName());
		TestHandler handler = new TestHandler(true, HELP_COMMAND_ID);
		service.activateHandler(HELP_COMMAND_ID, handler);

		IEclipseContext window = createContext(appContext, "windowContext");
		appContext.set(IServiceConstants.ACTIVE_CHILD, window);
		EHandlerService windowService = (EHandlerService) window
				.get(EHandlerService.class.getName());
		String windowRC = HELP_COMMAND_ID + ".window";
		TestHandler windowHandler = new TestHandler(true, windowRC);
		windowService.activateHandler(HELP_COMMAND_ID, windowHandler);
		assertEquals(windowRC, service.executeHandler(helpCommand));
		assertEquals(windowRC, windowService.executeHandler(helpCommand));

		IEclipseContext dialog = createContext(appContext, "dialogContext");
		EHandlerService dialogService = (EHandlerService) dialog
				.get(EHandlerService.class.getName());
		assertEquals(HELP_COMMAND_ID, dialogService.executeHandler(helpCommand));
	}

	private void defineCommands(IEclipseContext appContext) {
		ECommandService cmdService = (ECommandService) appContext
				.get(ECommandService.class.getName());
		Category category = cmdService.getCategory("cat." + HELP_COMMAND_ID);
		category.define("Help Category", null);
		Command command = cmdService.getCommand(HELP_COMMAND_ID);
		command.define("Help Command", null, category);
		command = cmdService.getCommand(HELP_COMMAND1_ID);
		command.define("Help 1 Command", null, category);
	}

	private IEclipseContext createGlobalContext() {
		IEclipseContext serviceContext = EclipseContextFactory
				.createServiceContext(Activator.getDefault().getBundle()
						.getBundleContext());
		// global initialization and setup, usually done by workbench
		IEclipseContext appContext = createContext(serviceContext,
				"globalContext");
		appContext.set(IContributionFactory.class.getName(), MWindowTest
				.getCFactory());
		appContext.set(CommandManager.class.getName(), new CommandManager());

		// supply the services
		appContext.set(ECommandService.class.getName(),
				new ContextCommandService(appContext));
		appContext.set(EHandlerService.class.getName(),
				new HandlerContextFunction());
		return appContext;
	}

	private IEclipseContext createContext(IEclipseContext parentContext,
			String level) {
		IEclipseContext appContext = EclipseContextFactory.create(
				parentContext, UISchedulerStrategy.getInstance());
		appContext.set(IContextConstants.DEBUG_STRING, level);
		return appContext;
	}
}
