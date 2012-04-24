var ThunderbirdApp = {

  FLvoffset: 80, 
 
  
  OpenURL: function (agrs)
  {
        
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

