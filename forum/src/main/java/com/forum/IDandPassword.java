package com.forum;

import java.util.HashMap;

public class IDandPassword {
    
    HashMap<String,String> logininfo = new HashMap<String,String>();
    IDandPassword() {
        logininfo.put("alexandre", "a");
        logininfo.put("altrevis", "a1");
        logininfo.put("a", "a");
    }
    @SuppressWarnings("rawtypes")
    protected HashMap getLoginInfo() {
        return logininfo;
    }
}
