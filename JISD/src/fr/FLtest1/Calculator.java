package fr.FLtest1;

public class Calculator {

	int field;

	int sample() {
		a(); // field unchanged
		b(); // field changed
		return field;
	}

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