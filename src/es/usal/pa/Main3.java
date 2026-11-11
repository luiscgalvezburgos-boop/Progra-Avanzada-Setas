package es.usal.pa;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Scanner;

import smile.classification.DecisionTree;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.measure.NominalScale;
import smile.data.type.StructType;
import smile.io.Read;
import smile.model.cart.InternalNode;
import smile.model.cart.Node;
import smile.model.cart.NominalNode;
import smile.model.cart.OrdinalNode;
import smile.model.cart.SplitRule;
import smile.model.cart.DecisionNode;
import smile.model.cart.DecisionTreeNavegar2;
import smile.classification.DecisionTree.Options;

/**
 * Ejemplo que funciona correctamente.
 */
public class Main3 {

	public static void main(String[] args) throws IOException, ParseException, URISyntaxException {
		// TODO Auto-generated method stub
		
		System.out.println(System.getProperty("user.dir"));
		
		DataFrame dataFrame = Read.arff("mushrooms.arff");		
		//DataFrame x = dataFrame.drop("class");
		//DataFrame y = dataFrame.select("class");
		dataFrame=dataFrame.dropna();
		Options options=new Options(SplitRule.ENTROPY, Integer.MAX_VALUE, Integer.MAX_VALUE, 1);
	    
		
		//DecisionTree decisionTree=DecisionTree.fit(Formula.lhs("class"), dataFrameIris);
		DecisionTree decisionTree=DecisionTree.fit(Formula.of("class~."), dataFrame, options);
		
		
		
		System.out.println(decisionTree);
		
		DecisionTreeNavegar2 decisionTreeNavegar2=new DecisionTreeNavegar2(decisionTree);
		Scanner scanner=new Scanner(System.in);
		String temp;
		while(!decisionTreeNavegar2.esNodoDecision())
		{
			System.out.println("Responde (s/n): "+decisionTreeNavegar2.condicionActual());
			temp=scanner.nextLine();
			if(temp.toLowerCase().equals("s"))
				decisionTreeNavegar2.navegarNodo(true);
			else
				decisionTreeNavegar2.navegarNodo(false);
		}
		
		System.out.println("Clasificación "+decisionTreeNavegar2.getClasificacion("class"));
		
	}

}
