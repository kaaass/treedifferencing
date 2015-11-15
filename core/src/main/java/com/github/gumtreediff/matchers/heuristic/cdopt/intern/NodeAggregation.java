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

import java.util.LinkedList;
import java.util.List;

import com.github.gumtreediff.tree.AbstractTree.FakeTree;
import com.github.gumtreediff.tree.ITree;

/**
 * A class representing a subtree, whose nodes have been linked together. It is
 * used by {@link CdOptimizedMatcher.tree.SIFEMatcher} for the implementation of
 * the best-match strategy.
 *
 * @author Marius Kamp
 */
public class NodeAggregation extends FakeTree {

	public static final int tag = -1000;

	int hash = Integer.MIN_VALUE;

	private ITree subTree;

	public NodeAggregation(final ITree subTree) {
		setAssociatedTree(subTree);
	}

	public ITree getAssociatedTree() {
		return subTree;
	}

	@Override
	public List<ITree> getChildren() {
		return null;
	}

	public int getHash() {
		return hash;
	}

	@Override
	public int getId() {
		return -1;
	}

	@Override
	public String getLabel() {
		return null;
	}

	public int getTag() {
		return tag;
	}

	@Override
	public int getType() {
		return tag;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

	public void setAssociatedTree(final ITree subTree) {

		if (subTree != null) {
			this.subTree = subTree;
			StringBuilder builder = new StringBuilder();
			LinkedList<ITree> workList = new LinkedList<>();
			workList.add(subTree);
			while (!workList.isEmpty()) {
				ITree node = workList.removeFirst();
				builder.append(node.getType() + node.getLabel());
				workList.addAll(node.getChildren());
			}
			hash = builder.toString().hashCode();
		} else {
			throw new NullPointerException("The associated tree must not be null");
		}
	}

	@Override
	public String toString() {
		return subTree.toString();
	}
}
