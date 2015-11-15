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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.heuristic.cdopt.similarity.NGramCalculator;
import com.github.gumtreediff.tree.ITree;


public class InnerNodeMatcher {

	Map<ITree, ArrayList<ITree>> directChildrenMap1 = null;
	Map<ITree, ArrayList<ITree>> directChildrenMap2 = null;
	LabelConfiguration labelConfiguration;
	Map<ITree, ArrayList<ITree>> leavesMap1 = null;
	Map<ITree, ArrayList<ITree>> leavesMap2 = null;

	Set<Mapping> matchedNodes;

	private NGramCalculator stringSim = new NGramCalculator(2, 10, 10);

	final int subtreeSizeThreshold = 4;
	final float subtreeThresholdLarge = 0.6f;
	final float subtreeThresholdSmall = 0.4f;
	final float subtreeThresholdValueMismatch = 0.7f;
	final float valueThreshold = 0.6f;

	public InnerNodeMatcher(LabelConfiguration labelConfiguration, Map<ITree, ArrayList<ITree>> leavesMap1,
			Map<ITree, ArrayList<ITree>> leavesMap2, Map<ITree, ArrayList<ITree>> directChildrenMap1,
			Map<ITree, ArrayList<ITree>> directChildrenMap2, Set<Mapping> matchedNodes) {
		this.labelConfiguration = labelConfiguration;
		this.leavesMap1 = leavesMap1;
		this.leavesMap2 = leavesMap2;
		this.directChildrenMap1 = directChildrenMap1;
		assert (directChildrenMap1 != null);
		this.directChildrenMap2 = directChildrenMap2;
		this.matchedNodes = matchedNodes;

	}

	private float childrenSimilarity(final List<ITree> firstChildren, final List<ITree> secondChildren,
			final List<ITree> firstDirectChildren, final List<ITree> secondDirectChildren) {
		int common = 0;
		assert (firstChildren != null);
		assert (secondChildren != null);
		final int max = Math.max(firstChildren.size(), secondChildren.size());
		int[] firstDCCount = new int[firstDirectChildren.size()];
		int[] secondDCCount = new int[secondDirectChildren.size()];
		@SuppressWarnings("unchecked")
		ArrayList<ITree>[] firstDCLists = new ArrayList[firstDirectChildren.size()];
		@SuppressWarnings("unchecked")
		ArrayList<ITree>[] secondDCLists = new ArrayList[secondDirectChildren.size()];
		for (int i = 0; i < firstDirectChildren.size(); i++) {
			firstDCLists[i] = leavesMap1.get(firstDirectChildren.get(i));
			assert (firstDCLists[i] != null);
		}
		for (int i = 0; i < secondDirectChildren.size(); i++) {
			secondDCLists[i] = leavesMap2.get(secondDirectChildren.get(i));
			assert (secondDCLists[i] != null);
		}
		if (max <= 0) {
			return 1.0f;
		}

		int posFirst = -1;
		outer: for (final ITree firstNode : firstChildren) {
			posFirst++;
			int posSecond = -1;
			if (firstNode == null) {
				continue;
			}

			for (final ITree secondNode : secondChildren) {
				posSecond++;
				if (secondNode == null) {
					continue;
				}

				final Mapping pair = new Mapping(firstNode, secondNode);

				if (matchedNodes.contains(pair)) {
					common++;
					int counter = 0;
					for (int i = 0; i < firstDCLists.length; i++) {
						if (firstDCLists[i].contains(firstNode)) {
							firstDCCount[i]++;
							break;
						} else if (counter == posFirst && firstDCLists[i].isEmpty()) {
							firstDCCount[i]++;
							break;
						}
						counter += (firstDCCount[i] == 0 ? 1 : firstDCCount[i]);
					}
					counter = 0;
					for (int i = 0; i < secondDCLists.length; i++) {
						if (secondDCLists[i].contains(secondNode)) {
							secondDCCount[i]++;
							break;
						} else if (counter == posSecond && secondDCLists[i].isEmpty()) {
							secondDCCount[i]++;
							break;
						}
						counter += (secondDCCount[i] == 0 ? 1 : secondDCCount[i]);
					}
					continue outer;
				}
			}
		}
		if (common > max) {
			System.err.println("what?");
		}
		assert common <= max;
		float tmp = 0.0f;
		for (int i = 0; i < firstDCLists.length; i++) {
			tmp += firstDCCount[i] / (float) (firstDCLists[i].size() == 0 ? 1 : firstDCLists[i].size());
		}
		for (int i = 0; i < secondDCLists.length; i++) {
			tmp += secondDCCount[i] / (float) (secondDCLists[i].size() == 0 ? 1 : secondDCLists[i].size());
		}
		tmp = tmp / (firstDCLists.length + secondDCLists.length);
		return tmp;
	}

	private boolean isMatch(final float childrenSimilarity, final float valueSimilarity, final int childrenCount) {
		if (valueSimilarity < valueThreshold) {
			return childrenSimilarity >= subtreeThresholdValueMismatch;
		}
		if (childrenCount <= subtreeSizeThreshold) {
			return childrenSimilarity >= subtreeThresholdSmall;
		}
		return childrenSimilarity >= subtreeThresholdLarge;
	}

	public boolean match(float labelSimilarity, float combinedSimilarity, ITree first, ITree second) {
		if (labelSimilarity < valueThreshold) {
			if (labelSimilarity != 0 && combinedSimilarity != 0 && first.getId() == second.getId()) {
				return true;
			} else {
				return combinedSimilarity >= subtreeThresholdValueMismatch;
			}
		}
		return true;
	}

	public boolean match(ITree first, ITree second, float similarity) {
		if (first.getChildren() == null || first.getChildren().size() == 0) {
			return false;
		}
		if (first.getType() != second.getType()) {
			return false;
		}
		final List<ITree> firstChldrn = leavesMap1.get(first);
		final List<ITree> secondChldrn = leavesMap2.get(second);
		final List<ITree> firstDirectCh = directChildrenMap1.get(first);
		final List<ITree> secondDirectCh = directChildrenMap2.get(second);
		if (firstChldrn == null) {
			assert (false);
		}
		if (secondChldrn == null) {
			assert (false);
		}
		final float childrenSim = childrenSimilarity(firstChldrn, secondChldrn, firstDirectCh, secondDirectCh);
		final int childrenCount = Math.max(firstChldrn.size(), secondChldrn.size());

		return isMatch(childrenSim, similarity, childrenCount);

	}

	public float newSimilarity(ITree first, ITree second, float labelSim) {
		float childSim = 0.0f;
		if (first.getChildren() == null || first.getChildren().size() == 0) {
			childSim = 0.0f;
		} else if (first.getType() != second.getType()) {
			childSim = 0.0f;
		} else {
			final List<ITree> firstChldrn = leavesMap1.get(first);
			final List<ITree> secondChldrn = leavesMap2.get(second);
			final List<ITree> firstDirectCh = directChildrenMap1.get(first);
			final List<ITree> secondDirectCh = directChildrenMap2.get(second);
			if (firstChldrn == null) {
				assert (false);
			}
			if (secondChldrn == null) {
				assert (false);
			}
			childSim = childrenSimilarity(firstChldrn, secondChldrn, firstDirectCh, secondDirectCh);
		}
		return (labelSim + childSim) / 2;

	}

	public float similarity(ITree first, ITree second) {
		if (first.getType() != second.getType()) {
			return 0.0f;
		}
		if (first.getChildren() == null || second.getChildren() == null) {
			return 0.0f;
		}
		if (first.getChildren().size() == 0 || second.getChildren().size() == 0) {
			return 0.0f;
		}
		if (labelConfiguration.labelsForValueCompare.contains(first.getType())) {
			if (first.getLabel().equals(second.getLabel())) {
				return 1.0f;
			}
			return 0.2f;
		} else if (labelConfiguration.labelsForStringCompare.contains(first.getType())) {
			return stringSim.similarity(first.getLabel(), second.getLabel());

		} else {
			return 1.0f;
		}
	}

}
