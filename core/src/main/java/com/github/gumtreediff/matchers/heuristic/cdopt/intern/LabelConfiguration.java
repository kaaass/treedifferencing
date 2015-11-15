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

import java.util.HashSet;

public class LabelConfiguration {
	public final int basicTypeLabel;
	public final int classLabel;
	public final int identifierLabel;
	HashSet<Integer> labelsForBoolCompare;
	HashSet<Integer> labelsForIntCompare;

	HashSet<Integer> labelsForRealCompare;
	HashSet<Integer> labelsForStringCompare;
	HashSet<Integer> labelsForValueCompare;
	public final int qualifierLabel;
	public final int rootLabel;

	public LabelConfiguration(HashSet<Integer> labelsForValueCompare) {
		this(-1, -1, -1, -1, -1, labelsForValueCompare, new HashSet<Integer>(), new HashSet<Integer>(),
				new HashSet<Integer>(), new HashSet<Integer>());

	}

	public LabelConfiguration(int identifierLabel, int rootLabel, int classLabel, int basicTypeLabel,
			int qualifierLabel, HashSet<Integer> labelsForValueCompare, HashSet<Integer> labelsForRealCompare,
			HashSet<Integer> labelsForIntCompare, HashSet<Integer> labelsForStringCompare,
			HashSet<Integer> labelsForBoolCompare) {
		this.identifierLabel = identifierLabel;
		this.rootLabel = rootLabel;
		this.classLabel = classLabel;
		this.labelsForValueCompare = labelsForValueCompare;
		this.labelsForBoolCompare = labelsForBoolCompare;
		this.labelsForRealCompare = labelsForRealCompare;
		this.labelsForIntCompare = labelsForIntCompare;
		this.labelsForStringCompare = labelsForStringCompare;
		this.labelsForBoolCompare = labelsForBoolCompare;
		this.basicTypeLabel = basicTypeLabel;
		this.qualifierLabel = qualifierLabel;
	}
	
}
