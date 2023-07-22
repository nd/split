package history;

import com.intellij.openapi.util.Pair;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class HistTest {
  @Test
  public void radials() {
    Hist<String> h = new Hist<>();
    h.pushPlace("1");
    assertPlace(h, "1", null);

    // move to 2
    h.backFromPlace("2");
    assertPlace(h, "1", "2");
    assertEquals(List.of("1", "2", "1"), h.getPlaces());

    // move to 3
    h.backFromPlace("3");
    assertPlace(h, "1", "3");
    assertEquals(List.of("1", "3", "1", "2", "1"), h.getPlaces());

    // move to 4
    h.backFromPlace("4");
    assertPlace(h, "1", "4");

    assertEquals(List.of("1", "4", "1", "3", "1", "2", "1"), h.getPlaces());
  }

  @Test
  public void backFromPlace() {
    Hist<String> h = new Hist<>();
    h.pushPlace("1");
    h.pushPlace("2");
    h.back();
    assertPlace(h, "1", "2");

    h.backFromPlace("3");
    assertPlace(h, "1", "3");
    assertEquals(List.of("1", "3", "1", "2"), h.getPlaces());
  }

  @Test
  public void pushPlaceMiddle() {
    Hist<String> h = new Hist<>();
    h.pushPlace("1");
    h.pushPlace("2");
    h.pushPlace("3");
    h.back();
    h.back();
    assertPlace(h, "1", "2");

    h.pushPlace("4");
    assertPlace(h, "4", null);
    assertEquals(List.of("1", "4"), h.getPlaces());

    h.back();
    h.back();
    assertPlace(h, null, "1");
    h.pushPlace("5");
    assertPlace(h, "5", null);
    assertEquals(List.of("5"), h.getPlaces());
  }

  @Test
  public void pushPlace() {
    Hist<String> h = new Hist<>();
    assertPlace(h, null, null);
    h.pushPlace("1");
    assertPlace(h, "1", null);
    assertTrue(h.canBackward());
    assertFalse(h.canForward());
    h.pushPlace("2");
    h.pushPlace("3");
    assertEquals(List.of("1", "2", "3"), h.getPlaces());
    assertPlace(h, "3", null);

    h.back();
    assertPlace(h, "2", "3");
    assertTrue(h.canForward());
    assertTrue(h.canBackward());

    h.back();
    assertPlace(h, "1", "2");
    assertTrue(h.canForward());
    assertTrue(h.canBackward());

    h.back();
    assertPlace(h, null, "1");
    assertFalse(h.back());
    assertPlace(h, null, "1");
    assertTrue(h.canForward());
    assertFalse(h.canBackward());

    h.forward();
    assertPlace(h, "1", "2");
    assertTrue(h.canBackward());

    h.forward();
    assertPlace(h, "2", "3");

    h.forward();
    assertPlace(h, "3", null);

    assertFalse(h.forward());
  }

  @Test
  public void testEmpty() {
    Hist<String> h = new Hist<>();
    assertFalse(h.canForward());
    assertFalse(h.canBackward());
  }

  private void assertPlace(Hist<String> h, String p1, String p2) {
    Pair<String, String> currentPlace = h.getPosition();
    assertEquals(Pair.create(p1, p2), currentPlace);
  }
}
