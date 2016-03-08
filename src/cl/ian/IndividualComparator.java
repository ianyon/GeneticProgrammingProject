package cl.ian;

import ec.Individual;
import ec.gp.koza.KozaFitness;

import java.util.Comparator;

/**
 * Created by Ian on 07/03/2016.
 */
public class IndividualComparator<I> implements Comparator<Individual> {
  @Override
  public int compare(Individual x, Individual y) {
    if (x.fitness.betterThan(y.fitness)) {
      return -1;
    }
    if (((KozaFitness) x.fitness).standardizedFitness() > ((KozaFitness) y.fitness).standardizedFitness()) {
      return 1;
    }
    return 0;
  }
}