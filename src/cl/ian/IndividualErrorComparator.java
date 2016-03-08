package cl.ian;

import cl.ian.gp.HitLevelKozaFitness;
import ec.Individual;
import ec.gp.koza.KozaFitness;

import java.util.Comparator;

/**
 * Created by Ian on 07/03/2016.
 */
public class IndividualErrorComparator implements Comparator<Individual> {
  @Override
  public int compare(Individual x, Individual y) {
    final HitLevelKozaFitness xFitness = (HitLevelKozaFitness) x.fitness;
    final HitLevelKozaFitness yFitness = (HitLevelKozaFitness) y.fitness;
    if (xFitness.errorBetterThan(y.fitness)) {
      return -1;
    }
    if (xFitness.errorAvg > yFitness.errorAvg) {
      return 1;
    }
    return 0;
  }
}