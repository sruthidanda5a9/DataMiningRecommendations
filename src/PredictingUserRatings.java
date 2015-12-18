import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

/**
 * 
 */

/**
 * @author Sruthi danda
 *
 */
/*
 * This file is used to predict the ratings of the user given by movies and to find out MAE
 */
public class PredictingUserRatings {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		
		int trainingData[][] = new int[80000][4];
		String filename ="u1.base";
		String data= null;
		int trainingdataCount = 0;
		int testDataCount = 0;
		String testFileName = "u1.test";
		
		int testdata[][] = new int[20000][4];
		/*
		 * step 1: Reading training data from the file in to local array.
		 * 		reading test data from file in to an array to find the MAE
		 */
		try {

			BufferedReader bufferedReader = new BufferedReader( new FileReader(filename));
			while((data = bufferedReader.readLine()) != null) {
				String[] tempData = data.split("\t");
				//System.out.println(tempData.length + " tempData length");
				for(int j=0;j<=3;j++)
				{
					trainingData[trainingdataCount][j]= (int) Float.parseFloat(tempData[j]);
				}
				trainingdataCount++;
			}  
		}catch(FileNotFoundException ex) {
			System.out.println("file not found");            
		}
		catch(IOException ex) {
			System.out.println("error in opening file");
		}
		
		/*
		 * Reading data from test data into test data array.
		 * 
		 */
		try {

			BufferedReader bufferedReader = new BufferedReader( new FileReader(testFileName));
			while((data = bufferedReader.readLine()) != null) {
				String[] tempData = data.split("\t");
				for(int j=0;j<=3;j++)
				{
					testdata[testDataCount][j]= (int) Float.parseFloat(tempData[j]);
				}
				testDataCount++;
			}  
		}catch(FileNotFoundException ex) {
			System.out.println("file not found");            
		}
		catch(IOException ex) {
			System.out.println("error in opening file");
		}
		
		/*
		 * Step 2: calculate the max number of users and max number of movies in the training set set.
		 * This helps in initializing the remaining arrays.
		 */
		
		int maxUser = (int) trainingData[0][0];
		int maxMovie = (int)trainingData[0][1];
		int trainingcount =0;
		while (trainingcount < trainingdataCount)
		{
			if ( maxUser < (int)( trainingData[trainingcount][0]) )
				maxUser =  trainingData[trainingcount][0]; 
			if ( maxMovie < (int)( trainingData[trainingcount][1]) )
				maxMovie =  trainingData[trainingcount][1]; 
				trainingcount++;
		}
		System.out.println("Training data number of  users**   "+ maxUser + "  Training data number of movies ** "+maxMovie);
		int maxUserTest = (int) testdata[0][0];
		int maxMovieTest = (int)testdata[0][1];
		int testCount =0;
		while (testCount < testDataCount)
		{
			if ( maxUserTest < (int)( testdata[testCount][0]) )
				maxUserTest =  testdata[testCount][0]; 
			if ( maxMovieTest < (int)( testdata[testCount][1]) )
				maxMovieTest =  testdata[testCount][1]; 
			testCount++;
		}
		System.out.println("Test data total number of users***   " + maxUserTest + "   Test data number of Movies**   "+maxMovieTest);
		
		if( maxMovie < maxMovieTest)
		{
			maxMovie = maxMovieTest;
		}
		if( maxUser < maxUserTest)
		{
			maxUser = maxUserTest;
		}
		
		
		/*
		 * step 3: initialize the variables required to find out the predicted ratings.
		 */
		
		float userMovieRating[][] = new float[maxUser][maxMovie];
		// To store user and movie rating
		int testCoust = 0;
		
		float itemPredectionsforEachUser[][] = new float[maxMovie][2];
		//To store the predicted ratings of each movie for the user.
		float meanAbsoluteErrork1[] = new float[maxUser];
		float meanAbsoluteErrork10[] = new float[maxUser];
		float meanAbsoluteErrork50[] = new float[maxUser];
		float meanAbsoluteErrork100[] = new float[maxUser];
		//to store the mean absolute error for each user.
		float testuserMovieRating[][] = new float[maxUser][maxMovie];
		
		
		/*
		 * Step 4: arranging the data in to new array.
		 */
		trainingcount = 0;
		while (trainingcount < trainingdataCount)
		{
			userMovieRating[(int) trainingData[trainingcount][0]-1][(int) trainingData[trainingcount][1]-1] = trainingData[trainingcount][2];
			trainingcount++;
		}
		while ( testCoust < testDataCount)
		{
			testuserMovieRating[(int) testdata[testCoust][0]-1][(int) testdata[testCoust][1]-1] = testdata[testCoust][2];
			testCoust++;
		}
		/*
		 * Step 5: first we need to read the data from the usersimilarity.txt file to predict the ratings based on them.
		 */
		
		float userSimilaity[][] = new float[maxUser][maxUser];
		String userSimilerfilename = "userSimilarity.txt";
		data= null;
		float nearestNeighnours[][] = new float [maxUser][2];

		try {
			int userSimilarityCount=0;
			BufferedReader bufferedReader = new BufferedReader( new FileReader(userSimilerfilename));

			while((data = bufferedReader.readLine()) != null) {
				String[] tempData = data.split("\\,");
				for(int j=0; j< maxUser ;j++)
				{
					userSimilaity[userSimilarityCount][j]= Float.parseFloat(tempData[j]);
				}
				userSimilarityCount++;
			}  
		}
		catch(FileNotFoundException ex) {
			System.out.println("file not found");            
		}
		catch(IOException ex) {
			System.out.println("error in opening file");
		}

		for( int i=0;i< maxUser ; i++)
		{
			for ( int j=0;j<=i ;j++ )
			{
				if( Double.isNaN(userSimilaity[i][j]) || j==i)
				{
					userSimilaity[i][j]=0;
					userSimilaity[j][i]=0;
				}
			}
		}
		
		/*
		 * Step 6: finding the closest neighbors to each and every user among them we are picking only the TOP K users to predict the ratings of the users.
		 */

		for( int i=0; i< maxUser ;i++)
		{
			//System.out.println(" this is for user "+ i);
			nearestNeighnours =  nearestNeighnours(userSimilaity[i],maxUser,i);
			itemPredectionsforEachUser = predectingItemRatings(nearestNeighnours, 1,userMovieRating ,maxMovie,i,testuserMovieRating[i]);
			meanAbsoluteErrork1[i] =Math.abs( findingMeanAbsoluteErrorforEachUser(itemPredectionsforEachUser, testuserMovieRating[i],maxMovie) );
			itemPredectionsforEachUser = predectingItemRatings(nearestNeighnours, 10 ,userMovieRating ,maxMovie,i,testuserMovieRating[i]);
			meanAbsoluteErrork10[i] = Math.abs( findingMeanAbsoluteErrorforEachUser(itemPredectionsforEachUser, testuserMovieRating[i],maxMovie));
			itemPredectionsforEachUser = predectingItemRatings(nearestNeighnours, 50 ,userMovieRating ,maxMovie,i,testuserMovieRating[i]);
			meanAbsoluteErrork50[i] = findingMeanAbsoluteErrorforEachUser(itemPredectionsforEachUser, testuserMovieRating[i],maxMovie);
			itemPredectionsforEachUser = predectingItemRatings(nearestNeighnours, 100 ,userMovieRating ,maxMovie,i,testuserMovieRating[i]);
			meanAbsoluteErrork100[i] = findingMeanAbsoluteErrorforEachUser(itemPredectionsforEachUser, testuserMovieRating[i],maxMovie); 
		}
		float sumMAEk1=0;
		float sumMAEk10=0;
		float sumMAEk50=0;
		float sumMAEk100=0;
		for ( int i=0;i < maxUser;i++)
		{
			sumMAEk1= Math.abs(meanAbsoluteErrork1[i]+ sumMAEk1) ;
			sumMAEk10 = Math.abs(meanAbsoluteErrork10[i] + sumMAEk10) ;
			sumMAEk50 = Math.abs(meanAbsoluteErrork50[i]+ sumMAEk50 );
			sumMAEk100 = Math.abs(meanAbsoluteErrork100[i]+ sumMAEk100 );
		}
		System.out.println( "nearest one:"+sumMAEk1 + "nearest 10: " + sumMAEk10 + "nearest 50:  " + sumMAEk50 + "nearest 100:  " + sumMAEk100);
		System.out.println(testDataCount);
		System.out.println(Math.abs(sumMAEk1/testDataCount));
		System.out.println(Math.abs(sumMAEk10/testDataCount));
		System.out.println(Math.abs(sumMAEk50/testDataCount));
		System.out.println(Math.abs(sumMAEk100/testDataCount));
		
		
	}

	
	/*
	 * Step 7: This method is used to sort the neighbors of each user
	 */
	private static float[][] nearestNeighnours(float[] userSimilaity,int maxUser , int userIndex) {
		float[][] nearestNeighnours = new float[maxUser][2];
		float temp;
		float classTemp;
		for ( int i=0 ; i< userSimilaity.length; i++)
		{
			if(userIndex != i )
			{
			nearestNeighnours[i][0] = userSimilaity[i];
			nearestNeighnours[i][1] = i;
			}
			else
			{
			nearestNeighnours[i][0] = -10;
			nearestNeighnours[i][1] = i;
			}
		} 
		for ( int i=0 ; i< userSimilaity.length; i++)
		{
			for ( int j=i+1 ;j< userSimilaity.length; j++)
			{
				if ( nearestNeighnours[i][0] < nearestNeighnours[j][0])
				{
					temp = nearestNeighnours[i][0];
					classTemp = nearestNeighnours[i][1];
					nearestNeighnours[i][0] = nearestNeighnours[j][0];
					nearestNeighnours[i][1] = nearestNeighnours [j][1];
					nearestNeighnours[j][0] = temp;
					nearestNeighnours[j][1] = classTemp;
					/*
					 * While sorting we need to have the index of each user to represent the original user position.
					 */
				}
			}
		}
		/*for ( int i=0 ; i< userSimilaity.length; i++)
		{
			System.out.println(nearestNeighnours[i][0]+"    "+ nearestNeighnours[i][1]);
		} */
		return nearestNeighnours;
	}
	
	/*
	 * Step 8: predicting the ratings.
	 */
	private static float[][] predectingItemRatings(float[][] nearestNeighnours, int nearesrUsers, float[][] userMovieRating,
			int maxMovie, int userIndex, float testuserMovieRating[]) {
		float[][] itemPredectionsforEachUser = new float[maxMovie][2] ;
		int start =0;
		int count=0;
		int count1=0,count2=0;
		for( int j=0; j< maxMovie;j++)
		{
			
			float num =0;
			float den=0;
			for ( int k=start; k < nearesrUsers ;k++ )
			{
				if ( containsRating ( userMovieRating[(int) nearestNeighnours[k][1]][j]) &&  containsRating(testuserMovieRating[j]))
				{
					num += nearestNeighnours[k][0] * userMovieRating[(int) nearestNeighnours[k][1]][j];
					den += Math.abs(nearestNeighnours[k][0]);
				}
			}
			if ( den != 0.0)
			{
			itemPredectionsforEachUser[j][0]= (num/den);
			itemPredectionsforEachUser[j][1]=j;
			}
			else
			{
				itemPredectionsforEachUser[j][0]= -1;
				itemPredectionsforEachUser[j][1]=j;
			}
			
		}
		return itemPredectionsforEachUser;
	}
	
	/*
	 * step 9: ContainsRating method is used to verify does the user gave rating to the movie or not.
	 */
	private static boolean containsRating(float f) {
		int check = (int) f;
		if( check >0  && check <= 5)
			return true;
		else
			return false;
	}
	
	private static boolean containsRatingPredicted(float f) {
		int check = (int) f;
		if( check >= 0 && check <= 5 )
			return true;
		else
			return false;
	}
	
	/*
	 * Step 10: finding the MAE separately for each user ( predicted rating ---- absolute rating ) 
	 * By using this array we are finding the MAE for the complete data.
	 */

	private static float findingMeanAbsoluteErrorforEachUser(float[][] itemPredectionsforEachUser, float[] testuserMovieRating,
			int maxMovie) {
		float predictederrordifference = 0;
		int count= 0;
		
		for( int i= 0 ;i< maxMovie;i++)
		{
			if(containsRatingPredicted(itemPredectionsforEachUser[i][0]) && containsRating(testuserMovieRating[i]))
			{
				//System.out.println(( itemPredectionsforEachUser[i][0]) + "  " + (testuserMovieRating[i]));
				predictederrordifference = Math.abs( itemPredectionsforEachUser[i][0] - testuserMovieRating[i])+ predictederrordifference;
			}
		}
	//System.out.println(" The common movies with test data" + count  + " predicted difference "+ predictederrordifference);
		return predictederrordifference;
	}

}

