package history;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.util.OpenSourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class XOpenInNewSplit extends AnAction implements DumbAware {

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
      DataManager.registerDataProvider(editor.getComponent(), new DataProvider() {
        @Nullable
        @Override
        public Object getData(@NotNull String dataId) {
          if (OpenFileDescriptor.NAVIGATE_IN_EDITOR.is(dataId)) {
            return targetEditor;
          }
          return null;
        }
      });
      OpenSourceUtil.navigate(true, false, navs);
      Editor resultEditor = manager.getSelectedTextEditor();
      EditorWindow dstWindow = manager.getCurrentWindow();
      XManager xmanager = XManager.getInstance(project);
      xmanager.copyHistory(srcWindow, dstWindow);
      IdeDocumentHistoryImpl.PlaceInfo srcPlace = XManager.getPlaceInfo(project, srcWindow);
      XWindowHistory dstHistory = xmanager.getHistory(dstWindow);
      if (srcPlace != null && dstHistory != null) {
        dstHistory.addPlace(XManager.replaceWindow(srcPlace, dstWindow));
      }
      xmanager.addCurrentPlace(dstWindow);
    }, "XOpenInNewSplit", null);
  }
}
