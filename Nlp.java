package test;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.hcoref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;
public class Nlp {
	public static void main(String[] args) throws IOException {
		        String text = "斯坦福大学坐落在美国,它很大.小明吃棒冰,他很胖.";
		        args = new String[] {"-props", "edu/stanford/nlp/hcoref/properties/zh-coref-default.properties"};
		        //ssplit.boundaryTokenRegex=[.]|[!?]+|[。]|[！？]+
		        
		        Annotation document = new Annotation(text);
		        Properties props = StringUtils.argsToProperties(args);
		        props.replace("ssplit.boundaryTokenRegex", "[.]|[!?]+|[。]|[！？]+", "[.,]|[!?]+|[。，]|[！？]+");
		        System.out.println(args);
		        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		        System.out.println(props);
		        for (int i=0;i<10;i++){
		        	long startTime=System.currentTimeMillis();
			        pipeline.annotate(document);
//			        pipeline.prettyPrint(document, System.out);
	
			        for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
			            System.out.println("\t" + cc);
			        }
	
			        long endTime=System.currentTimeMillis(); 
			        long time = (endTime-startTime);
			        System.out.println(time);
		        }
	}
}
