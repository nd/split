package history;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XManager implements Disposable {
  private final Project myProject;
  private final Map<EditorWindow, XWindowHistory> myHistories = new ConcurrentHashMap<>();

  public XManager(@NotNull Project project) {
    myProject = project;
    MessageBusConnection bus = project.getMessageBus().connect(this);
    bus.subscribe(IdeDocumentHistoryImpl.RecentPlacesListener.TOPIC, new IdeDocumentHistoryImpl.RecentPlacesListener() {
      @Override
      public void recentPlaceAdded(@NotNull IdeDocumentHistoryImpl.PlaceInfo place, boolean isChanged) {
        if (!isChanged) {
          cleanObsoleteHistories();
          myHistories.computeIfAbsent(place.getWindow(), it -> new XWindowHistory(myProject)).addPlace(place);
        }
      }

      @Override
      public void recentPlaceRemoved(@NotNull IdeDocumentHistoryImpl.PlaceInfo changePlace, boolean isChanged) {
      }
    });
  }

  void copyHistory(@Nullable EditorWindow src, @Nullable EditorWindow dst) {
    if (src != null && dst != null) {
      XWindowHistory history = getHistory(src);
      if (history != null) {
        myHistories.put(dst, history.copyForWindow(dst));
      }
    }
  }

  private void cleanObsoleteHistories() {
    EditorWindow[] live = FileEditorManagerEx.getInstanceEx(myProject).getWindows();
    List<EditorWindow> obsolete = ContainerUtil.newSmartList();
    for (EditorWindow window : myHistories.keySet()) {
      if (!ArrayUtil.contains(window, live)) {
        obsolete.add(window);
      }
    }
    for (EditorWindow window : obsolete) {
      myHistories.remove(window);
    }
  }

  @Nullable
  public XWindowHistory getHistory(@Nullable EditorWindow window) {
    return window != null ? myHistories.get(window) : null;
  }

  public static XManager getInstance(Project project) {
    return project.getComponent(XManager.class);
  }

  @Override
  public void dispose() {
    myHistories.clear();
  }
}
