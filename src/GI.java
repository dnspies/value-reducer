public class GI {
	Game game;
	int mult;

	GI() {
	}

	public GI(Game game, int mult) {
		this.game = game;
		this.mult = mult;
	}

	public int hashCode() {
		return game.leftHash() ^ mult;
	}

	public boolean equals(Object other) {
		if (other instanceof GI) {
			GI o = (GI) other;
			return game == o.game && mult == o.mult;
		} else
			return false;
	}
}
