package uyun.bat.favourite.impl.facade;

public abstract class FacadeManager {
	private static FacadeManager instance = new FacadeManager() {
	};

	public static FacadeManager getInstance() {
		return instance;
	}

	private FavouriteFacade favouriteFacade;

	public FavouriteFacade getFavouriteFacade() {
		return favouriteFacade;
	}

	public void setFavouriteFacade(FavouriteFacade favouriteFacade) {
		this.favouriteFacade = favouriteFacade;
	}

}
