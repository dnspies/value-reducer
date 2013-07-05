public class Number implements Comparable<Number> {
	public static final Number ZERO = new Number(0, 1);
	public final int num, denom;

	public Number(int num, int denom) {
		this.num = num;
		assert denom > 0;
		this.denom = denom;
	}

	public Number(int num) {
		this(num, 1);
	}

	@Override
	public int compareTo(Number o) {
		return num * o.denom - o.num * denom;
	}

	@Override
	public boolean equals(Object o) {
		Number f = (Number) o;
		return num == f.num && denom == f.denom;
	}

	@Override
	public String toString() {
		if (denom == 1)
			return Integer.toString(num);
		else
			return num + "/" + denom;
	}

	public static Number next(Number left, Number right) {
		int prevDenom = Math.max(left.denom, right.denom);
		int denom = prevDenom * 2;
		int num = denom / left.denom * left.num + 1;
		return new Number(num, denom);
	}

	public Number addOne() {
		assert denom == 1;
		return new Number(num + 1, 1);
	}

	public Number subOne() {
		assert denom == 1;
		return new Number(num - 1, 1);
	}
}
