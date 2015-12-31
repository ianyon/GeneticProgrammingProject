package cl.ian;

/**
 * Created by ian on 6/17/15.
 */

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavascriptEngine {

	private ScriptEngine engine;

	public JavascriptEngine() throws ScriptException {
		ScriptEngineManager manager = new ScriptEngineManager();
		engine = manager.getEngineByName("js");

		//'rand','(Vmf(i)/Vinicio)','(Df(i)/1.205)','S','Rem(i)
	      /*try {
            Class<?> objClass = this.getClass().getClassLoader().loadClass("package.ObjectName");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*/
		//engine.eval("importPackage(Packages.fcfm.die)");
		engine.eval("importClass(Packages.fcfm.die.CustomOperators);");

		// We make the static methods top level in order to access them without referencing their class
		engine.eval("plus = CustomOperators.plus;");
		engine.eval("minus = CustomOperators.minus;");
		engine.eval("times = CustomOperators.times;");
		engine.eval("cpower = CustomOperators.cpower;");
		engine.eval("mysqrt = CustomOperators.mysqrt;");
		engine.eval("mydivide = CustomOperators.myDivide;");
		engine.eval("mypower = CustomOperators.myPower;");
		engine.eval("q_mypower2 = CustomOperators.q_myPower2;");
		engine.eval("q_mypower3 = CustomOperators.q_myPower3;");
		engine.eval("kozasqrt = CustomOperators.kozasqrt;");
		engine.eval("kozadivide = CustomOperators.kozadivide;");
		engine.eval("mylog2 = CustomOperators.mylog2;");
		engine.eval("mylog = CustomOperators.mylog;");
		engine.eval("mylog10 = CustomOperators.mylog10;");
	}

	public void updateVariable(double Vinicio, double Vmf, double Df, double S, double Rem) {
		ScriptEngine localEngine = engine;
		localEngine.put("Vinicio", Vinicio);
		localEngine.put("Vmf", Vmf);
		localEngine.put("Df", Df);
		localEngine.put("S", S);
		localEngine.put("Rem", Rem);
	}

	public void updateVariable(Object Vinicio, Object Vmf, Object Df, Object S, Object Rem) {
		ScriptEngine localEngine = engine;
		localEngine.put("Vinicio", Vinicio);
		localEngine.put("Vmf", Vmf);
		localEngine.put("Df", Df);
		localEngine.put("S", S);
		localEngine.put("Rem", Rem);
	}

	public double eval(String expr, Object initialVelocity, Object Vmf, Object Df, Object S, Object Rem) {

		updateVariable(initialVelocity, Vmf, Df, S, Rem);

		// Remove scripts to read double value
		// This is fastest than using static strings and faster than doing the replace directly on the string
		String from = "(i)", to = "";
		StringBuilder builder = new StringBuilder(expr);
		int index = builder.indexOf(from);
		while (index != -1) {
			builder.replace(index, index + from.length(), to);
			// Move to the end of the replacement
			index += to.length();
			index = builder.indexOf(from, index);
		}
		String script = builder.toString();

		// Execute the computation
		try {
			return (double) engine.eval(script);
		} catch (ScriptException e) {
			e.printStackTrace();
			System.out.println("FATAL ERROR: Malformed individual: " + script);
			System.exit(-1);
		}
		// Unreachable
		return 0;
	}

    /*public Complex64F eval(String expr) {
        // Remove scripts to read double value
        // This is fastest than using static strings and faster than doing the replace directly on the string
        String from = "(i)", to="";
        StringBuilder builder = new StringBuilder(expr);
        int index = builder.indexOf(from);
        while (index != -1){
            builder.replace(index, index + from.length(), to);
            // Move to the end of the replacement
            index += to.length();
            index = builder.indexOf(from, index);
        }
        String script = builder.toString();

        // Execute the computation
        try {
            return (Complex64F)engine.eval(script);
        } catch (ScriptException e) {
            e.printStackTrace();
            System.out.println("FATAL ERROR: Malformed individual: "+script);
            System.exit(-1);
        }
        // Unreachable
        return null;
    }*/

	public static void main(String[] args) throws ScriptException {
		JavascriptEngine engine = new JavascriptEngine();
		engine.updateVariable(1, 1, 1, 1, 2);
		//engine.updateVariable(1,1,1,1,new Complex64F(1,1));
		//System.out.println(""+engine.eval("4.5+5"));
		//System.out.println(""+engine.eval("times(8,cpower(2,3))"));
		//System.out.println("" + engine.eval("times(8,cpower(Rem(i),3))"));
		//Complex64F res = (Complex64F)engine.eval("plus(8,Rem(i));");;
		//System.out.println("" + res.real + " "+res.imaginary);
	}
}
