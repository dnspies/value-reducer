import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {
	private static final Game ZERO = Game.ZERO;
	private static final Game ONE = Game.construct(ZERO, null);
	private static final Game STAR = Game.construct(ZERO, ZERO);
	private static final Game UP = Game.construct(ZERO, STAR);
	private static final Game DOWN = UP.negate();
	private static final ArrayList<Game> starGames = new ArrayList<Game>();
	private static final ArrayList<Game> fracs = new ArrayList<Game>();
	private Game last = null;

	public Parser() {
		fracs.add(ONE);
	}

	public Object eval(String exp) {
		SpecScanner ss = new SpecScanner(exp);
		return evalObj(ss, true);
	}

	private Game evalInner(SpecScanner ss) {
		return (Game) evalObj(ss, false);
	}

	private Object evalObj(SpecScanner ss, boolean setLast) {
		final Game res;
		String next = ss.next();
		if (next.equals(")") || next.equals("|") || next.equals("}")) {
			ss.putBack(next);
			return null;
		} else if (next.equals("(")) {
			String op = ss.next();
			ArrayList<Game> args = new ArrayList<Game>();
			for (;;) {
				Game arg = evalInner(ss);
				if (arg == null)
					break;
				args.add(arg);
			}
			String cp = ss.next();
			assertEquals(")", cp);
			return apply(op, args, setLast);
		} else if (next.equals("{")) {
			ArrayList<Game> lefts = new ArrayList<Game>(), rights = new ArrayList<Game>();
			boolean side = Game.LEFT;
			for (;;) {
				Game arg = evalInner(ss);
				if (arg == null) {
					String piece = ss.next();
					if (piece.equals("|")) {
						if (side == Game.LEFT) {
							side = Game.RIGHT;
						} else
							throw new IllegalArgumentException(
									"Can only have 1 | in a description");
					} else if (piece.equals("}")) {
						if (side == Game.RIGHT)
							break;
						else
							throw new IllegalArgumentException(
									"Every game must have a |");
					} else
						throw new IllegalArgumentException("Unmatched {");
				} else if (side == Game.LEFT) {
					lefts.add(arg);
				} else if (side == Game.RIGHT)
					rights.add(arg);
				else
					throw new Error("WTF!!??");
			}
			res = Game.construct(lefts, rights);
		} else {
			res = parseGame(next);
		}
		if (setLast)
			last = res;
		return res;
	}

	private Object apply(String op, List<Game> args, boolean setLast) {
		if (op.length() > 1)
			throw new IllegalArgumentException("ops are 1 char, not: " + op);
		Game res;
		switch (op.charAt(0)) {
		case '+':
			res = ZERO;
			for (Game g : args) {
				res = res.add(g);
			}
			break;
		case '-':
			if (args.size() == 1) {
				res = args.get(0).negate();
			} else if (args.size() == 2) {
				res = args.get(0).subtract(args.get(1));
			} else
				throw new IllegalArgumentException(
						"- can only take 1 or 2 arguments");
			break;
		case '*':
			if (args.size() == 2) {
				Game args0 = args.get(0);
				Game args1 = args.get(1);
				if (args1.isInt()) {
					Game tmp = args1;
					args1 = args0;
					args0 = tmp;
				}
				if (!args0.isInt())
					throw new IllegalArgumentException(
							"Cannot multiply arbitrary games: " + args0
									+ " with " + args1);
				res = args1.multiply(args0.getNum());
				break;
			} else
				throw new IllegalArgumentException("* takes 2 arguments");
		case '?':
			if (args.size() == 1) {
				Game args0 = args.get(0);
				if (setLast)
					last = args0;
				return args0.partsString();
			} else
				throw new IllegalArgumentException("? takes 1 argument");
		case 'c':
			if (args.size() == 2) {
				Game args0 = args.get(0);
				Game args1 = args.get(1);
				boolean lessEq = args0.lessEq(args1);
				boolean lessFuzz = args0.lessFuzz(args1);
				if (lessEq && lessFuzz)
					return args0 + " < " + args1;
				else if (lessEq && !lessFuzz)
					return args0 + " = " + args1;
				else if (!lessEq && lessFuzz)
					return args0 + " || " + args1;
				else if (!lessEq && !lessFuzz)
					return args0 + " > " + args1;
				else
					throw new Error("WTF!!??");
			} else
				throw new IllegalArgumentException("c takes 2 arguments");
		default:
			throw new IllegalArgumentException("What is " + op.charAt(0) + " ?");
		}
		if (setLast)
			last = res;
		return res;
	}

	private void assertEquals(String expected, String received) {
		if (!received.equals(expected)) {
			throw new IllegalArgumentException("Expected: " + expected
					+ ", Received: " + received);
		}
	}

	private Game parseGame(String game) {
		if (game.equals("_"))
			return last;
		int upInd = Math.max(game.indexOf('^'), game.indexOf('v'));
		int lupInd = Math.max(game.lastIndexOf('^'), game.lastIndexOf('v'));
		String upString;
		if (upInd == -1)
			upString = "";
		else {
			upString = game.substring(upInd, lupInd + 1);
			game = game.substring(0, upInd) + game.substring(lupInd + 1);
			if (game.isEmpty())
				game = "0";
		}
		int starInd = game.indexOf("*");
		Number gamePart;
		int starPart;
		if (starInd == -1) {
			gamePart = parseNum(game);
			starPart = 0;
		} else if (starInd == 0) {
			gamePart = Number.ZERO;
			if (game.length() == 1)
				starPart = 1;
			else
				starPart = Integer.parseInt(game.substring(starInd + 1));
		} else if (starInd == game.length() - 1) {
			gamePart = parseNum(game.substring(0, starInd));
			starPart = 1;
		} else {
			gamePart = parseNum(game.substring(0, starInd));
			starPart = Integer.parseInt(game.substring(starInd + 1));
		}
		return makeNum(gamePart).add(getStar(starPart)).add(getUp(upString));
	}

	private Game getUp(String upString) {
		if (upString.isEmpty())
			return ZERO;
		char[] arr = upString.toCharArray();
		for (char c : arr) {
			if (c != arr[0])
				throw new IllegalArgumentException(
						"Ups and downs must all be the same, not: " + upString);
		}
		if (arr[0] == '^')
			return UP.multiply(arr.length);
		else if (arr[0] == 'v')
			return DOWN.multiply(arr.length);
		else
			throw new Error(upString);
	}

	private Game makeNum(Number gamePart) {
		int dpow = 0;
		int dnum = 1;
		if (gamePart.denom <= 0)
			throw new IllegalArgumentException("Denominator must be positive");
		while (dnum < gamePart.denom) {
			dnum *= 2;
			dpow++;
		}
		if (gamePart.denom < dnum)
			throw new IllegalArgumentException("Denominator must be power of 2");
		return getFrac(dpow).multiply(gamePart.num);
	}

	private Game getFrac(int dpow) {
		fracs.ensureCapacity(dpow + 1);
		while (fracs.size() <= dpow) {
			fracs.add(Game.construct(ZERO, fracs.get(fracs.size() - 1)));
		}
		return fracs.get(dpow);
	}

	private Number parseNum(String game) {
		int slashind = game.indexOf("/");
		if (slashind == -1) {
			return new Number(Integer.parseInt(game));
		} else {
			int num = Integer.parseInt(game.substring(0, slashind));
			int denom = Integer.parseInt(game.substring(slashind + 1));
			return new Number(num, denom);
		}
	}

	private Game getStar(int starPart) {
		starGames.ensureCapacity(starPart + 1);
		while (starGames.size() <= starPart) {
			starGames.add(Game.construct(starGames, starGames));
		}
		return starGames.get(starPart);
	}

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		Parser p = new Parser();
		while (scan.hasNext()) {
			String line = scan.nextLine();
			if (!line.isEmpty())
				System.out.println(p.eval(line));
		}
	}
}
