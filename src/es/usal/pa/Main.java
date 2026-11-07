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

public class Main {

	public static void main(String[] args) throws IOException, ParseException, URISyntaxException {
		// TODO Auto-generated method stub
		
		//para saber la ruta en la que tenemos que poner el fichero
		System.out.println(System.getProperty("user.dir"));
		
		//cargamos el juego de datos
		DataFrame dataFrameSetas = Read.arff("mushrooms.arff");
		dataFrameSetas=dataFrameSetas.dropna();
	    
		//construimos el árbol de decisión
		DecisionTree decisionTree=DecisionTree.fit(Formula.lhs("class"), dataFrameSetas);
		//sería equivalente a poner la fórmula donde a la izquierda del ~ se pone la variable
		//buscada y al derecha las variables independientes
		//DecisionTree decisionTree=DecisionTree.fit(Formula.of("class~."), dataFrameSetas);
		//En lugar de poner un . podemos poner literalmente todas las variables.
		/*DecisionTree decisionTree=DecisionTree.fit(Formula.of("""		
		class ~ cap-shape + cap-surface + cap-color + bruises? + odor +
        gill-attachment + gill-spacing + gill-size + gill-color +
        stalk-shape + stalk-root + stalk-surface-above-ring +
        stalk-surface-below-ring + stalk-color-above-ring +
        stalk-color-below-ring + veil-type + veil-color +
        ring-number + ring-type + spore-print-color +
        population + habitat
		"""),  dataFrameSetas);
		*/
		
		
		//lambda expression
		var crossValidation=CrossValidation.classification(10, Formula.lhs("class"), dataFrameSetas, (formula, data) -> DecisionTree.fit(formula, data));

		//https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/function/package-summary.html
		//https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/function/BiFunction.html
		//This is a functional interface and can therefore be used as the assignment target for a lambda expression or method reference.
		//Represents a function that accepts two arguments and produces a result.
		ClassificationValidations<DecisionTree> classificationValidations=CrossValidation.classification(10, Formula.lhs("class"), dataFrameSetas, new BiFunction<Formula, DataFrame, DecisionTree>() {
	        @Override
	        public DecisionTree apply(Formula formula, DataFrame data) {
	            return DecisionTree.fit(formula, data);
	        }
	    });
		
		System.out.println(decisionTree);
		System.out.println(crossValidation);
		System.out.println(classificationValidations);
		
		
		//predicción de todo el dataset
		System.out.println(Arrays.toString(decisionTree.predict(dataFrameSetas)));
		
		StructType structType=decisionTree.schema();
		
		//cómo saber la etiqueta del valor 1 y 0 con el que se clasifica para saber si es e edible comestible o p poisonous venenosa
		System.out.println("Valor 0 es "+((NominalScale)dataFrameSetas.column("class").measure()).levels()[0]);
		System.out.println("Valor 1 es "+((NominalScale)dataFrameSetas.column("class").measure()).level(1));
		
		
		
		//predicción de un ejemplo hay que construir la tupla con los valores numéricos de convertir las etiquetas
	    // Works for any smile.classification.Classifier<T>
		//https://qiita.com/tatsunidas/items/bb1f31bfce838d89621e
		
		String valores1[]={"x","s","n","t","p","f","c","n","k","e","e","s","s","w","w","p","w","o","p","k","s","u"};
		String valores2[]={"x","s","y","t","a","f","c","b","k","e","c","s","s","w","w","p","w","o","p","n","n","g"};
		int valoresenteros1[]=new int[valores1.length];
		int valoresenteros2[]=new int[valores2.length];
		
		//estas tuplas no son válidas porque como valores se pasan las etiquetas no los valores numéricos, si se usan daráin error
		Tuple tuple1=Tuple.of(dataFrameSetas.schema(), valores1);
		Tuple tuple2=Tuple.of(dataFrameSetas.schema(), valores2);
		
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
		
		tuple1=Tuple.of(dataFrameSetas.schema(), valoresenteros1);
		
		for(int i=0;i<valores2.length;i++)
		{
			temp=((NominalScale)dataFrameSetas.column(i).measure()).levels();
			//valores1[i]=((NominalScale)dataFrameSetas.column(i).measure()).
			for(int j=0;j<temp.length;j++)
			{
				if(temp[j].equals(valores2[i]))
					valoresenteros2[i]=j;
			}
			
		}
		tuple2=Tuple.of(dataFrameSetas.schema(), valoresenteros2);
		
		
		//ejempolo de clasificación de dos tuplas del datasets qeu hemos usado para entrenar 
		Tuple fila1=new Row(dataFrameSetas, 0);
		Tuple fila2=new Row(dataFrameSetas, 1);
		
		
		System.out.println("Categoría: "+decisionTree.predict(tuple1));
		System.out.println("Categoría: "+decisionTree.predict(tuple2));
		
		System.out.println("Categoría: "+decisionTree.predict(fila1));
		System.out.println("Categoría: "+decisionTree.predict(fila2));

	}
	

}
