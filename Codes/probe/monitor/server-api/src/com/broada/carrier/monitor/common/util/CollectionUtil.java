package com.broada.carrier.monitor.common.util;

import java.util.HashSet;
import java.util.Set;

public class CollectionUtil {

	public static <T> SetDiff<T> compareSet(T[] set1, T[] set2) {
		Set<T> setMores = new HashSet<T>();
		Set<T> setSames = new HashSet<T>();
		Set<T> setLacks = new HashSet<T>();

		for (T s1 : set1) {
			boolean more = true;
			for (T s2 : set2) {
				if (s1.equals(s2)) {
					more = false;
					break;
				}
			}

			if (more)
				setMores.add(s1);
			else
				setSames.add(s1);
		}

		for (T s2 : set2) {
			boolean more = true;
			for (T s1 : set1) {
				if (s1.equals(s2)) {
					more = false;
					break;
				}
			}

			if (more)
				setLacks.add(s2);
		}

		return new SetDiff<T>(setMores, setSames, setLacks);
	}

	public static class SetDiff<T> {
		private Set<T> mores;
		private Set<T> sames;
		private Set<T> lacks;

		public SetDiff(Set<T> mores, Set<T> sames, Set<T> lacks) {
			super();
			this.mores = mores;
			this.sames = sames;
			this.lacks = lacks;
		}

		public Set<T> getMores() {
			return mores;
		}

		public Set<T> getSames() {
			return sames;
		}

		public Set<T> getLacks() {
			return lacks;
		}

	}
}
