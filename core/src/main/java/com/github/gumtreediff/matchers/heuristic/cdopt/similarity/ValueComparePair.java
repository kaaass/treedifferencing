/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2015 Georg Dotzler <georg.dotzler@fau.de>
 * Copyright 2015 Marius Kamp <marius.kamp@fau.de>
 */
package com.github.gumtreediff.matchers.heuristic.cdopt.similarity;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.tree.ITree;

/**
 * A {@link ComparePair} with an additional value field. However, the value
 * field is not considered when comparing two instances of this class. Thus two
 * instances of ValueComparePair with the same elements but different values
 * would be considered equal and would return the same hash code.
 *
 * @author Marius Kamp
 */
public class ValueComparePair extends Mapping {
	private final Float value;

	public ValueComparePair(final ITree oldElement, final ITree newElement, final Float value) {
		super(oldElement, newElement);
		this.value = value;
	}

	/**
	 * Returns an equivalent {@link ComparePair} without the associated value.
	 */
	public final Mapping dropValue() {
		return new Mapping(this.first, this.second);
	}

	public final boolean equals(final Object obj) {
		return super.equals(obj);
	}

	/**
	 * Returns the value associated with this pair.
	 */
	public final Float getValue() {
		return value;
	}

	public final int hashCode() {
		return super.hashCode();
	}
}
