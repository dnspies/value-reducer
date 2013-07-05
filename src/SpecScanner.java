import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Scanner;

public class SpecScanner {
	private static final String[] singletons = new String[] { "(", ")", "{",
			"|", "}", "," };
	private static final HashSet<String> ignore = new HashSet<String>();
	static {
		ignore.add(",");
	}

	private final Scanner scan;
	private final ArrayDeque<String> nstack = new ArrayDeque<String>();

	public SpecScanner(String exp) {
		scan = new Scanner(exp);
	}

	public String next() {
		String res;
		do {
			String s = nstack.poll();
			if (s == null)
				s = scan.next();
			int fp = Integer.MAX_VALUE;
			for (String sing : singletons) {
				int p = s.indexOf(sing);
				if (p >= 0)
					fp = Math.min(fp, p);
			}
			if (fp == Integer.MAX_VALUE)
				res = s;
			else {
				if (fp == 0)
					fp = 1;
				if (fp < s.length())
					nstack.push(s.substring(fp));
				res = s.substring(0, fp);
			}
		} while (ignore.contains(res));
		return res;
	}

	public void putBack(String next) {
		nstack.push(next);
	}
}
