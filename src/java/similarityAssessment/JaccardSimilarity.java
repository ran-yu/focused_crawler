package similarityAssessment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import entityFilter.Candidates;
import entityFilter.Document;
import entityFilter.Seeds;

public class JaccardSimilarity {
	
	public static double similarity(Document doc_x, Document doc_y ){	
		if (doc_x == null || doc_y == null) return 0;
		return jaccardSimilarity(doc_x, doc_y);
	}
	public static double avgSimilarity(Map<String, Document> x, Document y, Map<String, Double> attritions){
		if(x.size() <= 0) return 0;
		Double res = 0.0;
		for(Entry<String, Document> seedDoc: x.entrySet()){
			//System.out.println(seedDoc.getKey());
			res += attritions.get(seedDoc.getKey()) * similarity(seedDoc.getValue(), y);
			System.out.println("AttritionFactor: "+attritions.get(seedDoc.getKey())+"\tSim: "+similarity(seedDoc.getValue(), y));
		}
		return res;
	}
	public static double avgSeedTermSimilarity(Set<Document> x){
		if(x.size() <= 1) return 0;
		Double res = 0.0;
		for(Document doc1: x){
			for(Document doc2: x){
				if(doc1 == doc2) {res+=1; continue;}
				res += similarity(doc1, doc2);
			}
		}
		return res/( x.size() * x.size() );
	}
	public static double similarity(Set<String> x, Set<String> y){
		if(x.size() <= 0 || y.size() <= 0)return 0;
		int common = 0;
		for(String str1: x){
			if(y.contains(str1)){
				common += 1;
			}
		}
		return common/(x.size() + y.size() - (double)common);
	}
	public static double avgSimilarity(Map<String, Set<String> > x, Set<String> y, Map<String, Double> attritions){
		if(x.size() == 0) return 0;
		double res = 0;
		for(Entry<String, Set<String> > entry: x.entrySet()){
			res += attritions.get(entry.getKey()) * similarity(entry.getValue(), y);
		}
		return res;
	}
	public static double avgSeedPropertySimilarity(Map<String, Set<String> > x){
		if(x.size() <= 1) return 0;
		Double res = 0.0;
		for(Entry<String, Set<String> > entry1: x.entrySet()){
			for(Entry<String, Set<String> > entry2 : x.entrySet()){
				if(entry1 == entry2){res+=1; continue;}
				res += similarity(entry1.getValue(), entry2.getValue());
			}
		}
		return res/( x.size() * x.size() );
	}
	public static double jaccardSimilarity(Document doc1, Document doc2) {
		String word;
		double similarity = 0;
		for (Iterator<String> it = doc1.words.keySet().iterator(); it.hasNext(); ) {
			word = it.next();
			if (doc2.words.containsKey(word)) {
				similarity += min(doc1.words.get(word)[0], doc2.words.get(word)[0]);
			}
		}
		if( (doc1.sumof_n_kj + doc2.sumof_n_kj - similarity) == 0){
			similarity = 0;
		}
		else {
			similarity = similarity / (doc1.sumof_n_kj + doc2.sumof_n_kj - similarity);
		}
		return similarity;
	}
	private static double min(Double x, Double y) {
		return x >= y ? y : x;
	}
}
