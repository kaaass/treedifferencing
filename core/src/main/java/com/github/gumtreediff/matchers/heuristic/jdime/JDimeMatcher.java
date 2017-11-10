/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with GumTree. If
 * not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2015-2016 Georg Dotzler <georg.dotzler@fau.de>
 * Copyright 2015-2016 Marius Kamp <marius.kamp@fau.de>
*/
package com.github.gumtreediff.matchers.heuristic.jdime;

import com.github.gumtreediff.gen.Registry;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Register;
import com.github.gumtreediff.tree.AstNodeArtifactWrapper;
import com.github.gumtreediff.tree.ITree;

import de.fosd.jdime.artifact.ast.ASTNodeArtifact;
import de.fosd.jdime.config.merge.MergeContext;
import de.fosd.jdime.matcher.matching.Color;
import de.fosd.jdime.matcher.matching.Matching;
import de.fosd.jdime.matcher.matching.Matchings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

@Register(id = "jdime", defaultMatcher = true, priority = Registry.Priority.HIGH)
public class JDimeMatcher extends Matcher {
    public JDimeMatcher(ITree src, ITree dst, MappingStore store) {
        super(src, dst, store);
    }

    private class MatchingComparator implements Comparator<Matching<ASTNodeArtifact>> {
        HashMap<ASTNodeArtifact, Integer> idMap = new HashMap<>();

        public MatchingComparator(AstNodeArtifactWrapper srcWrapper,
                AstNodeArtifactWrapper dstWrapper) {
            int pos = 0;
            for (ITree w : srcWrapper.preOrder()) {
                idMap.put(((AstNodeArtifactWrapper) w).tree, pos);
                pos++;
            }
            for (ITree w : dstWrapper.preOrder()) {
                idMap.put(((AstNodeArtifactWrapper) w).tree, pos);
                pos++;
            }
        }

        @Override
        public int compare(Matching<ASTNodeArtifact> o1, Matching<ASTNodeArtifact> o2) {
            int id1Left = idMap.get(o1.getLeft());
            int id2Left = idMap.get(o2.getLeft());
            int id1Right = idMap.get(o1.getRight());
            int id2Right = idMap.get(o2.getRight());
            if (id1Left != id2Left) {
                return Integer.compare(id1Left, id2Left);
            }
            return Integer.compare(id1Right, id2Right);
        }

    }

    @Override
    public void match() {
        mappings.link(src, dst);
        AstNodeArtifactWrapper srcWrapper = (AstNodeArtifactWrapper) src;
        AstNodeArtifactWrapper dstWrapper = (AstNodeArtifactWrapper) dst;
        HashMap<ASTNodeArtifact, AstNodeArtifactWrapper> srcMap = new HashMap<>();
        HashMap<ASTNodeArtifact, AstNodeArtifactWrapper> dstMap = new HashMap<>();
        for (ITree wrapper : srcWrapper.postOrder()) {
            AstNodeArtifactWrapper srcWrap = (AstNodeArtifactWrapper) wrapper;
            srcMap.put(srcWrap.tree, srcWrap);
        }
        for (ITree wrapper : dstWrapper.postOrder()) {
            AstNodeArtifactWrapper dstWrap = (AstNodeArtifactWrapper) wrapper;
            dstMap.put(dstWrap.tree, dstWrap);
        }
        ASTNodeArtifact srcArtifact = srcWrapper.tree;
        ASTNodeArtifact dstArtifact = dstWrapper.tree;
        de.fosd.jdime.matcher.Matcher<ASTNodeArtifact> matcher =
                new de.fosd.jdime.matcher.Matcher<>(srcArtifact, dstArtifact);
        MergeContext context = new MergeContext();
        context.setDiffOnly(true);
        context.setLookAhead(MergeContext.LOOKAHEAD_OFF);
        Matchings<ASTNodeArtifact> matchings = matcher.match(context, Color.GREEN);
        ArrayList<Matching<ASTNodeArtifact>> matchingList = new ArrayList<>(matchings.size());
        for (Matching<ASTNodeArtifact> m : matchings) {
            matchingList.add(m);
        }
        Collections.sort(matchingList, new MatchingComparator(srcWrapper, dstWrapper));
        for (Matching<ASTNodeArtifact> m : matchingList) {
            ASTNodeArtifact sourceArtifact = m.getLeft();
            ASTNodeArtifact destinationArtifact = m.getRight();
            assert (srcMap.get(sourceArtifact) != null);
            assert (dstMap.get(destinationArtifact) != null);
            AstNodeArtifactWrapper srcAst = srcMap.get(sourceArtifact);
            AstNodeArtifactWrapper dstAst = dstMap.get(destinationArtifact);
            if (!this.mappings.hasDst(dstAst) && !this.mappings.hasSrc(srcAst)) {
                mappings.link(srcAst, dstAst);
            }
        }

    }
}
