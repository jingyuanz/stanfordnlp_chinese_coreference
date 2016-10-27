import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.PropertiesUtils;
public class Nlp {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
//	    String props="test";
//	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		StanfordCoreNLP pipeline = new StanfordCoreNLP(
				PropertiesUtils.asProperties(
					"annotators", "segment, ssplit, pos, ner, parse, mention, coref",
					"customAnnotatorClass.segment", "edu.stanford.nlp.pipeline.ChineseSegmenterAnnotator",
					"segment.model","edu/stanford/nlp/models/segmenter/chinese/ctb.gz",
					"segment.sighanCorporaDict","edu/stanford/nlp/models/segmenter/chinese",
					"segment.serDictionary","edu/stanford/nlp/models/segmenter/chinese/dict-chris6.ser.gz",
					"segment.sighanPostProcessing","true",
					"ssplit.boundaryTokenRegex","[.]|[!?]+|[。]|[！？]+",
					"pos.model","edu/stanford/nlp/models/pos-tagger/chinese-distsim/chinese-distsim.tagger",
					"ner.model","edu/stanford/nlp/models/ner/chinese.misc.distsim.crf.ser.gz",
					"ner.applyNumericClassifiers","false",
					"ner.useSUTime","false",
					"parse.originalDependencies","true",
					"parse.model", "edu/stanford/nlp/models/lexparser/chineseFactored.ser.gz"
					));
	    Annotation annotation;
	    //if  data from file
	    File f = new File("/home/jingyuanz/Desktop/work/new/src/input.txt");
	    BufferedReader reader = null;
	    reader = new BufferedReader(new FileReader(f));
	    String tempString = reader.readLine();
	    String next = null;
	    long sta=System.currentTimeMillis();
	    int i = 0;
	    while ((next = reader.readLine()) != null) {
	    	String text = tempString + next;
	    	tempString = next;
	    	annotation = new Annotation(text);
	    	long starTime=System.currentTimeMillis();
	    	pipeline.annotate(annotation);
	    	long endTime=System.currentTimeMillis();
	    	i++;
	    	pipeline.prettyPrint(annotation, System.out);
	    	System.out.println(i);
	    	System.out.println("each time");
	    	System.out.println(endTime - starTime);
    	}
	    long end=System.currentTimeMillis();
	    System.out.println(sta - end);
	    
//	     annotation = new Annotation("这家酒店很好。它很漂亮，我很喜欢。");
	    
	    
	    
	    
//	    pipeline.prettyPrint(annotation, System.out);
	}
}
