package energyDemandEstimation;

import energyDemandEstimation.data.*;
import energyDemandEstimation.misc.RandomManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import energyDemandEstimation.ELM.elm;
import no.uib.cipr.matrix.NotConvergedException;

public class EnergyDemandEstimation {

	final static int nIterations = 100;

	public static void main(String[] args) throws NotConvergedException {

		RandomManager.setSeed(1234);

		Data data = new Data();
		Constructive constructive;
		elm elm;
		Solution sol = null;
		Solution bestSol;
		double bestAccuracy;
		GRASP grasp = new GRASP(data);
		int[] mostUsedVars = new int[14];

		System.out.println("-----Random Constructive-----");
		constructive = new CRandom();

		bestSol = null;
		bestAccuracy = 0;

		/* CSV */
		PrintWriter pw;
		boolean[] varsAux;
		String vars;
		try {
			pw = new PrintWriter(new File("var-error-CRandom.csv"));
			StringBuilder sb = new StringBuilder();
			sb.append("Variables");
			sb.append(";");
			sb.append("Accuracy");
			sb.append('\n');

			for (int i = 0; i < nIterations; i++) {
				sol = constructive.generateSolution();

				// Se prueba la posible solución
				elm = new elm(0, 20, "sig");
				double[][] trainData = data.getTrainData(sol.getSelectedVars());
				elm.train(trainData);
				
				
				varsAux = sol.getSelectedVars();
				vars = "[";
				for (int j = 0; j < varsAux.length; j++) {
					if (varsAux[j])
						vars = vars + " " + j;
				}
				vars = vars + " ]";
				sb.append(vars);
				sb.append(';');
				sb.append(elm.getTrainingAccuracy());
				sb.append('\n');

				if (elm.getTrainingAccuracy() > bestAccuracy) { // Si es mejor se guarda
					bestAccuracy = elm.getTrainingAccuracy();
					bestSol = sol;
					for (int j = 0; j < mostUsedVars.length; j++) {
						if (sol.getSelectedVars()[j])
							mostUsedVars[j]++;
					}
				}
			}

			pw.write(sb.toString());
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		/* CSV */

		System.out.println("La mejor ejecución del train ha tenido una accuracy de " + bestAccuracy);

		bestSol = grasp.improve(bestSol, mostUsedVars);

		elm = new elm(0, 20, "sig");
		elm.train(data.getTrainData(bestSol.getSelectedVars()));
		bestAccuracy = elm.getTrainingAccuracy();
		System.out.println("Despues del improve ha sido " + bestAccuracy);

		System.out.println("\n-----Votos Constructive-----");
		constructive = new CVotos(nIterations);

		bestSol = null;
		bestAccuracy = 0;

		for (int i = 0; i < nIterations; i++) {
			sol = constructive.generateSolution();

			// Se prueba la posible solución
			elm = new elm(0, 20, "sig");
			double[][] trainData = data.getTrainData(sol.getSelectedVars());
			elm.train(trainData);

			if (elm.getTrainingAccuracy() > bestAccuracy) { // Si es mejor se guarda
				bestAccuracy = elm.getTrainingAccuracy();
				bestSol = sol;
				for (int j = 0; j < mostUsedVars.length; j++) {
					if (sol.getSelectedVars()[j])
						mostUsedVars[j]++;
				}
			}
		}

		System.out.println("La mejor ejecución del train ha tenido una accuracy de " + bestAccuracy);

		sol = grasp.improve(sol, mostUsedVars);

		elm = new elm(0, 20, "sig");
		elm.train(data.getTrainData(sol.getSelectedVars()));
		bestAccuracy = elm.getTrainingAccuracy();
		System.out.println("Despues del improve ha sido " + bestAccuracy);

	}

}