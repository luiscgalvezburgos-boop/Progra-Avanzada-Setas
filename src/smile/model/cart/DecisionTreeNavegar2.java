package smile.model.cart;

import smile.classification.DecisionTree;
import smile.data.measure.NominalScale;
import smile.data.type.StructType;

/**
 * Aparentemente funciona correctamente con las pruebas básicas que he realizado
 */
public class DecisionTreeNavegar2 
{
	protected DecisionTree decisionTree;
	protected StructType structType;
	protected Node node;
	
	public DecisionTreeNavegar2(DecisionTree decisionTree)
	{
		this.decisionTree=decisionTree;
		structType=decisionTree.schema();
		node=decisionTree.root();
	}
	
	public String condicionActual()
	{
		if(node instanceof NominalNode)
			return ((NominalNode)node).toString(structType, true);
	
		if(node instanceof OrdinalNode)
			return ((OrdinalNode)node).toString(structType, true);
		
		
		return null;
	}
	
	public boolean navegarNodo(boolean lado)
	{
		if(!esNodoDecision())
			node=lado?((InternalNode)node).trueChild():((InternalNode)node).falseChild();
		
		return esNodoDecision();
	}
	
	public boolean esNodoDecision()
	{
		if(node instanceof DecisionNode)
			return true;
		
		return false;
	}
	
	public String getClasificacion(String clase)
	{
		if(esNodoDecision())
		{
			DecisionNode decisionNode=(DecisionNode)node;
			
			//System.out.println(decisionTree.response.measure());
			return ((NominalScale)decisionTree.response.measure()).level(decisionNode.output());
		}
		
		return null;
	}
}
