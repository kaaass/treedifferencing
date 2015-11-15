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

package com.github.gumtreediff.matchers.optimizations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;

/**
 * 
 * This implements lcs optimization Theta B.
 *
 */

public class LCSOptMatcher extends Matcher {

	public LCSOptMatcher(ITree src, ITree dst, MappingStore store) {
		super(src, dst, store);

	}

	private void advancedLCSMatching() {
		List<ITree> allNodesSrc = src.getTrees();
		List<ITree> allNodesDst = dst.getTrees();
		Set<ITree> unmatchedNodes1 = new HashSet<>();
		Set<ITree> unmatchedNodes2 = new HashSet<>();
		for (ITree node : allNodesSrc) {
			if (!node.isMatched()) {
				unmatchedNodes1.add(node);
			}
		}
		for (ITree node : allNodesDst) {
			if (!node.isMatched()) {
				unmatchedNodes2.add(node);
			}
		}
		if (unmatchedNodes1.size() > 0 && unmatchedNodes2.size() > 0) {
			ArrayList<ITree> workList = new ArrayList<>();
			getUnmatchedNodeListInPostOrder(src, workList);
			HashSet<ITree> checkedParent = new HashSet<>();
			for (ITree node : workList) {
				if (!unmatchedNodes1.contains(node)) {
					continue;
				}
				ITree parent = node.getParent();
				if (parent == null) {
					continue;
				}

				ITree partner = null;
				if (parent == src) {
					partner = dst;
				} else {
					partner = mappings.getDst(parent);
				}

				while (parent != null && partner == null) {
					parent = parent.getParent();
					partner = mappings.getDst(parent);
				}
				if (parent != null && partner != null) {
					if (checkedParent.contains(parent)) {
						// System.out.println("continue checked");
						continue;
					}
					checkedParent.add(parent);
					ArrayList<ITree> list1 = new ArrayList<>();
					ArrayList<ITree> list2 = new ArrayList<>();
					getNodeListInPostOrder(parent, list1);
					getNodeListInPostOrder(partner, list2);
					List<Mapping> lcsMatch = lcs(list1, list2, unmatchedNodes1, unmatchedNodes2);
					for (Mapping match : lcsMatch) {
						if (!mappings.hasSrc(match.first) && !mappings.hasDst(match.second)) {
							addMapping(match.first, match.second);
							unmatchedNodes1.remove(match.first);
							unmatchedNodes2.remove(match.second);
						}
					}
				}
			}
		}
	}

	private void backtrack(ArrayList<ITree> list1, ArrayList<ITree> list2, LinkedList<Mapping> resultList,
			int[][] matrix, int i, int j, Set<ITree> unmatchedNodes1, Set<ITree> unmatchedNodes2) {
		assert (i >= 0);
		assert (j >= 0);
		while (i > 0 && j > 0) {
			if (testCondition(list1.get(i - 1), list2.get(j - 1), unmatchedNodes1, unmatchedNodes2)) {
				if (!mappings.hasSrc(list1.get(i - 1))) {
					resultList.add(new Mapping(list1.get(i - 1), list2.get(j - 1)));
				}
			}
			if (matrix[i][j - 1] > matrix[i - 1][j]) {
				j--;
			} else {
				i--;
			}
		}
	}

	private void getNodeListInPostOrder(ITree tree, ArrayList<ITree> nodes) {
		if (tree != null) {
			for (ITree child : tree.getChildren()) {
				getNodeListInPostOrder(child, nodes);
			}
			nodes.add(tree);
		}
	}

	private void getUnmatchedNodeListInPostOrder(ITree tree, ArrayList<ITree> nodes) {
		if (tree != null) {
			for (ITree child : tree.getChildren()) {
				getNodeListInPostOrder(child, nodes);
			}
			if (!tree.isMatched()) {
				nodes.add(tree);
			}
		}
	}

	public List<Mapping> lcs(ArrayList<ITree> list1, ArrayList<ITree> list2, Set<ITree> unmatchedNodes1,
			Set<ITree> unmatchedNodes2) {
		int[][] matrix = new int[list1.size() + 1][list2.size() + 1];
		for (int i = 1; i < list1.size() + 1; i++) {
			for (int j = 1; j < list2.size() + 1; j++) {
				if (testCondition(list1.get(i - 1), list2.get(j - 1), unmatchedNodes1, unmatchedNodes2)) {
					matrix[i][j] = matrix[i - 1][j - 1] + 1;
				} else {
					matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
				}
			}
		}
		LinkedList<Mapping> resultList = new LinkedList<>();
		backtrack(list1, list2, resultList, matrix, list1.size(), list2.size(), unmatchedNodes1, unmatchedNodes2);
		return resultList;
	}

	@Override
	public void match() {
		advancedLCSMatching();

	}

	public boolean testCondition(ITree node1, ITree node2, Set<ITree> unmatchedNodes1, Set<ITree> unmatchedNodes2) {
		if (node1.getType() != node2.getType()) {
			return false;
		}
		if (mappings.hasSrc(node1) && mappings.getDst(node1) == node2) {
			return true;
		}
		if (unmatchedNodes1.contains(node1) && unmatchedNodes2.contains(node2)) {
			return true;
		}
		return false;
	}

}
