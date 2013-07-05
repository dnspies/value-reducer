import java.util.Arrays;

public class NumNim {
	public final Number number;
	public final int nimber;
	public final int numUp;

	public NumNim(Number number, int nimber, int numUp) {
		this.number = number;
		this.nimber = nimber;
		this.numUp = numUp;
	}

	@Override
	public boolean equals(Object other) {
		NumNim o = (NumNim) other;
		return number.equals(o.number) && nimber == o.nimber;
	}

	public static NumNim addStar(NumNim orig) {
		assert orig.nimber == 0;
		return new NumNim(orig.number, 1, orig.numUp);
	}

	public static NumNim between(NumNim left, NumNim right) {
		assert left.nimber == 0 && left.numUp == 0;
		assert right.nimber == 0 && right.numUp == 0;
		assert left.number.compareTo(right.number) < 0;
		return new NumNim(Number.next(left.number, right.number), 0, 0);
	}

	public static NumNim nextAdd(NumNim left) {
		assert left.nimber == 0 && left.numUp == 0;
		assert left.number.num >= 0 && left.number.denom == 1;
		return new NumNim(left.number.addOne(), left.nimber, left.numUp);
	}

	public static NumNim nextDown(NumNim right) {
		assert right.nimber == 0 && right.numUp == 0;
		assert right.number.num <= 0 && right.number.denom == 1;
		return new NumNim(right.number.subOne(), right.nimber, right.numUp);
	}

	@Override
	public String toString() {
		String res = numString() + upString() + starString();
		if (res.isEmpty())
			return "0";
		else
			return res;
	}

	private String starString() {
		if (nimber == 0)
			return "";
		else if (nimber == 1)
			return "*";
		else
			return "*" + nimber;
	}

	private String upString() {
		int reps = Math.abs(numUp);
		char c = numUp < 0 ? 'v' : '^';
		char[] res = new char[reps];
		Arrays.fill(res, c);
		return String.valueOf(res);
	}

	private String numString() {
		if (number.num == 0)
			return "";
		else
			return number.toString();
	}

	public NumNim addUPFStar() {
		return new NumNim(number, nimber ^ 1, numUp + 1);
	}

	public NumNim addDownFStar() {
		return new NumNim(number, nimber ^ 1, numUp - 1);
	}

	public boolean isNumber() {
		return nimber == 0 && numUp == 0;
	}

	public boolean isNumStar() {
		return nimber == 1 && numUp == 0;
	}
}
