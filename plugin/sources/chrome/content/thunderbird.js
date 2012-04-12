var ThunderbirdApp = {

  FLvoffset: 80, 
 
  
  OpenURL: function (agrs)
  {
        alert("ahoj");
  },

 
 
  getContextMenu: function()
  {
        var contextMenu = document.getElementById("mailContext");   
        if (contextMenu == null) {
            contextMenu = document.getElementById("msgComposeContext");
            }
        return contextMenu;
  }
  
  
}

