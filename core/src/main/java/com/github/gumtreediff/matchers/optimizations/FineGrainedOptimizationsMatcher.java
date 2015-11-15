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

import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;

/**
 * 
 * This implements the unmapped leaves optimization (Theta C), the inner node
 * repair optimization (Theta D) and the leaf move optimization (Theta E).
 *
 */
public class FineGrainedOptimizationsMatcher extends Matcher {

	private class ChangeMapComparator implements Comparator<Entry<ITree, IdentityHashMap<ITree, Integer>>> {

		@Override
		public int compare(Entry<ITree, IdentityHashMap<ITree, Integer>> o1,
				Entry<ITree, IdentityHashMap<ITree, Integer>> o2) {

			return Integer.compare(o1.getKey().getId(), o2.getKey().getId());
		}

	}

	private class MappingComparator implements Comparator<Mapping> {

		@Override
		public int compare(Mapping o1, Mapping o2) {
			if (o1.first.getId() != o2.first.getId()) {
				return Integer.compare(o1.first.getId(), o2.first.getId());
			}
			return Integer.compare(o1.second.getId(), o2.second.getId());
		}

	}

	public FineGrainedOptimizationsMatcher(ITree src, ITree dst, MappingStore store) {
		super(src, dst, store);
	}

	@Override
	protected void addMapping(ITree src, ITree dst) {
		assert (src != null);
		assert (dst != null);
		super.addMapping(src, dst);
	}

	private boolean allowedMatching(ITree key, ITree maxNodePartner) {
		while (key != null) {
			if (key == maxNodePartner) {
				return false;
			}
			key = key.getParent();
		}
		return true;
	}

	@Override
	public void match() {
		IdentityHashMap<ITree, IdentityHashMap<ITree, Integer>> parentCount = new IdentityHashMap<>();
		List<ITree> allNodesSrc = src.getTrees();
		List<ITree> allNodesDst = dst.getTrees();
		List<ITree> unmatchedNodes1 = new LinkedList<>();
		List<ITree> unmatchedNodes2 = new LinkedList<>();

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

		for (ITree node : unmatchedNodes1) {
			if (node.getId() == 1165 && node.getLabel().equals("IllegalArgumentException")) {
				System.err.println("here");
			}
			if (node.getChildren().size() == 0) {

				ITree parent = node.getParent();
				if (mappings.getDst(parent) != null) {
					ITree partner = mappings.getDst(parent);
					int pos = parent.getChildren().indexOf(node);
					if (pos < partner.getChildren().size()) {
						ITree child = partner.getChildren().get(pos);
						if (child.getType() == node.getType()) {
							if (child.getLabel().equals(node.getLabel())) {
								ITree childPartner = mappings.getSrc(child);
								if (childPartner != null) {
									if (!childPartner.getLabel().equals(node.getLabel())) {
										mappings.unlink(childPartner, child);
										addMapping(node, child);
									}
								} else {
									addMapping(node, child);

								}
							} else {
								ITree childPartner = mappings.getSrc(child);
								if (childPartner != null) {
									if (mappings.getDst(childPartner.getParent()) == null) {
										if (!childPartner.getLabel().equals(child.getLabel())) {
											mappings.unlink(childPartner, child);
											addMapping(node, child);
										}
									}
								} else {
									addMapping(node, child);
								}
							}
						} else {
							if (child.getChildren().size() == 1) {
								child = child.getChildren().get(0);
								if (child.getType() == node.getType() && child.getLabel().equals(node.getLabel())) {
									ITree childPartner = mappings.getSrc(child);
									if (childPartner != null) {
										if (!childPartner.getLabel().equals(node.getLabel())) {
											mappings.unlink(childPartner, child);
											addMapping(node, child);
										} else if (mappings.getDst(childPartner.getParent()) == null) {
											mappings.unlink(childPartner, child);
											addMapping(node, child);
										}
									}
								}
							} else {
								for (int i = 0; i < partner.getChildren().size(); i++) {
									ITree possibleMatch = partner.getChildren().get(i);
									if (possibleMatch.getType() == node.getType()
											&& possibleMatch.getLabel().equals(node.getLabel())) {
										ITree possibleMatchSrc = mappings.getSrc(possibleMatch);
										if (possibleMatchSrc == null) {
											addMapping(node, possibleMatch);
											break;
										} else {
											if (!possibleMatchSrc.getLabel().equals(possibleMatch.getLabel())) {
												mappings.unlink(possibleMatchSrc, possibleMatch);
												addMapping(node, possibleMatch);
												break;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		for (ITree node : unmatchedNodes2) {
			if (node.getId() == 1165 && node.getLabel().equals("IllegalArgumentException")) {
				System.err.println("here");
			}
			if (mappings.hasSrc(node)) {
				continue;
			}
			if (node.getChildren().size() == 0) {
				ITree parent = node.getParent();
				if (mappings.getSrc(parent) != null) {
					ITree partner = mappings.getSrc(parent);
					int pos = parent.getChildren().indexOf(node);
					if (pos < partner.getChildren().size()) {
						ITree child = partner.getChildren().get(pos);
						if (child.getType() == node.getType()) {
							if (child.getLabel().equals(node.getLabel())) {
								ITree tree = mappings.getDst(child);
								if (tree != null) {
									if (!tree.getLabel().equals(node.getLabel())) {
										mappings.unlink(child, tree);
										addMapping(child, node);
									}
								} else {
									addMapping(child, node);
								}
							} else {
								ITree childPartner = mappings.getDst(child);
								if (childPartner != null) {
									if (mappings.getSrc(childPartner.getParent()) == null) {
										if (!childPartner.getLabel().equals(child.getLabel())) {
											mappings.unlink(child, childPartner);
											addMapping(child, node);
										}
									}
								} else {
									addMapping(child, node);

								}
							}
						} else {
							if (child.getChildren().size() == 1) {
								child = child.getChildren().get(0);
								if (child.getType() == node.getType() && child.getLabel().equals(node.getLabel())) {
									ITree childPartner = mappings.getDst(child);
									if (childPartner != null) {
										if (!childPartner.getLabel().equals(node.getLabel())) {
											mappings.unlink(child, childPartner);
											addMapping(child, node);
										} else if (mappings.getSrc(childPartner.getParent()) == null) {
											mappings.unlink(childPartner, child);
											addMapping(node, child);
										}
									}
								}
							} else {
								for (int i = 0; i < partner.getChildren().size(); i++) {
									ITree possibleMatch = partner.getChildren().get(i);
									if (possibleMatch.getType() == node.getType()
											&& possibleMatch.getLabel().equals(node.getLabel())) {
										ITree possibleMatchDst = mappings.getDst(possibleMatch);
										if (possibleMatchDst == null) {
											addMapping(possibleMatch, node);
											break;
										} else {
											if (!possibleMatchDst.getLabel().equals(possibleMatch.getLabel())) {
												mappings.unlink(possibleMatch, possibleMatchDst);
												addMapping(possibleMatch, node);
												break;
											}
										}
									}
								}
							}
						}
					}
				} else if (unmatchedNodes2.contains(parent)) {
					ITree oldParent = parent;
					parent = parent.getParent();
					if (mappings.getSrc(parent) != null) {
						ITree partner = mappings.getSrc(parent);
						int pos = parent.getChildren().indexOf(oldParent);
						if (pos < partner.getChildren().size()) {
							ITree child = partner.getChildren().get(pos);
							if (child.getType() == node.getType() && child.getLabel().equals(node.getLabel())) {
								ITree tree = mappings.getDst(child);
								if (tree != null) {
									if (!tree.getLabel().equals(node.getLabel())) {
										mappings.unlink(child, tree);
										addMapping(child, node);
									}
								} else {
									addMapping(child, node);
								}
							}
						}
					}
				}
			}
		}

		for (Mapping pair : mappings.asSet()) {
			ITree parent = pair.first.getParent();
			ITree parentPartner = pair.second.getParent();
			if (parent != null && parentPartner != null) {
				IdentityHashMap<ITree, Integer> countMap = parentCount.get(parent);
				if (countMap == null) {
					countMap = new IdentityHashMap<>();
					parentCount.put(parent, countMap);
				}
				Integer count = countMap.get(parentPartner);
				if (count == null) {
					count = new Integer(0);
				}
				countMap.put(parentPartner, count + 1);
			}
		}
		LinkedList<Entry<ITree, IdentityHashMap<ITree, Integer>>> list = new LinkedList<>(parentCount.entrySet());
		Collections.sort(list, new ChangeMapComparator());
		for (Entry<ITree, IdentityHashMap<ITree, Integer>> countEntry : list) {
			int max = Integer.MIN_VALUE;
			int maxCount = 0;
			ITree maxNode = null;
			for (Entry<ITree, Integer> newNodeEntry : countEntry.getValue().entrySet()) {
				if (newNodeEntry.getValue() > max) {
					max = newNodeEntry.getValue();
					maxCount = 1;
					maxNode = newNodeEntry.getKey();
				} else if (newNodeEntry.getValue() == max) {
					maxCount++;
				}
			}
			if (maxCount == 1) {
				if (mappings.getDst(countEntry.getKey()) != null && mappings.getSrc(maxNode) != null) {
					ITree partner = mappings.getDst(countEntry.getKey());
					ITree maxNodePartner = mappings.getSrc(maxNode);
					if (partner != maxNode) {
						if (max > countEntry.getKey().getChildren().size() / 2
								|| countEntry.getKey().getChildren().size() == 1) {
							ITree parentPartner = mappings.getDst(countEntry.getKey().getParent());

							if (parentPartner != null && parentPartner == partner.getParent()) {
								continue;
							}
							if (allowedMatching(countEntry.getKey(), maxNodePartner)) {
								if (countEntry.getKey().getType() == maxNode.getType()) {
									if (maxNodePartner != null) {
										mappings.unlink(maxNodePartner, maxNode);
									}
									if (partner != null) {
										mappings.unlink(countEntry.getKey(), partner);
									}
									addMapping(countEntry.getKey(), maxNode);
								}
								if (maxNodePartner != null) {
									if (maxNodePartner.getType() == partner.getType()) {
										addMapping(maxNodePartner, partner);
									}
								}
							}
						}
					}
				}
			}
		}

		LinkedList<Mapping> workList = new LinkedList<>();
		LinkedList<Mapping> workListTmp = null;
		LinkedList<Mapping> changeMap = new LinkedList<>();

		for (Mapping pair : mappings.asSet()) {
			if (pair.first.isLeaf() && pair.second.isLeaf()) {
				if (!pair.first.getLabel().equals(pair.second.getLabel())) {
					workList.add(pair);
				}
			}

		}
		Collections.sort(workList, new MappingComparator());
		while (!workList.isEmpty()) {
			workListTmp = new LinkedList<>();
			for (Mapping pair : workList) {
				ITree firstParent = pair.first.getParent();
				if (!mappings.hasDst(firstParent)) {
					continue;
				}
				ITree secondParent = mappings.getDst(pair.first.getParent());
				reevaluateLeaves(firstParent, secondParent, pair, changeMap);
			}
			for (Mapping entry : changeMap) {
				addMapping(entry.first, entry.second);
				if (!entry.first.getLabel().equals(entry.second.getLabel()) && entry.first.isLeaf()
						&& entry.second.isLeaf()) {
					workListTmp.add(new Mapping(entry.first, entry.second));
				}
			}
			changeMap.clear();
			workList = workListTmp;
		}

		workList = new LinkedList<>();
		workListTmp = null;

		for (Mapping pair : mappings.asSet()) {
			if (pair.first.isLeaf() && pair.second.isLeaf()) {
				if (!pair.first.getLabel().equals(pair.second.getLabel())) {
					workList.add(pair);
				}
			}

		}
		Collections.sort(workList, new MappingComparator());
		while (!workList.isEmpty()) {
			workListTmp = new LinkedList<>();
			for (Mapping pair : workList) {
				ITree firstParent = pair.first.getParent();
				ITree secondParent = pair.second.getParent();
				reevaluateLeaves(firstParent, secondParent, pair, changeMap);
			}
			for (Mapping entry : changeMap) {
				addMapping(entry.first, entry.second);

				if (!entry.first.getLabel().equals(entry.second.getLabel()) && entry.first.isLeaf()
						&& entry.second.isLeaf()) {
					workListTmp.add(new Mapping(entry.first, entry.second));
				}
			}
			changeMap.clear();
			workList = workListTmp;
		}
	}

	private void reevaluateLeaves(ITree firstParent, ITree secondParent, Mapping pair, List<Mapping> changeMap) {

		int count = 0;
		ITree foundDstNode = null;
		ITree foundPosDstNode = null;
		int pos = firstParent.getChildren().indexOf(pair.first);

		for (int i = 0; i < secondParent.getChildren().size(); i++) {
			ITree child = secondParent.getChildren().get(i);
			if (child.getType() == pair.first.getType() && child.getLabel().equals(pair.first.getLabel())) {
				count++;
				foundDstNode = child;
				if (i == pos) {
					foundPosDstNode = child;
				}
			}
		}
		Mapping addedMappingKey = null;

		if ((count == 1 && foundDstNode != null) || foundPosDstNode != null) {
			if (count != 1 && foundPosDstNode != null) {
				foundDstNode = foundPosDstNode;
			}
			if (mappings.hasDst(foundDstNode)) {

				ITree foundSrc = mappings.getSrc(foundDstNode);
				if (!foundSrc.getLabel().equals(foundDstNode.getLabel())) {
					mappings.unlink(pair.first, pair.second);
					mappings.unlink(foundSrc, foundDstNode);
					changeMap.add(new Mapping(pair.first, foundDstNode));
					addedMappingKey = new Mapping(foundSrc, foundDstNode);
					if (foundDstNode != pair.second && foundSrc != pair.first) {
						changeMap.add(new Mapping(foundSrc, pair.second));
					}
				}
			} else {
				mappings.unlink(pair.first, pair.second);
				changeMap.add(new Mapping(pair.first, foundDstNode));
				for (ITree child : firstParent.getChildren()) {
					if (child.isLeaf() && !mappings.hasDst(child) && child.getType() == pair.second.getType()
							&& child.getLabel().equals(pair.second.getLabel())) {
						addMapping(child, pair.second);
						break;
					}
				}
			}
		}
		ITree foundSrcNode = null;
		ITree foundPosSrcNode = null;
		pos = secondParent.getChildren().indexOf(pair.second);
		for (int i = 0; i < firstParent.getChildren().size(); i++) {
			ITree child = firstParent.getChildren().get(i);
			if (child.getType() == pair.second.getType() && child.getLabel().equals(pair.second.getLabel())) {
				count++;
				foundSrcNode = child;
				if (i == pos) {
					foundPosSrcNode = child;
				}
			}
		}
		if ((count == 1 && foundSrcNode != null) || foundPosSrcNode != null) {
			if (count != 1 && foundPosSrcNode != null) {
				foundSrcNode = foundPosSrcNode;
			} else if (foundSrcNode == null) {
				foundSrcNode = foundPosSrcNode;
			}
			if (addedMappingKey != null) {
				changeMap.remove(addedMappingKey);
			}
			if (mappings.hasSrc(foundSrcNode)) {
				ITree foundDst = mappings.getSrc(foundSrcNode);
				if (foundDst != null && foundSrcNode != null && !foundDst.getLabel().equals(foundSrcNode.getLabel())) {
					mappings.unlink(pair.first, pair.second);
					mappings.unlink(foundSrcNode, foundDst);
					changeMap.add(new Mapping(foundSrcNode, pair.second));
					if (addedMappingKey == null && foundDst != null) {
						if (foundSrcNode != pair.first && foundDst != pair.second) {
							changeMap.add(new Mapping(pair.first, foundDst));
						}
					}
				}
			} else {
				mappings.unlink(pair.first, pair.second);
				changeMap.add(new Mapping(foundSrcNode, pair.second));
				for (ITree child : secondParent.getChildren()) {
					if (child.isLeaf() && !mappings.hasSrc(child) && child.getType() == pair.first.getType()
							&& child.getLabel().equals(pair.first.getLabel())) {
						addMapping(pair.first, child);
						break;
					}
				}
			}
		}
	}

}
