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

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.gumtreediff.matchers.heuristic.cdopt.similarity.NGramCalculator;
import com.github.gumtreediff.tree.ITree;

public class StringDifferenceRunnable implements Runnable {
	public static final int HASH_MAP_SIZE = 50000;

	public static void computeStringSimilarities(ConcurrentHashMap<String, Float> stringSimCache,
			ArrayList<ITree> subLeaves2, NGramCalculator stringSim, long stringCount, final ITree node1) {
		String s1 = (node1.getLabel());
		for (final ITree node2 : subLeaves2) {
			String s2 = (node2.getLabel());
			Float sim1 = stringSimCache.get(s1 + "@@" + s2);
			Float sim2 = stringSimCache.get(s2 + "@@" + s1);
			float sim = Float.MIN_VALUE;
			if (sim1 == null && sim2 == null) {
				if (s1.equals(s2)) {
					sim = 1.0f;
				} else {
					if (stringSimCache.size() < HASH_MAP_SIZE) {
						sim = stringSim.similarity(s1, s2);
						stringSimCache.put(s1 + "@@" + s2, sim);
					}
				}
			}
			stringCount++;
		}
	}

	private AtomicInteger counter = null;
	private int end;
	private int start;
	private long stringCount;
	private NGramCalculator stringSim = new NGramCalculator(2);
	private ConcurrentHashMap<String, Float> stringSimCache;
	private ITree[] subLeaves1;
	private ArrayList<ITree> subLeaves2;

	public StringDifferenceRunnable(ConcurrentHashMap<String, Float> stringSimCache, ITree[] subLeaves1,
			ArrayList<ITree> subLeaves2, long stringCount, int start, int end, AtomicInteger counter) {
		super();
		this.stringSimCache = stringSimCache;
		this.subLeaves1 = subLeaves1;
		this.subLeaves2 = subLeaves2;
		this.stringCount = stringCount;
		this.start = start;
		this.end = end;
		this.counter = counter;
		counter.incrementAndGet();
	}

	@Override
	public void run() {
		try {
			for (int i = start; i < end; i++) {
				computeStringSimilarities(stringSimCache, subLeaves2, stringSim, stringCount, subLeaves1[i]);
			}
			stringSim.clear();
			counter.decrementAndGet();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
