package com.github.tux2323.eclipse.command.viewer;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.part.ViewPart;

public class CommandView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.github.tux2323.eclipse.command.viewer.CommandView";

	private TableViewer viewer;

	private ViewFilter viewFilter;

	class ViewContentProvider implements IStructuredContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object inputElement) {
			return (Command[]) inputElement;
		}

	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public String getColumnText(Object obj, int columnIndex) {
			Command command = (Command) obj;
			switch (columnIndex) {
			case 0:
				return extractName(command);
			case 1:
				return extractDescription(command);
			case 2:
				return command.getId();
			case 3:
				return getKeyBinding(command);
			default:
				throw new RuntimeException("Should not happen");
			}
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
		}

	}

	class ViewFilter extends ViewerFilter {

		private String searchString;

		public void setSearchText(String s) {
			// Search must be a substring of the existing value
			this.searchString = ".*" + s + ".*";
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (searchString == null || searchString.length() == 0) {
				return true;
			}
			Command command = (Command) element;

			try {
				String name = extractName(command);
				if (name.matches(searchString)) {
					return true;
				}

				String description = extractDescription(command);
				if (description.matches(searchString)) {
					return true;
				}

				String id = command.getId();
				if (id.matches(searchString)) {
					return true;
				}
			} catch (Exception e) {}

			return false;
		}

	}

	private String extractName(Command command) {
		try {
			return command.getName();
		} catch (NotDefinedException e) {
			return "Not defined";
		}
	}

	private String extractDescription(Command command) {
		try {
			String description = command.getDescription();
			return description;
		} catch (NotDefinedException e) {
			return "Not defined";
		}
	}

	private String getKeyBinding(Command command) {
		IBindingService service = (IBindingService) getSite().getService(
				IBindingService.class);
		return service.getBestActiveBindingFormattedFor(command.getId());
	}

	private Command[] getDefinedCommands() {
		ICommandService service = getCommandService();
		Command[] definedCommands = service.getDefinedCommands();
		return definedCommands;
	}

	private ICommandService getCommandService() {
		return (ICommandService) getSite().getService(ICommandService.class);
	}

	public void createPartControl(Composite parent) {

		GridLayout layout = new GridLayout(2, false);
		parent.setLayout(layout);
		Label searchLabel = new Label(parent, SWT.NONE);
		searchLabel.setText("Search: ");
		final Text searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		searchText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				viewFilter.setSearchText(searchText.getText());
				viewer.refresh();
			}
		});

		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		String[] titles = { "Name", "Description", "Command ID", "Binding" };
		int[] bounds = { 220, 220, 220, 50 };
		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(titles[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
		}

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewFilter = new ViewFilter();
		viewer.addFilter(viewFilter);
		viewer.setInput(getDefinedCommands());

		getSite().setSelectionProvider(viewer);

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
		
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = bars.getToolBarManager();
		Action copy = new Action() {
			public void run() {
				CopyCommandClipboard clipboard = new CopyCommandClipboard();
				try {
					clipboard.execute(null);
				} catch (ExecutionException e) {
				}
			}
		};
		
		copy.setText("Copy");
		copy.setToolTipText("Copy to Clipboard as XML.");
		copy.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		
		toolBarManager.add(copy);
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
}