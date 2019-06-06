package history;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class XBack extends AnAction implements DumbAware {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    XWindowHistory history = XManager.getCurrentWindowHistory(e.getProject());
    if (history != null && history.canBack()) {
      history.back();
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    XWindowHistory history = XManager.getCurrentWindowHistory(e.getProject());
    if (history != null) {
      e.getPresentation().setEnabled(history.canBack());
    }
  }
}
