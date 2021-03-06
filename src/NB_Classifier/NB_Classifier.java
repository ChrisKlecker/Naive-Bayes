/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nb_classifier;

/**
 *
 * @author David Klecker
 */

import NB_Classifier.FeatureExtractor;
import NB_Classifier.FeatureExtractor.TokenObjectClass;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NB_Classifier {

    public static int m_TotalNumberOfClasses;
    public static int m_TotalNumberofDocuments;
    public static ArrayList<FeatureExtractor> pFeatures;
        
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

        String Class_Politics_Guns      = "./20_newsgroups_Train/talk.politics.guns";
        String Class_Politics_Mideast   = "./20_newsgroups_Train/talk.politics.mideast";
        String Class_Politics_Misc      = "./20_newsgroups_Train/talk.politics.misc";
        pFeatures                       = new ArrayList<FeatureExtractor>();
        
        TrainClass(Class_Politics_Guns, "Class Politics.Guns");
        
        TrainClass(Class_Politics_Mideast, "Class Politics.Mideast");

        TrainClass(Class_Politics_Misc, "Class Politics.Misc");

        BuildProbabilityOfClass(); //testing       

        BuildProbabilityOfEvidence();
        
        System.out.println("Done!");
    }
            
    public static void TrainClass(String ClassPath, String ClassDescription) throws IOException {
            
        FeatureExtractor pFeatureExtractor          = new FeatureExtractor(ClassPath, ClassDescription);
        pFeatureExtractor.TrainWithDocuments();

        pFeatures.add(pFeatureExtractor);
    }

    public static void BuildProbabilityOfClass(){
        
        pFeatures.forEach((vector) -> {
            double prob = (double)vector.Vector.size() / (double)m_TotalNumberofDocuments;
            vector.ProbabilityOfClass = prob;
        });
    }
        
    public static void BuildProbabilityOfEvidence(){
        HashMap<String, TokenObjectClass> FullTokenMap = new HashMap<String, TokenObjectClass>();
        int TotalNumberOfDocuments = 0;
        
        for(int i=0; i<pFeatures.size(); i++){
            FeatureExtractor feature = pFeatures.get(i);
            TotalNumberOfDocuments += feature.Vector.size();
        
            System.out.println("\nShowing Bags of Words for document " + feature.m_ClassOfDocuments);
            Set set             = feature.FullDocumentBagOfWords.m_TokenMap.entrySet();
            Iterator iterator   = set.iterator();

            while(iterator.hasNext()) {
                Map.Entry mentry    = (Map.Entry)iterator.next();
                TokenObjectClass token = (TokenObjectClass) mentry.getValue();

                if(FullTokenMap.containsKey(token.GetWord())){
                    TokenObjectClass tokenLocal = (TokenObjectClass)FullTokenMap.get(token.GetWord());
                    tokenLocal.SetCount(tokenLocal.GetCount() + token.GetCount());
                }
                else{
                    TokenObjectClass tokenLocal = feature.CreateNewTokenObject((String)mentry.getKey(), token.GetCount());
                    FullTokenMap.put((String)mentry.getKey(), tokenLocal);
                }
                    
                System.out.printf ("Word: %-15s exists in %-4d/%-4d of the documents. PoL:%-5.3f\n", token.GetWord(), token.GetCount(), feature.Vector.size(), token.GetPoL());
            }
        }
        
        HashMap<String, TokenObjectClass> SortedFullTokenMap = FeatureExtractor.sortByValues(FullTokenMap);

        int NumberOfWordsInFullBag = FullTokenMap.size();
        Set set = SortedFullTokenMap.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry mentry    = (Map.Entry)iterator.next();
            TokenObjectClass token = (TokenObjectClass)mentry.getValue();
            double probabilityofevidence = (double)token.GetCount() / (double)TotalNumberOfDocuments;
            token.SetPoE((float)probabilityofevidence);
            
            System.out.printf ("%-15s exists in %-4d/%-4d of the documents. PoE:%-5.3f\n", token.GetWord(), token.GetCount(), TotalNumberOfDocuments, token.GetPoE());
        }
    }   
}
