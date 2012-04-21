var eventextractor = {



    token_URL : "https://accounts.google.com/o/oauth2/auth?client_id=256241156366.apps.googleusercontent.com&redirect_uri=urn:ietf:wg:oauth:2.0:oob&response_type=code&scope=https://www.googleapis.com/auth/calendar",   
    database_Json : {},
    access_token : "none",
    refresh_token : "none",
    success_code : "none",
    calendar_name : "none",    
    curent_version : "EventExtractor 2.2",    
    myWindow : null,
    
    
    
    
    

    logMeIn: function() 
    {    
        this.success_code = document.getElementById('success_code').value;
    
        var request = new XMLHttpRequest();
        request.open('POST', 'https://www.googleapis.com/o/oauth2/token', false);   
        request.setRequestHeader('Host', 'accounts.google.com');
        request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');        
        request.send("code="+this.success_code+"&client_id=256241156366.apps.googleusercontent.com&client_secret=khNMF3Ph3idOf4uSfp4ECGPM&redirect_uri=urn:ietf:wg:oauth:2.0:oob&grant_type=authorization_code");
        
        if(request.status == "200")
        {            
            access_token = request.responseText.substring(request.responseText.indexOf("access_token") + 17, request.responseText.indexOf("token_type")- 6);
            refresh_token = request.responseText.substring(request.responseText.indexOf("refresh_token") + 18, request.responseText.indexOf("}") - 2);
            
            var wrk = Components.classes["@mozilla.org/windows-registry-key;1"].createInstance(Components.interfaces.nsIWindowsRegKey);
            wrk.create(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE\\Mozilla\\Thunderbird",wrk.ACCESS_WRITE);
            wrk.writeStringValue("Access_token", access_token);
            wrk.writeStringValue("Refresh_token", refresh_token);
            wrk.close();
            
            window.close();
            
        } 
        else
            eventextractor.showError("Unexpected error. Try again later.");
        
    },
    
    
    
    
    
    
    refreshToken: function() 
    {    
        var wrk = Components.classes["@mozilla.org/windows-registry-key;1"].createInstance(Components.interfaces.nsIWindowsRegKey);
        wrk.open(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE\\Mozilla\\Thunderbird",wrk.ACCESS_READ);
        refresh_token = wrk.readStringValue("Refresh_token");
        wrk.close();
            
        var request = new XMLHttpRequest();
        request.open('POST', 'https://www.googleapis.com/o/oauth2/token', false);   
        request.setRequestHeader('Host', 'accounts.google.com');
        request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');        
        request.send("client_id=256241156366.apps.googleusercontent.com&client_secret=khNMF3Ph3idOf4uSfp4ECGPM&refresh_token="+refresh_token+"&grant_type=refresh_token");
       
        if(request.status == "200")
        {  
            //vyparsujeme si access_token a ulozime ho do registra  
            access_token = request.responseText.substring(request.responseText.indexOf("access_token") + 17, request.responseText.indexOf("token_type")- 6);
            var wrk = Components.classes["@mozilla.org/windows-registry-key;1"].createInstance(Components.interfaces.nsIWindowsRegKey);
            wrk.create(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE\\Mozilla\\Thunderbird",wrk.ACCESS_WRITE);
            wrk.writeStringValue("Access_token", access_token);
            wrk.close();      
        } 
        else
            //obnova neprebehla korektne, je potrebna autorizacia
            eventextractor.showError("Authorization required. Try again after authorization.");  
            this.myWindow = window.open('chrome://eventextractor/content/authorization.xul','','resizable=no,scrollbars=no,location=yes,width=600,height=210,chrome=yes');
        
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
        //get access_token
        eventextractor.showInfo("Trying to get your calendar name.");
        var wrk = Components.classes["@mozilla.org/windows-registry-key;1"].createInstance(Components.interfaces.nsIWindowsRegKey);
        wrk.open(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE",wrk.ACCESS_READ);
        
        if (wrk.hasChild("Mozilla\\Thunderbird")) {
            var subkey = wrk.openChild("Mozilla\\Thunderbird", wrk.ACCESS_READ);
            if (subkey.hasValue("Access_token")){
                access_token = subkey.readStringValue("Access_token");
                subkey.close();
                wrk.close();
            
                //create request    
                var request = new XMLHttpRequest();
                request.open('GET', 'https://www.googleapis.com/calendar/v3/users/me/calendarList', false);   
                request.setRequestHeader('Host', 'www.googleapis.com');
                request.setRequestHeader('Authorization', "OAuth ".concat(access_token));       
                request.send();
               
                if(request.status == "401"){
                    eventextractor.refreshToken();
                    eventextractor.getCalendarName();              
                } else {
                    if(request.status == "200"){         
                        var response = request.responseText.split("\n");
                    
                        for(var i=0; i<response.length; i++){
                            if(response[i].search('"id"') != -1) calendar_name = response[i];
                            if(response[i].search('"accessRole"') != -1) 
                                if(response[i].search('"owner"') != -1) break;
                        }
                        calendar_name = calendar_name.substr(10, calendar_name.length-12);
                        eventextractor.showInfo("Your calendar name is '"+calendar_name+"'.");
                    
                    } else {
                    eventextractor.showError("Error in calendar name extraction. Please contact developer.");
                    }
                }
            }else {
                subkey.close();
                wrk.close();
                eventextractor.showError("Authorization required. Try again after authorization.");
                this.myWindow = window.open('chrome://eventextractor/content/authorization.xul','','resizable=no,scrollbars=no,location=yes,width=600,height=210,chrome=yes');
            } 
        
        } else {
                subkey.close();
                wrk.close();
                eventextractor.showError("Authorization required. Try again after authorization.");
                this.myWindow = window.open('chrome://eventextractor/content/authorization.xul','','resizable=no,scrollbars=no,location=yes,width=600,height=210,chrome=yes');
            }        
     },
     
     
    
    createNewEvent: function ()
    {
        eventextractor.showInfo("Creating event...");
        eventextractor.getCalendarName();   

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
        
        //get access_token
        var wrk = Components.classes["@mozilla.org/windows-registry-key;1"].createInstance(Components.interfaces.nsIWindowsRegKey);
        wrk.open(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE",wrk.ACCESS_READ);
        
        if (wrk.hasChild("Mozilla\\Thunderbird")) {
            
            var subkey = wrk.openChild("Mozilla\\Thunderbird", wrk.ACCESS_READ);
            access_token = subkey.readStringValue("Access_token");
            
            subkey.close();
            wrk.close();
            
            //create request    
            var request = new XMLHttpRequest();
            request.open('POST', 'https://www.googleapis.com/calendar/v3/calendars/'+calendar_name+'/events?sendNotifications=false&fields=end%2Clocation%2Cstart&pp=1&key={AIzaSyA_ufZ_72GAj0fFKitq6xXSGJ0TeZUwY_0}', false);   
            request.setRequestHeader('Host', 'www.googleapis.com');
            request.setRequestHeader('Authorization', "OAuth ".concat(access_token));   
            request.setRequestHeader('Content-Type', 'application/json');      
            request.send(requestText);
                 
            if(request.status == "401"){
                eventextractor.refreshToken();                
            } else {
                if(request.status == "200"){  
                    eventextractor.showInfo("Event created seccussfuly.");
                                  
                    var server;
                    var extractionMethod;
        
                    var wrk = Components.classes["@mozilla.org/windows-registry-key;1"].createInstance(Components.interfaces.nsIWindowsRegKey);
                    wrk.open(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE",wrk.ACCESS_READ);
        
                    if (wrk.hasChild("Mozilla\\Thunderbird")) {
                        var subkey = wrk.openChild("Mozilla\\Thunderbird", wrk.ACCESS_READ);
                        server = subkey.readStringValue("Server");
                        extractionMethod = subkey.readStringValue("ExtractionMethod");
                        subkey.close();
                        wrk.close();
                    } else {
                        wrk.create(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE\\Mozilla\\Thunderbird",wrk.ACCESS_WRITE);
                        wrk.writeStringValue("ExtractionMethod", "Extractor.EventRegex");
                        wrk.writeStringValue("Server", "http://127.0.0.1:5000");
                        wrk.close(); 
            
                        server = "http://127.0.0.1:5000";
                        extractionMethod = "Extractor.EventRegex";
                    }
                    
                    document.database_Json.version = this.curent_version;
                    document.database_Json.ExtractionMethod = extractionMethod;
                    document.database_Json.CalendarName = calendar_name;
                    document.database_Json.sended = new Object;
                    document.database_Json.sended.Name = document.getElementById("name").value;
                    document.database_Json.sended.Place = document.getElementById("place").value;
                    document.database_Json.sended.DateFrom = document.getElementById("date_from").value;
                    document.database_Json.sended.TimeFrom = document.getElementById("time_from").value;
                    document.database_Json.sended.DateTo = document.getElementById("date_to").value;
                    document.database_Json.sended.TimeTo = document.getElementById("time_to").value;
                    document.database_Json.sended.Description = document.getElementById("description").value;
                    
                    var jsonString = JSON.stringify(document.database_Json); 
                   
                    var request2 = new XMLHttpRequest();
                    request2.open('POST', server, false);   
                    request2.setRequestHeader('Authorization', this.curent_version);
                    request2.setRequestHeader('ExtractionMethod', extractionMethod);
                    request2.setRequestHeader('Content-Type', 'text/html');
                    request2.setRequestHeader('Action', 'SAVE');
        
                    request2.send(jsonString);      
                    
                    if(request2.status = "200")
                        eventextractor.showInfo("Data saved successfully.");
                    else
                        eventextractor.showError(request2.text);
                                
                    
                } else {
                    eventextractor.showError("Unexpected error. Check inserted data and try again later.");
                }
            }

            
        
        } else {
                this.myWindow = window.open('chrome://eventextractor/content/authorization.xul','','resizable=no,scrollbars=no,location=yes,width=600,height=210,chrome=yes'); 
            
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
    
        eventextractor.showInfo("Default value saved.");
    },
    
    
    
    
    getEventData: function()
    {
        
        var server;
        var extractionMethod;
        
        var wrk = Components.classes["@mozilla.org/windows-registry-key;1"].createInstance(Components.interfaces.nsIWindowsRegKey);
        wrk.open(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE",wrk.ACCESS_READ);
        
        if (wrk.hasChild("Mozilla\\Thunderbird")) {
            var subkey = wrk.openChild("Mozilla\\Thunderbird", wrk.ACCESS_READ);
            server = subkey.readStringValue("Server");
            extractionMethod = subkey.readStringValue("ExtractionMethod");
            subkey.close();
            wrk.close();
        } else {
            wrk.create(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE\\Mozilla\\Thunderbird",wrk.ACCESS_WRITE);
            wrk.writeStringValue("ExtractionMethod", "Extractor.EventRegex");
            wrk.writeStringValue("Server", "http://127.0.0.1:5000");
            wrk.close(); 
            
            server = "http://127.0.0.1:5000";
            extractionMethod = "Extractor.EventRegex";
        }
        
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
        
        messagepane = messagepane.concat("Content: " + folder.getMsgTextFromStream(listener.inputStream, selectedMessage.Charset, 65536, 32768, false, true, { }));
        messagepane = messagepane.concat("\n");
        
        eventextractor.showInfo("Waiting for extracted data.");
                         
        var request = new XMLHttpRequest();
        request.open('POST', server, false);   
        request.setRequestHeader('Authorization', this.curent_version);
        request.setRequestHeader('ExtractionMethod', extractionMethod);
        request.setRequestHeader('Content-Type', 'text/html');
        request.setRequestHeader('Action', 'ANALYZE');
        
        request.send(messagepane);
        
        
        
        if(request.status == "200"){
            var loaded = 0;
        
            var JSONdata = JSON.parse(request.responseText);
            
            var newJson = new Object();
            newJson.messagepane = messagepane;
            
            newJson.received = new Object;
            
            newJson.received.Name = new Array();
            for(var j=0; j<JSONdata.Name.length; j++){
                        if(JSONdata.Name[j] != "") loaded++;           
                        document.getElementById("name").appendItem(JSONdata.Name[j]); 
                        newJson.received.Name[j] = JSONdata.Name[j];
                        }
            document.getElementById("name").selectedIndex = 0;  
            
            newJson.received.Place = new Array();
            for(var j=0; j<JSONdata.Place.length; j++){    
                        if(JSONdata.Place[j] != "") loaded++;       
                        document.getElementById("place").appendItem(JSONdata.Place[j]); 
                        newJson.received.Place[j] = JSONdata.Place[j];
                        }
            document.getElementById("place").selectedIndex = 0;  
            
            newJson.received.DateFrom = new Array();
            for(var j=0; j<JSONdata.DateFrom.length; j++){  
                        if(JSONdata.DateFrom[j] != "") loaded++;         
                        document.getElementById("date_from").appendItem(JSONdata.DateFrom[j]); 
                        newJson.received.DateFrom[j] = JSONdata.DateFrom[j];
                        }
            document.getElementById("date_from").selectedIndex = 0;  
            
            newJson.received.TimeFrom = new Array();
            for(var j=0; j<JSONdata.TimeFrom.length; j++){     
                        if(JSONdata.TimeFrom[j] != "")loaded++;      
                        document.getElementById("time_from").appendItem(JSONdata.TimeFrom[j]); 
                        newJson.received.TimeFrom[j] = JSONdata.TimeFrom[j];
                        }
            document.getElementById("time_from").selectedIndex = 0;  
            
            newJson.received.DateTo = new Array();
            for(var j=0; j<JSONdata.DateTo.length; j++){ 
                        if(JSONdata.DateTo[j] != "")loaded++;          
                        document.getElementById("date_to").appendItem(JSONdata.DateTo[j]); 
                        newJson.received.DateTo[j] = JSONdata.DateTo[j];
                        }
            document.getElementById("date_to").selectedIndex = 0;  
            
            newJson.received.TimeTo = new Array();
            for(var j=0; j<JSONdata.TimeTo.length; j++){  
                        if(JSONdata.TimeTo[j] != "") loaded++;         
                        document.getElementById("time_to").appendItem(JSONdata.TimeTo[j]); 
                        newJson.received.TimeTo[j] = JSONdata.TimeTo[j];
                        }
            document.getElementById("time_to").selectedIndex = 0;  
            
            newJson.received.Description = JSONdata.Description;
            if(JSONdata.Description != "") loaded++;
            document.getElementById("description").value = JSONdata.Description; 
            
            document.database_Json = newJson;     
            
            eventextractor.showInfo("Extraction complete. Founded "+loaded+" items.");     
                       
        } else 
            eventextractor.showError(request.responseText);
            

    
    },






     onMenuItemCommand: function(event)
    {
    
        this.myWindow = window.open('chrome://eventextractor/content/window.xul','','resizable=no,scrollbars=no,location=yes,width=490,height=280,chrome=yes'); 
        
        

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
    
    
    

}

var options = {


    optionsWindow : null,
    
    

    openWindow: function (event)
    {
        this.optionsWindow = window.open('chrome://eventextractor/content/options.xul','','resizable=no,scrollbars=no,location=yes,width=400,height=230,chrome=yes'); 
    },
    
    
    
    
    
    readData: function ()
    {               
        var wrk = Components.classes["@mozilla.org/windows-registry-key;1"].createInstance(Components.interfaces.nsIWindowsRegKey);
        wrk.open(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE",wrk.ACCESS_READ);
        
        if (wrk.hasChild("Mozilla\\Thunderbird")) {
            var subkey = wrk.openChild("Mozilla\\Thunderbird", wrk.ACCESS_READ);
            document.getElementById("server_adress").value = subkey.readStringValue("Server");         
            
            var request = new XMLHttpRequest();
            request.open('POST', document.getElementById("server_adress").value, false);   
            request.setRequestHeader('Content-Type', 'text/html');
            request.setRequestHeader('Action', 'GET_METHODS');
        
            request.send();
        
            if(request.status == "200"){
                var JSONdata = JSON.parse(request.responseText);
            
                for(var j=0; j<JSONdata.Implementations.length; j++)          
                            document.getElementById("extraction_method").appendItem(JSONdata.Implementations[j]);                        
                document.getElementById("extraction_method").selectedIndex = 0;            
                       
            } else 
                eventextractor.showError(request.responseText);
                
            document.getElementById("extraction_method").value = subkey.readStringValue("ExtractionMethod");
            
            subkey.close();
            wrk.close();
            
        } else {
            wrk.create(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE\\Mozilla\\Thunderbird",wrk.ACCESS_WRITE);
            wrk.writeStringValue("ExtractionMethod", "Extractor.EventRegex");
            wrk.writeStringValue("Server", "http://127.0.0.1:5000");
            wrk.close(); 
            
            options.readData();       
        }
    },
    
        
    
    
    save: function()
    {
        var wrk = Components.classes["@mozilla.org/windows-registry-key;1"].createInstance(Components.interfaces.nsIWindowsRegKey);
            wrk.create(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE\\Mozilla\\Thunderbird",wrk.ACCESS_WRITE);
            wrk.writeStringValue("ExtractionMethod", document.getElementById("extraction_method").value);
            wrk.writeStringValue("Server", document.getElementById("server_adress").value);
            wrk.close();
            
            window.close();	       
    },
    
    
    
    
    resetRegistry: function ()
    {
        var wrk = Components.classes["@mozilla.org/windows-registry-key;1"].createInstance(Components.interfaces.nsIWindowsRegKey);
        wrk.open(wrk.ROOT_KEY_CURRENT_USER,"SOFTWARE",wrk.ACCESS_READ);
        if (wrk.hasChild("Mozilla\\Thunderbird"))
             wrk.removeChild("Mozilla\\Thunderbird");
        
        alert("Registers successfully cleaned.");        
        window.close();        
    },

};

