package cl.ian;

/**
 * Created by Ian on 10/02/2016.
 */
public class InputVariables {
    public double current;
    public double separation;
    public double flow;
    public double initTemperature;
    public double cellDiameter;

    public void set(double[] input) {
        current = input[0];
        separation = input[1];
        flow = input[2];
        initTemperature = input[3];
        cellDiameter = input[4];
    }
}
