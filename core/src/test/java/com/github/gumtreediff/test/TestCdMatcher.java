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
 * Copyright 2011-2015 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com>
 */

package com.github.gumtreediff.test;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.heuristic.cd.ChangeDistillerLeavesMatcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Pair;
import com.github.gumtreediff.tree.TreeContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCdMatcher {

    @Test
    public void testLeafMatcher() {
        Pair<TreeContext, TreeContext> trees = TreeLoader.getCdCustomPair();
        ITree src = trees.getFirst().getRoot();
        ITree dst = trees.getSecond().getRoot();
        Matcher matcher = new ChangeDistillerLeavesMatcher(src, dst, new MappingStore());
        matcher.match();
        assertEquals(2, matcher.getMappingSet().size());
        assertTrue(matcher.getMappings().has(src.getChild(0), dst.getChild(1)));
        assertTrue(matcher.getMappings().has(src.getChild(1), dst.getChild(0)));
    }

}
