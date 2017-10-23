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
 * Copyright 2015-2017 Georg Dotzler <georg.dotzler@fau.de>
 * Copyright 2015-2017 Marius Kamp <marius.kamp@fau.de>
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

import java.util.HashMap;

@Register(id = "jdime", defaultMatcher = true, priority = Registry.Priority.HIGH)
public class JDimeMatcher extends Matcher {
    public JDimeMatcher(ITree src, ITree dst, MappingStore store) {
        super(src, dst, store);
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
        de.fosd.jdime.matcher.Matcher<ASTNodeArtifact> matcher = new de.fosd.jdime.matcher.Matcher<>();
        MergeContext context = new MergeContext();
        context.setDiffOnly(true);
        //context.setLookAhead(MergeContext.LOOKAHEAD_FULL);
        Matchings<ASTNodeArtifact> matchings =
                matcher.match(context, srcArtifact, dstArtifact, Color.GREEN);
        for (Matching<ASTNodeArtifact> m : matchings) {
            ASTNodeArtifact sourceArtifact = m.getLeft();
            ASTNodeArtifact destinationArtifact = m.getRight();
            assert (srcMap.get(sourceArtifact) != null);
            assert (dstMap.get(destinationArtifact) != null);
            AstNodeArtifactWrapper srcAst = srcMap.get(sourceArtifact);
            AstNodeArtifactWrapper dstAst = dstMap.get(destinationArtifact);
            if (!this.mappings.hasDst(srcAst) && !this.mappings.hasSrc(dstAst)) {
                mappings.link(srcAst, dstAst);
            }
        }

    }
}
