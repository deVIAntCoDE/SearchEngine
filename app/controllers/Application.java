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
  static public EasySearchEngine engine =new EasySearchEngine();
  public static Date before;
  public static Date after;
  public static long time;
    
   @Before
    public static void loaddefault(){
         File indexBank =new File(AppConstants.INDEX_DIR_PATH);
         if(!indexBank.exists()){
             try {
                 EasySearchIndexBuilder.createIndexes(AppConstants.DATA_BANK_DIR_PATH, AppConstants.INDEX_DIR_PATH);
             } catch (Exception e) {
            System.out.println(e.getMessage());
        }
         }
         else{
             System.out.println("Indexes exist!!!!!");
         }
   }

   public static void index() {
       render();
    }
    
	public static void query(String query) {
		System.out.println("params---" + params.allSimple());

		if (query == null)
			index();

		try {
			before = new Date();
			System.out.println("before--------" + before);
			String[] suggestions = engine.getSuggestions(query);

			Result[] results = engine.performSearch(query);

			after = new Date();
			time = after.getTime() - before.getTime();
			if (params._contains("rf")) {
				String s = params.get("rf");
				if (s.equalsIgnoreCase("prf")) {
					results = engine.performPesudoRelevanceFeedback(query);
				} else {
					Map document = null;
					results = engine.performUserRelevanceFeedback(document);
				}
			}

			render("Application/index.html", suggestions, results, time);

		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
	}
}

