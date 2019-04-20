package insideCandle;

import java.time.LocalDate;
import java.time.Month;

import org.apache.commons.math3.util.Precision;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Test test = new Test();
		test.check();

	}

	public void check() {
		int div = 10;
		int number = 555;
		
		double high=105;
		double low = 0;
		
		System.out.println(Precision.round((289.95*0.99), 2));
		LocalDate currentDate = LocalDate.now();
		System.out.println(currentDate);


		/*
		 * if (number <= 500) { div = 5; } else if (number > 500 && number <= 1000) {
		 * div = 10; } else if (number >= 1000 && number <= 2000) { div = 20; }
		 */
		
	/*	int rounded = number / div * div;
		System.out.println(rounded);
		
		LocalDate currentDate = LocalDate.now();
		int dom = currentDate.getDayOfMonth();
		Month m = currentDate.getMonth();
		//System.out.println(currentDate+""+dom+""+m.toString().substring(0, 3).toUpperCase());
*/
	}

}
