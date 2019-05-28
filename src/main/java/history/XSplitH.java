package history;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.NotNull;

public class XSplitH extends XSplit {
  public XSplitH() {
    super(ActionManager.getInstance().getAction("SplitHorizontally"));
  }
}
