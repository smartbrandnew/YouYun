package uyun.bat.favourite.impl.logic;

public abstract class LogicManager {
	private static LogicManager instance = new LogicManager() {
	};

	public static LogicManager getInstance() {
		return instance;
	}

	private FavouriteLogic favouriteLogic;

	public FavouriteLogic getFavouriteLogic() {
		return favouriteLogic;
	}

	public void setFavouriteLogic(FavouriteLogic favouriteLogic) {
		this.favouriteLogic = favouriteLogic;
	}

}
