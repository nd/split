package history;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class XForward extends AnAction implements DumbAware {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    EditorWindow currentWindow = FileEditorManagerEx.getInstanceEx(e.getProject()).getCurrentWindow();
    XWindowHistory history = XManager.getInstance(e.getProject()).getHistory(currentWindow);
    if (history != null && history.canForward()) {
      history.forward();
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    EditorWindow currentWindow = FileEditorManagerEx.getInstanceEx(e.getProject()).getCurrentWindow();
    XWindowHistory history = XManager.getInstance(e.getProject()).getHistory(currentWindow);
    if (history != null) {
      e.getPresentation().setEnabled(history.canForward());
    }
  }
}
