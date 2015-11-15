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
package com.github.gumtreediff.matchers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import com.github.gumtreediff.matchers.CompositeMatcher;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.heuristic.cd.ChangeDistillerBottomUpMatcher;
import com.github.gumtreediff.matchers.heuristic.cd.ChangeDistillerParallelLeavesMatcher;
import com.github.gumtreediff.matchers.heuristic.cdopt.CdOptimizedMatcher;
import com.github.gumtreediff.matchers.heuristic.cdopt.intern.LabelConfiguration;
import com.github.gumtreediff.matchers.heuristic.cdopt.intern.TreeMatcherConfiguration;
import com.github.gumtreediff.matchers.heuristic.gt.GreedyBottomUpMatcher;
import com.github.gumtreediff.matchers.heuristic.gt.GreedySubtreeMatcher;
import com.github.gumtreediff.matchers.optimal.rted.RtedMatcher;
import com.github.gumtreediff.matchers.optimizations.FineGrainedOptimizationsMatcher;
import com.github.gumtreediff.matchers.optimizations.IdenticalSubtreeMatcher;
import com.github.gumtreediff.matchers.optimizations.LCSOptMatcher;
import com.github.gumtreediff.tree.ITree;


public class OptimizedVersions {

  public static class CD extends CompositeMatcher {
    public CD(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new ChangeDistillerParallelLeavesMatcher(src, dst, store),
              new ChangeDistillerBottomUpMatcher(src, dst, store) });
    }
  }

  public static class CD_A extends CompositeMatcher {
    public CD_A(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
              new ChangeDistillerParallelLeavesMatcher(src, dst, store),
              new ChangeDistillerBottomUpMatcher(src, dst, store) });
    }
  }

  public static class CD_AB extends CompositeMatcher {
    public CD_AB(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
              new ChangeDistillerParallelLeavesMatcher(src, dst, store),
              new ChangeDistillerBottomUpMatcher(src, dst, store),
              new LCSOptMatcher(src, dst, store)

      });
    }
  }

  public static class CD_ABCDE extends CompositeMatcher {
    public CD_ABCDE(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
              new ChangeDistillerParallelLeavesMatcher(src, dst, store),
              new ChangeDistillerBottomUpMatcher(src, dst, store),
              new LCSOptMatcher(src, dst, store),
              new FineGrainedOptimizationsMatcher(src, dst, store)

      });
    }
  }

  public static class CD_ACDE extends CompositeMatcher {
    public CD_ACDE(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
              new ChangeDistillerParallelLeavesMatcher(src, dst, store),
              new ChangeDistillerBottomUpMatcher(src, dst, store),
              new FineGrainedOptimizationsMatcher(src, dst, store) });
    }
  }

  public static class CD_B extends CompositeMatcher {
    public CD_B(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new ChangeDistillerParallelLeavesMatcher(src, dst, store),
              new ChangeDistillerBottomUpMatcher(src, dst, store),
              new LCSOptMatcher(src, dst, store) });
    }
  }

  public static class CD_BCDE extends CompositeMatcher {
    public CD_BCDE(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new ChangeDistillerParallelLeavesMatcher(src, dst, store),
              new ChangeDistillerBottomUpMatcher(src, dst, store),
              new LCSOptMatcher(src, dst, store),
              new FineGrainedOptimizationsMatcher(src, dst, store) });
    }
  }

  public static class CD_CDE extends CompositeMatcher {
    public CD_CDE(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new ChangeDistillerParallelLeavesMatcher(src, dst, store),
              new ChangeDistillerBottomUpMatcher(src, dst, store),
              new FineGrainedOptimizationsMatcher(src, dst, store) });
    }
  }

  public static class CD_OPT extends CompositeMatcher {
    public CD_OPT(ITree src, ITree dst, MappingStore store, ExecutorService executorService,
        TreeMatcherConfiguration configuration, LabelConfiguration labelConfiguration) {
      super(src, dst, store,
          new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
              new CdOptimizedMatcher(src, dst, store), new LCSOptMatcher(src, dst, store),
              new FineGrainedOptimizationsMatcher(src, dst, store)

      });
      ((CdOptimizedMatcher) matchers[1]).initSIFE(executorService, configuration,
          labelConfiguration);
    }

    public CD_OPT(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
              new CdOptimizedMatcher(src, dst, store), new LCSOptMatcher(src, dst, store),
              new FineGrainedOptimizationsMatcher(src, dst, store)

      });

      TreeMatcherConfiguration configuration = null;
      configuration = new TreeMatcherConfiguration(CdOptimizedMatcher.LEAF_THRESHOLD,
          CdOptimizedMatcher.WEIGHT_SIMILARITY, CdOptimizedMatcher.WEIGHT_POSITION);

      HashSet<Integer> labelsForValueCompare = new HashSet<Integer>();
      labelsForValueCompare
          .addAll(Arrays.asList(new Integer[] { org.eclipse.jdt.core.dom.ASTNode.CHARACTER_LITERAL,
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

      HashSet<Integer> labelsForRealCompare = new HashSet<Integer>();

      HashSet<Integer> labelsForIntCompare = new HashSet<Integer>();
      labelsForIntCompare
          .addAll(Arrays.asList(new Integer[] { org.eclipse.jdt.core.dom.ASTNode.NUMBER_LITERAL }));

      HashSet<Integer> labelsForStringCompare = new HashSet<Integer>();
      labelsForStringCompare
          .addAll(Arrays.asList(new Integer[] { org.eclipse.jdt.core.dom.ASTNode.SIMPLE_NAME,
              org.eclipse.jdt.core.dom.ASTNode.SIMPLE_TYPE,
              org.eclipse.jdt.core.dom.ASTNode.STRING_LITERAL,
              org.eclipse.jdt.core.dom.ASTNode.ARRAY_TYPE,
              org.eclipse.jdt.core.dom.ASTNode.PARAMETERIZED_TYPE,
              org.eclipse.jdt.core.dom.ASTNode.QUALIFIED_TYPE,
              org.eclipse.jdt.core.dom.ASTNode.QUALIFIED_NAME }));

      HashSet<Integer> labelsForBoolCompare = new HashSet<Integer>();
      labelsForBoolCompare.addAll(
          Arrays.asList(new Integer[] { org.eclipse.jdt.core.dom.ASTNode.BOOLEAN_LITERAL }));

      LabelConfiguration labelConfiguration = new LabelConfiguration(
          org.eclipse.jdt.core.dom.ASTNode.SIMPLE_NAME,
          org.eclipse.jdt.core.dom.ASTNode.COMPILATION_UNIT,
          org.eclipse.jdt.core.dom.ASTNode.TYPE_DECLARATION,
          org.eclipse.jdt.core.dom.ASTNode.SIMPLE_TYPE, org.eclipse.jdt.core.dom.ASTNode.MODIFIER,
          labelsForValueCompare, labelsForRealCompare, labelsForIntCompare, labelsForStringCompare,
          labelsForBoolCompare);

      ((CdOptimizedMatcher) matchers[1]).initSIFE(null, configuration, labelConfiguration);
    }

  }

  public static class GT extends CompositeMatcher {
    public GT(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store, new Matcher[] { new GreedySubtreeMatcher(src, dst, store),
          new GreedyBottomUpMatcher(src, dst, store) });
    }
  }

  public static class GT_A extends CompositeMatcher {
    public GT_A(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
              new GreedySubtreeMatcher(src, dst, store),
              new GreedyBottomUpMatcher(src, dst, store) });
    }
  }

  public static class GT_AB extends CompositeMatcher {
    public GT_AB(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
              new GreedySubtreeMatcher(src, dst, store), new GreedyBottomUpMatcher(src, dst, store),
              new LCSOptMatcher(src, dst, store)

      });
    }
  }

  public static class GT_ABCDE extends CompositeMatcher {
    public GT_ABCDE(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
              new GreedySubtreeMatcher(src, dst, store), new GreedyBottomUpMatcher(src, dst, store),
              new LCSOptMatcher(src, dst, store),
              new FineGrainedOptimizationsMatcher(src, dst, store)

      });
    }
  }

  public static class GT_ACDE extends CompositeMatcher {
    public GT_ACDE(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
              new GreedySubtreeMatcher(src, dst, store), new GreedyBottomUpMatcher(src, dst, store),
              new FineGrainedOptimizationsMatcher(src, dst, store) });
    }
  }

  public static class GT_B extends CompositeMatcher {
    public GT_B(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store, new Matcher[] { new GreedySubtreeMatcher(src, dst, store),
          new GreedyBottomUpMatcher(src, dst, store), new LCSOptMatcher(src, dst, store) });
    }
  }

  public static class GT_BCDE extends CompositeMatcher {
    public GT_BCDE(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new GreedySubtreeMatcher(src, dst, store),
              new GreedyBottomUpMatcher(src, dst, store), new LCSOptMatcher(src, dst, store),
              new FineGrainedOptimizationsMatcher(src, dst, store) });
    }
  }

  public static class GT_CDE extends CompositeMatcher {
    public GT_CDE(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new GreedySubtreeMatcher(src, dst, store),
              new GreedyBottomUpMatcher(src, dst, store),
              new FineGrainedOptimizationsMatcher(src, dst, store) });
    }
  }

  // public static class JSYNC extends CompositeMatcher {
  // public JSyncInitMatcher initMatcher;
  //
  // public JSYNC(ITree src, ITree dst, MappingStore store, byte[] oldFileData, byte[] newFileData)
  // {
  // super(src, dst, store, new Matcher[2]);
  // initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
  // JSyncMatcher jsyncMatcher = new JSyncMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store, initMatcher.visitorSrc, initMatcher.visitorDst,
  // initMatcher.mapFileSrc, initMatcher.mapFileDst);
  // matchers[0] = initMatcher;
  // matchers[1] = jsyncMatcher;
  // }
  // }
  //
  // public static class JSYNC_A extends CompositeMatcher {
  // public JSyncInitMatcher initMatcher;
  //
  // public JSYNC_A(ITree src, ITree dst, MappingStore store, byte[] oldFileData, byte[]
  // newFileData) {
  // super(src, dst, store, new Matcher[3]);
  // initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
  // matchers[0] = initMatcher;
  // matchers[1] = new IdenticalSubtreeMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store);
  // matchers[2] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store,
  // initMatcher.visitorSrc, initMatcher.visitorDst, initMatcher.mapFileSrc,
  // initMatcher.mapFileDst);
  //
  // }
  // }
  //
  // public static class JSYNC_AB extends CompositeMatcher {
  // public JSyncInitMatcher initMatcher;
  //
  // public JSYNC_AB(ITree src, ITree dst, MappingStore store, byte[] oldFileData, byte[]
  // newFileData) {
  // super(src, dst, store, new Matcher[4]);
  // initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
  // matchers[0] = initMatcher;
  // matchers[1] = new IdenticalSubtreeMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store);
  // matchers[2] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store,
  // initMatcher.visitorSrc, initMatcher.visitorDst, initMatcher.mapFileSrc,
  // initMatcher.mapFileDst);
  // matchers[3] = new LCSOptMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store);
  // }
  // }
  //
  // public static class JSYNC_ABCDE extends CompositeMatcher {
  // public JSyncInitMatcher initMatcher;
  //
  // public JSYNC_ABCDE(ITree src, ITree dst, MappingStore store, byte[] oldFileData, byte[]
  // newFileData) {
  // super(src, dst, store, new Matcher[5]);
  // initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
  // matchers[0] = initMatcher;
  // matchers[1] = new IdenticalSubtreeMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store);
  // matchers[2] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store,
  // initMatcher.visitorSrc, initMatcher.visitorDst, initMatcher.mapFileSrc,
  // initMatcher.mapFileDst);
  // matchers[3] = new LCSOptMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store);
  // matchers[4] = new FineGrainedOptimizationsMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store);
  // }
  // }
  //
  // public static class JSYNC_ACDE extends CompositeMatcher {
  // public JSyncInitMatcher initMatcher;
  //
  // public JSYNC_ACDE(ITree src, ITree dst, MappingStore store, byte[] oldFileData, byte[]
  // newFileData) {
  // super(src, dst, store, new Matcher[4]);
  // initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
  // matchers[0] = initMatcher;
  // matchers[1] = new IdenticalSubtreeMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store);
  // matchers[2] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store,
  // initMatcher.visitorSrc, initMatcher.visitorDst, initMatcher.mapFileSrc,
  // initMatcher.mapFileDst);
  // matchers[3] = new FineGrainedOptimizationsMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store);
  //
  // }
  // }
  //
  // public static class JSYNC_B extends CompositeMatcher {
  // public JSyncInitMatcher initMatcher;
  //
  // public JSYNC_B(ITree src, ITree dst, MappingStore store, byte[] oldFileData, byte[]
  // newFileData) {
  // super(src, dst, store, new Matcher[3]);
  // initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
  // matchers[0] = initMatcher;
  // matchers[1] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store,
  // initMatcher.visitorSrc, initMatcher.visitorDst, initMatcher.mapFileSrc,
  // initMatcher.mapFileDst);
  // matchers[2] = new LCSOptMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store);
  //
  // }
  // }
  //
  // public static class JSYNC_BCDE extends CompositeMatcher {
  // public JSyncInitMatcher initMatcher;
  //
  // public JSYNC_BCDE(ITree src, ITree dst, MappingStore store, byte[] oldFileData, byte[]
  // newFileData) {
  // super(src, dst, store, new Matcher[4]);
  // initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
  // matchers[0] = initMatcher;
  // matchers[1] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store,
  // initMatcher.visitorSrc, initMatcher.visitorDst, initMatcher.mapFileSrc,
  // initMatcher.mapFileDst);
  // matchers[2] = new LCSOptMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store);
  // matchers[3] = new FineGrainedOptimizationsMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store);
  //
  // }
  // }
  //
  // public static class JSYNC_CDE extends CompositeMatcher {
  // public JSyncInitMatcher initMatcher;
  //
  // public JSYNC_CDE(ITree src, ITree dst, MappingStore store, byte[] oldFileData, byte[]
  // newFileData) {
  // super(src, dst, store, new Matcher[3]);
  // initMatcher = new JSyncInitMatcher(src, dst, store, oldFileData, newFileData);
  // matchers[0] = initMatcher;
  // matchers[1] = new JSyncMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store,
  // initMatcher.visitorSrc, initMatcher.visitorDst, initMatcher.mapFileSrc,
  // initMatcher.mapFileDst);
  // matchers[2] = new FineGrainedOptimizationsMatcher(initMatcher.oldContext.getRoot(),
  // initMatcher.newContext.getRoot(), store);
  //
  // }
  // }

  public static class RTED_A extends CompositeMatcher {
    public RTED_A(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store, new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
          new RtedMatcher(src, dst, store) });
    }
  }

  public static class RTED_AB extends CompositeMatcher {
    public RTED_AB(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store, new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
          new RtedMatcher(src, dst, store), new LCSOptMatcher(src, dst, store)

      });
    }
  }

  public static class RTED_ABCDE extends CompositeMatcher {
    public RTED_ABCDE(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
              new RtedMatcher(src, dst, store), new LCSOptMatcher(src, dst, store),
              new FineGrainedOptimizationsMatcher(src, dst, store)

      });
    }
  }

  public static class RTED_ACDE extends CompositeMatcher {
    public RTED_ACDE(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store, new Matcher[] { new IdenticalSubtreeMatcher(src, dst, store),
          new RtedMatcher(src, dst, store), new FineGrainedOptimizationsMatcher(src, dst, store)

      });
    }
  }

  public static class RTED_B extends CompositeMatcher {
    public RTED_B(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store,
          new Matcher[] { new RtedMatcher(src, dst, store), new LCSOptMatcher(src, dst, store) });
    }
  }

  public static class RTED_BCDE extends CompositeMatcher {
    public RTED_BCDE(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store, new Matcher[] { new RtedMatcher(src, dst, store),
          new LCSOptMatcher(src, dst, store), new FineGrainedOptimizationsMatcher(src, dst, store)

      });
    }
  }

  public static class RTED_CDE extends CompositeMatcher {
    public RTED_CDE(ITree src, ITree dst, MappingStore store) {
      super(src, dst, store, new Matcher[] { new RtedMatcher(src, dst, store),
          new FineGrainedOptimizationsMatcher(src, dst, store) });
    }
  }

}
