package history;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/*
 History per window split.
 */
class XWindowHistory {
  private final Project myProject;
  private final XHist<IdeDocumentHistoryImpl.PlaceInfo> myHist = new XHist<>() {
    @Override
    boolean areEqual(IdeDocumentHistoryImpl.@Nullable PlaceInfo p1, IdeDocumentHistoryImpl.@Nullable PlaceInfo p2) {
      return p1 != null && p2 != null && sameOffset(p1, p2);
    }

    @Override
    boolean shouldBeJoined(IdeDocumentHistoryImpl.@Nullable PlaceInfo p1, IdeDocumentHistoryImpl.@Nullable PlaceInfo p2) {
      return p1 != null && p2 != null && sameLine(p1, p2);
    }
  };
  private volatile boolean myNavigationInProgress;

  XWindowHistory(@NotNull Project project) {
    myProject = project;
  }

  synchronized void addPlace(@NotNull IdeDocumentHistoryImpl.PlaceInfo place) {
    if (!myNavigationInProgress) {
      myHist.pushPlace(place);
    }
  }

  private static boolean sameLine(@NotNull IdeDocumentHistoryImpl.PlaceInfo place1,
                                  @NotNull IdeDocumentHistoryImpl.PlaceInfo place2) {
    if (!place1.getFile().isValid() || !place2.getFile().isValid()) {
      return false;
    }
    if (place1.getFile().getPath().equals(place2.getFile().getPath())) {
      RangeMarker pos1 = place1.getCaretPosition();
      RangeMarker pos2 = place2.getCaretPosition();
      // abs check is an heuristic to not compute line number: if positions
      // have more than 100 characters between them, consider them to be on
      // different lines. Computing line seems to be expensive e.g. in decompiled
      // files.
      if (pos1 != null && pos2 != null && Math.abs(pos1.getStartOffset() - pos2.getStartOffset()) < 100) {
        Document doc1 = pos1.getDocument();
        Document doc2 = pos2.getDocument();
        return pos1.getStartOffset() < doc1.getTextLength() &&
                pos2.getStartOffset() < doc2.getTextLength() &&
                doc1.getLineNumber(pos1.getStartOffset()) == doc2.getLineNumber(pos2.getStartOffset());
      }
    }
    return false;
  }

  private static boolean sameOffset(@NotNull IdeDocumentHistoryImpl.PlaceInfo place1,
                                    @NotNull IdeDocumentHistoryImpl.PlaceInfo place2) {
    if (place1.getFile().getPath().equals(place2.getFile().getPath())) {
      RangeMarker pos1 = place1.getCaretPosition();
      RangeMarker pos2 = place2.getCaretPosition();
      return pos1 != null && pos2 != null && pos1.getStartOffset() == pos2.getStartOffset();
    }
    return false;
  }

  synchronized void back() {
    if (myHist.canBackward()) {
      IdeDocumentHistoryImpl.PlaceInfo currentPlace = XManager.getCurrentPlaceInfo(myProject);
      if (currentPlace == null) {
        return;
      }
      if (myHist.backFromPlace(currentPlace)) {
        IdeDocumentHistoryImpl.PlaceInfo prevPlace = myHist.getPrevPlace();
        if (prevPlace != null) {
          gotoPlace(prevPlace);
        }
      }
    }
  }

  synchronized void forward() {
    if (myHist.canForward()) {
      IdeDocumentHistoryImpl.PlaceInfo currentPlace = XManager.getCurrentPlaceInfo(myProject);
      if (currentPlace == null) {
        return;
      }
      if (myHist.forwardFromPlace(currentPlace)) {
        // we went forward, so the next place is behind us
        IdeDocumentHistoryImpl.PlaceInfo nextPlace = myHist.getPrevPlace();
        if (nextPlace != null) {
          gotoPlace(nextPlace);
        }
      }
    }
  }

  synchronized boolean gotoToPrevFile() {
    IdeDocumentHistoryImpl.PlaceInfo currentPlace = XManager.getCurrentPlaceInfo(myProject);
    if (currentPlace == null) {
      return false;
    }
    IdeDocumentHistoryImpl.PlaceInfo placeWithPrevFile = null;
    List<IdeDocumentHistoryImpl.PlaceInfo> places = myHist.getPlaces();
    int i = myHist.getNextIdx() - 1;
    for (; i >= 0; i--) {
      IdeDocumentHistoryImpl.PlaceInfo prevPlace = places.get(i);
      if (!prevPlace.getFile().equals(currentPlace.getFile())) {
        placeWithPrevFile = prevPlace;
        break;
      }
    }
    if (placeWithPrevFile != null) {
      myHist.setNextPlace(i);
      gotoPlace(placeWithPrevFile);
      return true;
    }
    return false;
  }

  private synchronized void gotoPlace(@NotNull IdeDocumentHistoryImpl.PlaceInfo place) {
    try {
      myNavigationInProgress = true;
      if (place.getFile().isValid()) {
        IdeDocumentHistoryImpl.getInstance(myProject).gotoPlaceInfo(place);
      }
    } finally {
      myNavigationInProgress = false;
    }
  }

  @NotNull
  synchronized XWindowHistory copyForWindow(@NotNull EditorWindow window) {
    XWindowHistory copy = new XWindowHistory(myProject);
    for (IdeDocumentHistoryImpl.PlaceInfo place : myHist.getPlaces()) {
      copy.myHist.pushPlace(new IdeDocumentHistoryImpl.PlaceInfo(place.getFile(), place.getNavigationState(),
              place.getEditorTypeId(), window, place.getCaretPosition()));
    }
    return copy;
  }
}
