import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 */

/**
 * @author Sruthi danda
 *
 */
public class UserSimilarity {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int trainingData[][] = new int[80000][4];
		String filename ="u1.base";
		String data= null;
		int trainingdataCount = 0;
		
		/*
		 * step 1: Reading training data from the file in to local array.
		 */
		try {

			BufferedReader bufferedReader = new BufferedReader( new FileReader(filename));
			while((data = bufferedReader.readLine()) != null) {
				String[] tempData = data.split("\t");
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
		 * Step 2: calculate the max number of users and max number of movies in the test set.
		 * This helps in initializing the re-maining arrays.
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
		
		System.out.println("Number of users "+ maxUser +"  The number of Movies  "+maxMovie);
		
		/*
		 * step 3: initialize the variables required to find out the similarity between users.
		 */
		
		float userSimilarity[][] = new float[maxUser][maxUser];
		//to store the similarity between each and every user
		
		float userMovieRating[][] = new float[maxUser][maxMovie];
		// To store user and movie rating
		
		float avgMovieRating[] = new float[maxMovie]; 
		// to store the average ratings given by all the users to each and every movie
		
		float avgMovieRatingDifference[][] = new float[maxUser][maxMovie];
		//TO store the difference between the each movie rating with the average of that movie rating
		
		/*
		 * Step 4: arranging the read data in to new array.
		 */
		
		trainingcount=0;
		while (trainingcount < trainingdataCount)
		{
			userMovieRating [(int)( trainingData[trainingcount][0]-1)] [(int) (trainingData[trainingcount][1]-1)] = trainingData[trainingcount][2];
			trainingcount++;
		}
		
		/*
		 * step 5: divided the formula 1 in to steps 
		 * 			1. finding the average of each movie rating given by all the users.
		 * 			2. finding the difference between the each movie rating with the average of that movie.
		 */
		
		for(int i=0 ;i< maxMovie ;i++)
		{
			avgMovieRating[i] = 0;
		}
		
		for( int i = 0 ;i < maxMovie ;i++)
		{
			int averageMovie = 0;
			for (int j=0;j< maxUser; j++)
			{
				if( userMovieRating[j][i] != 0)
				{
				avgMovieRating[i] += userMovieRating[j][i];
				averageMovie++;
				}
			}
			avgMovieRating[i] = avgMovieRating[i]/maxUser;
		}
		
		/*
		 *Finding the difference between each movie rating with the average rating of that movie. 
		 */
		for( int i = 0 ;i < maxUser ;i++)
		{
			for (int j=0;j< maxMovie; j++)
			{
				avgMovieRatingDifference[i][j] = userMovieRating[i][j] - avgMovieRating[j];
			}
		}
		
		/*
		 * step 6: Finding user similarity and storing the result in a text file.
		 */
		for( int i = 0 ;i < maxUser ;i++)
		{
			for (int j=0;j<= i; j++)
			{
				float numarator = 0; float denominator1= 0 ;float denominator2=0;
				for(int k=0; k< maxMovie; k++)
				{
				//	if ( containsRating(userMovieRating[i][k]) && containsRating(userMovieRating[j][k]))
					{
						numarator += avgMovieRatingDifference[i][k] * avgMovieRatingDifference[j][k];
					
						denominator1 += avgMovieRatingDifference[i][k]*avgMovieRatingDifference[i][k];
						
						denominator2 += avgMovieRatingDifference[j][k]*avgMovieRatingDifference[j][k];
					}
				}
				
				userSimilarity[i][j] = (float) (numarator / (Math.sqrt(denominator1) * Math.sqrt(denominator2)));
				
				userSimilarity[j][i] = userSimilarity[i][j];
				//fINDING THE distance between user 1 and 2 but not calculating the distence between user 2 and 1.
				//They both are same.
			}
		}
		
		/*
		 * Writing the data into file
		 */
		try
		{
			FileWriter fw = new FileWriter("UserSimilarity.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			
			for(int i=0 ;i < userSimilarity.length+1; i++)
			{
				for( int j = 0 ;j <userSimilarity.length+1;j++)
				{
					if( i==0 || j==0)
					{
						bw.write(0+ ",");
					}
					else
					{
					bw.write(String.format("%.2f", userSimilarity[i-1][j-1]) + ",");
					}
					//bw.write(userSimilarity[i][j] + ",");

				}
				bw.write("\n");

			}
			bw.close();
		}
		catch(FileNotFoundException ex) {
			System.out.println("file not found");            
		}
		catch(IOException ex) {
			System.out.println("error in opening file");
		}

		System.out.println("Summary : UserSimilarity.java Program we calculated the similaritys between users and we stored them in UserSimilarity.txt file. ");
		System.out.println("max Number of Users"+ maxUser +" Max number of movies "+ maxMovie);
	}
	
	private static boolean containsRating(float check) {
		
		return (( check >= 1 && check <= 5));
			
	}

}
