package history;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.ex.FileEditorWithProvider;
import com.intellij.openapi.fileEditor.impl.EditorComposite;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(Service.Level.PROJECT)
public final class XManager implements Disposable {
  private final Project myProject;
  private final Map<EditorWindow, XWindowHistory> myHistories = new ConcurrentHashMap<>();

  public XManager(@NotNull Project project) {
    myProject = project;
    MessageBusConnection bus = project.getMessageBus().connect(this);
    bus.subscribe(IdeDocumentHistoryImpl.RecentPlacesListener.TOPIC, new IdeDocumentHistoryImpl.RecentPlacesListener() {
      @Override
      public void recentPlaceAdded(@NotNull IdeDocumentHistoryImpl.PlaceInfo commandStartPlace, boolean isChanged) {
        EditorWindow window = commandStartPlace.getWindow();
        if (!isChanged && window != null) {
          cleanObsoleteHistories();
          getHistory(window).addPlace(commandStartPlace);
        }
        IdeDocumentHistoryImpl.PlaceInfo commandEndPlace = getCurrentPlaceInfo(project);
        if (commandEndPlace != null) {
          getHistory(commandEndPlace.getWindow()).addPlace(commandEndPlace);
        }
      }

      @Override
      public void recentPlaceRemoved(@NotNull IdeDocumentHistoryImpl.PlaceInfo changePlace, boolean isChanged) {
      }
    });
  }

  @Nullable
  @Contract("null->null;!null->!null")
  public XWindowHistory getHistory(@Nullable EditorWindow window) {
    return window != null ? myHistories.computeIfAbsent(window, it -> new XWindowHistory(myProject)) : null;
  }

  void copyHistory(@Nullable EditorWindow src, @Nullable EditorWindow dst) {
    if (src != null && dst != null) {
      XWindowHistory history = getHistory(src);
      myHistories.put(dst, history.copyForWindow(dst));
    }
  }

  void addCurrentPlace(@Nullable EditorWindow window) {
    XWindowHistory history = window != null ? getHistory(window) : null;
    IdeDocumentHistoryImpl.PlaceInfo place = history != null ? getPlaceInfo(window.getManager().getProject(), window) : null;
    if (place != null) {
      history.addPlace(place);
    }
  }

  private void cleanObsoleteHistories() {
    EditorWindow[] live = FileEditorManagerEx.getInstanceEx(myProject).getWindows();
    List<EditorWindow> obsolete = new ArrayList<>(1);
    for (EditorWindow window : myHistories.keySet()) {
      if (!ArrayUtil.contains(window, live)) {
        obsolete.add(window);
      }
    }
    for (EditorWindow window : obsolete) {
      myHistories.remove(window);
    }
  }

  public static XManager getInstance(Project project) {
    return project.getService(XManager.class);
  }

  @Override
  public void dispose() {
    myHistories.clear();
  }

  @NotNull
  static IdeDocumentHistoryImpl.PlaceInfo replaceWindow(@NotNull IdeDocumentHistoryImpl.PlaceInfo info, @NotNull EditorWindow window) {
    return new IdeDocumentHistoryImpl.PlaceInfo(
            info.getFile(), info.getNavigationState(), info.getEditorTypeId(), window, info.getCaretPosition());
  }

  // code for getting current placeInfo from IdeDocumentHistoryImpl:
  @Nullable
  static IdeDocumentHistoryImpl.PlaceInfo getPlaceInfo(@NotNull Project project, @NotNull EditorWindow window) {
    EditorComposite selectedEditor = window.getSelectedComposite();
    FileEditorWithProvider editor = selectedEditor != null ? selectedEditor.getSelectedWithProvider() : null;
    return editor != null ? createPlaceInfo(project, editor.getFileEditor(), editor.getProvider()) : null;
  }

  @Nullable
  static IdeDocumentHistoryImpl.PlaceInfo getCurrentPlaceInfo(@NotNull Project project) {
    FileEditorWithProvider editor = getSelectedEditor(project);
    return editor != null ? createPlaceInfo(project, editor.getFileEditor(), editor.getProvider()) : null;
  }

  @Nullable
  private static FileEditorWithProvider getSelectedEditor(@NotNull Project project) {
    FileEditorManagerEx editorManager = FileEditorManagerEx.getInstanceEx(project);
    VirtualFile file = editorManager.getCurrentFile();
    return file == null ? null : editorManager.getSelectedEditorWithProvider(file);
  }

  @Nullable
  private static IdeDocumentHistoryImpl.PlaceInfo createPlaceInfo(@NotNull Project project, @NotNull FileEditor fileEditor, FileEditorProvider fileProvider) {
    if (!fileEditor.isValid()) {
      return null;
    }
    FileEditorManagerEx editorManager = FileEditorManagerEx.getInstanceEx(project);
    VirtualFile file = fileEditor.getFile();
    if (file == null) {
      return null;
    }
    FileEditorState state = fileEditor.getState(FileEditorStateLevel.NAVIGATION);
    return new IdeDocumentHistoryImpl.PlaceInfo(file, state, fileProvider.getEditorTypeId(), editorManager.getCurrentWindow(), getCaretPosition(fileEditor));
  }

  @Nullable
  private static RangeMarker getCaretPosition(@NotNull FileEditor fileEditor) {
    if (!(fileEditor instanceof TextEditor)) {
      return null;
    }
    Editor editor = ((TextEditor) fileEditor).getEditor();
    int offset = editor.getCaretModel().getOffset();
    return editor.getDocument().createRangeMarker(offset, offset);
  }

  @Nullable
  static XWindowHistory getCurrentWindowHistory(@Nullable Project project) {
    if (project == null) {
      return null;
    }
    EditorWindow currentWindow = FileEditorManagerEx.getInstanceEx(project).getCurrentWindow();
    return XManager.getInstance(project).getHistory(currentWindow);
  }
}
