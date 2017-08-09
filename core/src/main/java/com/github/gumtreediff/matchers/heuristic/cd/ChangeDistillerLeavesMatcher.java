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

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;

import org.simmetrics.StringMetrics;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Sequential variant of the ChangeDistiller leaves matcher.
 */
public class ChangeDistillerLeavesMatcher extends Matcher {

    private class ChangeDistillerCallableResult {
        public final List<Mapping> leafMappings;
        public final HashMap<Mapping, Double> simMap;

        public ChangeDistillerCallableResult(List<Mapping> leafMappings,
                                             HashMap<Mapping, Double> simMap) {
            this.leafMappings = leafMappings;
            this.simMap = simMap;
        }
    }

    private class ChangeDistillerLeavesMatcherCallable
            implements Callable<ChangeDistillerCallableResult> {

        HashMap<String, Double> cacheResults = new HashMap<>();
        private int cores;
        private List<ITree> dstLeaves;
        List<Mapping> leafMappings = new LinkedList<>();
        HashMap<Mapping, Double> simMap = new HashMap<>();
        private List<ITree> srcLeaves;
        private int start;

        public ChangeDistillerLeavesMatcherCallable(List<ITree> srcLeaves, List<ITree> dstLeaves,
                                                    int cores, int start) {
            this.srcLeaves = srcLeaves;
            this.dstLeaves = dstLeaves;
            this.cores = cores;
            this.start = start;
        }

        @Override
        public ChangeDistillerCallableResult call() throws Exception {
            for (int i = start; i < srcLeaves.size(); i += cores) {
                ITree srcLeaf = srcLeaves.get(i);
                for (ITree dstLeaf : dstLeaves) {
                    if (isMappingAllowed(srcLeaf,dstLeaf)) {
                        double sim = 0f;
                        // TODO: Use a unique string instead of @@
                        if (cacheResults.containsKey(srcLeaf.getLabel() + "@@" + dstLeaf.getLabel())) {
                            sim = cacheResults.get(srcLeaf.getLabel() + "@@" + dstLeaf.getLabel());
                        } else {
                            sim = StringMetrics.qGramsDistance().compare(srcLeaf.getLabel(), dstLeaf.getLabel());
                            cacheResults.put(srcLeaf.getLabel() + "@@" + dstLeaf.getLabel(), sim);
                        }
                        if (sim > LABEL_SIM_THRESHOLD) {
                            Mapping mapping = new Mapping(srcLeaf, dstLeaf);
                            leafMappings.add(new Mapping(srcLeaf, dstLeaf));
                            simMap.put(mapping, sim);
                        }
                    }
                }
            }
            return new ChangeDistillerCallableResult(leafMappings, simMap);
        }

    }

    private class LeafMappingComparator implements Comparator<Mapping> {
        HashMap<Mapping, Double> simMap = null;

        public LeafMappingComparator(HashMap<Mapping, Double> simMap) {
            this.simMap = simMap;
        }

        @Override
        public int compare(Mapping m1, Mapping m2) {
            return Double.compare(sim(m1), sim(m2));
        }

        public double sim(Mapping mapping) {

            return simMap.get(mapping);
        }

    }

    private static final double LABEL_SIM_THRESHOLD = 0.5D;

    public ChangeDistillerLeavesMatcher(ITree src, ITree dst, MappingStore store) {
        super(src, dst, store);
    }


    /**
     * Match.
     */
    @Override
    public void match() {
        List<ITree> dstLeaves = retainLeaves(TreeUtils.postOrder(dst));
        List<ITree> srcLeaves = retainLeaves(TreeUtils.postOrder(src));

        List<Mapping> leafMappings = new LinkedList<>();
        HashMap<Mapping, Double> simMap = new HashMap<>();
        int cores = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < cores; i++) {
            ChangeDistillerLeavesMatcherCallable callable =
                    new ChangeDistillerLeavesMatcherCallable(srcLeaves, dstLeaves, cores, i);
            ChangeDistillerCallableResult result;
            try {
                result = callable.call();
                leafMappings.addAll(result.leafMappings);
                simMap.putAll(result.simMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Set<ITree> srcIgnored = new HashSet<>();
        Set<ITree> dstIgnored = new HashSet<>();
        Collections.sort(leafMappings, new LeafMappingComparator(simMap));
        while (leafMappings.size() > 0) {
            Mapping best = leafMappings.remove(0);
            if (!(srcIgnored.contains(best.getFirst()) || dstIgnored.contains(best.getSecond()))) {
                addMapping(best.getFirst(), best.getSecond());
                srcIgnored.add(best.getFirst());
                dstIgnored.add(best.getSecond());
            }
        }
    }

    private List<ITree> retainLeaves(List<ITree> trees) {
        Iterator<ITree> iter = trees.iterator();
        while (iter.hasNext()) {
            ITree innerIter = iter.next();
            if (!innerIter.isLeaf()) {
                iter.remove();
            }
        }
        return trees;
    }
}
