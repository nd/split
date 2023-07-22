package history;

import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Hist<T> {
  private final ArrayList<T> myPlaces = new ArrayList<>();
  // we are between myPlaces[myNextIdx-1] and myPlaces[myNextIdx]
  private int myNextIdx = 0; // always >=0

  boolean canForward() {
    return myNextIdx < myPlaces.size();
  }

  boolean canBackward() {
    return myNextIdx > 0;
  }

  void pushPlace(@NotNull T t) {
    while (myNextIdx < myPlaces.size()) {
      myPlaces.remove(myPlaces.size() - 1);
    }
    myPlaces.add(t);
    myNextIdx++;
  }

  void insertPlace(@NotNull T t) {
    if (areEqual(getPrevPlace(), t)) {
      return;
    }
    if (areEqual(getNextPlace(), t)) {
      return;
    }
    myPlaces.add(myNextIdx, t);
    myNextIdx++;
  }

  boolean back() {
    return backFromPlace(getPrevPlace());
  }

  boolean backFromPlace(@Nullable T fromPlace) {
    if (myNextIdx > 0) {
      T lastBackPlace = myPlaces.get(myNextIdx - 1);
      if (areEqual(lastBackPlace, fromPlace)) {
        myNextIdx--;
      } else {
        myPlaces.add(myNextIdx, lastBackPlace);
        myPlaces.add(myNextIdx, fromPlace);
      }
      return true;
    }
    return false;
  }

  boolean forward() {
    if (myNextIdx < myPlaces.size()) {
      myNextIdx++;
      return true;
    }
    return false;
  }

  boolean areEqual(@Nullable T p1, @Nullable T p2) {
    return Objects.equals(p1, p2);
  }

  @Nullable T getNextPlace() {
    return myNextIdx < myPlaces.size() ? myPlaces.get(myNextIdx) : null;
  }

  @Nullable T getPrevPlace() {
    return myNextIdx > 0 ? myPlaces.get(myNextIdx - 1) : null;
  }

  @NotNull Pair<T, T> getPosition() {
    return Pair.create(getPrevPlace(), getNextPlace());
  }

  @NotNull List<T> getPlaces() {
    return myPlaces;
  }
}