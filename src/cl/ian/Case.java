package cl.ian;

/**
 * Created by Ian on 26/02/2016.
 */
public enum Case {
  FRICTION_FACTOR("FrictionFactor"),
  DRAG_COEFFICIENT("DragCoefficient"),
  NUSSELT_NUMBER("NusseltNumber");

  public final String text;

  Case(final String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }
}
