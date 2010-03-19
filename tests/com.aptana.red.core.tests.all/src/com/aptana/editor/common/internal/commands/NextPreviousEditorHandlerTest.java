package com.aptana.editor.common.internal.commands;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class NextPreviousEditorHandlerTest extends TestCase
{

	private static final String HTML_EDITOR_ID = "com.aptana.editor.html";
	private static final String NEXT_EDITOR_COMMAND_ID = "com.aptana.editor.NextEditorCommand";
	private static final String PREVIOUS_EDITOR_COMMAND_ID = "com.aptana.editor.PreviousEditorCommand";
	private static final String PROJECT_NAME = "editor_navigate";

	private IProject project;
	private List<IFile> files;
	private List<ITextEditor> editors;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		Class.forName("com.aptana.editor.html.Activator");
		project = createProject();
		files = new ArrayList<IFile>();
		editors = new ArrayList<ITextEditor>();
	}

	@Override
	protected void tearDown() throws Exception
	{
		try
		{
			for (ITextEditor editor : editors)
			{
				// Need to force the editor shut!
				editor.close(false);
			}
			for (IFile file : files)
			{
				// Delete the generated file
				file.delete(true, new NullProgressMonitor());
			}
			// Delete the generated project
			project.delete(true, new NullProgressMonitor());
		}
		finally
		{
			editors = null;
			files = null;
			project = null;
			super.tearDown();
		}
	}

	protected IFile createFile(IProject project, String fileName, String contents) throws CoreException
	{
		IFile file = project.getFile(fileName);
		ByteArrayInputStream source = new ByteArrayInputStream(contents.getBytes());
		file.create(source, true, new NullProgressMonitor());
		return file;
	}

	protected IProject createProject() throws CoreException
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(PROJECT_NAME);
		project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());
		return project;
	}

	protected IFile createAndOpenFile(String fileName, String contents) throws CoreException, PartInitException
	{
		IFile file = createFile(project, fileName, contents);
		files.add(file);
		openEditor(file);
		return file;
	}

	private ITextEditor openEditor(IFile file) throws PartInitException
	{
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ITextEditor editor = (ITextEditor) IDE.openEditor(page, file, HTML_EDITOR_ID);
		editors.add(editor);
		return editor;
	}

	public void testExecute() throws Exception
	{
		// Open multiple files!!!
		createAndOpenFile("example1.html",
				"<html>\n<head>\n<title>Title goes here</title>\n</head>\n<body>\n<div>\n<p>Hi</p>\n</div>\n</body>");
		createAndOpenFile("example2.html",
				"<html>\n<head>\n<title>Title goes here</title>\n</head>\n<body>\n<div>\n</div>\n</body>");
		createAndOpenFile("example3.html",
				"<html>\n<head>\n<title>Title goes here</title>\n</head>\n<body>\n<h1>HEADING</h1>\n</body>");
		

		assertEquals("example3.html", getActiveEditor().getTitle());
		
		executeCommand(NEXT_EDITOR_COMMAND_ID);
		assertEquals("example1.html", getActiveEditor().getTitle());

		executeCommand(PREVIOUS_EDITOR_COMMAND_ID);
		assertEquals("example3.html", getActiveEditor().getTitle());

		executeCommand(PREVIOUS_EDITOR_COMMAND_ID);
		assertEquals("example2.html", getActiveEditor().getTitle());
		
		executeCommand(PREVIOUS_EDITOR_COMMAND_ID);
		assertEquals("example1.html", getActiveEditor().getTitle());
		
		executeCommand(PREVIOUS_EDITOR_COMMAND_ID);
		assertEquals("example3.html", getActiveEditor().getTitle());
		
		executeCommand(NEXT_EDITOR_COMMAND_ID);
		assertEquals("example1.html", getActiveEditor().getTitle());
		
		executeCommand(NEXT_EDITOR_COMMAND_ID);
		assertEquals("example2.html", getActiveEditor().getTitle());
		
		executeCommand(NEXT_EDITOR_COMMAND_ID);
		assertEquals("example3.html", getActiveEditor().getTitle());
	}

	protected void executeCommand(String commandId) throws ExecutionException, NotDefinedException, NotEnabledException,
			NotHandledException
	{
		// Grab the handler service to execute our command
		IHandlerService service = (IHandlerService) getActiveEditor().getSite().getService(IHandlerService.class);
		service.executeCommand(commandId, null);
	}

	private IWorkbenchPart getActiveEditor()
	{
		// Grab ref to active editor
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		return page.getActiveEditor();
	}

}
