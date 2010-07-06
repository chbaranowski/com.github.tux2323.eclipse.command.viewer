package com.github.tux2323.eclipse.command.viewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class CopyCommandClipboard extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IViewPart view = page.findView(CommandView.ID);
		Clipboard cb = new Clipboard(Display.getDefault());
		ISelection selection = view.getSite().getSelectionProvider()
				.getSelection();
		List<Command> commandList = new ArrayList<Command>();
		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			for (Iterator<Command> iterator = sel.iterator(); iterator
					.hasNext();) {
				Command command = iterator.next();
				commandList.add(command);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (Command command : commandList) {
			sb.append(commandToString(command));
		}
		TextTransfer textTransfer = TextTransfer.getInstance();
		cb.setContents(new Object[] { sb.toString() },
				new Transfer[] { textTransfer });
		return null;
	}

	private String commandToString(Command command) {
		String key = "<key"
				+ System.getProperty("line.separator")
				+ "\t\tcommandId=\""
				+ command.getId()
				+ "\""
				+ System.getProperty("line.separator")
				+ "\t\tschemeId=\"com.github.tux2323.eclipse.keyschemeAcceleratorConfigurationleratorConfiguration\""
				+ System.getProperty("line.separator") + "\t\tsequence=\"?\">"
				+ System.getProperty("line.separator") + "\t</key>"
				+ System.getProperty("line.separator")
				+ System.getProperty("line.separator");
		return key;
	}
}
