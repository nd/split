package history;

import com.intellij.openapi.actionSystem.ActionManager;

public class XSplitH extends XSplit {
  public XSplitH() {
    super(ActionManager.getInstance().getAction("SplitHorizontally"));
  }
}
