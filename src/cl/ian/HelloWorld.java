package cl.ian;

import org.ejml.data.Complex64F;

import java.io.File;

public class HelloWorld {
	public static final String from = "(i)", to = "";

	public static void main(String[] args) {
		/*String str = "v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+v(i)+v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)++v(i)+";

		String fromLocal = "(i)", toLocal = "";

		long meanTime = 0;
		long startTime = System.nanoTime();

		StringBuilder builder = new StringBuilder(str);
		int index = builder.indexOf(from);
		while (index != -1) {
			builder.replace(index, index + from.length(), to);
			index += to.length(); // Move to the end of the replacement
			index = builder.indexOf(from, index);
		}
		String hola = builder.toString();
		meanTime = (System.nanoTime() - startTime) / 1000;
		System.out.println("Tiempo medio BUILDER STATIC: " + meanTime + "us");

		meanTime = 0;
		startTime = System.nanoTime();

		builder = new StringBuilder(str);
		index = builder.indexOf(from);
		while (index != -1) {
			builder.replace(index, index + fromLocal.length(), toLocal);
			index += toLocal.length(); // Move to the end of the replacement
			index = builder.indexOf(fromLocal, index);
		}
		hola = builder.toString();
		meanTime = (System.nanoTime() - startTime) / 1000;
		System.out.println("Tiempo medio BUILDER LOCAL: " + meanTime + "us");

		meanTime = 0;
		startTime = System.nanoTime();
		String script = str.replace("(i)", "");
		meanTime = (System.nanoTime() - startTime) / 1000;
		System.out.println("Tiempo medio STRING: " + meanTime + "us");

		meanTime = 0;
		startTime = System.nanoTime();
		Complex64F a = new Complex64F(0.23452, 0.18364),
				res = new Complex64F(0, 0);
		for (int i = 0; i < 100; i++) {
			res = res.times(a);
		}
		meanTime = (System.nanoTime() - startTime) / 1000;
		System.out.println("Tiempo medio COMPLEX COMPLETE: " + meanTime + "us");

		meanTime = 0;
		startTime = System.nanoTime();
		Complex64F b = new Complex64F(0.23452, 0.0),
				resu = new Complex64F(0, 0);
		for (int i = 0; i < 100; i++) {
			res = res.times(b);
		}
		meanTime = (System.nanoTime() - startTime) / 1000;
		System.out.println("Tiempo medio COMPLEX HALF: " + meanTime + "us");

		meanTime = 0;
		startTime = System.nanoTime();
		double c = 0.23452,
				resul = 0;
		for (int i = 0; i < 100; i++) {
			resul = resul * c;
		}
		meanTime = (System.nanoTime() - startTime) / 1000;
		System.out.println("Tiempo medio DOUBLE: " + meanTime + "us");

		System.out.println("Shift: " + (4 << 1) + "");
		*/
		String curDir = System.getProperty("user.dir");
		File GradeList = new File("Config.txt");
		System.out.println("Current sys dir: " + curDir);
		System.out.println("Current abs dir: " + GradeList.getAbsolutePath());
        if (GradeList.exists())
            System.out.println("Exists");
        else
            System.out.println("Doesn't Exists");
	}

	public String hello() {
		String helloWorld = "Hello World!";
		return helloWorld;
	}
}
