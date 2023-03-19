# Overview:
This is a Spring boot web application written in Java, eventually will be running on web server. The database should be configured on the server as well. Reverse proxy is required in order to expose the server to the Internet. Users can interact with the app by slash command in a Slack channel and all the clock-in and clock-out records are stored in the database. The records and staff’s hours can be requested and presented in a google sheet.
## User Manual

### Clock In 
* In the clocksummary channel, enter ”/” and type “clockin”
* Press Enter on keyboard or click send button
* Select or enter information in all the boxes
* Click “Submit”
You will see message mentioning you in the channel saying you just clocked in. 
In this case, you just clocked in successfully.
#### Error message: 
* "Failed to clock in, please clock out before you clock in again."
This indicates that you haven’t clocked out for your previous clock-in. Each clock-in must match a clock-out. 
* "Failed to clock in, please choose a time that is after you last clock out."
New shift cannot start before last clock out, but right at the time when last clock out is acceptable. 
### Clock Out
* In the clocksummary channel, enter ”/” and type “clockout”
* Press Enter on keyboard or click send button
* Select or enter information in all the boxes
* Click “Submit”
* You will see message mentioning you in the channel saying you just clocked out. 
	In this case, you just clocked in successfully.
#### Error message: 
* "Failed to clock out. I didn't find a clock-in record that matches your clock-out."
You need to clock in before you clock out. 
* "Failed to clock out. You need to choose a time that is after you clocked in."
### Staff Hours (admin)
* In the clocksummary channel, enter ”/” and type “hours”
* Press Enter on keyboard or click send button
* Select starting date and ending date
* Click “Submit”
* You will see message “Result is shown in the HOURS sheet:” with a google sheet link 
  below. Click on the link and go to “HOURS” tab at the button left. If no records found for 
	the selected time period, you will see message “No data found in this time period” in the 
  “HOURS” sheet. 

#### Error message: 
* "Failed. Please contact admin if you need the access"
		Only admin can request the staff hours. 
* "Failed. Ending date prior to starting date is not allowed"
* Note: You can change the sheet name at the top left, but do not change the tabs name 
                     at the button left  
                     
### My record 
If you made a typo or clock in /out accidentally, use this command to delete your  
previous record. However, you can only delete one record at a time from 24 hours ago. 
* In the clocksummary channel, enter ”/” and type “myrecord”
* Press Enter on keyboard or click send button
* You will see a block pops up showing your last record
* If you want to delete this record, select “DELETE” in the box
* Click “Submit”
* If no record found, you will see "No record found for past 24 hours" in the block.
	If successfully deleted record, you will see message "Your previous clock in/ out record  
  has been deleted!"
### Previous records (admin)
* In the clocksummary channel, enter ”/” and type “records”
* Press Enter on keyboard or click send button
* Select starting date and ending date
* Click “Submit”
* You will see message “Result is shown in the RECORDS sheet:” with a google sheet link 
  below. Click on the link and go to “RECORDS” tab at the button left. If no records found for 
  the selected time period, you will see message “No data found in this time period” in the 
  “RECORDS” sheet. 
  Theoretically, you will see records group by staff name for the selected time period. 
#### Error message: 
* "Failed. Please contact admin if you need the access"
   Only admin can request the staff records.
### Edit records from google sheet and update in database(admin)
* Go to RECORDS sheet
* Editable fields are: “Datetime”, “InOrOut”, “WhatFor”, “summary”, “company” and 
	          “hours”. You can edit the text under those fields.
* In the clocksummary channel, enter ”/” and type “update”
* Click “Submit”
* Note: Do not change “id”, “stafffId” and “name”
* You will see message “1 row(s) has been updated in database” sent to you in the 
         channel. The number of rows depends on how many rows you modified. If there are no 
         changes being made, you will see message “No changes made in database”.
#### Error message: 
* "Failed. Please contact admin if you need the access"
		Only admin can update the staff records to database. 
* "Failed. Invalid value appears nearby the entries."
		This means you typed something outside the table. 
* "Failed. Clock-in entry cannot have hours value."
		Only clock-out records can have hours value. 


