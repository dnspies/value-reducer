import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public class Game implements Comparable<Game> {
	public static final boolean LEFT = false;
	public static final boolean RIGHT = true;

	private static final Random rand = new Random(1L);

	private static final HashMap<Game, Game> allGames = new HashMap<Game, Game>();
	private static final HashMap<GamePair, Boolean> leMap = new HashMap<GamePair, Boolean>();
	private static final HashMap<GamePair, Game> sumMap = new HashMap<GamePair, Game>();
	private static final HashMap<GI, Game> multMap = new HashMap<GI, Game>();

	private static int constructTime;

	private final LinkedHashSet<Game> left = new LinkedHashSet<Game>(),
			right = new LinkedHashSet<Game>();
	private int leftHash, rightHash;
	private boolean inGames = false;
	private Game negated = null;

	private boolean nnChecked = false;
	private NumNim nn = null;
	private int timeConstructed;

	@Override
	public int hashCode() {
		int res = 0;
		for (Game g : left) {
			res ^= g.leftHash;
		}
		for (Game g : right) {
			res ^= g.rightHash;
		}
		return res;
	}

	@Override
	public boolean equals(Object other) {
		Game g = (Game) other;
		return this == g || left.equals(g.left) && right.equals(g.right);
	}

	private void simplify() {
		bypassReversible();
		removeDominated();
	}

	private Game(Collection<Game> left, Collection<Game> right) {
		this.left.addAll(left);
		this.right.addAll(right);
		simplify();
	}

	public static Game construct(Collection<Game> left, Collection<Game> right) {
		Game g = new Game(left, right);
		Game res = allGames.get(g);
		if (res == null) {
			g.addToGames();
//			System.err.println(g);
			res = g;
		}
		tempLE.clear();
		tempGE.clear();
		return res;
	}

	private void addToGames() {
		leftHash = rand.nextInt();
		rightHash = rand.nextInt();
		allGames.put(this, this);
		for (Entry<Game, Boolean> e : tempLE.entrySet()) {
			Game g2 = e.getKey();
			Boolean value = e.getValue();
			leMap.put(new GamePair(this, g2), value);
		}
		for (Entry<Game, Boolean> e : tempGE.entrySet()) {
			Game g2 = e.getKey();
			Boolean value = e.getValue();
			leMap.put(new GamePair(g2, this), value);
		}
		timeConstructed = constructTime++;
		inGames = true;
	}

	private void bypassReversible() {
		boolean changed;
		for (int i = 0; i < 2; i++) {
			boolean side = (i == 1);
			do {
				changed = false;
				Iterator<Game> leftIter = getSide(side).iterator();
				while (leftIter.hasNext()) {
					Game g = leftIter.next();
					LinkedHashSet<Game> newChildren = addReverse(g, side);
					if (newChildren != null) {
						leftIter.remove();
						getSide(side).addAll(newChildren);
						changed = true;
						break;
					}
				}
			} while (changed);
		}
	}

	private LinkedHashSet<Game> getSide(boolean side) {
		return side == LEFT ? left : right;
	}

	private LinkedHashSet<Game> addReverse(Game g, boolean side) {
		for (Game g2 : g.getSide(!side)) {
			if (betterForThan(side, g2)) {
				return g2.getSide(side);
			}
		}
		return null;
	}

	private boolean betterForThan(boolean side, Game g2) {
		return side == LEFT ? greatEq(g2) : lessEq(g2);
	}

	private void removeDominated() {
		Iterator<Game> leftChildren = left.iterator();
		while (leftChildren.hasNext()) {
			Game g = leftChildren.next();
			for (Game g2 : left) {
				if (g != g2 && g.lessEq(g2)) {
					leftChildren.remove();
					break;
				}
			}
		}
		Iterator<Game> rightChildren = right.iterator();
		while (rightChildren.hasNext()) {
			Game g = rightChildren.next();
			for (Game g2 : right) {
				if (g != g2 && g.greatEq(g2)) {
					rightChildren.remove();
					break;
				}
			}
		}
	}

	private static GamePair tmpPair = new GamePair();
	private static LinkedHashMap<Game, Boolean> tempLE = new LinkedHashMap<Game, Boolean>();
	private static LinkedHashMap<Game, Boolean> tempGE = new LinkedHashMap<Game, Boolean>();

	public boolean lessEq(Game g2) {
		if (inGames && g2.inGames) {
			tmpPair.first = this;
			tmpPair.second = g2;
			HashMap<GamePair, Boolean> map = leMap;
			Boolean cpsn = map.get(tmpPair);
			if (cpsn == null) {
				cpsn = cmputLessEq(g2);
				map.put(new GamePair(this, g2), cpsn);
			}
			return cpsn;
		} else {
			Boolean cpsn;
			HashMap<Game, Boolean> compMap;
			Game foreign;
			if (!inGames) {
				foreign = g2;
				compMap = tempLE;
			} else if (!g2.inGames) {
				foreign = this;
				compMap = tempGE;
			} else
				throw new Error("Qua!!??");
			cpsn = compMap.get(foreign);
			if (cpsn == null) {
				cpsn = cmputLessEq(g2);
				compMap.put(foreign, cpsn);
			}
			return cpsn;
		}
	}

	private boolean cmputLessEq(Game g2) {
		LinkedHashSet<Game> myLeft = left;
		LinkedHashSet<Game> opRight = g2.right;
		for (Game g : myLeft) {
			if (!g.lessFuzz(g2)) {
				return false;
			}
		}
		for (Game g : opRight) {
			if (!lessFuzz(g)) {
				return false;
			}
		}
		return true;
	}

	public boolean lessFuzz(Game g2) {
		return !greatEq(g2);
	}

	public boolean greatEq(Game g2) {
		return g2.lessEq(this);
	}

	public boolean greatFuzz(Game g2) {
		return !lessEq(g2);
	}

	int leftHash() {
		return leftHash;
	}

	int rightHash() {
		return rightHash;
	}

	public Game negate() {
		if (negated == null) {
			ArrayList<Game> l = new ArrayList<Game>(right.size());
			for (Game g : right) {
				l.add(g.negate());
			}
			ArrayList<Game> r = new ArrayList<Game>(left.size());
			for (Game g : left) {
				r.add(g.negate());
			}
			negated = construct(l, r);
		}
		return negated;
	}

	public Game add(Game g2) {
		tmpPair.first = this;
		tmpPair.second = g2;
		Game res = sumMap.get(tmpPair);
		if (res == null) {
			ArrayList<Game> l = null, r = null;
			for (int i = 0; i < 2; i++) {
				boolean side = (i == 1);
				ArrayList<Game> t = new ArrayList<Game>();
				for (Game g : getSide(side)) {
					t.add(g.add(g2));
				}
				for (Game g : g2.getSide(side)) {
					t.add(add(g));
				}
				if (side == LEFT)
					l = t;
				else if (side == RIGHT)
					r = t;
			}
			res = construct(l, r);
			sumMap.put(new GamePair(this, g2), res);
		}
		return res;
	}

	private static final GI tempGI = new GI();

	public Game multiply(int n) {
		tempGI.game = this;
		tempGI.mult = n;
		Game res = multMap.get(tempGI);
		if (res == null) {
			if (n < 0)
				res = negate().multiplyPos(-n);
			else
				res = multiplyPos(n);
			multMap.put(new GI(this, n), res);
		}
		return res;
	}

	private Game multiplyPos(int n) {
		if (n == 0)
			return ZERO;
		else if (n == 1)
			return this;
		else {
			return multiply(n / 2).add(multiply((n + 1) / 2));
		}
	}

	public String toString() {
		NumNim nn = checkNum();
		if (nn != null) {
			return nn.toString();
		}
		return partsString();
	}

	public String partsString() {
		StringBuilder sb = new StringBuilder("{");
		for (int i = 0; i < 2; i++) {
			boolean side = (i == 1);
			ArrayList<Game> gameList = new ArrayList<Game>(getSide(side).size());
			gameList.addAll(getSide(side));
			Collections.sort(gameList);
			boolean first = true;
			for (Game g : gameList) {
				if (first)
					first = false;
				else
					sb.append(',');
				sb.append(g);
			}
			if (side == LEFT)
				sb.append('|');
		}
		sb.append('}');
		return sb.toString();
	}

	private NumNim checkNum() {
		if (!inGames)
			return null;
		if (!nnChecked) {
			NumNim leftnn = null, rightnn = null;
			if (left.size() == 1) {
				leftnn = left.iterator().next().checkNum();
			}
			if (right.size() == 1) {
				rightnn = right.iterator().next().checkNum();
			}
			if (left.size() == 0 && right.size() == 0) {
				nn = new NumNim(new Number(0), 0, 0);
			} else if (left.size() == 1 && right.size() == 0) {
				nn = NumNim.nextAdd(leftnn);
			} else if (right.size() == 1 && left.size() == 0) {
				nn = NumNim.nextDown(rightnn);
			} else if (left.size() == 1 && right.size() == 1) {
				if (leftnn != null & rightnn != null) {
					if (leftnn.number.equals(rightnn.number)
							&& (leftnn.numUp != 0 || rightnn.numUp != 0 || leftnn.nimber
									+ rightnn.nimber == 1)) {
						if (leftnn.numUp == 0 && leftnn.nimber == 0
								&& rightnn.numUp >= 0 && rightnn.nimber <= 1) {
							nn = rightnn.addUPFStar();
						} else if (leftnn.numUp <= 0 && leftnn.nimber <= 1
								&& rightnn.numUp == 0 && rightnn.nimber == 0) {
							nn = leftnn.addDownFStar();
						}
					} else if (leftnn.number.compareTo(rightnn.number) < 0) {
						nn = NumNim.between(leftnn, rightnn);
					} else if (leftnn.equals(rightnn)) {
						nn = NumNim.addStar(leftnn);
					}
				}
			} else if (left.size() == 2 && right.size() == 1) {
				Game[] leftArrG = left.toArray(new Game[2]);
				Arrays.sort(leftArrG);
				NumNim[] leftArr = new NumNim[2];
				leftArr[0] = leftArrG[0].checkNum();
				leftArr[1] = leftArrG[1].checkNum();
				if (leftArr[0] != null && leftArr[1] != null
						&& leftArr[0].isNumber() && leftArr[1].isNumStar()
						&& leftArr[0].equals(rightnn)) {
					nn = new NumNim(rightnn.number, 1, 1);
				}
			} else if (left.size() == 1 && right.size() == 2) {
				Game[] rightArrG = right.toArray(new Game[2]);
				Arrays.sort(rightArrG);
				NumNim[] rightArr = new NumNim[2];
				rightArr[0] = rightArrG[0].checkNum();
				rightArr[1] = rightArrG[1].checkNum();
				if (rightArr[0] != null && rightArr[1] != null
						&& rightArr[0].isNumber() && rightArr[1].isNumStar()
						&& rightArr[0].equals(leftnn)) {
					nn = new NumNim(leftnn.number, 1, -1);
				}
			} else if (left.equals(right)) {
				boolean nnType = true;
				Number baseNum = null;
				for (Game g : left) {
					NumNim nn = g.checkNum();
					if (nn == null) {
						nnType = false;
						break;
					}
					if (baseNum == null)
						baseNum = nn.number;
				}
				if (nnType)
					nn = new NumNim(baseNum, left.size(), 0);
			}
			nnChecked = true;
		}
		if (nn == null)
			return null;
		else
			return nn;
	}

	public static Game constructImpartial(Game... options) {
		List<Game> l = Arrays.asList(options);
		return construct(l, l);
	}

	public static Game construct(Game... leftsAndRights) {
		int nullInd = -1;
		for (int i = 0; i < leftsAndRights.length; i++) {
			if (leftsAndRights[i] == null) {
				nullInd = i;
				break;
			}
		}
		if (nullInd == -1) {
			return construct(Collections.singleton(leftsAndRights[0]),
					Collections.singleton(leftsAndRights[1]));
		}
		ArrayList<Game> l = new ArrayList<Game>(nullInd);
		for (int i = 0; i < nullInd; i++) {
			l.add(leftsAndRights[i]);
		}
		ArrayList<Game> r = new ArrayList<Game>(leftsAndRights.length
				- (nullInd + 1));
		for (int i = nullInd + 1; i < leftsAndRights.length; i++) {
			r.add(leftsAndRights[i]);
		}
		return construct(l, r);
	}

	@Override
	public int compareTo(Game o) {
		return timeConstructed - o.timeConstructed;
	}

	public Game subtract(Game o) {
		return add(o.negate());
	}

	public boolean isInt() {
		return checkNum() != null && checkNum().nimber == 0
				&& checkNum().number.denom == 1;
	}

	public int getNum() {
		if (!isInt())
			throw new UnsupportedOperationException("NaN");
		return checkNum().number.num;
	}

	public static final Game ZERO = constructImpartial();
}
