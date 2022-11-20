package fr.FLtest1;

public class Calculator {
	public class ObjectA {void a(){}}
	public class ObjectB {ObjectA b(){return new ObjectA();}}
	public class ObjectC {}
	public class ObjectD {ObjectC d(){return new ObjectC();}}
	int field;
	ObjectA objA;
	ObjectB objB;
	ObjectC objC;
	ObjectD objD;

	int sample() {
		//ObjectA objA = objB.b(); // field unchanged
		//ObjectC objC = objD.d(); // field changed
		return field;
	}
/*
	void strategy2() {

		ObjectA objA = objB.b();
		// 中略
		objA.a();

		// Complex x, Complex z
		Complex w = x.add(z); // z: real=1 imaginary=NAN
		// 中略
		w.getReal();

	}

	void strategy2() {

		ObjectA objA = objB.b();

		// 中略
		objA.a();

		// Complex x, Complex z
		Complex w = x.add(z); // z: real=1 imaginary=NAN
		// 中略
		x.multiply(w).getReal(); // buggy
	}

	boolean createNumber() {
		boolean hasExp = false;
		boolean hasDecPoint = false;
		boolean allowSigns = false;
		boolean foundDigit = false;
		// 中略
		if (chars[i] == 'l' || chars[i] == 'L') {
			return foundDigit && !hasExp;
		}
	}
*/
	void f(){

	}

	void g() {

	}



	void sample2(boolean flag) {
		if (flag) {
			for (int i = 0; i < 10; i++) {
				f(); // <- buggy
			}
		} else {
			g(); //
		}
	}

	public Calculator() {
	}

	public boolean a() {
		return true;
	}
	public boolean b() {
		return false;
	}

	public int calculate(String op, int op1, int op2) {
		int result;
		if (op.equals("+")) {
			result = op1 + op2;
		} else if (op.equals("-")) {
			result = op1 - op2;
		} else if (op.equals("*")) {
			result = op1 / op2;//buggy
		} else if (op.equals("/")) {
			result = op1 / op2;
		} else if (op.equals("%")) {
			result = op1 % op2;
		} else {
			throw new UnsupportedOperationException(op);
		}
		return result;
	}
















}