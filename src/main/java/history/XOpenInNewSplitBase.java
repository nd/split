package history;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorComposite;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.util.ObjectUtils;
import com.intellij.util.OpenSourceUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class XOpenInNewSplitBase extends AnAction implements DumbAware {

  private final boolean myNavigateToNewSplit;

  XOpenInNewSplitBase(boolean navigateToNewSplit) {
    myNavigateToNewSplit = navigateToNewSplit;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    DataContext data = e.getDataContext();
    Project project = e.getProject();
    Editor editor = project != null ? FileEditorManagerEx.getInstanceEx(project).getSelectedTextEditor() : null;
    Navigatable[] navs = CommonDataKeys.NAVIGATABLE_ARRAY.getData(data);
    if (editor == null || navs == null || navs.length == 0) {
      return;
    }
    FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(project);
    CommandProcessor.getInstance().executeCommand(project, () -> {
      manager.unsplitAllWindow();
      EditorWindow srcWindow = manager.getCurrentWindow();
      srcWindow.closeAllExcept(srcWindow.getSelectedFile());
      srcWindow.split(SwingConstants.VERTICAL, true, null, true);
      Editor targetEditor = manager.getSelectedTextEditor();
      // prevent idea from opening file in other editor:
      JComponent srcEditorComponent = editor.getComponent();
      DataProvider originalDataProvider = DataManager.getDataProvider(srcEditorComponent);
      try {
        if (originalDataProvider != null) {
          DataManager.removeDataProvider(srcEditorComponent);
        }
        DataManager.registerDataProvider(srcEditorComponent, dataId -> {
          if (OpenFileDescriptor.NAVIGATE_IN_EDITOR.is(dataId)) {
            return targetEditor;
          }
          return originalDataProvider != null ? originalDataProvider.getData(dataId) : null;
        });
        OpenSourceUtil.navigate(true, false, navs);
        EditorWindow dstWindow = manager.getCurrentWindow();// dst window because we asked to focus on new window during split
        XManager xmanager = XManager.getInstance(project);
        xmanager.copyHistory(srcWindow, dstWindow);
        IdeDocumentHistoryImpl.PlaceInfo srcPlace = XManager.getPlaceInfo(project, srcWindow);
        IdeDocumentHistoryImpl.PlaceInfo dstPlace = XManager.getPlaceInfo(project, dstWindow);
        XWindowHistory dstHistory = xmanager.getHistory(dstWindow);
        if (srcPlace != null && dstHistory != null) {
          dstHistory.addPlace(XManager.replaceWindow(srcPlace, dstWindow));
        }
        xmanager.addCurrentPlace(dstWindow);

        if (myNavigateToNewSplit) {
          TextEditor textEditor = ObjectUtils.tryCast(manager.getSelectedEditor(), TextEditor.class);
          if (textEditor != null) {
            Editor editorAfterNavigate = textEditor.getEditor();
            EditorWindow currentWindow = manager.getCurrentWindow();
            FileEditorManager.getInstance(project).runWhenLoaded(editorAfterNavigate, () -> {
              EditorComposite selectedComposite = currentWindow.getSelectedComposite();
              JComponent component = selectedComposite != null ? selectedComposite.getPreferredFocusedComponent() : null;
              if (component != null) {
                component.requestFocusInWindow();
              }
            });
          }
        } else {
          // return back to src window
          manager.setCurrentWindow(manager.getNextWindow(dstWindow));
        }
      } finally {
        DataManager.removeDataProvider(srcEditorComponent);
        if (originalDataProvider != null) {
          DataManager.registerDataProvider(srcEditorComponent, originalDataProvider);
        }
      }
    }, "XOpenInNewSplit", null);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }
}
