/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTIcULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with GumTree. If
 * not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2015 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com>
 */

package com.github.gumtreediff.matchers.heuristic.cd;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;

import java.util.List;

public class ChangeDistillerBottomUpMatcher extends Matcher {

    private static final double STRUCT_SIM_THRESHOLD_1 = 0.6D;

    private static final double STRUCT_SIM_THRESHOLD_2 = 0.4D;

    public ChangeDistillerBottomUpMatcher(ITree src, ITree dst, MappingStore store) {
        super(src, dst, store);
    }


    /**
     * Match.
     */
    @Override
    public void match() {
        List<ITree> poDst = TreeUtils.postOrder(dst);
        for (ITree src : this.src.postOrder()) {
            int leaves = numberOfLeafs(src);
            for (ITree dst : poDst) {
                if (src.isMatchable(dst) && !(src.isLeaf() || dst.isLeaf())) {
                    double sim = chawatheSimilarity(src, dst);
                    if ((leaves > 4 && sim >= STRUCT_SIM_THRESHOLD_1)
                            || (leaves <= 4 && sim >= STRUCT_SIM_THRESHOLD_2)) {
                        addMapping(src, dst);
                        break;
                    }
                }
            }
        }
    }

    private int numberOfLeafs(ITree root) {
        int leaves = 0;
        for (ITree t : root.getDescendants()) {
            if (t.isLeaf()) {
                leaves++;
            }
        }
        return leaves;
    }
}
