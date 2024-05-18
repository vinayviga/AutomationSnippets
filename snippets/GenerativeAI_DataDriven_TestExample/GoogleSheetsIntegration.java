package opencart;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.ValueRange;

import  java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GoogleSheetsIntegration {
	private static final String APPLICATION_NAME = "Desktop client";
	private static final String spreadsheetId = "1SP_qfbkIEYf1P8DIV8d-yiDCXCFlUjzvzk0rjbnrTdQ";
	private static final String CREDENTIALS_FILE_PATH = "credential.json";//you can create your own credential file and paste it in the current folder
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);


	private static Credential authorize() throws IOException, GeneralSecurityException
	{
		GsonFactory instance = GsonFactory.getDefaultInstance();

		InputStream in = GoogleSheetsIntegration.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(instance, new InputStreamReader(in));
		List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
				.Builder(GoogleNetHttpTransport.newTrustedTransport(), instance, clientSecrets, scopes)
				.setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
				.setAccessType("offline")
				.build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(APPLICATION_NAME);

		return credential;

	}

	public static Sheets getSheetService() throws IOException, GeneralSecurityException
	{
		Credential credential = authorize();   

		return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), credential)
				.setApplicationName(APPLICATION_NAME)
				.build();
	}

	public static void sheetAppend(List<List<String>> users) throws IOException, GeneralSecurityException {
		Sheets sheetService = getSheetService();
		String range ="employee_list!A2:D11";
		boolean empty = false;
		
		//Reading the data to check if the targeted rows are empty or not
		ValueRange response = sheetService.spreadsheets().values()
				.get(spreadsheetId, range)
				.execute();
		
		List<List<Object>> values = response.getValues();
		if(values == null||values.isEmpty())
		{
			System.out.println("No data found, we can go ahead with writing data");
			empty = true;
			
		}else
		{
			for(List row : values)
			{
				System.out.println(row.get(0)+" "+row.get(1)+" "+row.get(2));
			}
		}
		
		//writing data to excel sheet
		
		for(List<String> user: users)
		{
			System.out.println(user);
			ValueRange appendBody = new ValueRange()
					.setValues(Arrays.asList(Arrays.asList(user.get(0),user.get(1),user.get(2),user.get(3))));
			
			try {
				sheetService.spreadsheets().values().append(spreadsheetId, "employee_list", appendBody)
				.setValueInputOption("USER_ENTERED")
				.setInsertDataOption("INSERT_ROWS")
				.setIncludeValuesInResponse(true)
				.execute();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	public static void deleteCells(int size) throws IOException, GeneralSecurityException
	{
		Sheets sheetService = getSheetService();
		String range ="employee_list!A2:D"+size;
		
		
		ClearValuesRequest clearValuesRequest = new ClearValuesRequest();
		sheetService.spreadsheets().values().clear(spreadsheetId,range,clearValuesRequest).execute();
		
	}
	

       
}
