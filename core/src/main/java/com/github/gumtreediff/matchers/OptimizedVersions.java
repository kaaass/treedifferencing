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

package com.github.gumtreediff.matchers;

import com.github.gumtreediff.matchers.CompositeMatcher;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.heuristic.cd.ChangeDistillerBottomUpMatcher;
import com.github.gumtreediff.matchers.heuristic.cd.ChangeDistillerLeavesMatcher;
import com.github.gumtreediff.matchers.heuristic.cd.ChangeDistillerParallelLeavesMatcher;
import com.github.gumtreediff.matchers.heuristic.gt.GreedyBottomUpMatcher;
import com.github.gumtreediff.matchers.heuristic.gt.GreedySubtreeMatcher;
import com.github.gumtreediff.matchers.optimal.rted.RtedMatcher;
import com.github.gumtreediff.tree.ITree;

import com.github.gumtreediff.matchers.heuristic.mtdiff.MtDiffOptimizedMatcher;
import com.github.gumtreediff.matchers.heuristic.mtdiff.intern.LabelConfiguration;
import com.github.gumtreediff.matchers.heuristic.mtdiff.intern.TreeMatcherConfiguration;
import com.github.gumtreediff.matchers.optimizations.FineGrainedOptimizationsMatcher;
import com.github.gumtreediff.matchers.optimizations.IdenticalSubtreeMatcher;
import com.github.gumtreediff.matchers.optimizations.LcsOptMatcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;

public class OptimizedVersions {

    public static class CdSeq extends CompositeMatcher {

        /**
         * Instantiates the sequential ChangeDistiller version.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public CdSeq(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new ChangeDistillerLeavesMatcher(src, dst, store),
                            new ChangeDistillerBottomUpMatcher(src, dst, store) });
        }
    }

    public static class Cd extends CompositeMatcher {

        /**
         * Instantiates the parallel ChangeDistiller version.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Cd(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new ChangeDistillerParallelLeavesMatcher(src, dst, store),
                            new ChangeDistillerBottomUpMatcher(src, dst, store) });
        }
    }

    public static class Cda extends CompositeMatcher {

        /**
         * Instantiates the parallel ChangeDistiller version with Theta A.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Cda(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                            new ChangeDistillerParallelLeavesMatcher(src, dst, store),
                            new ChangeDistillerBottomUpMatcher(src, dst, store) });
        }
    }

    public static class Cdab extends CompositeMatcher {

        /**
         * Instantiates the parallel ChangeDistiller version with Theta A-B.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Cdab(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                            new ChangeDistillerParallelLeavesMatcher(src, dst, store),
                            new ChangeDistillerBottomUpMatcher(src, dst, store),
                            new LcsOptMatcher(src, dst, store)

                    });
        }
    }

    public static class Cdabcde extends CompositeMatcher {

        /**
         * Instantiates the parallel ChangeDistiller version with Theta A-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Cdabcde(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                            new ChangeDistillerParallelLeavesMatcher(src, dst, store),
                            new ChangeDistillerBottomUpMatcher(src, dst, store),
                            new LcsOptMatcher(src, dst, store),
                            new FineGrainedOptimizationsMatcher(src, dst, store)

                    });
        }
    }

    public static class CdabcdeSeq extends CompositeMatcher {

        /**
         * Instantiates the sequential ChangeDistiller version with Theta A-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public CdabcdeSeq(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                            new ChangeDistillerLeavesMatcher(src, dst, store),
                            new ChangeDistillerBottomUpMatcher(src, dst, store),
                            new LcsOptMatcher(src, dst, store),
                            new FineGrainedOptimizationsMatcher(src, dst, store)

                    });
        }
    }

    public static class Cdacde extends CompositeMatcher {

        /**
         * Instantiates the parallel ChangeDistiller version with Theta A, C-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Cdacde(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                            new ChangeDistillerParallelLeavesMatcher(src, dst, store),
                            new ChangeDistillerBottomUpMatcher(src, dst, store),
                            new FineGrainedOptimizationsMatcher(src, dst, store) });
        }
    }

    public static class Cdb extends CompositeMatcher {

        /**
         * Instantiates the parallel ChangeDistiller version with Theta B.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Cdb(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new ChangeDistillerParallelLeavesMatcher(src, dst, store),
                            new ChangeDistillerBottomUpMatcher(src, dst, store),
                            new LcsOptMatcher(src, dst, store) });
        }
    }

    public static class Cdbcde extends CompositeMatcher {

        /**
         * Instantiates the parallel ChangeDistiller version with Theta B-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Cdbcde(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new ChangeDistillerParallelLeavesMatcher(src, dst, store),
                            new ChangeDistillerBottomUpMatcher(src, dst, store),
                            new LcsOptMatcher(src, dst, store),
                            new FineGrainedOptimizationsMatcher(src, dst, store) });
        }
    }

    public static class Cdcde extends CompositeMatcher {

        /**
         * Instantiates the parallel ChangeDistiller version with Theta C-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Cdcde(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new ChangeDistillerParallelLeavesMatcher(src, dst, store),
                            new ChangeDistillerBottomUpMatcher(src, dst, store),
                            new FineGrainedOptimizationsMatcher(src, dst, store) });
        }
    }

    public static class MtDiff extends CompositeMatcher {

        /**
         * Instantiates MTDIFF.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         * @param executorService the executor service
         * @param configuration the configuration
         * @param labelConfiguration the label configuration
         */
        public MtDiff(ITree src, ITree dst, MappingStore store, ExecutorService executorService,
                      TreeMatcherConfiguration configuration, LabelConfiguration labelConfiguration) {
            super(src, dst, store,
                    new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                            new MtDiffOptimizedMatcher(src, dst, store), new LcsOptMatcher(src, dst, store),
                            new FineGrainedOptimizationsMatcher(src, dst, store)

                    });
            ((MtDiffOptimizedMatcher) matchers[1]).initMtDiff(executorService, configuration,
                    labelConfiguration);
        }

        public static CompositeMatcher getMtDiffJava(ITree src,
                ITree dst, MappingStore store, ExecutorService executorService) {
            TreeMatcherConfiguration configuration = null;
            configuration = new TreeMatcherConfiguration(0.88f, 0.37f, 0.0024f);

            HashSet<Integer> labelsForValueCompare = new HashSet<Integer>();
            labelsForValueCompare
                    .addAll(Arrays.asList(new Integer[] {

                        org.eclipse.jdt.core.dom.ASTNode.CHARACTER_LITERAL,
                            org.eclipse.jdt.core.dom.ASTNode.PRIMITIVE_TYPE,
                            org.eclipse.jdt.core.dom.ASTNode.UNION_TYPE,
                            org.eclipse.jdt.core.dom.ASTNode.WILDCARD_TYPE,
                            org.eclipse.jdt.core.dom.ASTNode.MODIFIER,
                            org.eclipse.jdt.core.dom.ASTNode.STRING_LITERAL,
                            org.eclipse.jdt.core.dom.ASTNode.NUMBER_LITERAL,
                            org.eclipse.jdt.core.dom.ASTNode.CHARACTER_LITERAL,
                            org.eclipse.jdt.core.dom.ASTNode.BOOLEAN_LITERAL,
                            org.eclipse.jdt.core.dom.ASTNode.PREFIX_EXPRESSION,
                            org.eclipse.jdt.core.dom.ASTNode.POSTFIX_EXPRESSION,
                            org.eclipse.jdt.core.dom.ASTNode.ASSIGNMENT }));

            final HashSet<Integer> labelsForRealCompare = new HashSet<Integer>();

            HashSet<Integer> labelsForIntCompare = new HashSet<Integer>();
            labelsForIntCompare
                    .addAll(Arrays.asList(new Integer[] { org.eclipse.jdt.core.dom.ASTNode.NUMBER_LITERAL }));

            HashSet<Integer> labelsForStringCompare = new HashSet<Integer>();
            labelsForStringCompare.addAll(Arrays.asList(new Integer[] {
                    org.eclipse.jdt.core.dom.ASTNode.SIMPLE_NAME, org.eclipse.jdt.core.dom.ASTNode.SIMPLE_TYPE,
                    org.eclipse.jdt.core.dom.ASTNode.STRING_LITERAL,
                    org.eclipse.jdt.core.dom.ASTNode.ARRAY_TYPE,
                    org.eclipse.jdt.core.dom.ASTNode.PARAMETERIZED_TYPE,
                    org.eclipse.jdt.core.dom.ASTNode.QUALIFIED_TYPE,
                    org.eclipse.jdt.core.dom.ASTNode.QUALIFIED_NAME }));

            HashSet<Integer> labelsForBoolCompare = new HashSet<Integer>();
            labelsForBoolCompare
                    .addAll(Arrays.asList(new Integer[] { org.eclipse.jdt.core.dom.ASTNode.BOOLEAN_LITERAL }));

            LabelConfiguration labelConfiguration =
                    new LabelConfiguration(
                            org.eclipse.jdt.core.dom.ASTNode.SIMPLE_NAME,
                            org.eclipse.jdt.core.dom.ASTNode.COMPILATION_UNIT,
                            org.eclipse.jdt.core.dom.ASTNode.TYPE_DECLARATION,
                            org.eclipse.jdt.core.dom.ASTNode.SIMPLE_TYPE, org.eclipse.jdt.core.dom.ASTNode.MODIFIER,
                            labelsForValueCompare, labelsForRealCompare, labelsForIntCompare,
                            labelsForStringCompare, labelsForBoolCompare);
            return new MtDiff(src, dst, store, executorService, configuration, labelConfiguration);
        }

    }

    public static class Gt extends CompositeMatcher {

        /**
         * Instantiates GumTree.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Gt(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store, new Matcher[] { new GreedySubtreeMatcher(src, dst, store),
                    new GreedyBottomUpMatcher(src, dst, store) });
        }
    }

    public static class Gta extends CompositeMatcher {

        /**
         * Instantiates GumTree with Theta A.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Gta(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                            new GreedySubtreeMatcher(src, dst, store),
                            new GreedyBottomUpMatcher(src, dst, store) });
        }
    }

    public static class Gtab extends CompositeMatcher {

        /**
         * Instantiates GumTree with Theta A-B.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Gtab(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                            new GreedySubtreeMatcher(src, dst, store), new GreedyBottomUpMatcher(src, dst, store),
                            new LcsOptMatcher(src, dst, store)

                    });
        }
    }

    public static class Gtabcde extends CompositeMatcher {

        /**
         * Instantiates GumTree with Theta A-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Gtabcde(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                            new GreedySubtreeMatcher(src, dst, store), new GreedyBottomUpMatcher(src, dst, store),
                            new LcsOptMatcher(src, dst, store),
                            new FineGrainedOptimizationsMatcher(src, dst, store)

                    });
        }
    }

    public static class Gtacde extends CompositeMatcher {

        /**
         * Instantiates GumTree with Theta A, C-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Gtacde(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                            new GreedySubtreeMatcher(src, dst, store), new GreedyBottomUpMatcher(src, dst, store),
                            new FineGrainedOptimizationsMatcher(src, dst, store) });
        }
    }

    public static class Gtb extends CompositeMatcher {

        /**
         * Instantiates GumTree with Theta B.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Gtb(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store, new Matcher[] { new GreedySubtreeMatcher(src, dst, store),
                    new GreedyBottomUpMatcher(src, dst, store), new LcsOptMatcher(src, dst, store) });
        }
    }

    public static class Gtbcde extends CompositeMatcher {

        /**
         * Instantiates GumTree with Theta B-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Gtbcde(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new GreedySubtreeMatcher(src, dst, store),
                            new GreedyBottomUpMatcher(src, dst, store), new LcsOptMatcher(src, dst, store),
                            new FineGrainedOptimizationsMatcher(src, dst, store) });
        }
    }

    public static class Gtcde extends CompositeMatcher {

        /**
         * Instantiates GumTree with Theta C-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Gtcde(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new GreedySubtreeMatcher(src, dst, store),
                            new GreedyBottomUpMatcher(src, dst, store),
                            new FineGrainedOptimizationsMatcher(src, dst, store) });
        }
    }

    //    public static class Jsync extends CompositeMatcher {
    //        public JSyncInitMatcher initMatcher;
    //
    //        /**
    //         * Instantiates a new jsync.
    //         *
    //         * @param src the src
    //         * @param dst the dst
    //         * @param store the store
    //         * @param oldFileData the old file data
    //         * @param newFileData the new file data
    //         */
    //        public Jsync(ITree src, ITree dst, MappingStore store, byte[] oldFileData, byte[] newFileData) {
    //            super(src, dst, store, new Matcher[2]);
    //            initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
    //            JSyncMatcher jsyncMatcher = new JSyncMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store, initMatcher.visitorSrc, initMatcher.visitorDst,
    //                    initMatcher.mapFileSrc, initMatcher.mapFileDst);
    //            matchers[0] = initMatcher;
    //            matchers[1] = jsyncMatcher;
    //        }
    //    }
    //
    //    public static class Jsynca extends CompositeMatcher {
    //        public JSyncInitMatcher initMatcher;
    //
    //        /**
    //         * Instantiates a new jsynca.
    //         *
    //         * @param src the src
    //         * @param dst the dst
    //         * @param store the store
    //         * @param oldFileData the old file data
    //         * @param newFileData the new file data
    //         */
    //        public Jsynca(ITree src, ITree dst, MappingStore store, byte[] oldFileData,
    //                      byte[] newFileData) {
    //            super(src, dst, store, new Matcher[3]);
    //            initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
    //            matchers[0] = initMatcher;
    //            matchers[1] = new IdenticalSubtreeMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store);
    //            matchers[2] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store, initMatcher.visitorSrc, initMatcher.visitorDst,
    //                    initMatcher.mapFileSrc, initMatcher.mapFileDst);
    //
    //        }
    //    }
    //
    //    public static class Jsyncab extends CompositeMatcher {
    //        public JSyncInitMatcher initMatcher;
    //
    //        /**
    //         * Instantiates a new jsyncab.
    //         *
    //         * @param src the src
    //         * @param dst the dst
    //         * @param store the store
    //         * @param oldFileData the old file data
    //         * @param newFileData the new file data
    //         */
    //        public Jsyncab(ITree src, ITree dst, MappingStore store, byte[] oldFileData,
    //                       byte[] newFileData) {
    //            super(src, dst, store, new Matcher[4]);
    //            initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
    //            matchers[0] = initMatcher;
    //            matchers[1] = new IdenticalSubtreeMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store);
    //            matchers[2] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store, initMatcher.visitorSrc, initMatcher.visitorDst,
    //                    initMatcher.mapFileSrc, initMatcher.mapFileDst);
    //            matchers[3] =
    //                    new LcsOptMatcher(initMatcher.oldContext.getRoot(),
    //                            initMatcher.newContext.getRoot(), store);
    //        }
    //    }
    //
    //    public static class Jsyncabcde extends CompositeMatcher {
    //        public JSyncInitMatcher initMatcher;
    //
    //        /**
    //         * Instantiates a new jsyncabcde.
    //         *
    //         * @param src the src
    //         * @param dst the dst
    //         * @param store the store
    //         * @param oldFileData the old file data
    //         * @param newFileData the new file data
    //         */
    //        public Jsyncabcde(ITree src, ITree dst, MappingStore store, byte[] oldFileData,
    //                          byte[] newFileData) {
    //            super(src, dst, store, new Matcher[5]);
    //            initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
    //            matchers[0] = initMatcher;
    //            matchers[1] = new IdenticalSubtreeMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store);
    //            matchers[2] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store, initMatcher.visitorSrc, initMatcher.visitorDst,
    //                    initMatcher.mapFileSrc, initMatcher.mapFileDst);
    //            matchers[3] =
    //                    new LcsOptMatcher(initMatcher.oldContext.getRoot(),
    //                            initMatcher.newContext.getRoot(), store);
    //            matchers[4] = new FineGrainedOptimizationsMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store);
    //        }
    //    }
    //
    //    public static class Jsyncacde extends CompositeMatcher {
    //        public JSyncInitMatcher initMatcher;
    //
    //        /**
    //         * Instantiates a new jsyncacde.
    //         *
    //         * @param src the src
    //         * @param dst the dst
    //         * @param store the store
    //         * @param oldFileData the old file data
    //         * @param newFileData the new file data
    //         */
    //        public Jsyncacde(ITree src, ITree dst, MappingStore store, byte[] oldFileData,
    //                         byte[] newFileData) {
    //            super(src, dst, store, new Matcher[4]);
    //            initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
    //            matchers[0] = initMatcher;
    //            matchers[1] = new IdenticalSubtreeMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store);
    //            matchers[2] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store, initMatcher.visitorSrc, initMatcher.visitorDst,
    //                    initMatcher.mapFileSrc, initMatcher.mapFileDst);
    //            matchers[3] = new FineGrainedOptimizationsMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store);
    //
    //        }
    //    }
    //
    //    public static class Jsyncb extends CompositeMatcher {
    //        public JSyncInitMatcher initMatcher;
    //
    //        /**
    //         * Instantiates a new jsyncb.
    //         *
    //         * @param src the src
    //         * @param dst the dst
    //         * @param store the store
    //         * @param oldFileData the old file data
    //         * @param newFileData the new file data
    //         */
    //        public Jsyncb(ITree src, ITree dst, MappingStore store, byte[] oldFileData,
    //                      byte[] newFileData) {
    //            super(src, dst, store, new Matcher[3]);
    //            initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
    //            matchers[0] = initMatcher;
    //            matchers[1] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store, initMatcher.visitorSrc, initMatcher.visitorDst,
    //                    initMatcher.mapFileSrc, initMatcher.mapFileDst);
    //            matchers[2] =
    //                    new LcsOptMatcher(initMatcher.oldContext.getRoot(),
    //                            initMatcher.newContext.getRoot(), store);
    //
    //        }
    //    }
    //
    //    public static class Jsyncbcde extends CompositeMatcher {
    //        public JSyncInitMatcher initMatcher;
    //
    //        /**
    //         * Instantiates a new jsyncbcde.
    //         *
    //         * @param src the src
    //         * @param dst the dst
    //         * @param store the store
    //         * @param oldFileData the old file data
    //         * @param newFileData the new file data
    //         */
    //        public Jsyncbcde(ITree src, ITree dst, MappingStore store, byte[] oldFileData,
    //                         byte[] newFileData) {
    //            super(src, dst, store, new Matcher[4]);
    //            initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
    //            matchers[0] = initMatcher;
    //            matchers[1] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store, initMatcher.visitorSrc, initMatcher.visitorDst,
    //                    initMatcher.mapFileSrc, initMatcher.mapFileDst);
    //            matchers[2] =
    //                    new LcsOptMatcher(initMatcher.oldContext.getRoot(),
    //                            initMatcher.newContext.getRoot(), store);
    //            matchers[3] = new FineGrainedOptimizationsMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store);
    //
    //        }
    //    }
    //
    //    public static class Jsynccde extends CompositeMatcher {
    //        public JSyncInitMatcher initMatcher;
    //
    //        /**
    //         * Instantiates a new jsynccde.
    //         *
    //         * @param src the src
    //         * @param dst the dst
    //         * @param store the store
    //         * @param oldFileData the old file data
    //         * @param newFileData the new file data
    //         */
    //        public Jsynccde(ITree src, ITree dst, MappingStore store, byte[] oldFileData,
    //                        byte[] newFileData) {
    //            super(src, dst, store, new Matcher[3]);
    //            initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
    //            matchers[0] = initMatcher;
    //            matchers[1] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store, initMatcher.visitorSrc, initMatcher.visitorDst,
    //                    initMatcher.mapFileSrc, initMatcher.mapFileDst);
    //            matchers[2] = new FineGrainedOptimizationsMatcher(initMatcher.oldContext.getRoot(),
    //                    initMatcher.newContext.getRoot(), store);
    //
    //        }
    //    }

    public static class Rteda extends CompositeMatcher {

        /**
         * Instantiates RTED with Theta A.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Rteda(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store, new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                    new RtedMatcher(src, dst, store) });
        }
    }

    public static class Rtedab extends CompositeMatcher {

        /**
         * Instantiates RTED with Theta A-B.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Rtedab(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store, new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                    new RtedMatcher(src, dst, store), new LcsOptMatcher(src, dst, store)

            });
        }
    }

    public static class Rtedabcde extends CompositeMatcher {

        /**
         * Instantiates RTED with Theta A-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Rtedabcde(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                            new RtedMatcher(src, dst, store), new LcsOptMatcher(src, dst, store),
                            new FineGrainedOptimizationsMatcher(src, dst, store)

                    });
        }
    }

    public static class Rtedacde extends CompositeMatcher {

        /**
         * Instantiates RTED with Theta A, C-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Rtedacde(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store, new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
                    new RtedMatcher(src, dst, store), new FineGrainedOptimizationsMatcher(src, dst, store)

            });
        }
    }

    public static class Rtedb extends CompositeMatcher {

        /**
         * Instantiates RTED with Theta B.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Rtedb(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store,
                    new Matcher[] { new RtedMatcher(src, dst, store), new LcsOptMatcher(src, dst, store) });
        }
    }

    public static class Rtedbcde extends CompositeMatcher {

        /**
         * Instantiates RTED with Theta B-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Rtedbcde(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store, new Matcher[] { new RtedMatcher(src, dst, store),
                    new LcsOptMatcher(src, dst, store), new FineGrainedOptimizationsMatcher(src, dst, store)

            });
        }
    }

    public static class Rtedcde extends CompositeMatcher {

        /**
         * Instantiates RTED with Theta C-E.
         *
         * @param src the src
         * @param dst the dst
         * @param store the store
         */
        public Rtedcde(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store, new Matcher[] { new RtedMatcher(src, dst, store),
                    new FineGrainedOptimizationsMatcher(src, dst, store) });
        }
    }

}
