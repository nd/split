package history;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

public class XSplit extends AnAction implements DumbAware {
  private final AnAction myDelegate;

  public XSplit(@NotNull AnAction delegate) {
    myDelegate = delegate;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    FileEditorManagerEx editorManager = FileEditorManagerEx.getInstanceEx(project);
    EditorWindow currentWindow = editorManager.getCurrentWindow();
    if (currentWindow == null) {
      return;
    }
    EditorWindow[] windowsBefore = editorManager.getWindows();
    myDelegate.actionPerformed(e);
    EditorWindow[] windowsAfter = editorManager.getWindows();
    EditorWindow created = null;
    for (EditorWindow window : windowsAfter) {
      if (ArrayUtil.find(windowsBefore, window) < 0) {
        created = window;
        break;
      }
    }
    if (created != null) {
      XManager.getInstance(project).copyHistory(currentWindow, created);
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    myDelegate.update(e);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return myDelegate.getActionUpdateThread();
  }
}
