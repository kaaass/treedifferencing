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
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.heuristic.cdopt.similarity.NGramCalculator;
import com.github.gumtreediff.tree.ITree;


public class OptimizedTreeDifferenceRunnable implements Callable<Set<MatchingCandidate>> {

	private ConcurrentHashMap<ITree, ConcurrentHashMap<ITree, MatchingCandidate>> candidateMap = new ConcurrentHashMap<>();
	private AtomicInteger count;
	public Map<ITree, ArrayList<ITree>> directChildrenMap1 = null;
	public Map<ITree, ArrayList<ITree>> directChildrenMap2 = null;
	private ConcurrentSkipListSet<MatchingCandidate> initialList;

	private HashSet<MatchingCandidate> initialListOld;
	private LabelConfiguration labelConfiguration;
	public Map<ITree, ArrayList<ITree>> leavesMap1 = null;
	public Map<ITree, ArrayList<ITree>> leavesMap2 = null;
	private LMatcher lMatcher;
	private ArrayList<ITree> newNodes;
	private ArrayList<ITree> oldNodes;
	private boolean onlyOneClassPair;
	private IdentityHashMap<ITree, Integer> orderedListNew;
	private IdentityHashMap<ITree, Integer> orderedListOld;
	public Map<ITree, ITree> parents1;
	public Map<ITree, ITree> parents2;
	private IdentityHashMap<ITree, Mapping> resultMap;
	ITree root1;
	ITree root2;
	private ConcurrentHashMap<ITree, ConcurrentHashMap<ITree, Float>> similarityCache;
	private AtomicLong similarityEntries;
	private NGramCalculator stringSim = new NGramCalculator(2, 10, 10);
	private ConcurrentHashMap<String, Float> stringSimCache;
	private double weightPosition;
	private double weightSimilarity;

	public OptimizedTreeDifferenceRunnable(ArrayList<ITree> oldNodes, ArrayList<ITree> newNodes,
			HashSet<MatchingCandidate> initialListOld, AtomicInteger count,
			ConcurrentHashMap<String, Float> stringSimCache, boolean onlyOneClassPair,
			IdentityHashMap<ITree, Integer> orderedListOld, IdentityHashMap<ITree, Integer> orderedListNew,
			IdentityHashMap<ITree, Mapping> resultMap,
			ConcurrentHashMap<ITree, ConcurrentHashMap<ITree, Float>> similarityCache, AtomicLong similarityEntries,
			Map<ITree, ITree> parents1, Map<ITree, ITree> parents2, Map<ITree, ArrayList<ITree>> leavesMap1,
			Map<ITree, ArrayList<ITree>> leavesMap2, LabelConfiguration labelConfiguration, LMatcher lMatcher,
			Map<ITree, ArrayList<ITree>> directChildrenMap1, Map<ITree, ArrayList<ITree>> directChildrenMap2,
			ITree root1, ITree root2, double weightSimilarity, double weightPosition) {
		super();
		this.oldNodes = oldNodes;
		this.newNodes = newNodes;
		this.initialListOld = initialListOld;
		this.count = count;
		this.similarityCache = similarityCache;
		this.stringSimCache = stringSimCache;
		this.onlyOneClassPair = onlyOneClassPair;
		this.orderedListOld = orderedListOld;
		this.orderedListNew = orderedListNew;
		this.resultMap = resultMap;
		count.incrementAndGet();
		this.similarityEntries = similarityEntries;
		this.labelConfiguration = labelConfiguration;
		this.lMatcher = lMatcher;
		this.leavesMap1 = leavesMap1;
		this.leavesMap2 = leavesMap2;
		this.parents1 = parents1;
		this.parents2 = parents2;
		this.directChildrenMap1 = directChildrenMap1;
		this.directChildrenMap2 = directChildrenMap2;
		this.root1 = root1;
		this.root2 = root2;
		this.weightSimilarity = weightSimilarity;
		this.weightPosition = weightPosition;
	}

	@Override
	public Set<MatchingCandidate> call() throws Exception {
		try {
			initialList = new ConcurrentSkipListSet<>(new PairComparator(orderedListOld, orderedListNew));
			initialList.addAll(initialListOld);
			BreadthFirstComparator<ITree> compOld = new BreadthFirstComparator<ITree>(orderedListOld);
			Collections.sort(oldNodes, compOld);
			compOld = null;
			BreadthFirstComparator<ITree> compNew = new BreadthFirstComparator<ITree>(orderedListNew);
			Collections.sort(newNodes, compNew);
			compNew = null;
			boolean[][] aggregationFinished = new boolean[oldNodes.size()][newNodes.size()];
			double[][] similarityScores = new double[oldNodes.size()][newNodes.size()];
			ITree[] firstAggregations = new ITree[oldNodes.size()];
			ITree[] secondAggregations = new ITree[newNodes.size()];
			ConcurrentSkipListSet<MatchingCandidate> resultList = new ConcurrentSkipListSet<>(
					new PairComparator(orderedListOld, orderedListNew));
			ConcurrentHashMap<ITree, MatchingCandidate> currentResultMap = new ConcurrentHashMap<>();
			AtomicBoolean[] doneOld = new AtomicBoolean[oldNodes.size()];
			for (int i = 0; i < oldNodes.size(); i++) {
				doneOld[i] = new AtomicBoolean();
			}
			for (MatchingCandidate mc : initialList) {

				ConcurrentHashMap<ITree, MatchingCandidate> tmp = candidateMap.get(mc.first);
				if (tmp == null) {
					tmp = new ConcurrentHashMap<>();
					candidateMap.put(mc.first, tmp);

				}
				tmp.put(mc.second, mc);
			}
			for (int i = 0; i < oldNodes.size(); i++) {
				firstAggregations[i] = oldNodes.get(i);
			}
			for (int j = 0; j < newNodes.size(); j++) {
				secondAggregations[j] = newNodes.get(j);
			}
			for (int i = 0; i < oldNodes.size(); i++) {
				ITree oldNode = oldNodes.get(i);
				ConcurrentHashMap<ITree, MatchingCandidate> tmp = candidateMap.get(oldNode);
				if (tmp != null) {
					for (int j = 0; j < newNodes.size(); j++) {
						ITree newNode = newNodes.get(j);
						MatchingCandidate mc = tmp.get(newNode);
						if (mc == null) {
							aggregationFinished[i][j] = true;
							similarityScores[i][j] = Float.MIN_VALUE;
						} else {
							aggregationFinished[i][j] = false;
							similarityScores[i][j] = mc.getValue();
						}
					}
				} else {
					for (int j = 0; j < newNodes.size(); j++) {
						aggregationFinished[i][j] = true;
						similarityScores[i][j] = Float.MIN_VALUE;
					}
				}
			}
			AtomicBoolean changed = new AtomicBoolean(true);
			AtomicIntegerArray foundMaxArray = new AtomicIntegerArray(oldNodes.size());

			new RestructuredTreeDiffHelperRunnable(aggregationFinished, firstAggregations, secondAggregations,
					currentResultMap, changed, oldNodes, newNodes, resultMap, stringSim, stringSimCache,
					onlyOneClassPair, similarityScores, initialList, candidateMap, foundMaxArray, similarityCache,
					similarityEntries, parents1, parents2, leavesMap1, leavesMap2, labelConfiguration, lMatcher,
					directChildrenMap1, directChildrenMap2, root1, root2, weightSimilarity, weightPosition).call();

			resultList.addAll(initialList);
			count.decrementAndGet();
			stringSim.clear();
			initialList.clear();
			for (Entry<ITree, ConcurrentHashMap<ITree, MatchingCandidate>> entry : candidateMap.entrySet()) {
				if (entry.getValue() != null) {
					entry.getValue().clear();
				}
			}
			candidateMap.clear();
			firstAggregations = null;
			secondAggregations = null;
			similarityScores = null;
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
