package history;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class XForward extends AnAction implements DumbAware {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    XWindowHistory history = XManager.getCurrentWindowHistory(e.getProject());
    if (history != null) {
      history.forward();
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    XWindowHistory history = XManager.getCurrentWindowHistory(e.getProject());
    e.getPresentation().setEnabled(history != null);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.EDT;
  }
}
