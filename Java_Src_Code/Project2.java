import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;


public class Project2 {
	
	
	public static HashMap<String,Vector<Integer>> hPosList=null;
	public static int number_of_terms=0;
	static Vector<Integer> IntersectionArray ;
	static Vector<Integer> unionArray;
	static int MAX_LANGUAGES=12;
	 //Document doc = null;   
	 static IndexReader reader;
	 //static PrintWriter writer;
	 static Integer index_of_term_with_smallest_list_size;
	 static String[] languages = {"text_nl", "text_fr", "text_de", "text_ja", "text_ru", "text_pt", "text_es",
			 "text_es", "text_it", "text_da", "text_no", "text_sv"};
	 static Vector<String>  Term_List ;
	 static Vector<Boolean> IfAlreadyProcessed;
	 static Vector<Integer>[] Term_Posting_List;
	 static Integer[] Term_Pointers;
	 static int number_of_docs=  50000; //reader.maxDoc();
	 static Writer output;
	 static BufferedReader input;
	 static int intersection_comparisons;
	 static int union_comparisons;
	 
	 
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {

	 int i=0;
	 //String x = "D:\\1st Semester\\Information-retrieval\\Projects\\Project2\\index\\index";
	 //String y= "D:\\1st Semester\\Information-retrieval\\Projects\\Project2\\input.txt";
	 //String z= "D:\\1st Semester\\Information-retrieval\\Projects\\Project2\\outfilename.txt";
	 String path = args[0];
	 //path = URLEncoder.encode(path, "UTF-8"); 
	 FileSystem fs = FileSystems.getDefault();
	 Path path1 = fs.getPath(path);
	 reader = DirectoryReader.open(FSDirectory.open(path1));
	 //writer = new PrintWriter("D://1st Semester//Information-retrieval//Projects//Project2//the-file-name.txt", "UTF-8");
	 //System.out.println("maximum number of docs = "+reader.maxDoc());

	 input = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]),"UTF-8"));
	 output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[2]), "UTF-8"));

	 
	 	//Initialize all variables 

		//Make hashmap Table
	 	hPosList = new HashMap<String,Vector<Integer>>();
		 MakehashTable();

		 String line;
		 line = input.readLine();
		 
		 
		

	     while (line != null)
 	     {
try{
	    	//System.out.println("New Series of terms Started");
	 	 	IntersectionArray = new Vector<Integer>();
		 	unionArray = new Vector<Integer>();
			Term_List =new Vector<String>();
			IfAlreadyProcessed =new Vector<Boolean>();
			 
	     	//System.out.println("line  " + line);
	     	String[] words = line.split(" ");
	     	
	     	//add all terms read from file to Terms_List Array
	     	for(i=0;i<words.length;i++)
	     	{
	     		Term_List.add(words[i]);
	     		//System.out.println(words[i]);
	     	}
	     	
	     	line = input.readLine();


		 for (i=0;i<(Term_List.size());i++) {
			 IfAlreadyProcessed.add(false);
		 }

		 //Term_Posting_List array will extract posting list for each term as read from file
		 Term_Posting_List = new Vector[Term_List.size()];
		 
		 //Maintain 'n' pointers for 'n' terms in DAAT
		 Term_Pointers = new Integer[Term_List.size()];
		 
		 
		 //Print posting list for all terms
		 for(i=0;i<Term_List.size();i++)
		 {
			 Term_Posting_List[i] =(hPosList.get(Term_List.get(i)));
			 output.write("GetPostings"+'\n');
			 output.write(Term_List.get(i)+'\n');
			 output.write("Postings list: ");
			 if(Term_Posting_List[i]!=null)
			 {
				 for(int j=0;j<Term_Posting_List[i].size();j++)
				 {
					 output.write(Term_Posting_List[i].get(j).toString()+" ");
				 }
			 }
			 output.write('\n');
			 Term_Pointers[i]=0;
		 }

		 /*
		 Scoring_TAAT_AND();
		 Scoring_TAAT_OR();
	 	 IntersectionArray = new Vector<Integer>();
		 unionArray = new Vector<Integer>();
		  */
		 
		 intersection_comparisons =0;
		 union_comparisons =0;
		 
		 //Get TAAT_AND
		 TAAT_Getintersection();
		 //Get TAAT_OR
		 TAAT_GetUnion();
		 
		 //Save TAAT results to File
		 PRINT_TAAT_RESULTS();
		 
		 
		 IntersectionArray = new Vector<Integer>();
	 	 unionArray = new Vector<Integer>();

	 	 //Do DAAT_AND and DAAT_OR and save results to file
		 DAAT_AND();
		 DAAT_OR();
	 	 IntersectionArray = new Vector<Integer>();
		 unionArray = new Vector<Integer>();

 }
catch(Exception e)
{
		System.out.println("Exception occured" + e);
}
 	    }


	 
	 //System.out.println("Execution Finish");
	 reader.close(); 
	 input.close();
	 output.close();
	 
	}
	

	
	static public boolean binarySearch(Vector<Integer> vTemp, int key) 
	{
	     int low = 0;
	     int size=vTemp.size();
	     int high = size - 1;
	      
	     while(high >= low) {
	          int middle = (low + high) / 2;
	          if(vTemp.get(middle) == key) {
	              return true;
	          }
	          if(vTemp.get(middle) < key) {
	              low = middle + 1;
	          }
	          if(vTemp.get(middle) > key) {
	              high = middle - 1;
	          }
	     }
	     return false;
	}
	
	

 	@SuppressWarnings("static-access")
	public static void MakehashTable() throws IOException {
	 for (int i=0;i<MAX_LANGUAGES;i++) 
	 {
		 TermsEnum termEnum = MultiFields.getTerms(reader, languages[i]).iterator();	
		 BytesRef bytesRef = null;
			 
			 while ((bytesRef = termEnum.next()) != null) 
			 {
				 	
		            //int freq = reader.docFreq(new Term(languages[i], bytesRef));  //field name,term
		            String termText = bytesRef.utf8ToString();
		            //System.out.println(bytesRef.utf8ToString() + " in " + freq + " documents"+"\n");
		            //writer.println(bytesRef.utf8ToString() + " in " + freq + " documents");
			 
		            PostingsEnum postenum = MultiFields.getTermDocsEnum(reader, languages[i], bytesRef);
					//System.out.print(bytesRef.utf8ToString() +"[");
		            
					 while((postenum.nextDoc() != postenum.NO_MORE_DOCS))
					 {
						 //System.out.print(postenum.docID()+" ");
						 //System.out.print(reader.document(postenum.docID()));
						 //http://read.pudn.com/downloads149/doc/646021/PostingList.java__.htm
					     //if(hPosList.containsKey(termText))           // if token already present   
					     if(hPosList.get(termText) != null)           // if token already present
					     { 
					    	 number_of_terms++;
					    	 //System.out.println("Token already Present");
						      Vector<Integer> vTemp= (Vector<Integer>) hPosList.get(termText);   
						      //if(! vTemp.contains(postenum.docID()))    // if doc_id for current token not already present   
						      if(binarySearch(vTemp,postenum.docID())==false)  // if doc_id for current token not already present   
						      {    
						    	  vTemp.addElement(postenum.docID());  // otherwise ignore as needn't to add again    	    
						      }
					    	  
					     }   
					     else                                     // if token already not present   
					     {
					    	 number_of_terms++;
						      Vector<Integer> vPList=new Vector<Integer>();   
						      vPList.addElement(postenum.docID());             // create vector and add doc_id   
						      hPosList.put(termText,vPList);      
					     } 
						 
					 }
					 
					 //System.out.print("]"+"\n");
			}

		  }
	 
	 //sort the posting list
	 for (Map.Entry<String, Vector<Integer>> entry : hPosList.entrySet()) {
		    String key = entry.getKey();
		 	Collections.sort(hPosList.get(key));
		}
}

 	public static void DAAT_AND() throws IOException {
		 int i=0;
		 int j=0, index=0;
		 int smallest=Integer.MAX_VALUE;
		 int common_elements = 0;
		 int Number_of_comparisons =0;
		 int Number_of_documents_in_results=0;
		 int count_for_this_iteration =0;
		 
		 output.write("DaatAnd"+'\n');		 
		 for(i=0;i<Term_List.size();i++)
		 {
			 output.write(Term_List.get(i)+ " ");
		 }		 
		 output.write("\n"); 
		 output.write("Results: ");
		 
		 
		 //Initialise the pointers to first term of each posting list
		 for(i=0;i<Term_List.size();i++)
		 {
			 Term_Pointers[i]=0;
		 }
		 Boolean isend=false;
		 
		 
try{
		 while(true)
		 {
			 	common_elements = 0;
			 	isend = false;
			 	smallest=Integer.MAX_VALUE;
			 	count_for_this_iteration=0;
		 		//Find Smallest element among all lists
			 	for(i=0;i<Term_List.size();i++) 
			 	 {
			 		 if((Term_Posting_List[i]==null)||(Term_List.size()==1)||(Term_Pointers[i] >= Term_Posting_List[i].size()))
			 		{
			 			isend=true;
			 			break;	
			 		}
			 		count_for_this_iteration++;
			 		if((Term_Posting_List[i].get(Term_Pointers[i])) < smallest)
			 		{
			 			smallest=Term_Posting_List[i].get(Term_Pointers[i]);
			 		} 
			 	 }
			 	 
			 	 if((isend==true)||(smallest==Integer.MAX_VALUE))
			 	 {
			 		 break;
			 	 }
			 	 Number_of_comparisons+= (count_for_this_iteration-1); //keep on accumulating comparsions
			 	 for(j=0;j<Term_List.size();j++)
			 	 {
			 		 //increase all pointers which have value==smallest
					 if((Term_Posting_List[j].get(Term_Pointers[j])) == smallest)
					 {
						 Term_Pointers[j]++;
						 common_elements++;
						 
					 }
			 	 }
			 	
			 	 //if Document_id is present in all docs, increment the intersection array
			 	 if(common_elements==Term_List.size())
			 	 {
			 		 Number_of_documents_in_results++;
			 		 output.write(smallest+" ");
					 IntersectionArray.add(index,smallest);
					 index++;			 		 
					 //System.out.println(" smallest "+smallest);
			 	 }
			 	 
	}
}
catch(Exception e)
{
		System.out.println("Exception occured" + e);
}
		 if(Number_of_documents_in_results==0)
		 {
			 output.write("empty");
		 }
		 output.write("\n");
		 output.write("Number of documents in results: " + Number_of_documents_in_results);
		 output.write("\n");
		 output.write("Number of comparisons: " + Number_of_comparisons);
		 output.write("\n");
		 
 	}
 	
  	
 	
 	public static void DAAT_OR() throws IOException {
 		
		 int i=0;
		 int index=0;
		 int smallest=Integer.MAX_VALUE;
		 int Number_of_comparisons =0;
		 int Number_of_documents_in_results=0;
		 int count_for_this_iteration=0;
		 int count_of_non_exhaustive_lists=0;
		 output.write("DaatOr"+'\n');		 
		 for(i=0;i<Term_List.size();i++)
		 {
			 output.write(Term_List.get(i)+ " ");
		 }		 
		 output.write("\n"); 
		 output.write("Results: ");
		 
		 //initilaise the pointer
		 for(i=0;i<Term_List.size();i++)
		 {
			 Term_Pointers[i]=0;
		 }
try{
		 while(true)
		 {

			 	smallest=Integer.MAX_VALUE;
		 		count_for_this_iteration=0;
		 		count_of_non_exhaustive_lists=0;
		 		
		 		//Find Smallest element among all lists
			 	 for(i=0;i<Term_List.size();i++)  //i represents particular terms
			 	 {
			 		if (Term_Posting_List[i]!=null)
			 		{

		 				if(Term_Pointers[i] < Term_Posting_List[i].size()) //check non-exhausted lists
					 		{
			 						count_of_non_exhaustive_lists++;		 					
					 				count_for_this_iteration++;
					 				//System.out.println(Term_Pointers[i] + "," + Term_Posting_List[i].size());
							 		if(Term_Posting_List[i].get(Term_Pointers[i]) < smallest)
							 		{
							 			smallest=Term_Posting_List[i].get(Term_Pointers[i]);
							 		}
					 		}
			 		}
			 	}
		 	 
			 	//System.out.println(" smallest "+smallest);
			 	 if(smallest==Integer.MAX_VALUE)
			 	 {
			 		 break;
			 	 }
			 	 if(count_of_non_exhaustive_lists!=1)  //Dont increment count when number of non-exhausted lists remaining is 1 
			 	 {
			 		 Number_of_comparisons+=(count_for_this_iteration-1); //for n non-exhausted and non-empty terms only n-1 comparisons are needed 				 	 
			 	 }
			 	 
			 	 for(i=0;i<Term_List.size();i++)
			 	 {
			 		 if(Term_Posting_List[i]!=null)
			 		 {
			 		 	if (Term_Pointers[i] < Term_Posting_List[i].size()) //check only in non-exhausted lists
				 		{
			 		 		//increase all pointers which have value==smallest
			 		 		if(Term_Posting_List[i].get(Term_Pointers[i]) == smallest)
			 		 		{
								 Term_Pointers[i]++;
			 		 		}
				 		}
			 		 }
			 	}
			 	Number_of_documents_in_results++;
		 		 output.write(smallest+" ");
				unionArray.add(index,smallest);
				//System.out.println("Union_OR  " + smallest);
				index++;

			 
		 }
}
catch(Exception e)
{
		System.out.println("Exception occured" + e);
}
		 if(Number_of_documents_in_results==0)
		 {
			 output.write("empty");
		 }
		 output.write("\n");
		 output.write("Number of documents in results: " + Number_of_documents_in_results);
		 output.write("\n");
		 output.write("Number of comparisons: " + Number_of_comparisons);
		 output.write("\n");
	}
 
 	
 	//Optimization for Intersection: get smallest posting list
	public static void NAIVEDAAT_GetSmallestTermSizeList() throws IOException {
 		
		index_of_term_with_smallest_list_size=Integer.MAX_VALUE;
		 Integer min=Integer.MAX_VALUE; //term with minimum docs
try{
		for(int i = 0; i < Term_List.size(); i++) {
			//System.out.print(Term_Posting_List[i].size());
		      if((Term_Posting_List[i].size() < min)&&(IfAlreadyProcessed.get(i)==false)) {
		    	 min = Term_Posting_List[i].size();
		    	 index_of_term_with_smallest_list_size = i;
		      }
		}
		
		if(index_of_term_with_smallest_list_size!=Integer.MAX_VALUE)
		{		
			IfAlreadyProcessed.set(index_of_term_with_smallest_list_size, true);
		}
}
catch(Exception e)
{
		System.out.println("Exception occured" + e);
}
	}
	
	
	

	public static void TAAT_Getintersection() throws IOException {
	 	Vector<Integer> vTemp1 = new Vector<Integer>();
	 	Vector<Integer> vTemp2 = new Vector<Integer>();	

try{
	 	if((Term_List.size()==0)||(Term_List.size()==1))
	 	{
		 	IntersectionArray = new Vector<Integer>();	
	 		return;
	 	}

	 	for(int i=0;(i<Term_List.size());i++)
		{
			if(hPosList.get(Term_List.get(i))==null)
			{
		 	 	IntersectionArray = new Vector<Integer>();
				return;
			}
			 if(i == 0)  //If i is first element, find intersection of 1st 2 lists with smallest size
			    {
				 	NAIVEDAAT_GetSmallestTermSizeList();
			 		vTemp1=  hPosList.get(Term_List.get(index_of_term_with_smallest_list_size));
			 		NAIVEDAAT_GetSmallestTermSizeList();
			 		vTemp2= hPosList.get(Term_List.get(index_of_term_with_smallest_list_size));
			 		TAAT_Intersection(vTemp1,vTemp2);
				    i++;
				 	
			    }
			    else //if i is not first element then take intersection of intermiediate lists and new list
			    {
			    	NAIVEDAAT_GetSmallestTermSizeList();
			 		vTemp1= hPosList.get(Term_List.get(index_of_term_with_smallest_list_size));
			 		TAAT_Intersection(IntersectionArray,vTemp1);
			    }
		}
}
catch(Exception e)
{
		System.out.println("Exception occured" + e);
}
	}


	public static void TAAT_GetUnion() throws IOException {
		
		int i=0;
	 	Vector<Integer> vTemp1 = new Vector<Integer>();
		for(i=0;(i<Term_List.size());i++)
		{
		 	vTemp1= hPosList.get(Term_List.get(i));
			TAAT_Union(unionArray,vTemp1);
		}
	}
	
 	public static void TAAT_Intersection(Vector<Integer> vTemp1, Vector<Integer> vTemp2) throws IOException {
try{ 		
		 int i=0;
		 int j=0, index=0;
	 	 IntersectionArray = new Vector<Integer>();		 
		 if ((vTemp1==null)||(vTemp2==null))
		 {
			 return;
		 }

		 else 
		 {
		 		while((i<vTemp1.size()) && (j<vTemp2.size()))
		 		{
		 			 intersection_comparisons++;
					 if(vTemp1.get(i).equals(vTemp2.get(j)))
					 {
						 IntersectionArray.add(index,vTemp1.get(i));
						 index++;
						 i++;
						 j++;
					 }
					 else if(vTemp1.get(i) < vTemp2.get(j))
					 {
						 i++;
					 }
					 else if (vTemp2.get(j) < vTemp1.get(i))
					 {
						 j++;
					 }
		 	}

	 }
	 
}
catch(Exception e)
{
		System.out.println("Exception occured" + e);
}
	}
	
	
	
	public static void TAAT_Union(Vector<Integer> vTemp1, Vector<Integer> vTemp2) throws IOException {
try{	
	
		 int i=0;
		 int j=0, index=0;
	 	 unionArray = new Vector<Integer>();	 
		 if ((vTemp1==null)&&(vTemp2==null))
		 {
			 return;
		 }
		 else if ((vTemp1!=null)&&(vTemp2==null))
		 {
			 for(i=0;i<vTemp1.size();i++)
			 {
				 unionArray.add(index,vTemp1.get(i));
				 index++;
			 }
			 return;
		 }

		 else if ((vTemp1==null)&&(vTemp2!=null))
		 {
			 for(i=0;i<vTemp2.size();i++)
			 {
				 unionArray.add(index,vTemp2.get(i));
				 index++;
			 }
			 return;
		 }
		 else  // if both terms have non-zero posting
		 {
			 i=0;
			 j=0;
			 	while((i<vTemp1.size()) || (j<vTemp2.size()))
			 	{
			 		//System.out.println(i+ "  "+j);
			 		if((i<vTemp1.size()) && (j<vTemp2.size()))
			 		{
			 			 union_comparisons++;
						 if(vTemp1.get(i).equals(vTemp2.get(j)))
						 {
							 unionArray.add(index,vTemp1.get(i));
							 index++;
							 i++;
							 j++;
						 }
						 else if((vTemp1.get(i)) < (vTemp2.get(j)))
						 {
							 unionArray.add(index,vTemp1.get(i));
							 index++;
							 i++;
						 }
						 else
						 {
							 //System.out.println(i+ " "+ vTemp1.size()+ " "+j+ "  "+ vTemp2.size());
							 unionArray.add(index,vTemp2.get(j));
							 index++;
							 j++;
						 }
			 	}
			 	else if((i<vTemp1.size()) && (j>=vTemp2.size()))
			 	{
					 unionArray.add(index,vTemp1.get(i));
					 index++;
					 i++;
			 	}
			 	else if((i>=vTemp1.size()) && (j<vTemp2.size()))
			 	{
					 unionArray.add(index,vTemp2.get(j));
					 index++;
					 j++;
			 	}
			 }
		 }
	}
catch(Exception e)
{
		System.out.println("Exception occured" + e);
		return;
}
		 //System.out.println("number of unique terms = "+ number_of_terms);
	}

	
	static public void PRINT_TAAT_RESULTS() throws IOException
	{

				output.write("TaatAnd"+'\n');		 
				 for(int i=0;i<Term_List.size();i++)
				 {
					 output.write(Term_List.get(i)+ " ");
				 }		 
				 output.write("\n"); 
				 output.write("Results: ");
	try{				 
				 if(IntersectionArray.size()==0)
				 {
					 output.write("empty");
				 }

				 else
				 {
		
					 for(Integer intersection_term : IntersectionArray) 
				 		{
					 		output.write(intersection_term.toString());
					 		output.write(" ");
				 		}
				 }
	}
	catch(Exception e)
	{
			System.out.println("Exception occured" + e);
	}
				 output.write("\n");
				 output.write("Number of documents in results: " + IntersectionArray.size());
				 output.write("\n");
				 output.write("Number of comparisons: " + intersection_comparisons);
				 output.write("\n");
				 
				 	
				 	
				 
				 output.write("TaatOr"+'\n');		 
				 for(int i=0;i<Term_List.size();i++)
				 {
					 output.write(Term_List.get(i)+ " ");
				 }		 
				 output.write("\n"); 
				 output.write("Results: ");
try{
				 if(unionArray.size()==0)
				 {
					 output.write("empty");
				 }

				 else
				 {
					 	for(Integer union_terms : unionArray) 
				 		{
					 		output.write(union_terms.toString());
					 		output.write(" ");
				 		}
				 }
}
catch(Exception e)
{
		System.out.println("Exception occured" + e);
}
					 output.write("\n");
					 output.write("Number of documents in results: " + unionArray.size());
					 output.write("\n");
					 output.write("Number of comparisons: " + union_comparisons);
					 output.write("\n");
			 
	}

	
 	/*
 	public static void Scoring_TAAT_OR() throws IOException {
 		 int i=0;
		 int j=0;
		 int Number_of_documents_in_results=0;
		 int Number_of_comparisons=0;
		 Integer[] taat_or = new Integer[number_of_docs];
		 output.write("Scoring_TaatOr"+'\n');
		 for(i=0;i<Term_List.size();i++)
		 {
			 output.write(Term_List.get(i)+ " ");
		 }		 
		 output.write("\n"); 
		 output.write("Results: "); 
try{		 
		 for(i=0;i<number_of_docs;i++)
		 {
			 taat_or[i]=0;
		 }

		 for(i=0;i<Term_List.size();i++)
		 {
			 if(Term_Posting_List[i] !=null)
			 {
				 for(j=0;j<Term_Posting_List[i].size();j++)
				 {
					 Number_of_comparisons++;
					 taat_or[Term_Posting_List[i].get(j)] += 1;
				 }
			 }
		 }
		 
		 for(i=0;i<number_of_docs;i++)
		 {

				 if(taat_or[i] > 0)
				 {
					 //System.out.println("TAAT_OR  " + i);
					 Number_of_documents_in_results++;
					 output.write(i+" ");
				 }
		 }
}
catch(Exception e)
{
		System.out.println("Exception occured" + e);
}
		 if(Number_of_documents_in_results==0)
		 {
			 output.write("empty");
		 }
		 output.write("\n");
		 output.write("Number of documents in results: " + Number_of_documents_in_results);
		 output.write("\n");
		 output.write("Number of comparisons: " + Number_of_comparisons);
		 output.write("\n");		 
 	}

 	
 	public static void Scoring_TAAT_AND() throws IOException {
 		
		 int i=0;
		 int j=0;
		 int Number_of_documents_in_results = 0;
		 int Number_of_comparisons =0;
		 Integer[] taat_and = new Integer[number_of_docs];
		 output.write("Scoring_TaatAnd"+'\n');
		 for(i=0;i<Term_List.size();i++)
		 {
			 output.write(Term_List.get(i)+ " ");
		 }		 
		 output.write("\n"); 
		 output.write("Results: "); 
try{
 
		for(i=0;i<number_of_docs;i++)
		 {
			 taat_and[i]=0;
		 }

		 for(i=0;i<Term_List.size();i++)
		 {
			 if(Term_Posting_List[i] !=null)
			 {
				 for(j=0;j<Term_Posting_List[i].size();j++)
				 {
					 Number_of_comparisons++;
					 taat_and[Term_Posting_List[i].get(j)] += 1;
				 }
			 }
		 }
		 
		 for(i=0;i<number_of_docs;i++)
		 {

				 if(taat_and[i] == (Term_List.size()))
				 {
					 //System.out.println("TAAT_AND  " + i);
					 Number_of_documents_in_results++;
					 output.write(i+" ");
				 }
		 }
}
catch(Exception e)
{
		System.out.println("Exception occured" + e);
}
		 if(Number_of_documents_in_results==0)
		 {
			 output.write("empty");
		 }
		 output.write("\n");
		 output.write("Number of documents in results: " + Number_of_documents_in_results);
		 output.write("\n");
		 output.write("Number of comparisons: " + Number_of_comparisons);
		 output.write("\n");
	}
 	
 	*/
}
