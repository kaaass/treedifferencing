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
package com.github.gumtreediff.matchers.heuristic.cdopt.intern;

import java.util.concurrent.atomic.AtomicLong;

import com.github.gumtreediff.matchers.heuristic.cdopt.similarity.ValueComparePair;
import com.github.gumtreediff.tree.ITree;


/**
 * A class representing a matching candidate.
 */
public class MatchingCandidate extends ValueComparePair implements Comparable<MatchingCandidate> {
	private static AtomicLong counter = new AtomicLong();

	private final long id;

	public MatchingCandidate(final ITree oldElement, final ITree newElement, final Float value) {
		super(oldElement, newElement, value);

		id = counter.incrementAndGet();
	}

	@Override
	public int compareTo(MatchingCandidate o) {
		return Float.compare(o.getValue(), this.getValue());
	}

	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "(" + this.first.toString() + ", " + this.second.toString() + ")";
	}
}
