package controllers;

import engine.easy.search.*;
import engine.easy.indexer.*;
import engine.easy.util.*;
import engine.easy.analyzer.*;
import engine.easy.ranking_model.BM25;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    static public EasySearchEngine engine = new EasySearchEngine();
    public static Date before;
    public static Date after;
    public static long time;
    
    @Before
    public static void loaddefault() {
        File indexBank = new File(AppConstants.INDEX_DIR_PATH);
        if (!indexBank.exists()) {
            try {
                EasySearchIndexBuilder.createIndexes(AppConstants.DATA_BANK_DIR_PATH, AppConstants.INDEX_DIR_PATH);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Indexes exist!!!!!");
        }
    }
    
    public static void index() {
        render();
    }
    
    public static void query(String query) {
        System.out.println("params---" + params.allSimple());
        
        if (query == null) {
            index();
        }
        Result[] results=null;
        try {
            before = new Date();
            System.out.println("before--------" + before);
            String[] suggestions = engine.getSuggestions(query);
           
            if (params._contains("rf")) {
                results = engine.performPesudoRelevanceFeedback(query);
            }else{
                
            results = engine.performSearch(query); 
            }
            after = new Date();
            time = after.getTime() - before.getTime();
            System.out.println("query time=============="+time);
            render("Application/index.html", suggestions, results, time);
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        
       
    }
    
    
    
    public static void userRelevanceFeedback(String ids) {
        System.out.println("params:" + params.allSimple());
        System.out.println("user relevance-----");
        
        if (ids == null || ids.isEmpty()) {
            index();
        }
        before=new Date();
        System.out.println("ids======" + ids);
        
        Map<Integer, Float> docIds = new HashMap<Integer, Float>();
        String[] Ids = ids.split(",");
        for (String id : Ids) {
            Integer ID=Math.abs(Integer.parseInt(id));
            float score =Integer.parseInt(id)/AppConstants.RESULTS_SIZE;
            
            if (!docIds.containsKey(ID)) {
                docIds.put(ID, score);
            }
            else{
                //score =docIds.get(ID)+score;
                docIds.put(ID, (docIds.get(ID)+score));
            }
            
        }
        Result[] results = engine.performUserRelevanceFeedback(docIds);
        after=new Date();
        time=after.getTime()- before.getTime();
        render("Application/index.html", results, time);
        
    }
    //
//<ul>
//    #{list items:results, as:'result' }
//        <li>${result}</li>
//    #{/list}
//</ul>
}
