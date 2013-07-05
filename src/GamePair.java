public class GamePair {
	Game first;
	Game second;

	public GamePair(Game g1, Game g2) {
		first = g1;
		second = g2;
	}

	public GamePair() {
	}

	@Override
	public int hashCode() {
		return first.leftHash() ^ second.rightHash();
	}

	@Override
	public boolean equals(Object other) {
		GamePair gp = (GamePair) other;
		return first.equals(gp.first) && second.equals(gp.second);
	}
}
