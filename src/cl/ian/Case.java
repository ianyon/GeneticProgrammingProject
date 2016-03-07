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

  public static Case chooseCase(String s) {
    if (s.equals(Case.FRICTION_FACTOR.text))
      return Case.FRICTION_FACTOR;
    else if (s.equals(Case.DRAG_COEFFICIENT.text))
      return Case.DRAG_COEFFICIENT;
    else if (s.equals(Case.NUSSELT_NUMBER.text))
      return Case.NUSSELT_NUMBER;
    else {
      System.out.println("Wrong case");
      System.exit(-2);
      return Case.FRICTION_FACTOR;
    }
  }
}
