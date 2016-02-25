/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package cl.ian.gp;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.gp.koza.MutationPipeline;
import ec.util.Parameter;

/* 
 * RetriesMutationPipeline.java
 */

/**
 * MutationPipeline is a GPBreedingPipeline which
 * implements a strongly-typed version of the
 * "Point Mutation" operator as described in Koza I.
 * Actually, that's not quite true.  Koza doesn't have any tree depth restrictions
 * on his mutation operator.  This one does -- if the tree gets deeper than
 * the maximum tree depth, then the new subtree is rejected and another one is
 * tried.  Similar to how the Crosssover operator is implemented.
 * <p>
 * <p>Mutated trees are restricted to being <tt>maxdepth</tt> depth at
 * most and at most <tt>maxsize</tt> number of nodes.  If in
 * <tt>tries</tt> attemptes, the pipeline cannot come up with a
 * mutated tree within the depth limit, then it simply copies the
 * original individual wholesale with no mutation.
 * <p>
 * <p>One additional feature: if <tt>equal</tt> is true, then MutationPipeline
 * will attempt to replace the subtree with a tree of approximately equal size.
 * How this is done exactly, and how close it is, is entirely up to the pipeline's
 * tree builder -- for example, Grow/Full/HalfBuilder don't support this at all, while
 * RandomBranch will replace it with a tree of the same size or "slightly smaller"
 * as described in the algorithm.
 * <p>
 * <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 * ...as many as the child produces
 * <p>
 * <p><b>Number of Sources</b><br>
 * 1
 * <p>
 * <p><b>Parameters</b><br>
 * <table>
 * <tr><td valign=top><i>base</i>.<tt>tries</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(number of times to try finding valid pairs of nodes)</td></tr>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>maxdepth</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(maximum valid depth of a crossed-over subtree)</td></tr>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>maxsize</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(maximum valid size, in nodes, of a crossed-over subtree)</td></tr>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>ns</tt><br>
 * <font size=-1>classname, inherits and != GPNodeSelector</font></td>
 * <td valign=top>(GPNodeSelector for tree)</td></tr>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>build</tt>.0<br>
 * <font size=-1>classname, inherits and != GPNodeBuilder</font></td>
 * <td valign=top>(GPNodeBuilder for new subtree)</td></tr>
 * <p>
 * <tr><td valign=top><tt>equal</tt><br>
 * <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</td>
 * <td valign=top>(do we attempt to replace the subtree with a new one of roughly the same size?)</td></tr>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>tree.0</tt><br>
 * <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 * <td valign=top>(tree chosen for mutation; if parameter doesn't exist, tree is picked at random)</td></tr>
 * <p>
 * </table>
 * <p>
 * <p><b>Default Base</b><br>
 * gp.koza.mutate
 * <p>
 * <p><b>Parameter bases</b><br>
 * <table>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>ns</tt><br>
 * <td>nodeselect</td></tr>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>build</tt><br>
 * <td>builder</td></tr>
 * <p>
 * </table>
 *
 * @author Sean Luke
 * @version 1.0
 */

public class RetriesMutationPipeline extends MutationPipeline {
  private static final long serialVersionUID = 1;

  /**
   * The number of times the pipeline tries to build a valid mutated
   * tree before it gives up and just passes on the original
   */
  int numTries;

  /**
   * The maximum depth of a mutated tree
   */
  int maxDepth;

  /**
   * The largest tree (measured as a nodecount) the pipeline is allowed to form.
   */
  public int maxSize;

  /**
   * Do we try to replace the subtree with another of the same size?
   */
  boolean equalSize;

  /**
   * Is our tree fixed?  If not, this is -1
   */
  int tree;

  public void setup(final EvolutionState state, final Parameter base) {
    super.setup(state, base);

    Parameter def = defaultBase();

    numTries = state.parameters.getInt(
        base.push(P_NUM_TRIES), def.push(P_NUM_TRIES), 1);
    if (numTries == 0)
      state.output.fatal("Mutation Pipeline has an invalid number of tries (it must be >= 1).", base.push(P_NUM_TRIES), def.push(P_NUM_TRIES));

    maxDepth = state.parameters.getInt(
        base.push(P_MAXDEPTH), def.push(P_MAXDEPTH), 1);
    if (maxDepth == 0)
      state.output.fatal("The Mutation Pipeline " + base + "has an invalid maximum depth (it must be >= 1).", base.push(P_MAXDEPTH), def.push(P_MAXDEPTH));

    maxSize = NO_SIZE_LIMIT;
    if (state.parameters.exists(base.push(P_MAXSIZE), def.push(P_MAXSIZE))) {
      maxSize = state.parameters.getInt(base.push(P_MAXSIZE), def.push(P_MAXSIZE), 1);
      if (maxSize < 1)
        state.output.fatal("Maximum tree size, if defined, must be >= 1");
    }

    equalSize = state.parameters.getBoolean(
        base.push(P_EQUALSIZE), def.push(P_EQUALSIZE), false);

    tree = TREE_UNFIXED;
    if (state.parameters.exists(base.push(P_TREE).push("" + 0),
        def.push(P_TREE).push("" + 0))) {
      tree = state.parameters.getInt(base.push(P_TREE).push("" + 0),
          def.push(P_TREE).push("" + 0), 0);
      if (tree == -1)
        state.output.fatal("Tree fixed value, if defined, must be >= 0");
    }
  }

  /**
   * Returns true if inner1 can feasibly be swapped into inner2's position
   */
  public boolean verifyPoints(GPNode inner1, GPNode inner2) {
    return super.verifyPoints(inner1, inner2) || !inner1.rootedTreeEquals(inner2);
  }
}
