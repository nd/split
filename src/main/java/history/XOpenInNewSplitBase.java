package history;

import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorComposite;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl;
import com.intellij.openapi.options.advanced.AdvancedSettings;
import com.intellij.openapi.options.advanced.AdvancedSettingsImpl;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
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
    JComponent srcEditorComponent = editor.getComponent();
    DataProvider originalDataProvider = DataManager.getDataProvider(srcEditorComponent);
    manager.unsplitAllWindow();
    EditorWindow srcWindow = manager.getCurrentWindow();
    XManager xmanager = XManager.getInstance(project);
    if (srcWindow == null) {
      return;
    }
    srcWindow.split(SwingConstants.VERTICAL, true, null, true);
    ApplicationManager.getApplication().invokeLater(() -> {
      Editor targetEditor = manager.getSelectedTextEditor();
      boolean originalOpenInactiveSplitter = AdvancedSettings.getBoolean(FileEditorManagerImpl.EDITOR_OPEN_INACTIVE_SPLITTER);
      AdvancedSettings.setBoolean(FileEditorManagerImpl.EDITOR_OPEN_INACTIVE_SPLITTER, false);
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
        xmanager.copyHistory(srcWindow, dstWindow);
        IdeDocumentHistoryImpl.PlaceInfo srcPlace = XManager.getPlaceInfo(project, srcWindow);
        IdeDocumentHistoryImpl.PlaceInfo dstPlace = XManager.getPlaceInfo(project, dstWindow);
        XWindowHistory dstHistory = xmanager.getHistory(dstWindow);
        if (srcPlace != null && dstHistory != null) {
          dstHistory.addPlace(XManager.replaceWindow(srcPlace, dstWindow));
        }
        xmanager.addCurrentPlace(dstWindow);
        if (!myNavigateToNewSplit) {
          // return back to src window
          manager.setCurrentWindow(manager.getNextWindow(dstWindow));
        }
      } finally {
        AdvancedSettings.setBoolean(FileEditorManagerImpl.EDITOR_OPEN_INACTIVE_SPLITTER, originalOpenInactiveSplitter);
        DataManager.removeDataProvider(srcEditorComponent);
        if (originalDataProvider != null) {
          DataManager.registerDataProvider(srcEditorComponent, originalDataProvider);
        }
      }
    }, ModalityState.NON_MODAL);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }
}
