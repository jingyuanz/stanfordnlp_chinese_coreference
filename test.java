import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.lang.System;

import edu.stanford.nlp.dcoref.Dictionaries;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.io.EncodingPrintWriter.out;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;
import edu.stanford.nlp.util.IntTuple;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.Triple;

public class test {
	public static void main(String[] args) throws Exception {
//		String out = "qa1_test_combined.txt";
		String out = "aa.txt";
//		String fileName = "train_corpus.txt";
		String fileName = "test_corpus.txt";
		if (args.length > 0) {
			fileName = args[0];
		}
		if (args.length > 1) {
			out = args[1];
		}
		args = new String[] { "-props", "edu/stanford/nlp/hcoref/properties/zh-coref-default.properties" };
		Properties props = StringUtils.argsToProperties(args);

		String encoding = "utf-8";

		File file = new File(fileName);
		InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
		BufferedReader bufferedReader = new BufferedReader(read);
		// String prev = bufferedReader.readLine();
		String line = null;
		int lineNum = 0;
		props.replace("ssplit.boundaryTokenRegex", "[.]|[!?]+|[。]|[！？]+", "[.,]|[!?]+|[。，]|[！？]+");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//		FileWriter fw = new FileWriter("dataset.txt", true);
		FileWriter fw = new FileWriter(out, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter outfile = new PrintWriter(bw);
		int num = 0;
		while ((line = bufferedReader.readLine()) != null) {
			lineNum++;
			
			System.out.println(lineNum);
//			String txt = "中资银行纷纷在纽约、伦敦、巴黎等金融中心建立分行，而外资银行能提供的服务和产品范围受到限制，这意味着它们的产品和服务相比中资银行越来越无优势可言。";
			String txt = line.trim();
			txt = txt.replaceAll("\\s+", "");
			if (txt.length()<=100){
				Annotation document = new Annotation(txt);
				try {
					pipeline.annotate(document);
					
					
	
					
					if (document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values().size() > 0) {
						Map<String,List<String>> ne_dict = new Hashtable<String,List<String>>();
						List<String> sentencesToWrite = new ArrayList<String>();
						List<List<Tripple>> repr_sentencesToWrite = new ArrayList<List<Tripple>>();
						int max_ne = 0;
						int max_nn = 0;
						Boolean is_before = true;
						List<CoreMap> sentences = document.get(SentencesAnnotation.class);
						for (CoreMap sentence : sentences) {
							List<String> origin = new ArrayList<String>();
							List<Tripple> repr_sent = new ArrayList<Tripple>();
							// traversing the words in the current sentence
							// a CoreLabel is a CoreMap with additional token-specific
							// methods
							String prev_ne = null;
							String prev_pos = null;
							String prev_word = null;
							int count = 0;
							for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
								
								// this is the text of the token
								String word = token.get(TextAnnotation.class);
								// this is the POS tag of the token
								String pos = token.get(PartOfSpeechAnnotation.class);
								// this is the NER label of the token
								String ne = token.get(NamedEntityTagAnnotation.class);
								origin.add(pos+'/'+ne+'/'+word);						
								
								if (!ne.equals("O")){
									if (!ne.equals(prev_ne)){
										if (is_before){
											int value = 0;
											if (!ne_dict.containsKey(ne)){
												List<String> new_l = new ArrayList<String>();
												new_l.add(word);
												ne_dict.put(ne, new_l);
											}else{
												if (ne_dict.get(ne).contains(word)){
													ne_dict.get(ne).remove(word);
													ne_dict.get(ne).add(word);
												}else{
													ne_dict.get(ne).add(word);
												}
											}
											value = ne_dict.get(ne).indexOf(word);
											repr_sent.add(new Tripple(ne,value,true,count));
										}else{
											
											repr_sent.add(new Tripple(ne,-1,true,count));
										}
									}else{
										if (is_before){
											ne_dict.get(ne).remove(prev_word);
											ne_dict.get(ne).add(word);
											repr_sent.get(repr_sent.size()-1).position ++;
										}
									}
									
								}else{
									if (!pos.equals(prev_pos)||!prev_ne.equals("O")){
										
										if (!pos.equals("NN") || !is_before){
											if (pos.equals("PN")){
												is_before = false;
											}
	
											repr_sent.add(new Tripple(pos,-1,false,count));
										}else{
											if (!ne_dict.containsKey("NN")){
												List<String> new_l = new ArrayList<String>();
												new_l.add(word);
												ne_dict.put("NN", new_l);
											}else{
												if (ne_dict.get("NN").contains(word)){
													ne_dict.get("NN").remove(word);
													ne_dict.get("NN").add(word);
												}else{
													ne_dict.get("NN").add(word);
												}
											}
											int nn_index = ne_dict.get("NN").indexOf(word);
											repr_sent.add(new Tripple(pos,nn_index,false,count));
										}
											
									}else{
										if (is_before && pos.equals(prev_pos) && pos.equals("NN")){
											ne_dict.get("NN").remove(prev_word);
											ne_dict.get("NN").add(word);
											repr_sent.get(repr_sent.size()-1).position ++;
										}
									}
								}
								prev_ne = ne;
								prev_pos = pos;
								prev_word = word;
								count ++;
							}
							System.out.println(origin);
							repr_sent.remove(repr_sent.size()-1);
							
							repr_sentencesToWrite.add(repr_sent);
	//						
							
	//						String joint_sent = String.join(" ", sent);
	//						joint_sent = joint_sent + ".";
	//						sentencesToWrite.add(joint_sent);
	
						}
						for (List<Tripple> repr_sent : repr_sentencesToWrite){
							List<String> sent = new ArrayList<String>();
							for (Tripple tup : repr_sent){
								String repr = null;
								if (tup.index>-1){
									if (tup.is_ne){
										repr = tup.word + (ne_dict.get(tup.word).size()-tup.index);
										
									}else{
										repr = tup.word + (ne_dict.get("NN").size()-tup.index);
									}
								}else{
									repr = tup.word;
								}
								tup.word = repr;
								sent.add(repr);
			
							}
							String joint_sent = String.join(" ", sent);
							joint_sent = joint_sent + ".";
							sentencesToWrite.add(joint_sent);
							System.out.println(joint_sent);
						}
						Tuple<CorefChain.CorefMention,CorefChain.CorefMention> chain = new Tuple();
						for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
	//						Map<IntPair, Set<CorefChain.CorefMention>> map = cc.getMentionMap();
							System.out.println(cc);
	//						List<Tuple<CorefChain.CorefMention,CorefChain.CorefMention>> chains = new ArrayList<Tuple<CorefChain.CorefMention,CorefChain.CorefMention>>();
							if (chain.pro==null || chain.no==null){
								chain = new Tuple();
							}
							for (CorefChain.CorefMention mention : cc.getMentionsInTextualOrder()){
								if (mention.mentionType.equals(edu.stanford.nlp.hcoref.data.Dictionaries.MentionType.PRONOMINAL)){
									if (chain.pro==null) {
										chain.pro = mention;
									}
								}
								else{
									if (chain.no==null) {
										chain.no = mention;
									}
									
								}
							}
						}
						if (chain.no!=null && chain.pro!=null && sentencesToWrite.size()<=10){
							
							
//							for (CorefChain.CorefMention pro : pros){
							if (chain.pro.position.get(0)>=chain.no.position.get(0)){
								num++;
								System.out.println("found"+num);
								System.out.println(chain.pro.mentionSpan+"---->"+chain.no.mentionSpan);
								List<Tripple> repr_pro_sent = repr_sentencesToWrite.get(chain.pro.position.get(0)-1);
								String repr_pro = null;
								for (Tripple tripple : repr_pro_sent){
									if (tripple.position==chain.pro.startIndex-1){
										repr_pro = tripple.word;
									}
								}
								List<Tripple> repr_no_sent = repr_sentencesToWrite.get(chain.no.position.get(0)-1);
								String repr_no = null;
								for (Tripple tripple : repr_no_sent){

									if (tripple.position==chain.no.endIndex-2){
										repr_no = tripple.word;
									}
								}
								
								if (repr_no==null || repr_pro==null){
									System.out.println("Wrong!!!");
								}else{
									int i = 1;
									for (String s : sentencesToWrite){
										outfile.println(i + " " + s);
										i ++;
									}
									System.out.println(repr_pro+" "+repr_no);
									outfile.print(i+" "+repr_pro+" ->?");
									outfile.print("\t"+repr_no+"\t"+chain.pro.position.get(0)+" "+chain.no.position.get(0));
									outfile.println();
									i ++;
								}
							}
							
	
						}
	
					}
				} catch (Exception e) {
					System.out.println(e);
					
				}
			}else{
				System.out.println("line too long");
			}
		}
		outfile.close();

//		CSVUtil.createCSVFile(header, dataset, out, "data");
	}
}

class Tripple {
	String word;
	int index;
	Boolean is_ne;
	int position;
	public Tripple(String word, int index, Boolean is_ne, int position){
		this.word = word;
		this.index = index;
		this.is_ne = is_ne;
		this.position = position;
	}
}

class Tuple<A,B> {
	A pro;
	B no;
	public Tuple(){
		this.pro = null;
		this.no = null;
	}
	public Tuple(A a, B b){
		this.pro = a;
		this.no = b;
	}
}

