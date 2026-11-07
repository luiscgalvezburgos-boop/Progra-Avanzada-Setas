package es.usal.pa;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import smile.classification.DecisionTree;
import smile.data.AbstractTuple;
import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.Row;
import smile.data.formula.Formula;
import smile.data.measure.NominalScale;
import smile.data.type.StructType;
import smile.io.Read;
import smile.validation.ClassificationValidations;
import smile.validation.CrossValidation;

public class Main2 {

	public static void main(String[] args) throws IOException, ParseException, URISyntaxException {
		// TODO Auto-generated method stub
		
		//para saber la ruta en la que tenemos que poner el fichero
		System.out.println(System.getProperty("user.dir"));
		
		//Paso 1. Cargar datos
		//cargamos el juego de datos
		DataFrame dataFrameSetas = Read.arff("mushrooms.arff");
		dataFrameSetas=dataFrameSetas.dropna();
	    
		
		//Paso 2. Realizar Entrenamiento
		//construimos el árbol de decisión
		DecisionTree decisionTree=DecisionTree.fit(Formula.lhs("class"), dataFrameSetas);
		//lambda expression
		//var crossValidation=CrossValidation.classification(10, Formula.lhs("class"), dataFrameSetas, (formula, data) -> DecisionTree.fit(formula, data));

		
		//System.out.println(decisionTree);
		//System.out.println(crossValidation);
		
		
		
		//Paso 3. Realizar predicción.
		
		//predicción de un ejemplo hay que construir la tupla con los valores numéricos de convertir las etiquetas
	    // Works for any smile.classification.Classifier<T>
		//https://qiita.com/tatsunidas/items/bb1f31bfce838d89621e
		
		String valores1[]={"x","s","n","t","p","f","c","n","k","e","e","s","s","w","w","p","w","o","p","k","s","u"};
		int valoresenteros1[]=new int[valores1.length];
		
		
		String temp[];
		for(int i=0;i<valores1.length;i++)
		{
			temp=((NominalScale)dataFrameSetas.column(i).measure()).levels();
			//valores1[i]=((NominalScale)dataFrameSetas.column(i).measure()).
			for(int j=0;j<temp.length;j++)
			{
				if(temp[j].equals(valores1[i]))
					valoresenteros1[i]=j;
			}
			
		}
		
		Tuple tuple1=Tuple.of(dataFrameSetas.schema(), valoresenteros1);
		
		//cómo saber la etiqueta del valor 1 y 0 con el que se clasifica para saber si es e edible comestible o p poisonous venenosa
		System.out.println("Valor 0 es "+((NominalScale)dataFrameSetas.column("class").measure()).levels()[0]);
		System.out.println("Valor 1 es "+((NominalScale)dataFrameSetas.column("class").measure()).level(1));

		
		System.out.println("Categoría: "+decisionTree.predict(tuple1));
		

	}
	

}
