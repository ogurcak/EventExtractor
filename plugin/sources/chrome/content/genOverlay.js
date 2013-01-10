var eventextractor = {


    token_URL : "https://accounts.google.com/o/oauth2/auth?client_id=256241156366.apps.googleusercontent.com&redirect_uri=urn:ietf:wg:oauth:2.0:oob&response_type=code&scope=https://www.googleapis.com/auth/calendar",   
    database_Json : {},
    success_code : "none",   
    curent_version : "EventExtractor 2.3",   
	message_id : "0",	
    myWindow : null,
	prefs: null,
    
    
    
    
    

	
	
	
	
    logMeIn: function() 
    {    
		this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService).getBranch("extractor.");
        this.success_code = document.getElementById('success_code').value;
    
        var request = new XMLHttpRequest();
        request.open('POST', 'https://www.googleapis.com/o/oauth2/token', false);   
        request.setRequestHeader('Host', 'accounts.google.com');
        request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');        
        request.send("code="+this.success_code+"&client_id=256241156366.apps.googleusercontent.com&client_secret=khNMF3Ph3idOf4uSfp4ECGPM&redirect_uri=urn:ietf:wg:oauth:2.0:oob&grant_type=authorization_code");
        
        if(request.status == "200")
        {            
            access_token = request.responseText.substring(request.responseText.indexOf("access_token") + 17, request.responseText.indexOf("token_type")- 6);
            this.prefs.setCharPref("access_token", access_token);
			
			refresh_token = request.responseText.substring(request.responseText.indexOf("refresh_token") + 18, request.responseText.indexOf("}") - 2);
            this.prefs.setCharPref("refresh_token", refresh_token);
			         
            window.close();            
        } 
        else
            eventextractor.showError(request.responseText);        
    },
    
    
    
    
    

	
    refreshToken: function() 
    {    
		this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService).getBranch("extractor.");
		
		refresh_token = this.prefs.getCharPref("refresh_token");
            
        var request = new XMLHttpRequest();
        request.open('POST', 'https://www.googleapis.com/o/oauth2/token', false);   
        request.setRequestHeader('Host', 'accounts.google.com');
        request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');        
        request.send("client_id=256241156366.apps.googleusercontent.com&client_secret=khNMF3Ph3idOf4uSfp4ECGPM&refresh_token="+refresh_token+"&grant_type=refresh_token");
       
        if(request.status == "200")
        {  
            //vyparsujeme si access_token a ulozime ho do registra  
            access_token = request.responseText.substring(request.responseText.indexOf("access_token") + 17, request.responseText.indexOf("token_type")- 6);
            this.prefs.setCharPref("access_token", access_token);
            eventextractor.getCalendarName();                  
        } 
        else{
            //obnova neprebehla korektne, je potrebna autorizacia
            eventextractor.showError("Authorization required. Try again after authorization.");  
            this.myWindow = window.open('chrome://eventextractor/content/authorization.xul','','resizable=no,scrollbars=no,location=yes,width=600,height=230,chrome=yes,centerscreen');
        }
    },
    
    

	
	
	
	
    openURL: function ()
    {
        var ioservice = Components.classes["@mozilla.org/network/io-service;1"]
                          .getService(Components.interfaces.nsIIOService);
        var uriToOpen = ioservice.newURI(this.token_URL, null, null);
        
        var extps = Components.classes["@mozilla.org/uriloader/external-protocol-service;1"]
                      .getService(Components.interfaces.nsIExternalProtocolService);
        extps.loadURI(uriToOpen, null);
    },
    
    
    

	
	
	
    getCalendarName: function ()
    {      
		this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService).getBranch("extractor.");
		
        if (this.prefs.getCharPref("access_token") != ""){
          
            //create request    
            var request = new XMLHttpRequest();
            request.open('GET', 'https://www.googleapis.com/calendar/v3/users/me/calendarList', false);   
            request.setRequestHeader('Host', 'www.googleapis.com');
            request.setRequestHeader('Authorization', "OAuth ".concat(this.prefs.getCharPref("access_token")));       
            request.send();
               
            if(request.status == "401"){
                eventextractor.refreshToken();
            } else {
                if(request.status == "200"){         
                    var response = request.responseText.split("\n");
                    
                    for(var i=0; i<response.length; i++){
                        if(response[i].search('"id"') != -1) 
							document.getElementById("calendar").appendItem(response[i].substr(10, response[i].length-12));

                    }
                    eventextractor.showInfo("Calendar names extracted.");
                    
                } else {
                    eventextractor.showError("Error in calendar name extraction. Please contact developer.");
                }
            }
        }else {
            eventextractor.showError("Authorization required. Try again after authorization.");
            this.myWindow = window.open('chrome://eventextractor/content/authorization.xul','','resizable=no,scrollbars=no,location=yes,width=600,height=230,chrome=yes,centerscreen');
        }       
    },
     
	 

	 
	 
	 
	 
	setDefaultCalendarName: function()
    {      
        eventextractor.showInfo("Trying to get default calendar name.");
        
		this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService).getBranch("extractor.");
		
		//create request    
		var request = new XMLHttpRequest();
		request.open('GET', 'https://www.googleapis.com/calendar/v3/users/me/calendarList', false);   
		request.setRequestHeader('Host', 'www.googleapis.com');
		request.setRequestHeader('Authorization', "OAuth ".concat(this.prefs.getCharPref("access_token")));       
		request.send();
          
        if(request.status != "401")                    
			if(request.status == "200"){         
				var response = request.responseText.split("\n");
                    
				for(var i=0; i<response.length; i++){
					if(response[i].search('"id"') != -1) this.prefs.setCharPref("calendar", response[i]);
					if(response[i].search('"accessRole"') != -1) 
						if(response[i].search('"owner"') != -1) break;
				}
				this.prefs.setCharPref("calendar", this.prefs.getCharPref("calendar").substr(10, this.prefs.getCharPref("calendar").length-12));
				eventextractor.showInfo("Your default calendar name is '"+this.prefs.getCharPref("calendar")+"'.");
                    
			} else {
				eventextractor.showError("Error in calendar name extraction. Please contact developer.");
			}
	},
     
	 
	 
	 

	 
	 
    createNewEvent: function ()
    {
		this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService).getBranch("extractor.");
	
        eventextractor.showInfo("Creating event...");
		
        if(this.prefs.getCharPref("calendar") == "")
			eventextractor.setDefaultCalendarName();   

        var description = document.getElementById("description").value.replace(/\n/gi, "  ");
        
        if(document.getElementById("date_from").value == ""){
            var now = new Date();
            var date = now.getDate();
            date = ((date < 10) ? "0" : "") + date;
            var month = now.getMonth() + 1;
            month = ((month < 10) ? "0" : "") + month;
            var year = now.getYear() + 1900;    
            document.getElementById("date_from").value = year+"-"+month+"-"+date;
            document.getElementById("date_to").value = year+"-"+month+"-"+date;
        }
        
        if(document.getElementById("date_to").value == ""){
            document.getElementById("date_to").value = document.getElementById("date_from").value;
        }
        
        if(document.getElementById("time_from").value == ""){            
            document.getElementById("all_day").checked = true;
            eventextractor.allDayChecking();
        }
        
        if(document.getElementById("time_to").value == "" && document.getElementById("time_from").value != ""){
            var now = new Date(document.getElementById("date_from").value+"T"+document.getElementById("time_from").value+":00");
            now.setHours(now.getHours()+1);
            var hours = now.getHours();
            var minutes = now.getMinutes();
            var timeValue = "" + hours;
            timeValue += ((minutes < 10) ? ":0" : ":") + minutes;  
            document.getElementById("time_to").value = timeValue;
        }
        
        var timeOffset;
        var d = new Date();
        var centralTime = d.getTimezoneOffset()/60;
        if(centralTime > 0){
            if(centralTime > 9) timeOffset = "-"+centralTime;
            else timeOffset = "-0"+centralTime;
        }else{
            if(centralTime < -9)  timeOffset = "+"+(-centralTime);
            else timeOffset = "+0"+(-centralTime);
        }  
            
        var requestText;
        if(document.getElementById("all_day").checked)
            requestText = '{\n"summary": "'+document.getElementById("name").value+'",\n"location": "'+document.getElementById("place").value+'",\n"description": \''+ description+'\',\n"start": {\n"date": "'+document.getElementById("date_from").value+'"\n},\n"end": {\n "date":"'+document.getElementById("date_to").value+'"\n}\n}';
        else
            requestText = '{\n"summary": "'+document.getElementById("name").value+'",\n"location": "'+document.getElementById("place").value+'",\n"description": \''+ description+'\',\n"start": {\n"dateTime": "'+document.getElementById("date_from").value+'T'+document.getElementById("time_from").value+':00.000'+timeOffset+':00"\n},\n"end": {\n "dateTime":"'+document.getElementById("date_to").value+'T'+document.getElementById("time_to").value+':00.000'+timeOffset+':00"\n}\n}';
       
        if (this.prefs.getCharPref("access_token") != "") {
            
            //create request    
            var request = new XMLHttpRequest();
            request.open('POST', 'https://www.googleapis.com/calendar/v3/calendars/'+this.prefs.getCharPref("calendar")+'/events?sendNotifications=false&fields=end%2Clocation%2Cstart&pp=1&key={AIzaSyA_ufZ_72GAj0fFKitq6xXSGJ0TeZUwY_0}', false);   
            request.setRequestHeader('Host', 'www.googleapis.com');
            request.setRequestHeader('Authorization', "OAuth ".concat(this.prefs.getCharPref("access_token")));   
            request.setRequestHeader('Content-Type', 'application/json');      
            request.send(requestText);
                 
            if(request.status == "401"){
                eventextractor.refreshToken();                
            } else {
                if(request.status == "200"){  
                    eventextractor.showInfo("Event created seccussfuly.");
                    
					document.database_Json = new Object();
					document.database_Json.id = this.message_id;
                    document.database_Json.Name = document.getElementById("name").value;
                    document.database_Json.Place = document.getElementById("place").value;
                    document.database_Json.DateFrom = document.getElementById("date_from").value;
                    document.database_Json.TimeFrom = document.getElementById("time_from").value;
                    document.database_Json.DateTo = document.getElementById("date_to").value;
                    document.database_Json.TimeTo = document.getElementById("time_to").value;
					document.database_Json.AllDay = document.getElementById("all_day").checked;
                    document.database_Json.Description = document.getElementById("description").value;
                                      
                    var request2 = new XMLHttpRequest();
                    request2.open('POST', this.prefs.getCharPref("server"), true);   
                    request2.setRequestHeader('Authorization', this.curent_version);
                    request2.setRequestHeader('ExtractionMethod', this.prefs.getCharPref("extractionMethod"));
                    request2.setRequestHeader('Content-Type', 'application/json');
                    request2.setRequestHeader('Action', 'SAVE');
					
                    request2.send(JSON.stringify(document.database_Json) + "\n");      
                      
                    
                } else {
                    eventextractor.showError("Unexpected error. Check inserted data and try again later.");
                }
            }
        } else {
                this.myWindow = window.open('chrome://eventextractor/content/authorization.xul','','resizable=no,scrollbars=no,location=yes,width=600,height=230,chrome=yes,centerscreen'); 
        }
    },
    
    

    

	
	
    setDefaultValue: function()
    {
        var now = new Date();
        
        var hours = now.getHours();
        var minutes = now.getMinutes();
        var timeValue = "" + hours;
        timeValue += ((minutes < 10) ? ":0" : ":") + minutes;
        document.getElementById("time_from").removeAllItems();
        document.getElementById("time_from").appendItem(timeValue);
        document.getElementById("time_from").selectedIndex = 0;
        
        var date = now.getDate();
        date = ((date < 10) ? "0" : "") + date;
        var month = now.getMonth() + 1;
        month = ((month < 10) ? "0" : "") + month;
        var year = now.getYear() + 1900;        
        document.getElementById("date_from").removeAllItems();
        document.getElementById("date_from").appendItem(year + "-" + month + "-" + date);
        document.getElementById("date_from").selectedIndex = 0;
        
        now = new Date();
        now.setHours(now.getHours()+1);        
        hours = now.getHours();
        minutes = now.getMinutes();
        timeValue = "" + hours;
        timeValue += ((minutes < 10) ? ":0" : ":") + minutes;
        document.getElementById("time_to").removeAllItems();
        document.getElementById("time_to").appendItem(timeValue);
        document.getElementById("time_to").selectedIndex = 0;
        
        date = now.getDate();
        date = ((date < 10) ? "0" : "") + date;
        month = now.getMonth() + 1;
        month = ((month < 10) ? "0" : "") + month;
        year = now.getYear() + 1900;        
        document.getElementById("date_to").removeAllItems();
        document.getElementById("date_to").appendItem(year + "-" + month + "-" + date);
        document.getElementById("date_to").selectedIndex = 0;        
        
        document.getElementById("name").value = "";
        document.getElementById("name").removeAllItems();
        document.getElementById("place").value = "";
        document.getElementById("place").removeAllItems();
        document.getElementById("description").value = ""; 
    
        eventextractor.showInfo("Default value set.");
    },
    
    
    

	
	
	
    getEventData: function()
    {
		this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService).getBranch("extractor.");
        
        var win = Components.classes["@mozilla.org/appshell/window-mediator;1"].getService(Components.interfaces.nsIWindowMediator).getMostRecentWindow("mail:3pane");
        var selectedMessage = win.gFolderDisplay.selectedMessage;
        
        var messagepane = "";
        messagepane = messagepane.concat("From: " + selectedMessage.author + "\n");
        messagepane = messagepane.concat("To: " + selectedMessage.recipients + "\n");
        messagepane = messagepane.concat("Date: " +selectedMessage.dateInSeconds +"\n");
        messagepane = messagepane.concat("Subject: " + selectedMessage.subject + "\n");
               
        let messenger = Components.classes["@mozilla.org/messenger;1"].createInstance(Components.interfaces.nsIMessenger);
        let listener = Components.classes["@mozilla.org/network/sync-stream-listener;1"].createInstance(Components.interfaces.nsISyncStreamListener);
        let uri = selectedMessage.folder.getUriForMsg(selectedMessage);
        messenger.messageServiceFromURI(uri).streamMessage(uri, listener, null, null, false, "");
        let folder = selectedMessage.folder;
        
        messagepane = messagepane.concat("Content: " + folder.getMsgTextFromStream(listener.inputStream, "ISO-8859-2", 65536, 32768, false, true, { }));
        messagepane = messagepane.concat("\n");
        
        eventextractor.showInfo("Waiting for extracted data.");
                         
        var request = new XMLHttpRequest();
        request.open('POST', this.prefs.getCharPref("server"), false);   
        request.setRequestHeader('Authorization', this.curent_version);
        request.setRequestHeader('ExtractionMethod', this.prefs.getCharPref("extractionMethod"));
        request.setRequestHeader('Content-Type', 'text/html;charset=ISO-8859-2');
        request.setRequestHeader('Action', 'ANALYZE');
        
        request.send(messagepane);
        
        if(request.status == "200"){
            var loaded = 0;
        
            var JSONdata = JSON.parse(request.responseText);
            
            
            for(var j=0; j<JSONdata.Name.length; j++){
                        if(JSONdata.Name[j] != "") loaded++;           
                        document.getElementById("name").appendItem(JSONdata.Name[j]); 
                        }
            document.getElementById("name").selectedIndex = 0;  
            
            for(var j=0; j<JSONdata.Place.length; j++){    
                        if(JSONdata.Place[j] != "") loaded++;       
                        document.getElementById("place").appendItem(JSONdata.Place[j]); 
                        }
            document.getElementById("place").selectedIndex = 0;  
            
            for(var j=0; j<JSONdata.DateFrom.length; j++){  
                        if(JSONdata.DateFrom[j] != "") loaded++;         
                        document.getElementById("date_from").appendItem(JSONdata.DateFrom[j]); 
                        }
            document.getElementById("date_from").selectedIndex = 0;  
            
            for(var j=0; j<JSONdata.TimeFrom.length; j++){     
                        if(JSONdata.TimeFrom[j] != "")loaded++;      
                        document.getElementById("time_from").appendItem(JSONdata.TimeFrom[j]); 
                        }
            document.getElementById("time_from").selectedIndex = 0;  
            
            for(var j=0; j<JSONdata.DateTo.length; j++){ 
                        if(JSONdata.DateTo[j] != "")loaded++;          
                        document.getElementById("date_to").appendItem(JSONdata.DateTo[j]); 
                        }
            document.getElementById("date_to").selectedIndex = 0;  
            
            for(var j=0; j<JSONdata.TimeTo.length; j++){  
                        if(JSONdata.TimeTo[j] != "") loaded++;         
                        document.getElementById("time_to").appendItem(JSONdata.TimeTo[j]); 
                        }
            document.getElementById("time_to").selectedIndex = 0;  
            
            if(JSONdata.Description != "") loaded++;
            document.getElementById("description").value = JSONdata.Description; 
            
            eventextractor.showInfo("Extraction complete. Founded "+loaded+" items.");     
                       
        } else 
            eventextractor.showError(request.responseText);
	},



	
	
	
	
	readSettings: function()
	{
		this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService).getBranch("extractor.");
		
		document.getElementById("server").value = this.prefs.getCharPref("server");
		
		document.getElementById("calendar").removeAllItems();
		eventextractor.getCalendarName();
		document.getElementById("calendar").value = this.prefs.getCharPref("calendar");
		
		eventextractor.showInfo("Waiting for available extraction methods.");
		
		document.getElementById("extraction_method").removeAllItems();
		 
		var request = new XMLHttpRequest();
        request.open('POST', this.prefs.getCharPref("server"), false);   
        request.setRequestHeader('Content-Type', 'text/html');
        request.setRequestHeader('Action', 'GET_METHODS');
        
        request.send();
        
        if(request.status == "200"){
            var JSONdata = JSON.parse(request.responseText);
            
			var j=0;
            for(j=0; j<JSONdata.Implementations.length; j++)          
                document.getElementById("extraction_method").appendItem(JSONdata.Implementations[j]); 
			eventextractor.showInfo("Founded "+j+" methods.");
        } else 
            eventextractor.showError(request.responseText);
                        
		document.getElementById("extraction_method").value = this.prefs.getCharPref("extractionMethod");
	},

	
	
	
	


     onMenuItemCommand: function(event)
    {    
        myWindow = window.open('chrome://eventextractor/content/window.xul','','resizable=no,scrollbars=no,location=yes,width=500,height=282,chrome=yes,centerscreen');        
    },
	
	
	
	
	

	
	resize: function()
	{
		if(document.getElementById("groupbox_settings").collapsed){
			document.getElementById("groupbox_settings").collapsed = false;	
			myWindow = window.resizeTo(500, 470);
			eventextractor.readSettings();			
		} else
		{			
			document.getElementById("groupbox_settings").collapsed = true;	
			myWindow = window.resizeTo(500, 282);		
			myWindow = window.resizeTo(500, 282);			
		}	
	},
    
    
    
	
	
	

    allDayChecking: function()
    {
        if(document.getElementById("all_day").checked)
        {
            document.getElementById("time_from").disabled = true;
            document.getElementById("time_to").disabled = true;
            document.getElementById("time_from").value = "";
            document.getElementById("time_to").value = "";
        } else
        {
            document.getElementById("time_from").disabled = false;
            document.getElementById("time_to").disabled = false;
            document.getElementById("time_from").selectedIndex = 0; 
            document.getElementById("time_to").selectedIndex = 0; 
        }
    },
    
    
    

	
	
	
    showError: function (text)
    {
        document.getElementById("icon2").width = 0;
        document.getElementById("icon2").height = 0; 
        document.getElementById("icon1").width = 13;
        document.getElementById("icon1").height = 13;        
        document.getElementById("info_label").value = text;
        document.getElementById("info_label").style.color = "red";
    },
    
    
    

	
	
	
    showInfo: function (text)
    {
        document.getElementById("icon1").width = 0;
        document.getElementById("icon1").height = 0; 
        document.getElementById("icon2").width = 14;
        document.getElementById("icon2").height = 13;        
        document.getElementById("info_label").value = text;
        document.getElementById("info_label").style.color = "black";
    },
    
    
	
	

	
	
    saveSettings: function()
    {
        this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService).getBranch("extractor.");
		
		this.prefs.setCharPref("extractionMethod", document.getElementById("extraction_method").value);
		this.prefs.setCharPref("calendar", document.getElementById("calendar").value);
		this.prefs.setCharPref("server", document.getElementById("server").value);
		
		document.getElementById("name").removeAllItems();		
		document.getElementById("date_from").removeAllItems();
		document.getElementById("time_from").removeAllItems();
		document.getElementById("date_to").removeAllItems();
		document.getElementById("time_to").removeAllItems();
		document.getElementById("all_day").checked = false;
		document.getElementById("place").removeAllItems();
		document.getElementById("description").value = "";
		
		eventextractor.readSettings();
		eventextractor.getEventData();
    },
	
	

	
	
	
	
	resetSettings: function()
	{
		this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService).getBranch("extractor.");
		
		this.prefs.setCharPref("extractionMethod", "ogurcak.fiit.SK_extractor");
		this.prefs.setCharPref("calendar", "");
		this.prefs.setCharPref("server", "http://events.email.ui.sav.sk:5000");
		this.prefs.setCharPref("access_token", "");
		this.prefs.setCharPref("refresh_token", "");
		
		window.close();
	},
	
	
	
	

	
	
};

